package com.finanzas.app.domain.repository

import com.finanzas.app.domain.model.Transaction
import com.finanzas.app.domain.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactionsWithDetailsFlow(): Flow<List<TransactionWithDetails>>
    fun getRecentTransactionsWithDetailsFlow(limit: Int = 50): Flow<List<TransactionWithDetails>>
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun registerMovement(transaction: Transaction): Result<Unit>
    suspend fun updateMovement(transaction: Transaction): Result<Unit>
    suspend fun deleteMovement(transactionId: String): Result<Unit>
    suspend fun getUnsyncedTransactions(): List<Transaction>
    suspend fun markTransactionsAsSynced(ids: List<String>, timestamp: Long)
    suspend fun getAllTransactionsForExport(): List<TransactionWithDetails>

    /** Cuantos movimientos tocan esta cuenta. Se usa para saber si su moneda ya no puede cambiar. */
    suspend fun countMovementsForAccount(accountId: String): Int
}
