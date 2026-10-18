package com.finanzas.app.data.remote

import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.Transaction

/**
 * Contrato para sincronización Offline-First con backend remoto (REST/GraphQL/gRPC).
 */
interface RemoteSyncDataSource {
    suspend fun pushTransactions(transactions: List<Transaction>): Result<List<String>>
    suspend fun pullTransactions(sinceTimestamp: Long): Result<List<Transaction>>
    suspend fun syncAccounts(accounts: List<Account>): Result<Unit>
}
