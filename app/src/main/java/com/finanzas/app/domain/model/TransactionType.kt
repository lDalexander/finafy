package com.finanzas.app.domain.model

/**
 * Representa los 5 tipos de movimientos financieros soportados por la aplicación.
 */
enum class TransactionType(val displayName: String) {
    EXPENSE("Gasto"),
    CREDIT_PURCHASE("Tarjeta Crédito"),
    TRANSFER("Transferencia"),
    REMITTANCE("Giro / Remesa"),
    INCOME("Ingreso")
}
