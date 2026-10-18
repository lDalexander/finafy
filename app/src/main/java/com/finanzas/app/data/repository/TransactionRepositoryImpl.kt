package com.finanzas.app.data.repository

import com.finanzas.app.data.local.dao.AccountDao
import com.finanzas.app.data.local.dao.CategoryDao
import com.finanzas.app.data.local.dao.TransactionDao
import com.finanzas.app.data.mapper.toDomain
import com.finanzas.app.data.mapper.toEntity
import com.finanzas.app.domain.model.Transaction
import com.finanzas.app.domain.model.TransactionWithDetails
import com.finanzas.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao
) : TransactionRepository {

    override fun getAllTransactionsWithDetailsFlow(): Flow<List<TransactionWithDetails>> {
        return combine(
            transactionDao.getAllTransactionsFlow(),
            accountDao.getAllAccountsFlow(),
            categoryDao.getAllCategoriesFlow()
        ) { transactions, accounts, categories ->
            val accountMap = accounts.associateBy { it.id }
            val categoryMap = categories.associateBy { it.id }

            transactions.map { entity ->
                TransactionWithDetails(
                    transaction = entity.toDomain(),
                    sourceAccount = entity.sourceAccountId?.let { accountMap[it]?.toDomain() },
                    targetAccount = entity.targetAccountId?.let { accountMap[it]?.toDomain() },
                    category = entity.categoryId?.let { categoryMap[it]?.toDomain() }
                )
            }
        }
    }

    override fun getRecentTransactionsWithDetailsFlow(limit: Int): Flow<List<TransactionWithDetails>> {
        return combine(
            transactionDao.getRecentTransactionsFlow(limit),
            accountDao.getAllAccountsFlow(),
            categoryDao.getAllCategoriesFlow()
        ) { transactions, accounts, categories ->
            val accountMap = accounts.associateBy { it.id }
            val categoryMap = categories.associateBy { it.id }

            transactions.map { entity ->
                TransactionWithDetails(
                    transaction = entity.toDomain(),
                    sourceAccount = entity.sourceAccountId?.let { accountMap[it]?.toDomain() },
                    targetAccount = entity.targetAccountId?.let { accountMap[it]?.toDomain() },
                    category = entity.categoryId?.let { categoryMap[it]?.toDomain() }
                )
            }
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun registerMovement(transaction: Transaction): Result<Unit> {
        return try {
            transactionDao.registerMovementAtomic(transaction.toEntity(), accountDao)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMovement(transaction: Transaction): Result<Unit> {
        return try {
            transactionDao.updateMovementAtomic(transaction.toEntity(), accountDao)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMovement(transactionId: String): Result<Unit> {
        return try {
            transactionDao.deleteMovementAtomic(transactionId, accountDao)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUnsyncedTransactions(): List<Transaction> {
        return transactionDao.getUnsyncedTransactions().map { it.toDomain() }
    }

    override suspend fun markTransactionsAsSynced(ids: List<String>, timestamp: Long) {
        transactionDao.markAsSynced(ids, timestamp)
    }

    override suspend fun countMovementsForAccount(accountId: String): Int {
        return transactionDao.countMovementsForAccount(accountId)
    }

    override suspend fun getAllTransactionsForExport(): List<TransactionWithDetails> {
        val transactions = transactionDao.getAllTransactions()
        val accounts = accountDao.getAllAccounts().associateBy { it.id }
        val categoryMap = categoryDao.getAllCategories().associateBy { it.id }

        return transactions.map { entity ->
            TransactionWithDetails(
                transaction = entity.toDomain(),
                sourceAccount = entity.sourceAccountId?.let { accounts[it]?.toDomain() },
                targetAccount = entity.targetAccountId?.let { accounts[it]?.toDomain() },
                category = entity.categoryId?.let { categoryMap[it]?.toDomain() }
            )
        }
    }
}
