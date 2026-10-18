package com.finanzas.app.domain.usecase

import com.finanzas.app.domain.repository.DebtRepository

class DeleteDebtUseCase(
    private val debtRepository: DebtRepository
) {
    suspend operator fun invoke(debtId: String): Result<Unit> {
        return debtRepository.deleteDebt(debtId)
    }
}
