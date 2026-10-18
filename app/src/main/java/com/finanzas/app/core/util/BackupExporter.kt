package com.finanzas.app.core.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.TransactionWithDetails
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

object BackupExporter {

    fun generateJson(accounts: List<Account>, transactions: List<TransactionWithDetails>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("exportDate", DateFormatter.formatIso(System.currentTimeMillis()))

        val accountsArray = JSONArray()
        for (acc in accounts) {
            val accObj = JSONObject()
            accObj.put("id", acc.id)
            accObj.put("name", acc.name)
            accObj.put("type", acc.type.name)
            accObj.put("currency", acc.currency)
            accObj.put("currentBalance", acc.currentBalance)
            accObj.put("colorHex", acc.colorHex)
            accObj.put("isDefault", acc.isDefault)
            accountsArray.put(accObj)
        }
        root.put("accounts", accountsArray)

        val txArray = JSONArray()
        for (item in transactions) {
            val tx = item.transaction
            val txObj = JSONObject()
            txObj.put("id", tx.id)
            txObj.put("type", tx.type.name)
            txObj.put("amount", tx.amount)
            txObj.put("feeAmount", tx.feeAmount)
            txObj.put("sourceAccount", item.sourceAccount?.name ?: tx.sourceAccountId)
            txObj.put("targetAccount", item.targetAccount?.name ?: tx.targetAccountId)
            txObj.put("category", item.category?.name ?: tx.categoryId)
            txObj.put("targetAmount", tx.targetAmount)
            txObj.put("targetCurrency", tx.targetCurrency)
            txObj.put("exchangeRate", tx.exchangeRate)
            txObj.put("installments", tx.installments)
            txObj.put("timestamp", tx.timestamp)
            txObj.put("dateIso", DateFormatter.formatIso(tx.timestamp))
            txObj.put("note", tx.note)
            txObj.put("isSynced", tx.isSynced)
            txArray.put(txObj)
        }
        root.put("transactions", txArray)

        return root.toString(2)
    }

    fun generateCsv(transactions: List<TransactionWithDetails>): String {
        val sb = StringBuilder()
        // Header
        sb.append("ID,Fecha,Tipo,Monto,Comision,Total_Debitado,Cuenta_Origen,Cuenta_Destino,Categoria,Monto_Destino,Moneda_Destino,Tasa_Cambio,Cuotas,Nota\n")

        for (item in transactions) {
            val tx = item.transaction
            sb.append("\"${tx.id}\",")
            sb.append("\"${DateFormatter.formatIso(tx.timestamp)}\",")
            sb.append("\"${tx.type.displayName}\",")
            sb.append("${tx.amount},")
            sb.append("${tx.feeAmount},")
            sb.append("${tx.totalDebited},")
            sb.append("\"${item.sourceAccount?.name ?: ""}\",")
            sb.append("\"${item.targetAccount?.name ?: ""}\",")
            sb.append("\"${item.category?.name ?: ""}\",")
            sb.append("${tx.targetAmount ?: ""},")
            sb.append("\"${tx.targetCurrency ?: ""}\",")
            sb.append("${tx.exchangeRate ?: ""},")
            sb.append("${tx.installments ?: ""},")
            sb.append("\"${(tx.note ?: "").replace("\"", "\"\"")}\"\n")
        }

        return sb.toString()
    }

    fun shareExportFile(context: Context, content: String, fileName: String, mimeType: String) {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val file = File(exportDir, fileName)
        FileWriter(file).use { writer ->
            writer.write(content)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Respaldo Finanzas: $fileName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Exportar respaldo vía...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
