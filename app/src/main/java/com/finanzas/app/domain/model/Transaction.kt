package com.finanzas.app.domain.model

import java.util.UUID

/**
 * Representa cualquier movimiento financiero registrado en la app.
 *
 * @param id UUID único del movimiento
 * @param type Tipo de movimiento (EXPENSE, CREDIT_PURCHASE, TRANSFER, REMITTANCE, INCOME)
 * @param sourceAccountId ID de la cuenta origen (de donde sale el dinero)
 * @param targetAccountId ID de la cuenta destino (donde entra el dinero en transferencias o ingresos)
 * @param amount Monto base de la transacción en la moneda principal
 * @param categoryId ID de la categoría asignada
 * @param feeAmount Comisión o tarifa bancaria/giro aplicada (default 0.0)
 * @param targetAmount Monto recibido en moneda destino (ej. en COP para giros/remesas)
 * @param targetCurrency Código de moneda destino (ej. "COP", "EUR")
 * @param exchangeRate Tasa de cambio aplicada (ej. 4100.50 COP por 1 USD)
 * @param installments Número de cuotas diferidas si fue con Tarjeta de Crédito (opcional)
 * @param timestamp Epoch millis de la fecha y hora del registro
 * @param note Nota o detalle opcional del movimiento
 * @param isSynced Bandera para sincronización offline-first con backend
 * @param syncTimestamp Timestamp de la última sincronización con la nube
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val sourceAccountId: String? = null,
    val targetAccountId: String? = null,
    val amount: Double,
    val categoryId: String? = null,
    val feeAmount: Double = 0.0,
    val targetAmount: Double? = null,
    val targetCurrency: String? = null,
    val exchangeRate: Double? = null,
    val installments: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null,
    val isSynced: Boolean = false,
    val syncTimestamp: Long? = null
) {
    /**
     * Calcula el monto neto total debitado de la cuenta origen (monto + comisiones).
     */
    val totalDebited: Double
        get() = amount + feeAmount
}
