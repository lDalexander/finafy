package com.finanzas.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finanzas.app.domain.model.TransactionType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val iconName: String,
    val type: TransactionType,
    val colorHex: String = "#3B82F6"
)
