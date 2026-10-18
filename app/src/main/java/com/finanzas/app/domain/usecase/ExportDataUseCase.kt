package com.finanzas.app.domain.usecase

import android.content.Context
import com.finanzas.app.core.util.BackupExporter
import com.finanzas.app.domain.repository.AccountRepository
import com.finanzas.app.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportDataUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend fun exportToJson(context: Context) {
        val accounts = accountRepository.getAllAccounts()
        val transactions = transactionRepository.getAllTransactionsForExport()
        val json = BackupExporter.generateJson(accounts, transactions)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        BackupExporter.shareExportFile(
            context = context,
            content = json,
            fileName = "finanzas_backup_$timestamp.json",
            mimeType = "application/json"
        )
    }

    suspend fun exportToCsv(context: Context) {
        val transactions = transactionRepository.getAllTransactionsForExport()
        val csv = BackupExporter.generateCsv(transactions)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        BackupExporter.shareExportFile(
            context = context,
            content = csv,
            fileName = "movimientos_$timestamp.csv",
            mimeType = "text/csv"
        )
    }
}
