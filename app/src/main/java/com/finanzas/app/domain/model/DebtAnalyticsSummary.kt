package com.finanzas.app.domain.model

data class DebtAnalyticsSummary(
    val totalActiveDebtsCount: Int = 0,
    val totalRemainingDebt: Double = 0.0,
    val totalOriginalPrincipal: Double = 0.0,
    val totalPaidPrincipal: Double = 0.0,
    val totalPaidInterest: Double = 0.0,
    val totalPaidLateFees: Double = 0.0,
    val totalPaidOverall: Double = 0.0,
    val pendingInstallmentsCount: Int = 0
)
