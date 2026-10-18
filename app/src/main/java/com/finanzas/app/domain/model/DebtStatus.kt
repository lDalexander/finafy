package com.finanzas.app.domain.model

enum class DebtStatus(val displayName: String) {
    ACTIVE("Activa"),
    PAID("Pagada"),
    OVERDUE("En Mora")
}
