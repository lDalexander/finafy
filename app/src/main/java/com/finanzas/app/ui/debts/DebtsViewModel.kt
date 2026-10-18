package com.finanzas.app.ui.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzas.app.domain.model.Debt
import com.finanzas.app.domain.model.DebtInstallment
import com.finanzas.app.domain.model.DebtWithInstallments
import com.finanzas.app.domain.repository.AccountRepository
import com.finanzas.app.domain.repository.DebtRepository
import com.finanzas.app.domain.usecase.CreateDebtUseCase
import com.finanzas.app.domain.usecase.DeleteDebtUseCase
import com.finanzas.app.domain.usecase.GetDebtsSummaryUseCase
import com.finanzas.app.domain.usecase.PayDebtInstallmentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DebtsViewModel(
    private val debtRepository: DebtRepository,
    private val accountRepository: AccountRepository,
    private val createDebtUseCase: CreateDebtUseCase,
    private val payDebtInstallmentUseCase: PayDebtInstallmentUseCase,
    private val getDebtsSummaryUseCase: GetDebtsSummaryUseCase,
    private val deleteDebtUseCase: DeleteDebtUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebtsUiState())
    val uiState: StateFlow<DebtsUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            debtRepository.getAllDebtsWithInstallmentsFlow().collect { debts ->
                _uiState.update { state ->
                    val updatedDetail = state.selectedDebtForDetail?.let { currentDetail ->
                        debts.find { it.debt.id == currentDetail.debt.id }
                    }
                    state.copy(
                        debtsWithInstallments = debts,
                        selectedDebtForDetail = updatedDetail
                    )
                }
            }
        }

        viewModelScope.launch {
            getDebtsSummaryUseCase().collect { summary ->
                _uiState.update { it.copy(analyticsSummary = summary) }
            }
        }

        viewModelScope.launch {
            accountRepository.getAllAccountsFlow().collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }
    }

    fun showCreateDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateDebtDialog = show, errorMessage = null) }
    }

    fun selectDebtForDetail(debtWithInstallments: DebtWithInstallments?) {
        _uiState.update { it.copy(selectedDebtForDetail = debtWithInstallments) }
    }

    fun selectDebtForAbono(debtWithInstallments: DebtWithInstallments?) {
        _uiState.update { it.copy(selectedDebtForAbono = debtWithInstallments) }
    }

    fun selectInstallmentForPayment(debt: Debt?, installment: DebtInstallment?) {
        val pair = if (debt != null && installment != null) Pair(debt, installment) else null
        _uiState.update { it.copy(selectedInstallmentForPayment = pair) }
    }

    fun addAbono(
        debtId: String,
        amount: Double,
        sourceAccountId: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = debtRepository.addDebtAbono(
                debtId = debtId,
                abonoAmount = amount,
                paidDate = System.currentTimeMillis(),
                sourceAccountId = sourceAccountId,
                notes = notes
            )
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedDebtForAbono = null,
                        feedbackMessage = "Abono a la deuda registrado con éxito"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar el abono"
                    )
                }
            }
        }
    }

    fun createDebt(
        title: String,
        entityName: String,
        originalPrincipal: Double,
        interestRatePercentage: Double,
        totalInstallments: Int,
        customInstallmentAmount: Double? = null,
        dueDayOfMonth: Int = 15,
        currency: String = "USD",
        accountId: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = createDebtUseCase(
                title = title,
                entityName = entityName,
                originalPrincipal = originalPrincipal,
                interestRatePercentage = interestRatePercentage,
                totalInstallments = totalInstallments,
                customInstallmentAmount = customInstallmentAmount,
                dueDayOfMonth = dueDayOfMonth,
                currency = currency,
                accountId = accountId,
                notes = notes
            )
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showCreateDebtDialog = false,
                        feedbackMessage = "Deuda/Crédito registrado con éxito"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar la deuda"
                    )
                }
            }
        }
    }

    fun payInstallment(
        installmentId: String,
        actualAmountPaid: Double,
        lateFee: Double,
        sourceAccountId: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = payDebtInstallmentUseCase(
                installmentId = installmentId,
                actualAmountPaid = actualAmountPaid,
                lateFee = lateFee,
                sourceAccountId = sourceAccountId,
                notes = notes
            )
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedInstallmentForPayment = null,
                        feedbackMessage = "Cuota pagada correctamente"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar el pago"
                    )
                }
            }
        }
    }

    fun deleteDebt(debtId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = deleteDebtUseCase(debtId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedDebtForDetail = null,
                        feedbackMessage = "Deuda eliminada"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al eliminar"
                    )
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null, errorMessage = null) }
    }
}
