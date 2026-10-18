package com.finanzas.app.data.repository

import com.finanzas.app.data.local.dao.AccountDao
import com.finanzas.app.data.local.dao.DebtDao
import com.finanzas.app.data.local.dao.TransactionDao
import com.finanzas.app.data.mapper.toDomain
import com.finanzas.app.data.mapper.toEntity
import com.finanzas.app.domain.model.Debt
import com.finanzas.app.domain.model.DebtInstallment
import com.finanzas.app.domain.model.DebtWithInstallments
import com.finanzas.app.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DebtRepositoryImpl(
    private val debtDao: DebtDao,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) : DebtRepository {

    override fun getAllDebtsWithInstallmentsFlow(): Flow<List<DebtWithInstallments>> {
        return combine(
            debtDao.getAllDebtsFlow(),
            debtDao.getAllInstallmentsFlow()
        ) { debts, installments ->
            val installmentsGrouped = installments.groupBy { it.debtId }
            debts.map { debtEntity ->
                val items = installmentsGrouped[debtEntity.id]?.map { it.toDomain() } ?: emptyList()
                DebtWithInstallments(
                    debt = debtEntity.toDomain(),
                    installments = items.sortedBy { it.installmentNumber }
                )
            }
        }
    }

    override fun getDebtWithInstallmentsFlow(debtId: String): Flow<DebtWithInstallments?> {
        return combine(
            debtDao.getDebtFlow(debtId),
            debtDao.getInstallmentsForDebtFlow(debtId)
        ) { debtEntity, installments ->
            debtEntity?.let {
                DebtWithInstallments(
                    debt = it.toDomain(),
                    installments = installments.map { inst -> inst.toDomain() }.sortedBy { it.installmentNumber }
                )
            }
        }
    }

    override suspend fun getDebtById(debtId: String): Debt? {
        return debtDao.getDebtById(debtId)?.toDomain()
    }

    override suspend fun createDebtWithInstallments(
        debt: Debt,
        installments: List<DebtInstallment>
    ): Result<Unit> {
        return try {
            debtDao.insertDebtWithInstallmentsAtomic(
                debt = debt.toEntity(),
                installments = installments.map { it.toEntity() }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun payInstallment(
        installmentId: String,
        paidDate: Long,
        actualAmountPaid: Double,
        lateFee: Double,
        sourceAccountId: String?,
        notes: String?
    ): Result<Unit> {
        return try {
            debtDao.payInstallmentAtomic(
                installmentId = installmentId,
                paidDate = paidDate,
                actualAmount = actualAmountPaid,
                lateFee = lateFee,
                sourceAccountId = sourceAccountId,
                notes = notes,
                accountDao = accountDao,
                transactionDao = transactionDao
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addDebtAbono(
        debtId: String,
        abonoAmount: Double,
        paidDate: Long,
        sourceAccountId: String?,
        notes: String?
    ): Result<Unit> {
        return try {
            debtDao.addDebtAbonoAtomic(
                debtId = debtId,
                abonoAmount = abonoAmount,
                paidDate = paidDate,
                sourceAccountId = sourceAccountId,
                notes = notes,
                accountDao = accountDao,
                transactionDao = transactionDao
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDebt(debtId: String): Result<Unit> {
        return try {
            debtDao.deleteDebtById(debtId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
