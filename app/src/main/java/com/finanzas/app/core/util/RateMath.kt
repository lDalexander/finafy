package com.finanzas.app.core.util

import kotlin.math.pow

/**
 * Conversion entre el recargo PLANO que usa la app y la tasa efectiva real.
 *
 * La app pide "% de interes" y aplica  interes = capital * (%/100)  una sola vez,
 * sin importar el plazo. Eso es un recargo plano (add-on), no una tasa.
 * El costo real del dinero es muy superior, porque el capital se va devolviendo
 * mes a mes mientras el recargo se cobro sobre el saldo inicial completo.
 *
 * Ejemplos (verificados):
 *   10% a  3 cuotas -> TEM 4,921%  -> TEA 77,98%
 *   10% a 12 cuotas -> TEM 1,498%  -> TEA 19,53%
 *   15% a 24 cuotas -> TEM 1,150%  -> TEA 14,70%
 */
object RateMath {

    /**
     * Tasa efectiva mensual: la r que resuelve
     *     capital = sum_{t=1..n} cuota / (1+r)^t
     * Se resuelve por biseccion; el valor presente es estrictamente decreciente
     * en r, asi que la raiz es unica en (0, 1].
     */
    fun monthlyEffectiveRate(principal: Double, installment: Double, months: Int): Double {
        if (principal <= 0.0 || installment <= 0.0 || months <= 0) return 0.0
        val total = installment * months
        if (total <= principal) return 0.0

        var lo = 0.0
        var hi = 1.0
        repeat(120) {
            val mid = (lo + hi) / 2.0
            var pv = 0.0
            for (t in 1..months) pv += installment / (1.0 + mid).pow(t)
            if (pv > principal) lo = mid else hi = mid
        }
        return (lo + hi) / 2.0
    }

    /** Tasa efectiva anual equivalente: (1+TEM)^12 - 1 */
    fun effectiveAnnualRate(principal: Double, installment: Double, months: Int): Double {
        val tem = monthlyEffectiveRate(principal, installment, months)
        if (tem <= 0.0) return 0.0
        return (1.0 + tem).pow(12) - 1.0
    }
}
