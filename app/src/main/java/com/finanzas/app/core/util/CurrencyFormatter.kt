package com.finanzas.app.core.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {

    /**
     * Tolerancia de medio centavo. Toda comparacion monetaria entre Double debe usarla:
     * en binario64, 756.43 + 11.06 + 232.51 = 999.9999999999999 != 1000.0
     */
    const val EPSILON: Double = 0.005

    /** Redondeo a centavos (HALF_UP), estable frente al error de representacion binaria. */
    fun roundMoney(amount: Double): Double =
        BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).toDouble()

    /** a >= b con tolerancia de medio centavo. */
    fun gte(a: Double, b: Double): Boolean = a >= b - EPSILON

    /** a > 0 con tolerancia de medio centavo (evita saldos residuales de 1e-13). */
    fun isPositive(a: Double): Boolean = a > EPSILON

    fun format(amount: Double, currencyCode: String = "USD"): String {
        return when (currencyCode) {
            "USD" -> NumberFormat.getCurrencyInstance(Locale.US).format(amount)
            "COP" -> NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(amount)
            else -> "${String.format(Locale.US, "%,.2f", amount)} $currencyCode"
        }
    }

    /**
     * Version breve para graficos y chips. Respeta la moneda: los pesos colombianos
     * no llevan decimales ni simbolo de dolar.
     */
    fun formatCompact(amount: Double, currencyCode: String = "USD"): String {
        return when (currencyCode) {
            "COP" -> "COL$ " + String.format(Locale.US, "%,.0f", amount)
            "USD" -> "$ " + String.format(Locale.US, "%,.2f", amount)
            else -> String.format(Locale.US, "%,.2f", amount) + " " + currencyCode
        }
    }

    /** Porcentaje sin ceros sobrantes: 10.0 -> "10", 12.75 -> "12.75" */
    fun formatPercent(value: Double, decimals: Int = 2): String {
        val s = String.format(Locale.US, "%.${decimals}f", value)
        return if (s.contains('.')) s.trimEnd('0').trimEnd('.') else s
    }

    /** Tasa de cambio: 4150.0 -> "4,150.00" */
    fun formatRate(rate: Double): String = String.format(Locale.US, "%,.2f", rate)

    fun formatSigned(amount: Double, currencyCode: String = "USD"): String {
        val sign = if (amount < 0) "-" else "+"
        return sign + format(abs(amount), currencyCode)
    }
}
