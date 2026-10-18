package com.finanzas.app.domain.model

import com.finanzas.app.core.util.CurrencyFormatter

/**
 * Vista consolidada de una deuda y su calendario de cuotas.
 *
 * MODELO CONTABLE (invariantes que este archivo garantiza):
 *
 *  INV-D1  La mora NUNCA amortiza deuda. Sale de la caja pero no reduce el saldo.
 *  INV-D2  Una sola definicion de saldo pendiente por modalidad:
 *            - cuotas fijas : suma de (capital + interes) de las cuotas NO pagadas
 *            - abonos libres: total financiado - lo amortizado
 *          Un abono sobre una deuda de cuotas fijas amortiza el calendario
 *          (ver DebtDao.amortizePending), asi que ambas ramas reaccionan igual.
 *  INV-D3  Toda comparacion contra cero usa EPSILON (medio centavo). En binario64
 *          756.43 + 11.06 + 232.51 = 999.9999999999999, no 1000.0.
 */
data class DebtWithInstallments(
    val debt: Debt,
    val installments: List<DebtInstallment>
) {
    val isFlexible: Boolean
        get() = debt.totalInstallments <= 0

    val paidInstallmentsCount: Int
        get() = installments.count { it.isPaid }

    /**
     * Dinero efectivamente desembolsado (capital + interes + mora).
     * Incluye pagos parciales: una cuota pendiente puede tener actualPaidAmount > 0.
     */
    val totalPaidOverall: Double
        get() = installments.sumOf {
            it.actualPaidAmount ?: (if (it.isPaid) it.totalDue else 0.0)
        }

    /** Mora pagada. Solo se registra en el momento del pago. */
    val totalPaidLateFees: Double
        get() = installments.sumOf { it.lateFeeAmount }

    /** INV-D1: la porcion del desembolso que si amortiza deuda. */
    val totalPaidAmortizing: Double
        get() = (totalPaidOverall - totalPaidLateFees).coerceAtLeast(0.0)

    /** INV-D2 + INV-D3 */
    val remainingTotalDebt: Double
        get() {
            val raw = if (isFlexible) {
                debt.totalWithInterest - totalPaidAmortizing
            } else {
                installments.filter { !it.isPaid }.sumOf { it.principalAmount + it.interestAmount }
            }
            return if (raw <= CurrencyFormatter.EPSILON) 0.0 else raw
        }

    /** Cuanto del total financiado ya quedo cubierto. */
    val totalAmortized: Double
        get() = (debt.totalWithInterest - remainingTotalDebt).coerceAtLeast(0.0)

    val totalPaidInterest: Double
        get() = installments.filter { it.isPaid }.sumOf { it.interestAmount }

    /** Capital amortizado = amortizado total menos los intereses ya cubiertos. */
    val totalPaidPrincipal: Double
        get() = (totalAmortized - totalPaidInterest).coerceAtLeast(0.0)

    val isSettled: Boolean
        get() = remainingTotalDebt <= 0.0

    val pendingInstallmentsCount: Int
        get() = if (isFlexible) {
            if (isSettled) 0 else 1
        } else {
            installments.count { !it.isPaid }
        }

    val nextPendingInstallment: DebtInstallment?
        get() = installments.filter { !it.isPaid }.minByOrNull { it.dueDate }

    /**
     * Progreso por MONTO, no por conteo de cuotas: un abono parcial mueve la barra
     * en proporcion al dinero aportado, que es lo que el usuario espera ver.
     */
    val progress: Float
        get() = if (debt.totalWithInterest > 0.0) {
            (totalAmortized / debt.totalWithInterest).toFloat().coerceIn(0f, 1f)
        } else 0f
}
