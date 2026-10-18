package com.finanzas.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finanzas.app.domain.model.DebtStatus

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey val id: String,
    val title: String,
    val entityName: String,
    val originalPrincipal: Double,
    val interestRatePercentage: Double,
    val totalInstallments: Int,
    val installmentAmount: Double,
    val totalWithInterest: Double,
    val startDate: Long,
    val dueDayOfMonth: Int,
    val currency: String,
    val accountId: String?,
    val notes: String?,
    val status: DebtStatus
)
