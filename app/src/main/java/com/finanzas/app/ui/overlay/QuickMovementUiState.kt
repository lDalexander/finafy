package com.finanzas.app.ui.overlay

import com.finanzas.app.core.util.RemittanceMath
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.AccountType
import com.finanzas.app.domain.model.Category
import com.finanzas.app.domain.model.ExchangeRateInfo
import com.finanzas.app.domain.model.QuoteSource
import com.finanzas.app.domain.model.RemittanceQuote
import com.finanzas.app.domain.model.TransactionType

/**
 * Estado inmutable de la captura rapida y de giros.
 *
 * SEMANTICA DEL MONTO EN GIROS: [amountInput] es el TOTAL que sale de la cuenta,
 * con la comision adentro. Es el mismo modelo que usa Western Union:
 *
 *     recibido_COP = (total - comision) * tasa
 *
 * Para los demas tipos de movimiento el monto es el monto, sin mas.
 */
data class QuickMovementUiState(
    val amountInput: String = "0",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedSourceAccount: Account? = null,
    val selectedTargetAccount: Account? = null,
    val selectedCategory: Category? = null,

    // Giros (USD -> COP)
    val exchangeRateInfo: ExchangeRateInfo = ExchangeRateInfo(),
    val remittanceQuote: RemittanceQuote? = null,
    val feeInput: String = "",
    val targetCopInput: String = "0",
    val spreadPercent: Double = 2.0,
    /** true en cuanto el usuario edita los COP a mano: habilita la calibracion. */
    val isCopEditedByUser: Boolean = false,
    val isFetchingRate: Boolean = false,

    // Tarjeta de Credito
    val installments: Int = 1,

    // Metadatos
    val noteInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
) {
    val numericAmount: Double
        get() = amountInput.toDoubleOrNull() ?: 0.0

    val isRemittance: Boolean
        get() = selectedType == TransactionType.REMITTANCE

    /**
     * Moneda del movimiento. No es una preferencia: es la de la cuenta que lo
     * origina (o lo recibe, en ingresos). Una cuenta = una moneda.
     * Los giros siempre salen en USD desde Ecuador; lo que llega en COP se
     * maneja aparte con targetAmount.
     */
    val activeCurrency: String
        get() = when (selectedType) {
            TransactionType.REMITTANCE -> "USD"
            TransactionType.INCOME ->
                selectedTargetAccount?.currency ?: selectedSourceAccount?.currency ?: "USD"
            else -> selectedSourceAccount?.currency ?: "USD"
        }

    /** En COP los saltos de +1 / +5 / +10 no sirven de nada: se escalan a miles. */
    val quickAddScale: Double
        get() = if (activeCurrency == "COP") 1000.0 else 1.0

    // ---- Giros ----

    /** Comision: la que cotiza el operador, salvo que se sobreescriba a mano. */
    val remittanceFee: Double
        get() = feeInput.toDoubleOrNull() ?: remittanceQuote?.effectiveFee ?: 0.0

    /** Tasa aplicada, ya corregida por el factor Ecuador. */
    val remittanceRate: Double
        get() = remittanceQuote?.effectiveRate?.takeIf { it > 0.0 }
            ?: exchangeRateInfo.effectiveRate

    /** Tasa de mercado real, para medir cuanto cuesta el giro. */
    val midMarketRate: Double
        get() = remittanceQuote?.midMarketRate?.takeIf { it > 0.0 }
            ?: exchangeRateInfo.officialTrm

    /** USD que quedan tras la comision y que efectivamente se convierten. */
    val convertedUsd: Double
        get() = RemittanceMath.convertedUsd(numericAmount, remittanceFee)

    /** COP estimados segun la cotizacion vigente. */
    val estimatedCop: Double
        get() = RemittanceMath.receivedCop(numericAmount, remittanceFee, remittanceRate)

    /** COP que se registraran: lo tecleado si el usuario lo edito, si no la estimacion. */
    val numericTargetCop: Double
        get() = if (isCopEditedByUser) (targetCopInput.toDoubleOrNull() ?: estimatedCop)
        else estimatedCop

    /** Tasa realmente implicita en la operacion que se va a guardar. */
    val appliedRate: Double
        get() = RemittanceMath.impliedRate(numericAmount, remittanceFee, numericTargetCop)
            .takeIf { it > 0.0 } ?: remittanceRate

    /** Costo total del giro (comision + margen) frente a la tasa de mercado. */
    val totalCostPercent: Double
        get() = RemittanceMath.totalCostPercent(numericAmount, remittanceFee, appliedRate, midMarketRate)

    val totalCostUsd: Double
        get() = RemittanceMath.totalCostUsd(numericAmount, remittanceFee, appliedRate, midMarketRate)

    /** Total que sale de la cuenta. En giros la comision ya esta adentro. */
    val totalDebitedUsd: Double
        get() = if (isRemittance) numericAmount else numericAmount + remittanceFee

    val quoteIsLive: Boolean
        get() = remittanceQuote?.source == QuoteSource.LIVE

    val quoteIsStale: Boolean
        get() = remittanceQuote == null || remittanceQuote.source == QuoteSource.FALLBACK

    /** Western Union no cotiza este monto (mide >7.000 USD en la API). */
    val quoteUnavailable: Boolean
        get() = remittanceQuote != null && remittanceQuote.westernUnion == null

    // Alias que siguen usando otras pantallas
    val trmOficial: Double get() = midMarketRate
    val tasaEfectivaWU: Double get() = remittanceRate
    val numericFee: Double get() = remittanceFee

    /** Cuentas origen validas segun el tipo de movimiento. */
    val availableSourceAccounts: List<Account>
        get() = when (selectedType) {
            TransactionType.CREDIT_PURCHASE -> accounts.filter { it.type == AccountType.CREDIT_CARD }
            TransactionType.REMITTANCE -> accounts.filter { it.currency == "USD" && it.type != AccountType.CREDIT_CARD }
            else -> accounts.filter { it.type != AccountType.CREDIT_CARD }
        }

    /**
     * Cuentas destino validas.
     *
     * En TRANSFER solo aparecen cuentas de la MISMA moneda que la origen: mover
     * dinero entre monedas distintas no es una transferencia, es un giro (que si
     * lleva tasa de cambio). Sin este filtro se podian pasar 100 USD a una cuenta
     * en COP y quedaban registrados como 100 pesos.
     */
    val availableTargetAccounts: List<Account>
        get() = when (selectedType) {
            TransactionType.TRANSFER -> {
                val moneda = selectedSourceAccount?.currency
                accounts.filter { it.id != selectedSourceAccount?.id && (moneda == null || it.currency == moneda) }
            }
            TransactionType.REMITTANCE -> accounts.filter { it.currency == "COP" }
            else -> accounts
        }
}
