package com.finanzas.app.ui.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.core.util.RemittanceMath
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.AccountType
import com.finanzas.app.domain.model.Category
import com.finanzas.app.domain.model.Transaction
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.domain.repository.AccountRepository
import com.finanzas.app.domain.repository.CategoryRepository
import com.finanzas.app.domain.repository.CurrencyRepository
import com.finanzas.app.domain.usecase.RegisterMovementUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class QuickMovementViewModel(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val currencyRepository: CurrencyRepository,
    private val registerMovementUseCase: RegisterMovementUseCase
) : ViewModel() {

    private companion object {
        /** Espera antes de cotizar mientras se teclea, para no pedir una cotizacion por digito. */
        const val QUOTE_DEBOUNCE_MS = 450L
    }

    private val _uiState = MutableStateFlow(QuickMovementUiState())
    val uiState: StateFlow<QuickMovementUiState> = _uiState.asStateFlow()

    private var quoteJob: Job? = null

    init {
        loadInitialData()
        observeExchangeRate()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            accountRepository.getAllAccountsFlow().collect { accounts ->
                _uiState.update { current ->
                    val defaultUsd = accounts.firstOrNull { it.isDefault && it.currency == "USD" }
                        ?: accounts.firstOrNull { it.currency == "USD" }
                    val creditCard = accounts.firstOrNull { it.type == AccountType.CREDIT_CARD }
                    val defaultCop = accounts.firstOrNull { it.currency == "COP" }

                    val initialSource = when (current.selectedType) {
                        TransactionType.CREDIT_PURCHASE -> creditCard
                        TransactionType.REMITTANCE -> defaultUsd
                        else -> defaultUsd ?: accounts.firstOrNull()
                    }

                    val initialTarget = when (current.selectedType) {
                        TransactionType.REMITTANCE -> defaultCop
                        else -> accounts.firstOrNull { it.id != initialSource?.id }
                    }

                    current.copy(
                        accounts = accounts,
                        selectedSourceAccount = current.selectedSourceAccount ?: initialSource,
                        selectedTargetAccount = current.selectedTargetAccount ?: initialTarget
                    )
                }
            }
        }

        viewModelScope.launch {
            categoryRepository.getAllCategoriesFlow().collect { allCategories ->
                _uiState.update { current ->
                    val filtered = allCategories.filter { it.type == current.selectedType }
                    current.copy(
                        categories = filtered,
                        selectedCategory = current.selectedCategory ?: filtered.firstOrNull()
                    )
                }
            }
        }
    }

    private fun observeExchangeRate() {
        viewModelScope.launch {
            currencyRepository.getExchangeRateFlow().collect { rateInfo ->
                _uiState.update { it.copy(exchangeRateInfo = rateInfo) }
            }
        }
        viewModelScope.launch {
            currencyRepository.getRemittanceQuoteFlow().collect { quote ->
                _uiState.update { current ->
                    val actualizado = current.copy(
                        remittanceQuote = quote,
                        spreadPercent = quote?.spreadPercent ?: current.spreadPercent
                    )
                    syncCopField(actualizado)
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Cotizacion
    // ------------------------------------------------------------------

    /**
     * Pide cotizacion para el monto actual. La tasa de Western Union depende del
     * monto, asi que hay que recotizar cuando el monto cambia; el debounce evita
     * una llamada por tecla.
     */
    private fun scheduleQuote(force: Boolean = false) {
        if (!_uiState.value.isRemittance) return
        quoteJob?.cancel()
        quoteJob = viewModelScope.launch {
            if (!force) delay(QUOTE_DEBOUNCE_MS)
            val monto = _uiState.value.numericAmount
            if (monto <= 0.0) return@launch

            _uiState.update { it.copy(isFetchingRate = true) }
            runCatching { currencyRepository.refreshRemittanceQuote(monto, force) }
            _uiState.update { it.copy(isFetchingRate = false) }
        }
    }

    fun fetchExchangeRate(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingRate = true) }
            runCatching { currencyRepository.getLatestRate(_uiState.value.spreadPercent, forceRefresh) }
            _uiState.update { it.copy(isFetchingRate = false) }
        }
        scheduleQuote(force = forceRefresh)
    }

    /** Refresca el campo de COP mientras el usuario no lo haya tocado. */
    private fun syncCopField(state: QuickMovementUiState): QuickMovementUiState {
        if (!state.isRemittance || state.isCopEditedByUser) return state
        return state.copy(targetCopInput = String.format(Locale.US, "%.0f", state.estimatedCop))
    }

    // ------------------------------------------------------------------
    // Teclado numerico (USD)
    // ------------------------------------------------------------------

    fun onDigitClick(digit: String) {
        _uiState.update { current ->
            val currentText = current.amountInput
            val newText = when {
                currentText == "0" && digit != "." -> digit
                digit == "." && currentText.contains(".") -> currentText
                currentText.contains(".") && currentText.substringAfter(".").length >= 2 -> currentText
                currentText.length >= 8 -> currentText
                else -> currentText + digit
            }
            // Cambiar el monto invalida lo que el usuario hubiera escrito en COP.
            syncCopField(
                current.copy(amountInput = newText, errorMessage = null, isCopEditedByUser = false)
            )
        }
        scheduleQuote()
    }

    fun onBackspace() {
        _uiState.update { current ->
            val newText = if (current.amountInput.length <= 1) "0" else current.amountInput.dropLast(1)
            syncCopField(current.copy(amountInput = newText, isCopEditedByUser = false))
        }
        scheduleQuote()
    }

    fun onQuickAdd(amountToAdd: Double) {
        _uiState.update { current ->
            val nuevo = (current.amountInput.toDoubleOrNull() ?: 0.0) + amountToAdd
            val texto = String.format(Locale.US, "%.2f", nuevo).removeSuffix(".00")
            syncCopField(current.copy(amountInput = texto, isCopEditedByUser = false))
        }
        scheduleQuote()
    }

    // ------------------------------------------------------------------
    // Giros
    // ------------------------------------------------------------------

    /**
     * El usuario escribe los COP acordados en ventanilla. Se recalcula cuanto hay
     * que entregar en total para que lleguen esos pesos:
     *
     *     total = COP / tasa + comision
     */
    fun onCopInputChanged(copText: String) {
        _uiState.update { current ->
            val cop = copText.toDoubleOrNull() ?: 0.0
            if (cop <= 0.0) {
                return@update current.copy(targetCopInput = copText, isCopEditedByUser = true)
            }
            val totalNecesario = RemittanceMath.totalUsdForTargetCop(
                targetCop = cop,
                feeUsd = current.remittanceFee,
                rate = current.remittanceRate
            )
            current.copy(
                targetCopInput = copText,
                amountInput = String.format(Locale.US, "%.2f", totalNecesario),
                isCopEditedByUser = true,
                errorMessage = null
            )
        }
    }

    fun onFeeChanged(fee: String) {
        _uiState.update { syncCopField(it.copy(feeInput = fee)) }
    }

    /** Margen manual, solo cuando no hay cotizacion en vivo. */
    fun onSpreadChanged(spread: Double) {
        viewModelScope.launch { currencyRepository.updateSpread(spread) }
        _uiState.update { current ->
            syncCopField(
                current.copy(
                    spreadPercent = spread,
                    exchangeRateInfo = current.exchangeRateInfo.copy(
                        spreadPercent = spread,
                        westernUnionRate = current.midMarketRate * (1.0 - spread / 100.0),
                        isRealWesternUnionRate = false
                    )
                )
            )
        }
    }

    fun resetCalibration() {
        viewModelScope.launch {
            currencyRepository.resetCalibration()
            scheduleQuote(force = true)
        }
    }

    // ------------------------------------------------------------------
    // Seleccion
    // ------------------------------------------------------------------

    fun onTypeSelected(type: TransactionType) {
        _uiState.update { current ->
            val newSource = when (type) {
                TransactionType.CREDIT_PURCHASE -> current.accounts.firstOrNull { it.type == AccountType.CREDIT_CARD }
                TransactionType.REMITTANCE -> current.accounts.firstOrNull { it.currency == "USD" && it.isDefault }
                    ?: current.accounts.firstOrNull { it.currency == "USD" }
                TransactionType.TRANSFER -> current.accounts.firstOrNull { it.type != AccountType.CREDIT_CARD && it.isDefault }
                    ?: current.accounts.firstOrNull { it.type != AccountType.CREDIT_CARD }
                else -> current.accounts.firstOrNull { it.isDefault } ?: current.accounts.firstOrNull()
            }

            val newTarget = when (type) {
                TransactionType.REMITTANCE -> current.accounts.firstOrNull { it.currency == "COP" }
                TransactionType.TRANSFER -> current.accounts.firstOrNull { it.type == AccountType.CREDIT_CARD }
                    ?: current.accounts.firstOrNull { it.id != newSource?.id }
                else -> current.accounts.firstOrNull { it.id != newSource?.id }
            }

            syncCopField(
                current.copy(
                    selectedType = type,
                    selectedSourceAccount = newSource,
                    selectedTargetAccount = newTarget,
                    selectedCategory = null,
                    // Vacio: la comision la pone la cotizacion del operador.
                    feeInput = "",
                    isCopEditedByUser = false
                )
            )
        }

        viewModelScope.launch {
            val cats = categoryRepository.getCategoriesByType(type)
            _uiState.update { it.copy(categories = cats, selectedCategory = cats.firstOrNull()) }
        }

        if (type == TransactionType.REMITTANCE) scheduleQuote(force = true)
    }

    fun onSourceAccountSelected(account: Account) {
        _uiState.update { it.copy(selectedSourceAccount = account) }
    }

    fun onTargetAccountSelected(account: Account) {
        _uiState.update { it.copy(selectedTargetAccount = account) }
    }

    fun onCategorySelected(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onInstallmentsChanged(installments: Int) {
        _uiState.update { it.copy(installments = installments) }
    }

    fun onNoteChanged(note: String) {
        _uiState.update { it.copy(noteInput = note) }
    }

    // ------------------------------------------------------------------
    // Guardado
    // ------------------------------------------------------------------

    fun submitMovement(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.numericAmount <= 0.0) {
            _uiState.update { it.copy(errorMessage = "El monto debe ser mayor a 0.00") }
            return
        }

        if (state.isRemittance) {
            if (state.selectedSourceAccount == null) {
                _uiState.update { it.copy(errorMessage = "Selecciona una cuenta de origen en USD") }
                return
            }
            if (state.convertedUsd <= CurrencyFormatter.EPSILON) {
                _uiState.update {
                    it.copy(errorMessage = "El total debe superar la comisión de ${CurrencyFormatter.format(state.remittanceFee)}")
                }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val isCardPayment = state.selectedType == TransactionType.TRANSFER &&
                    state.selectedTargetAccount?.type == AccountType.CREDIT_CARD

            val defaultNote = when {
                isCardPayment -> "Pago a tarjeta ${state.selectedTargetAccount?.name ?: "Crédito"}"
                state.isRemittance -> "Giro a Colombia · tasa " +
                        CurrencyFormatter.formatRate(state.appliedRate) +
                        " (mercado " + CurrencyFormatter.formatRate(state.midMarketRate) + ")"
                else -> null
            }

            val transaction = if (state.isRemittance) {
                // El monto convertido y la comision se guardan por separado, de modo
                // que amount + feeAmount == lo que realmente salio de la cuenta.
                Transaction(
                    type = TransactionType.REMITTANCE,
                    sourceAccountId = state.selectedSourceAccount?.id,
                    targetAccountId = state.selectedTargetAccount?.id,
                    amount = state.convertedUsd,
                    categoryId = state.selectedCategory?.id,
                    feeAmount = state.remittanceFee,
                    targetAmount = state.numericTargetCop,
                    targetCurrency = "COP",
                    exchangeRate = state.appliedRate,
                    note = state.noteInput.ifBlank { defaultNote }
                )
            } else {
                Transaction(
                    type = state.selectedType,
                    sourceAccountId = state.selectedSourceAccount?.id,
                    targetAccountId = state.selectedTargetAccount?.id,
                    amount = state.numericAmount,
                    categoryId = state.selectedCategory?.id,
                    feeAmount = 0.0,
                    installments = if (state.selectedType == TransactionType.CREDIT_PURCHASE) state.installments else null,
                    note = state.noteInput.ifBlank { defaultNote }
                )
            }

            val result = registerMovementUseCase(transaction)

            if (result.isSuccess) {
                // Solo se calibra con COP escritos a mano: si se guardara la propia
                // estimacion, el factor se realimentaria hacia 1.0 y no aprenderia nada.
                if (state.isRemittance && state.isCopEditedByUser) {
                    runCatching {
                        currencyRepository.calibrateFromCounter(
                            totalUsd = state.numericAmount,
                            feeUsd = state.remittanceFee,
                            actualCop = state.numericTargetCop
                        )
                    }
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar movimiento"
                    )
                }
            }
        }
    }
}
