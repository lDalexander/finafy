package com.finanzas.app.core.util

/**
 * Normalizacion dimensional.
 *
 * Sumar 1.200 USD + 4.150.000 COP y mostrar el resultado con simbolo de dolar
 * inflaba el patrimonio ~1.887x. Todo agregado (saldo total, ingresos, gastos,
 * deudas, informes) debe pasar por aqui antes de sumar.
 *
 * Regla: la moneda base de la app es USD. Un monto solo se suma a otro despues
 * de convertirse a la base con la TRM vigente.
 */
object MoneyConverter {

    const val BASE_CURRENCY: String = "USD"

    /**
     * Convierte [amount] de [currency] a USD.
     *
     * @param usdToCopRate cuantos COP vale 1 USD (TRM oficial).
     *        Si es <= 0 se asume 1.0 para no producir infinitos ni NaN.
     */
    fun toBase(amount: Double, currency: String?, usdToCopRate: Double): Double {
        return when (currency) {
            null, BASE_CURRENCY -> amount
            "COP" -> if (usdToCopRate > 0.0) amount / usdToCopRate else amount
            // Monedas no soportadas: se devuelven sin convertir. Documentado a
            // proposito: la app solo maneja USD y COP hoy.
            else -> amount
        }
    }

    /** Convierte de USD a [currency]. */
    fun fromBase(amountUsd: Double, currency: String?, usdToCopRate: Double): Double {
        return when (currency) {
            null, BASE_CURRENCY -> amountUsd
            "COP" -> if (usdToCopRate > 0.0) amountUsd * usdToCopRate else amountUsd
            else -> amountUsd
        }
    }
}
