package com.finanzas.app.domain.repository

import com.finanzas.app.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccountsFlow(): Flow<List<Account>>
    suspend fun getAllAccounts(): List<Account>
    suspend fun getAccountById(id: String): Account?
    suspend fun insertAccount(account: Account)
    suspend fun updateAccount(account: Account)
    suspend fun updateBalance(accountId: String, newBalance: Double)
    suspend fun deleteAccount(accountId: String)
    suspend fun getDefaultAccount(): Account?
}
