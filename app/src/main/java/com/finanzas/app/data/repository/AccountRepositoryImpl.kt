package com.finanzas.app.data.repository

import com.finanzas.app.data.local.dao.AccountDao
import com.finanzas.app.data.mapper.toDomain
import com.finanzas.app.data.mapper.toEntity
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getAllAccountsFlow(): Flow<List<Account>> {
        return accountDao.getAllAccountsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllAccounts(): List<Account> {
        return accountDao.getAllAccounts().map { it.toDomain() }
    }

    override suspend fun getAccountById(id: String): Account? {
        return accountDao.getAccountById(id)?.toDomain()
    }

    override suspend fun insertAccount(account: Account) {
        accountDao.insertAccount(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toEntity())
    }

    override suspend fun updateBalance(accountId: String, newBalance: Double) {
        accountDao.updateBalance(accountId, newBalance)
    }

    override suspend fun deleteAccount(accountId: String) {
        accountDao.deleteAccountById(accountId)
    }

    override suspend fun getDefaultAccount(): Account? {
        return accountDao.getDefaultAccount()?.toDomain()
    }
}
