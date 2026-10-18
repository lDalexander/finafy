package com.finanzas.app.data.remote

import android.util.Log
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.Transaction
import kotlinx.coroutines.delay

/**
 * Implementación simulada de backend para desarrollo y pruebas offline-first.
 * Simula latencia de red y retorno exitoso de IDs sincronizados.
 */
class FakeRemoteSyncDataSource : RemoteSyncDataSource {

    companion object {
        private const val TAG = "FakeRemoteSync"
    }

    override suspend fun pushTransactions(transactions: List<Transaction>): Result<List<String>> {
        return try {
            Log.d(TAG, "Enviando ${transactions.size} movimientos al backend remoto...")
            // Simular latencia de red de 600ms
            delay(600)
            val syncedIds = transactions.map { it.id }
            Log.d(TAG, "Movimientos sincronizados exitosamente: $syncedIds")
            Result.success(syncedIds)
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando con backend: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun pullTransactions(sinceTimestamp: Long): Result<List<Transaction>> {
        return try {
            delay(500)
            Log.d(TAG, "Consultando nuevos movimientos desde timestamp: $sinceTimestamp")
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncAccounts(accounts: List<Account>): Result<Unit> {
        return try {
            delay(400)
            Log.d(TAG, "Sincronizando ${accounts.size} cuentas con backend...")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
