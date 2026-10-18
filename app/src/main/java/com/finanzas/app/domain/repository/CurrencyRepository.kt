package com.finanzas.app.domain.repository

import com.finanzas.app.domain.model.ExchangeRateInfo
import com.finanzas.app.domain.model.RemittanceCalibration
import com.finanzas.app.domain.model.RemittanceQuote
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    /** Tasa de mercado (mid-market). La usan los agregados para normalizar COP -> USD. */
    fun getExchangeRateFlow(): Flow<ExchangeRateInfo>

    suspend fun getLatestRate(spreadPercent: Double = 2.0, forceRefresh: Boolean = false): ExchangeRateInfo

    /** Margen manual, para cuando se prefiere estimar a mano en vez de usar la cotizacion. */
    suspend fun updateSpread(spreadPercent: Double)

    // ---- Giros ----

    /** Ultima cotizacion de giro calculada. Null mientras no se haya pedido ninguna. */
    fun getRemittanceQuoteFlow(): Flow<RemittanceQuote?>

    /**
     * Cotiza un giro por el TOTAL que sale de la cuenta (comision incluida).
     * La tasa de Western Union depende del monto, asi que hay que pedirla con
     * el monto real, no con uno fijo.
     */
    suspend fun refreshRemittanceQuote(sendAmountUsd: Double, forceRefresh: Boolean = false): RemittanceQuote

    /**
     * Ajusta el factor Ecuador con lo que realmente entregaron en ventanilla.
     *
     * @param totalUsd   total entregado al operador, comision incluida
     * @param feeUsd     comision cobrada
     * @param actualCop  pesos que efectivamente recibio el destinatario
     */
    suspend fun calibrateFromCounter(totalUsd: Double, feeUsd: Double, actualCop: Double)

    fun getCalibration(): RemittanceCalibration

    /** Borra lo aprendido y vuelve a confiar solo en la API. */
    suspend fun resetCalibration()
}
