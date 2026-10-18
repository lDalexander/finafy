package com.finanzas.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finanzas.app.domain.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: AccountType,
    val currency: String = "USD",
    val currentBalance: Double = 0.0,
    val colorHex: String = "#10B981",
    val isDefault: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
