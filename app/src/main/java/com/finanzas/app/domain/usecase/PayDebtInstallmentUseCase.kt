package com.finanzas.app.domain.usecase

import com.finanzas.app.domain.repository.DebtRepository

class PayDebtInstallmentUseCase(
    private val debtRepository: DebtRepository
) {
    suspend operator fun invoke(
        installmentId: String,
        paidDate: Long = System.currentTimeMillis(),
        actualAmountPaid: Double,
        lateFee: Double = 0.0,
        sourceAccountId: String? = null,
        notes: String? = null
    ): Result<Unit> {
        if (actualAmountPaid <= 0.0) {
            return Result.failure(IllegalArgumentException("El monto a pagar debe ser mayor a 0.00"))
        }
        return debtRepository.payInstallment(
            installmentId = installmentId,
            paidDate = paidDate,
            actualAmountPaid = actualAmountPaid,
            lateFee = lateFee,
            sourceAccountId = sourceAccountId,
            notes = notes
        )
    }
}
