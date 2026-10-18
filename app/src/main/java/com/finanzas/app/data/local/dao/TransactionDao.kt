package com.finanzas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.finanzas.app.data.local.entity.TransactionEntity
import com.finanzas.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    abstract fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    abstract fun getRecentTransactionsFlow(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    abstract suspend fun getTransactionById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    abstract suspend fun deleteTransactionById(id: String)

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    abstract suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET isSynced = 1, syncTimestamp = :timestamp WHERE id IN (:ids)")
    abstract suspend fun markAsSynced(ids: List<String>, timestamp: Long)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    abstract suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions")
    abstract suspend fun getTransactionsCount(): Int

    /** Movimientos en los que participa la cuenta, como origen o como destino. */
    @Query(
        "SELECT COUNT(*) FROM transactions " +
                "WHERE sourceAccountId = :accountId OR targetAccountId = :accountId"
    )
    abstract suspend fun countMovementsForAccount(accountId: String): Int

    /**
     * Registra un movimiento y actualiza atómicamente los saldos de las cuentas involucradas.
     */
    @Transaction
    open suspend fun registerMovementAtomic(
        transaction: TransactionEntity,
        accountDao: AccountDao
    ) {
        insertTransaction(transaction)

        when (transaction.type) {
            TransactionType.EXPENSE -> {
                transaction.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val newBalance = source.currentBalance - (transaction.amount + transaction.feeAmount)
                        accountDao.updateBalance(sourceId, newBalance)
                    }
                }
            }

            TransactionType.INCOME -> {
                val accountId = transaction.targetAccountId ?: transaction.sourceAccountId
                accountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val newBalance = target.currentBalance + (transaction.amount - transaction.feeAmount)
                        accountDao.updateBalance(targetId, newBalance)
                    }
                }
            }

            TransactionType.TRANSFER -> {
                transaction.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val newBalance = source.currentBalance - (transaction.amount + transaction.feeAmount)
                        accountDao.updateBalance(sourceId, newBalance)
                    }
                }
                transaction.targetAccountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val newBalance = if (target.type == com.finanzas.app.domain.model.AccountType.CREDIT_CARD) {
                            // C-04: sin saturacion. max(x-a,0)+a != x hacia que al ELIMINAR
                            // un sobrepago se fabricara deuda de la nada. Un saldo negativo
                            // en tarjeta es informacion legitima (saldo a favor).
                            target.currentBalance - transaction.amount
                        } else {
                            target.currentBalance + transaction.amount
                        }
                        accountDao.updateBalance(targetId, newBalance)
                    }
                }
            }

            TransactionType.CREDIT_PURCHASE -> {
                transaction.sourceAccountId?.let { cardId ->
                    val card = accountDao.getAccountById(cardId)
                    if (card != null) {
                        // En tarjetas de crédito, el balance representa el cupo utilizado (deuda acumulada)
                        val newBalance = card.currentBalance + transaction.amount + transaction.feeAmount
                        accountDao.updateBalance(cardId, newBalance)
                    }
                }
            }

            TransactionType.REMITTANCE -> {
                transaction.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val newBalance = source.currentBalance - (transaction.amount + transaction.feeAmount)
                        accountDao.updateBalance(sourceId, newBalance)
                    }
                }
                transaction.targetAccountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val receivedAmount = transaction.targetAmount
                            ?: (transaction.amount * (transaction.exchangeRate ?: 1.0))
                        val newTargetBalance = target.currentBalance + receivedAmount
                        accountDao.updateBalance(targetId, newTargetBalance)
                    }
                }
            }
        }
    }

    /**
     * Elimina un movimiento revirtiendo atómicamente el impacto en los saldos.
     */
    @Transaction
    open suspend fun deleteMovementAtomic(
        transactionId: String,
        accountDao: AccountDao
    ) {
        val transaction = getTransactionById(transactionId) ?: return

        when (transaction.type) {
            TransactionType.EXPENSE -> {
                transaction.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val restoredBalance = source.currentBalance + (transaction.amount + transaction.feeAmount)
                        accountDao.updateBalance(sourceId, restoredBalance)
                    }
                }
            }

            TransactionType.INCOME -> {
                val accountId = transaction.targetAccountId ?: transaction.sourceAccountId
                accountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val restoredBalance = target.currentBalance - (transaction.amount - transaction.feeAmount)
                        accountDao.updateBalance(targetId, restoredBalance)
                    }
                }
            }

            TransactionType.TRANSFER -> {
                transaction.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val restoredBalance = source.currentBalance + (transaction.amount + transaction.feeAmount)
                        accountDao.updateBalance(sourceId, restoredBalance)
                    }
                }
                transaction.targetAccountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val restoredBalance = if (target.type == com.finanzas.app.domain.model.AccountType.CREDIT_CARD) {
                            target.currentBalance + transaction.amount
                        } else {
                            target.currentBalance - transaction.amount
                        }
                        accountDao.updateBalance(targetId, restoredBalance)
                    }
                }
            }

            TransactionType.CREDIT_PURCHASE -> {
                transaction.sourceAccountId?.let { cardId ->
                    val card = accountDao.getAccountById(cardId)
                    if (card != null) {
                        val restoredBalance = card.currentBalance - (transaction.amount + transaction.feeAmount)
                        accountDao.updateBalance(cardId, restoredBalance)
                    }
                }
            }

            TransactionType.REMITTANCE -> {
                transaction.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val restoredBalance = source.currentBalance + (transaction.amount + transaction.feeAmount)
                        accountDao.updateBalance(sourceId, restoredBalance)
                    }
                }
                transaction.targetAccountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val receivedAmount = transaction.targetAmount
                            ?: (transaction.amount * (transaction.exchangeRate ?: 1.0))
                        val restoredTargetBalance = target.currentBalance - receivedAmount
                        accountDao.updateBalance(targetId, restoredTargetBalance)
                    }
                }
            }
        }

        deleteTransactionById(transactionId)
    }

    /**
     * Actualiza un movimiento revirtiendo el impacto anterior y aplicando el nuevo impacto en los saldos.
     */
    @Transaction
    open suspend fun updateMovementAtomic(
        updatedTransaction: TransactionEntity,
        accountDao: AccountDao
    ) {
        val existing = getTransactionById(updatedTransaction.id) ?: return

        // 1. Revertir saldo anterior
        when (existing.type) {
            TransactionType.EXPENSE -> {
                existing.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val restoredBalance = source.currentBalance + (existing.amount + existing.feeAmount)
                        accountDao.updateBalance(sourceId, restoredBalance)
                    }
                }
            }

            TransactionType.INCOME -> {
                val accountId = existing.targetAccountId ?: existing.sourceAccountId
                accountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val restoredBalance = target.currentBalance - (existing.amount - existing.feeAmount)
                        accountDao.updateBalance(targetId, restoredBalance)
                    }
                }
            }

            TransactionType.TRANSFER -> {
                existing.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val restoredBalance = source.currentBalance + (existing.amount + existing.feeAmount)
                        accountDao.updateBalance(sourceId, restoredBalance)
                    }
                }
                existing.targetAccountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val restoredBalance = if (target.type == com.finanzas.app.domain.model.AccountType.CREDIT_CARD) {
                            target.currentBalance + existing.amount
                        } else {
                            target.currentBalance - existing.amount
                        }
                        accountDao.updateBalance(targetId, restoredBalance)
                    }
                }
            }

            TransactionType.CREDIT_PURCHASE -> {
                existing.sourceAccountId?.let { cardId ->
                    val card = accountDao.getAccountById(cardId)
                    if (card != null) {
                        val restoredBalance = card.currentBalance - (existing.amount + existing.feeAmount)
                        accountDao.updateBalance(cardId, restoredBalance)
                    }
                }
            }

            TransactionType.REMITTANCE -> {
                existing.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val restoredBalance = source.currentBalance + (existing.amount + existing.feeAmount)
                        accountDao.updateBalance(sourceId, restoredBalance)
                    }
                }
                existing.targetAccountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val receivedAmount = existing.targetAmount
                            ?: (existing.amount * (existing.exchangeRate ?: 1.0))
                        val restoredTargetBalance = target.currentBalance - receivedAmount
                        accountDao.updateBalance(targetId, restoredTargetBalance)
                    }
                }
            }
        }

        // 2. Aplicar nuevo saldo
        when (updatedTransaction.type) {
            TransactionType.EXPENSE -> {
                updatedTransaction.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val newBalance = source.currentBalance - (updatedTransaction.amount + updatedTransaction.feeAmount)
                        accountDao.updateBalance(sourceId, newBalance)
                    }
                }
            }

            TransactionType.INCOME -> {
                val accountId = updatedTransaction.targetAccountId ?: updatedTransaction.sourceAccountId
                accountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val newBalance = target.currentBalance + (updatedTransaction.amount - updatedTransaction.feeAmount)
                        accountDao.updateBalance(targetId, newBalance)
                    }
                }
            }

            TransactionType.TRANSFER -> {
                updatedTransaction.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val newBalance = source.currentBalance - (updatedTransaction.amount + updatedTransaction.feeAmount)
                        accountDao.updateBalance(sourceId, newBalance)
                    }
                }
                updatedTransaction.targetAccountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val newBalance = if (target.type == com.finanzas.app.domain.model.AccountType.CREDIT_CARD) {
                            target.currentBalance - updatedTransaction.amount
                        } else {
                            target.currentBalance + updatedTransaction.amount
                        }
                        accountDao.updateBalance(targetId, newBalance)
                    }
                }
            }

            TransactionType.CREDIT_PURCHASE -> {
                updatedTransaction.sourceAccountId?.let { cardId ->
                    val card = accountDao.getAccountById(cardId)
                    if (card != null) {
                        val newBalance = card.currentBalance + updatedTransaction.amount + updatedTransaction.feeAmount
                        accountDao.updateBalance(cardId, newBalance)
                    }
                }
            }

            TransactionType.REMITTANCE -> {
                updatedTransaction.sourceAccountId?.let { sourceId ->
                    val source = accountDao.getAccountById(sourceId)
                    if (source != null) {
                        val newBalance = source.currentBalance - (updatedTransaction.amount + updatedTransaction.feeAmount)
                        accountDao.updateBalance(sourceId, newBalance)
                    }
                }
                updatedTransaction.targetAccountId?.let { targetId ->
                    val target = accountDao.getAccountById(targetId)
                    if (target != null) {
                        val receivedAmount = updatedTransaction.targetAmount
                            ?: (updatedTransaction.amount * (updatedTransaction.exchangeRate ?: 1.0))
                        val newTargetBalance = target.currentBalance + receivedAmount
                        accountDao.updateBalance(targetId, newTargetBalance)
                    }
                }
            }
        }

        insertTransaction(updatedTransaction)
    }
}
