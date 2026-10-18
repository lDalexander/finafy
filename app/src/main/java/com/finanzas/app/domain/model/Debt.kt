package com.finanzas.app.domain.model

import java.util.UUID

data class Debt(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val entityName: String,
    val originalPrincipal: Double,
    val interestRatePercentage: Double = 0.0,
    val totalInstallments: Int = 1,
    val installmentAmount: Double = 0.0,
    val totalWithInterest: Double = originalPrincipal,
    val startDate: Long = System.currentTimeMillis(),
    val dueDayOfMonth: Int = 15,
    val currency: String = "USD",
    val accountId: String? = null,
    val notes: String? = null,
    val status: DebtStatus = DebtStatus.ACTIVE
)
