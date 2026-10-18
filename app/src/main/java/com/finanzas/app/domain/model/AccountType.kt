package com.finanzas.app.domain.model

/**
 * Clasificación de los tipos de cuentas financieras en el sistema.
 */
enum class AccountType(val displayName: String) {
    CASH("Efectivo"),
    BANK_CHECKING("Cuenta Corriente"),
    BANK_SAVINGS("Cuenta de Ahorros"),
    CREDIT_CARD("Tarjeta de Crédito"),
    WALLET("Billetera Digital")
}
