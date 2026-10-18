package com.finanzas.app.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzas.app.FinanzasApp
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.FinancialSummary
import com.finanzas.app.domain.model.Transaction
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.domain.model.TransactionWithDetails
import com.finanzas.app.domain.repository.AccountRepository
import com.finanzas.app.domain.repository.CurrencyRepository
import com.finanzas.app.domain.repository.TransactionRepository
import com.finanzas.app.domain.usecase.ExportDataUseCase
import com.finanzas.app.domain.usecase.GetFinancialSummaryUseCase
import com.finanzas.app.domain.usecase.SyncDataUseCase
import com.finanzas.app.service.FloatingMovementService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

import com.finanzas.app.domain.model.Category
import com.finanzas.app.domain.repository.CategoryRepository

data class MainDashboardUiState(
    val financialSummary: FinancialSummary = FinancialSummary(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactions: List<TransactionWithDetails> = emptyList(),
    val isFloatingServiceActive: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val selectedFilter: TransactionType? = null,
    val isSyncing: Boolean = false,
    val userFeedbackMessage: String? = null,
    val showExportDialog: Boolean = false,
    val showCreateAccountDialog: Boolean = false,
    val selectedAccountToEdit: Account? = null,
    /** Movimientos de la cuenta en edicion. Si es > 0 su moneda queda bloqueada. */
    val selectedAccountMovementCount: Int = 0,
    val selectedTransactionToEdit: TransactionWithDetails? = null,
    val showCreateCategoryDialog: Boolean = false,
    val categoryTypeForCreation: TransactionType = TransactionType.EXPENSE,
    /** TRM vigente (COP por 1 USD). Necesaria para normalizar los informes. */
    val usdToCopRate: Double = 4150.0
)

class MainViewModel(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository,
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase,
    private val exportDataUseCase: ExportDataUseCase,
    private val syncDataUseCase: SyncDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainDashboardUiState())
    val uiState: StateFlow<MainDashboardUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            getFinancialSummaryUseCase().collect { summary ->
                _uiState.update { it.copy(financialSummary = summary) }
            }
        }

        viewModelScope.launch {
            accountRepository.getAllAccountsFlow().collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }

        viewModelScope.launch {
            categoryRepository.getAllCategoriesFlow().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }

        viewModelScope.launch {
            transactionRepository.getAllTransactionsWithDetailsFlow().collect { list ->
                _uiState.update { it.copy(transactions = list) }
            }
        }

        viewModelScope.launch {
            currencyRepository.getExchangeRateFlow().collect { rate ->
                _uiState.update { it.copy(usdToCopRate = rate.officialTrm) }
            }
        }
    }

    fun updateServiceAndPermissionState(hasOverlayPermission: Boolean) {
        _uiState.update {
            it.copy(
                hasOverlayPermission = hasOverlayPermission,
                isFloatingServiceActive = FloatingMovementService.isRunning
            )
        }
    }

    fun toggleFloatingService(app: FinanzasApp, enable: Boolean) {
        if (enable) {
            if (app.hasOverlayPermission()) {
                app.startFloatingService()
                _uiState.update { it.copy(isFloatingServiceActive = true) }
            } else {
                _uiState.update {
                    it.copy(
                        userFeedbackMessage = "Se requiere el permiso de superposición para activar el botón"
                    )
                }
            }
        } else {
            app.stopFloatingService()
            _uiState.update { it.copy(isFloatingServiceActive = false) }
        }
    }

    fun setFilter(type: TransactionType?) {
        _uiState.update { it.copy(selectedFilter = type) }
    }

    fun setShowExportDialog(show: Boolean) {
        _uiState.update { it.copy(showExportDialog = show) }
    }

    fun setShowCreateAccountDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateAccountDialog = show) }
    }

    fun setShowCreateCategoryDialog(show: Boolean, type: TransactionType = TransactionType.EXPENSE) {
        _uiState.update { it.copy(showCreateCategoryDialog = show, categoryTypeForCreation = type) }
    }

    fun createCategory(
        name: String,
        iconName: String,
        type: TransactionType,
        colorHex: String
    ) {
        viewModelScope.launch {
            try {
                val category = Category(
                    id = "cat_custom_${System.currentTimeMillis()}",
                    name = name,
                    iconName = iconName,
                    type = type,
                    colorHex = colorHex
                )
                categoryRepository.insertCategory(category)
                _uiState.update {
                    it.copy(
                        showCreateCategoryDialog = false,
                        userFeedbackMessage = "Categoría '${category.name}' creada con éxito"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userFeedbackMessage = "Error al crear categoría: ${e.message}") }
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(categoryId)
                _uiState.update { it.copy(userFeedbackMessage = "Categoría eliminada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userFeedbackMessage = "Error al eliminar categoría: ${e.message}") }
            }
        }
    }

    fun createAccount(
        name: String,
        type: com.finanzas.app.domain.model.AccountType,
        currency: String = "USD",
        initialBalance: Double = 0.0,
        colorHex: String = "#3B82F6",
        isDefault: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val account = Account(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name.ifBlank { type.displayName },
                    type = type,
                    currency = currency,
                    currentBalance = initialBalance,
                    colorHex = colorHex,
                    isDefault = isDefault
                )
                accountRepository.insertAccount(account)
                _uiState.update {
                    it.copy(
                        showCreateAccountDialog = false,
                        userFeedbackMessage = "Cuenta '${account.name}' agregada con éxito"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userFeedbackMessage = "Error al crear cuenta: ${e.message}") }
            }
        }
    }

    fun selectAccountToEdit(account: Account?) {
        _uiState.update { it.copy(selectedAccountToEdit = account, selectedAccountMovementCount = 0) }
        if (account == null) return
        viewModelScope.launch {
            val movimientos = runCatching {
                transactionRepository.countMovementsForAccount(account.id)
            }.getOrDefault(0)
            _uiState.update { it.copy(selectedAccountMovementCount = movimientos) }
        }
    }

    /**
     * Edita SOLO los datos de presentacion de la cuenta: nombre, tipo, moneda,
     * color y si es la predeterminada.
     *
     * El saldo NO se edita desde aqui. Es una cantidad derivada:
     *     saldo = saldo_inicial + suma(movimientos)
     * Sobreescribirlo a mano rompia esa identidad de forma permanente e
     * irrastreable: los informes seguian sumando los movimientos viejos y el
     * numero del dashboard ya no los explicaba.
     *
     * Para corregir un saldo se registra un ingreso o un gasto, o se elimina el
     * movimiento que sobra: en ambos casos queda el rastro y las cuentas cuadran.
     *
     * El saldo previo se relee de la base y se impone aqui, no se confia en el
     * que traiga [account]: asi la garantia vale para cualquier llamador.
     */
    fun updateAccount(account: Account) {
        viewModelScope.launch {
            try {
                val previo = accountRepository.getAccountById(account.id)
                val saldoReal = previo?.currentBalance ?: account.currentBalance

                // La moneda tampoco se cambia si la cuenta ya tiene historial: la app
                // deduce la moneda de cada movimiento de su cuenta, asi que cambiarla
                // reinterpretaria de golpe todo el historico (400.000 COP pasarian a
                // leerse como 400.000 USD). Se valida aqui y no solo en la UI.
                val tieneHistorial = previo != null &&
                        (transactionRepository.countMovementsForAccount(account.id) > 0 ||
                                abs(saldoReal) > CurrencyFormatter.EPSILON)
                val monedaFinal = if (tieneHistorial) (previo?.currency ?: account.currency)
                                  else account.currency

                accountRepository.updateAccount(
                    account.copy(currentBalance = saldoReal, currency = monedaFinal)
                )

                _uiState.update {
                    it.copy(
                        selectedAccountToEdit = null,
                        userFeedbackMessage = "Cuenta '${account.name}' actualizada con éxito"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userFeedbackMessage = "Error al actualizar cuenta: ${e.message}") }
            }
        }
    }

    fun selectTransactionToEdit(item: TransactionWithDetails?) {
        _uiState.update { it.copy(selectedTransactionToEdit = item) }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val result = transactionRepository.updateMovement(transaction)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        selectedTransactionToEdit = null,
                        userFeedbackMessage = "Movimiento actualizado y saldos recalculados"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(userFeedbackMessage = "Error al actualizar movimiento: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            try {
                accountRepository.deleteAccount(accountId)
                _uiState.update { it.copy(userFeedbackMessage = "Cuenta eliminada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userFeedbackMessage = "Error al eliminar: ${e.message}") }
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            val result = transactionRepository.deleteMovement(transactionId)
            if (result.isSuccess) {
                _uiState.update { it.copy(userFeedbackMessage = "Movimiento eliminado y saldo restaurado") }
            } else {
                _uiState.update { it.copy(userFeedbackMessage = "Error al eliminar: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun syncCloudData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, userFeedbackMessage = null) }
            val result = syncDataUseCase()
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                val msg = if (count > 0) "Sincronizados $count movimientos con el servidor" else "Todos los datos están al día"
                _uiState.update { it.copy(isSyncing = false, userFeedbackMessage = msg) }
            } else {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        userFeedbackMessage = "Error de sincronización: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun exportJson(context: Context) {
        viewModelScope.launch {
            try {
                exportDataUseCase.exportToJson(context)
            } catch (e: Exception) {
                _uiState.update { it.copy(userFeedbackMessage = "Error exportando JSON: ${e.message}") }
            }
        }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            try {
                exportDataUseCase.exportToCsv(context)
            } catch (e: Exception) {
                _uiState.update { it.copy(userFeedbackMessage = "Error exportando CSV: ${e.message}") }
            }
        }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(userFeedbackMessage = null) }
    }
}
