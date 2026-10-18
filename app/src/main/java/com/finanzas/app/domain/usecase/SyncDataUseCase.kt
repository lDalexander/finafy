package com.finanzas.app.domain.usecase

import com.finanzas.app.data.remote.RemoteSyncDataSource
import com.finanzas.app.domain.repository.AccountRepository
import com.finanzas.app.domain.repository.TransactionRepository

class SyncDataUseCase(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val remoteSyncDataSource: RemoteSyncDataSource
) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            // 1. Sincronizar cuentas
            val accounts = accountRepository.getAllAccounts()
            accountRepository.getAllAccounts()
            remoteSyncDataSource.syncAccounts(accounts).getOrThrow()

            // 2. Sincronizar transacciones pendientes
            val unsynced = transactionRepository.getUnsyncedTransactions()
            if (unsynced.isEmpty()) {
                return Result.success(0)
            }

            val result = remoteSyncDataSource.pushTransactions(unsynced)
            val syncedIds = result.getOrThrow()

            val now = System.currentTimeMillis()
            transactionRepository.markTransactionsAsSynced(syncedIds, now)

            Result.success(syncedIds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
