package com.finanzas.app.domain.model

import java.util.UUID

/**
 * Entidad de categoría para clasificar los movimientos financieros.
 */
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val iconName: String,
    val type: TransactionType,
    val colorHex: String = "#3B82F6"
)
