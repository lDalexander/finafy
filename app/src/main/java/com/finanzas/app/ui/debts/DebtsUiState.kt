package com.finanzas.app.ui.debts

import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.Debt
import com.finanzas.app.domain.model.DebtAnalyticsSummary
import com.finanzas.app.domain.model.DebtInstallment
import com.finanzas.app.domain.model.DebtWithInstallments

data class DebtsUiState(
    val debtsWithInstallments: List<DebtWithInstallments> = emptyList(),
    val analyticsSummary: DebtAnalyticsSummary = DebtAnalyticsSummary(),
    val accounts: List<Account> = emptyList(),
    val selectedDebtForDetail: DebtWithInstallments? = null,
    val selectedDebtForAbono: DebtWithInstallments? = null,
    val selectedInstallmentForPayment: Pair<Debt, DebtInstallment>? = null,
    val showCreateDebtDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val feedbackMessage: String? = null
)
