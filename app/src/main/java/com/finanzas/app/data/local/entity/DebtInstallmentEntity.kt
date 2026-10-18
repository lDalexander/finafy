package com.finanzas.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debt_installments",
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["debtId"])]
)
data class DebtInstallmentEntity(
    @PrimaryKey val id: String,
    val debtId: String,
    val installmentNumber: Int,
    val dueDate: Long,
    val principalAmount: Double,
    val interestAmount: Double,
    val lateFeeAmount: Double,
    val isPaid: Boolean,
    val paidDate: Long?,
    val actualPaidAmount: Double?,
    val paymentAccountId: String?,
    val notes: String?
)
