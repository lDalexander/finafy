package com.finanzas.app.core.util

import com.finanzas.app.domain.model.RemittanceCalibration
import kotlin.math.abs

/**
 * Aritmetica de giros. Funciones puras: sin red, sin estado, verificables.
 *
 * Modelo (medido contra cotizaciones reales de Western Union):
 *     recibido_COP = (total_USD - fee_USD) * tasa
 *
 * La comision se descuenta del envio ANTES de convertir.
 */
object RemittanceMath {

    /** Banda aceptable para el factor de calibracion. Fuera de aqui se descarta la muestra. */
    private const val MIN_FACTOR = 0.85
    private const val MAX_FACTOR = 1.15

    /** Peso de la observacion nueva en el promedio exponencial. */
    private const val ALPHA = 0.4

    /** USD que efectivamente se convierten (lo que queda tras la comision). */
    fun convertedUsd(totalUsd: Double, feeUsd: Double): Double =
        (totalUsd - feeUsd).coerceAtLeast(0.0)

    /** COP que recibe el destinatario. */
    fun receivedCop(totalUsd: Double, feeUsd: Double, rate: Double): Double {
        if (rate <= 0.0) return 0.0
        return convertedUsd(totalUsd, feeUsd) * rate
    }

    /**
     * Problema inverso: cuanto hay que entregar en total para que lleguen
     * exactamente [targetCop].
     *
     *     targetCop = (total - fee) * tasa   =>   total = targetCop / tasa + fee
     */
    fun totalUsdForTargetCop(targetCop: Double, feeUsd: Double, rate: Double): Double {
        if (rate <= 0.0) return 0.0
        return targetCop / rate + feeUsd
    }

    /**
     * Tasa implicita de ventanilla: los COP realmente entregados divididos por
     * los USD que se convirtieron (total menos comision).
     */
    fun impliedRate(totalUsd: Double, feeUsd: Double, actualCop: Double): Double {
        val convertidos = convertedUsd(totalUsd, feeUsd)
        if (convertidos <= 0.0) return 0.0
        return actualCop / convertidos
    }

    /**
     * Costo total del giro respecto a la tasa de mercado, en porcentaje.
     * Junta las dos fuentes de costo: la comision y el margen sobre la tasa.
     *
     *     costo% = 1 - recibido_real / recibido_a_tasa_de_mercado
     */
    fun totalCostPercent(totalUsd: Double, feeUsd: Double, rate: Double, midMarketRate: Double): Double {
        if (totalUsd <= 0.0 || midMarketRate <= 0.0) return 0.0
        val real = receivedCop(totalUsd, feeUsd, rate)
        val ideal = totalUsd * midMarketRate
        if (ideal <= 0.0) return 0.0
        return (1.0 - real / ideal) * 100.0
    }

    /** Cuanto cuesta el giro en USD equivalentes (comision + margen). */
    fun totalCostUsd(totalUsd: Double, feeUsd: Double, rate: Double, midMarketRate: Double): Double {
        if (midMarketRate <= 0.0) return 0.0
        val real = receivedCop(totalUsd, feeUsd, rate)
        return totalUsd - (real / midMarketRate)
    }

    /**
     * Incorpora una observacion de ventanilla al factor de calibracion Ecuador.
     *
     * Promedio exponencial: una sola ventanilla atipica no secuestra la
     * estimacion, pero el factor converge si el desvio es sistematico.
     * Muestras fuera de la banda [0.85, 1.15] se descartan por sospecha de
     * error de tecleo (un cero de mas al escribir los pesos).
     *
     * @return la calibracion actualizada, o la misma si la muestra se descarto.
     */
    fun calibrate(
        current: RemittanceCalibration,
        apiRate: Double,
        observedRate: Double,
        now: Long = System.currentTimeMillis()
    ): RemittanceCalibration {
        if (apiRate <= 0.0 || observedRate <= 0.0) return current

        val observedFactor = observedRate / apiRate
        if (observedFactor < MIN_FACTOR || observedFactor > MAX_FACTOR) return current

        val nuevoFactor = if (current.samples == 0) {
            observedFactor
        } else {
            (1.0 - ALPHA) * current.factor + ALPHA * observedFactor
        }

        return RemittanceCalibration(
            factor = nuevoFactor.coerceIn(MIN_FACTOR, MAX_FACTOR),
            samples = current.samples + 1,
            lastUpdated = now
        )
    }

    /** Aplica el factor Ecuador a una tasa de la API. */
    fun applyCalibration(apiRate: Double, calibration: RemittanceCalibration): Double =
        apiRate * calibration.factor

    /** true si dos tasas difieren lo bastante como para molestarse en avisar. */
    fun ratesDifferMaterially(a: Double, b: Double, tolerancePercent: Double = 0.25): Boolean {
        if (a <= 0.0 || b <= 0.0) return false
        return abs(a - b) / b * 100.0 > tolerancePercent
    }
}
