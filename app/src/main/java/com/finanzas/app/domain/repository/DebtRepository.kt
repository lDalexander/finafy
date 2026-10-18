package com.finanzas.app.domain.repository

import com.finanzas.app.domain.model.Debt
import com.finanzas.app.domain.model.DebtInstallment
import com.finanzas.app.domain.model.DebtWithInstallments
import kotlinx.coroutines.flow.Flow

interface DebtRepository {
    fun getAllDebtsWithInstallmentsFlow(): Flow<List<DebtWithInstallments>>
    fun getDebtWithInstallmentsFlow(debtId: String): Flow<DebtWithInstallments?>
    suspend fun getDebtById(debtId: String): Debt?
    suspend fun createDebtWithInstallments(debt: Debt, installments: List<DebtInstallment>): Result<Unit>
    suspend fun payInstallment(
        installmentId: String,
        paidDate: Long,
        actualAmountPaid: Double,
        lateFee: Double,
        sourceAccountId: String?,
        notes: String?
    ): Result<Unit>
    suspend fun addDebtAbono(
        debtId: String,
        abonoAmount: Double,
        paidDate: Long = System.currentTimeMillis(),
        sourceAccountId: String?,
        notes: String?
    ): Result<Unit>
    suspend fun deleteDebt(debtId: String): Result<Unit>
}
