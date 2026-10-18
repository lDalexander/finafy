package com.finanzas.app.domain.model

import java.util.UUID

/**
 * Representa una cuenta, bolsillo bancario o tarjeta de crédito.
 *
 * @param id Identificador único UUID
 * @param name Nombre descriptivo (ej. "Banco Pichincha", "Billetera Efectivo", "Visa Infinite")
 * @param type Tipo de cuenta según AccountType
 * @param currency Código de moneda ISO (default "USD")
 * @param currentBalance Saldo actual disponible. Para Tarjetas de Crédito representa el cupo utilizado (deuda).
 * @param colorHex Color identificador en formato hexadecimal (ej. "#10B981")
 * @param isDefault Indica si es la cuenta seleccionada por defecto para gastos rápidos
 */
data class Account(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AccountType,
    val currency: String = "USD",
    val currentBalance: Double = 0.0,
    val colorHex: String = "#10B981",
    val isDefault: Boolean = false
) {
    val isCreditCard: Boolean get() = type == AccountType.CREDIT_CARD
}
