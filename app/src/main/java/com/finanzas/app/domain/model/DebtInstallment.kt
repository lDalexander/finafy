package com.finanzas.app.domain.model

import java.util.UUID

data class DebtInstallment(
    val id: String = UUID.randomUUID().toString(),
    val debtId: String,
    val installmentNumber: Int,
    val dueDate: Long,
    val principalAmount: Double,
    val interestAmount: Double = 0.0,
    val lateFeeAmount: Double = 0.0,
    val isPaid: Boolean = false,
    val paidDate: Long? = null,
    val actualPaidAmount: Double? = null,
    val paymentAccountId: String? = null,
    val notes: String? = null
) {
    val totalDue: Double
        get() = principalAmount + interestAmount + lateFeeAmount

    val isOverdue: Boolean
        get() = !isPaid && dueDate < System.currentTimeMillis()
}
