package com.finanzas.app.domain.model

/**
 * Modelo enriquecido con las entidades de Cuenta y Categoría resueltas para presentación en la UI.
 */
data class TransactionWithDetails(
    val transaction: Transaction,
    val sourceAccount: Account? = null,
    val targetAccount: Account? = null,
    val category: Category? = null
)
