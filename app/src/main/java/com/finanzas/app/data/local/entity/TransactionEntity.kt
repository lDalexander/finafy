package com.finanzas.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.finanzas.app.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [
        Index("timestamp"),
        Index("type"),
        Index("sourceAccountId"),
        Index("targetAccountId"),
        Index("categoryId"),
        Index("isSynced")
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
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
)
