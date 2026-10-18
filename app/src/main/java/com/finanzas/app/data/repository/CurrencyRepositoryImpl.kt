package com.finanzas.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.finanzas.app.core.util.RemittanceMath
import com.finanzas.app.data.remote.api.ExchangeRateApi
import com.finanzas.app.data.remote.api.WiseComparisonApi
import com.finanzas.app.data.remote.dto.WiseProviderDto
import com.finanzas.app.domain.model.ExchangeRateInfo
import com.finanzas.app.domain.model.ProviderQuote
import com.finanzas.app.domain.model.QuoteSource
import com.finanzas.app.domain.model.RemittanceCalibration
import com.finanzas.app.domain.model.RemittanceQuote
import com.finanzas.app.domain.repository.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong

class CurrencyRepositoryImpl(
    private val context: Context,
    private val exchangeRateApi: ExchangeRateApi,
    private val wiseComparisonApi: WiseComparisonApi = WiseComparisonApi.create()
) : CurrencyRepository {

    companion object {
        private const val TAG = "CurrencyRepository"
        private const val PREFS_NAME = "finanzas_currency_prefs"

        // Claves v2: el formato paso de Float a bits de Double.
        private const val KEY_TRM = "cached_official_trm_v2"
        private const val KEY_WU_RATE = "cached_wu_rate_v2"
        private const val KEY_SPREAD = "cached_spread_percent_v2"
        private const val KEY_UPDATED = "cached_last_updated"
        private const val KEY_IS_REAL_WU = "cached_is_real_wu"

        private const val KEY_CAL_FACTOR = "wu_calibration_factor"
        private const val KEY_CAL_SAMPLES = "wu_calibration_samples"
        private const val KEY_CAL_UPDATED = "wu_calibration_updated"

        private const val DEFAULT_TRM = 4150.0
        private const val DEFAULT_WU_RATE = 4067.0
        private const val DEFAULT_SPREAD = 2.0

        private const val WU_ALIAS = "western-union"
        private const val CACHE_TTL_MS = 1000L * 60 * 15

        /** Por encima de esto Western Union deja de cotizar en la API. */
        private const val WU_MAX_QUOTE_USD = 7_000.0
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _rateStateFlow = MutableStateFlow(loadFromCache())
    private val _quoteFlow = MutableStateFlow<RemittanceQuote?>(null)

    /** Cotizaciones ya pedidas, por tramo de monto. La tasa de WU es escalonada. */
    private val quoteCache = mutableMapOf<Long, RemittanceQuote>()

    override fun getExchangeRateFlow(): Flow<ExchangeRateInfo> = _rateStateFlow.asStateFlow()

    override fun getRemittanceQuoteFlow(): Flow<RemittanceQuote?> = _quoteFlow.asStateFlow()

    // ------------------------------------------------------------------
    // Cache local
    // ------------------------------------------------------------------

    private fun loadFromCache(): ExchangeRateInfo {
        val trm = Double.fromBits(prefs.getLong(KEY_TRM, DEFAULT_TRM.toRawBits()))
        val wuRate = Double.fromBits(prefs.getLong(KEY_WU_RATE, DEFAULT_WU_RATE.toRawBits()))
        val spread = Double.fromBits(prefs.getLong(KEY_SPREAD, DEFAULT_SPREAD.toRawBits()))
        val updated = prefs.getLong(KEY_UPDATED, 0L)
        val isRealWu = prefs.getBoolean(KEY_IS_REAL_WU, false)

        return ExchangeRateInfo(
            baseCurrency = "USD",
            targetCurrency = "COP",
            officialTrm = trm,
            westernUnionRate = wuRate,
            spreadPercent = spread,
            lastUpdated = updated,
            isOfflineFallback = true,
            isRealWesternUnionRate = isRealWu
        )
    }

    private fun saveToCache(info: ExchangeRateInfo) {
        prefs.edit()
            .putLong(KEY_TRM, info.officialTrm.toRawBits())
            .putLong(KEY_WU_RATE, info.westernUnionRate.toRawBits())
            .putLong(KEY_SPREAD, info.spreadPercent.toRawBits())
            .putLong(KEY_UPDATED, info.lastUpdated)
            .putBoolean(KEY_IS_REAL_WU, info.isRealWesternUnionRate)
            .apply()
    }

    override fun getCalibration(): RemittanceCalibration = RemittanceCalibration(
        factor = Double.fromBits(prefs.getLong(KEY_CAL_FACTOR, 1.0.toRawBits())),
        samples = prefs.getInt(KEY_CAL_SAMPLES, 0),
        lastUpdated = prefs.getLong(KEY_CAL_UPDATED, 0L)
    )

    private fun saveCalibration(cal: RemittanceCalibration) {
        prefs.edit()
            .putLong(KEY_CAL_FACTOR, cal.factor.toRawBits())
            .putInt(KEY_CAL_SAMPLES, cal.samples)
            .putLong(KEY_CAL_UPDATED, cal.lastUpdated)
            .apply()
    }

    override suspend fun resetCalibration() {
        saveCalibration(RemittanceCalibration())
        quoteCache.clear()
    }

    // ------------------------------------------------------------------
    // Tasa de mercado
    // ------------------------------------------------------------------

    override suspend fun getLatestRate(spreadPercent: Double, forceRefresh: Boolean): ExchangeRateInfo {
        return withContext(Dispatchers.IO) {
            val cached = _rateStateFlow.value
            val cacheAge = System.currentTimeMillis() - cached.lastUpdated
            val isCacheValid = cacheAge < CACHE_TTL_MS

            if (!forceRefresh && isCacheValid && !cached.isOfflineFallback) {
                return@withContext cached
            }

            // 1. Cotizaciones reales: da la tasa de mercado y la de WU en una sola llamada.
            runCatching { fetchQuote(100.0) }.getOrNull()?.let { quote ->
                val info = quote.toExchangeRateInfo()
                saveToCache(info)
                _rateStateFlow.value = info
                Log.d(TAG, "Cotizaciones OK: mid=${info.officialTrm} WU=${info.westernUnionRate}")
                return@withContext info
            }

            // 2. Respaldo: solo la tasa de mercado, con el margen manual configurado.
            runCatching {
                val response = exchangeRateApi.getLatestRates("USD")
                val copRate = response.body()?.rates?.get("COP")
                if (response.isSuccessful && copRate != null && copRate > 0.0) {
                    val info = ExchangeRateInfo(
                        officialTrm = copRate,
                        westernUnionRate = copRate * (1.0 - spreadPercent / 100.0),
                        spreadPercent = spreadPercent,
                        lastUpdated = System.currentTimeMillis(),
                        isOfflineFallback = false,
                        isRealWesternUnionRate = false
                    )
                    saveToCache(info)
                    _rateStateFlow.value = info
                    Log.d(TAG, "TRM de respaldo: $copRate")
                    return@withContext info
                }
            }.onFailure { Log.w(TAG, "Sin red para tasas: ${it.message}") }

            // 3. Sin red: ultima conocida.
            val fallback = cached.copy(spreadPercent = spreadPercent, isOfflineFallback = true)
            _rateStateFlow.value = fallback
            fallback
        }
    }

    override suspend fun updateSpread(spreadPercent: Double) {
        val current = _rateStateFlow.value
        val updated = current.copy(
            spreadPercent = spreadPercent,
            westernUnionRate = current.officialTrm * (1.0 - spreadPercent / 100.0),
            isRealWesternUnionRate = false
        )
        _rateStateFlow.value = updated
        prefs.edit().putLong(KEY_SPREAD, spreadPercent.toRawBits()).apply()
    }

    // ------------------------------------------------------------------
    // Giros
    // ------------------------------------------------------------------

    /**
     * Agrupa montos cercanos para no pedir una cotizacion por cada tecla.
     * Los tramos de WU son anchos (la frontera medida esta entre 500 y 600),
     * asi que redondear a 50 abajo y a 250 arriba no cambia la tasa devuelta.
     */
    private fun bucketOf(amount: Double): Long {
        val step = if (amount < 1000.0) 50.0 else 250.0
        return (amount / step).toLong().coerceAtLeast(1L) * step.roundToLong()
    }

    override suspend fun refreshRemittanceQuote(sendAmountUsd: Double, forceRefresh: Boolean): RemittanceQuote {
        return withContext(Dispatchers.IO) {
            val monto = sendAmountUsd.coerceAtLeast(1.0)
            val bucket = bucketOf(monto)

            if (!forceRefresh) {
                quoteCache[bucket]?.let { cacheada ->
                    if (System.currentTimeMillis() - cacheada.fetchedAt < CACHE_TTL_MS) {
                        val ajustada = cacheada.copy(sendAmountUsd = monto, source = QuoteSource.CACHE)
                            .recalculatedFor(monto)
                        _quoteFlow.value = ajustada
                        return@withContext ajustada
                    }
                }
            }

            val fresca = runCatching { fetchQuote(monto) }.getOrElse { error ->
                Log.w(TAG, "Sin cotizacion en vivo: ${error.message}")
                null
            }

            val resultado = fresca ?: fallbackQuote(monto)
            if (fresca != null) quoteCache[bucket] = fresca
            _quoteFlow.value = resultado

            // La tasa de mercado que devuelve esta llamada tambien alimenta
            // la normalizacion de moneda del resto de la app.
            if (fresca != null && fresca.midMarketRate > 0.0) {
                val info = fresca.toExchangeRateInfo()
                saveToCache(info)
                _rateStateFlow.value = info
            }

            resultado
        }
    }

    /** Pide la comparacion y la traduce al modelo de dominio, ya calibrada. */
    private suspend fun fetchQuote(sendAmountUsd: Double): RemittanceQuote {
        val response = wiseComparisonApi.getComparisons(
            sourceCurrency = "USD",
            targetCurrency = "COP",
            sendAmount = sendAmountUsd
        )
        require(response.isSuccessful) { "HTTP ${response.code()}" }

        val providers = response.body()?.providers.orEmpty()
        require(providers.isNotEmpty()) { "respuesta sin proveedores" }

        val midMarketRate = providers
            .flatMap { p -> p.quotes.orEmpty() }
            .firstOrNull { it.isConsideredMidMarketRate == true }
            ?.rate
            ?: providers.flatMap { it.quotes.orEmpty() }.mapNotNull { it.rate }.maxOrNull()
            ?: 0.0
        require(midMarketRate > 0.0) { "sin tasa de mercado" }

        val calibration = getCalibration()

        val wuRaw = providers.firstOrNull { esWesternUnion(it) }?.quotes?.firstOrNull()
        val westernUnion = wuRaw?.rate?.takeIf { it > 0.0 }?.let { rateApi ->
            val fee = wuRaw.fee ?: 0.0
            // Ajuste Ecuador sobre la tasa; la comision se toma tal cual.
            val rate = RemittanceMath.applyCalibration(rateApi, calibration)
            ProviderQuote(
                alias = WU_ALIAS,
                displayName = "Western Union",
                rate = rate,
                fee = fee,
                receivedCop = RemittanceMath.receivedCop(sendAmountUsd, fee, rate)
            )
        }

        val alternatives = providers
            .filterNot { esWesternUnion(it) }
            .mapNotNull { provider ->
                val q = provider.quotes?.firstOrNull() ?: return@mapNotNull null
                val rate = q.rate?.takeIf { it > 0.0 } ?: return@mapNotNull null
                val fee = q.fee ?: 0.0
                ProviderQuote(
                    alias = provider.alias.orEmpty(),
                    displayName = provider.name ?: provider.alias.orEmpty(),
                    rate = rate,
                    fee = fee,
                    receivedCop = q.receivedAmount ?: RemittanceMath.receivedCop(sendAmountUsd, fee, rate),
                    isMidMarket = q.isConsideredMidMarketRate == true
                )
            }
            .sortedByDescending { it.receivedCop }

        return RemittanceQuote(
            sendAmountUsd = sendAmountUsd,
            midMarketRate = midMarketRate,
            westernUnion = westernUnion,
            alternatives = alternatives,
            calibration = calibration,
            fetchedAt = System.currentTimeMillis(),
            source = QuoteSource.LIVE
        )
    }

    private fun esWesternUnion(provider: WiseProviderDto): Boolean =
        provider.alias?.contains("western", ignoreCase = true) == true ||
                provider.name?.contains("western union", ignoreCase = true) == true

    /** Sin red: se estima con la ultima tasa conocida y el margen configurado. */
    private fun fallbackQuote(sendAmountUsd: Double): RemittanceQuote {
        val info = _rateStateFlow.value
        val calibration = getCalibration()
        val rate = RemittanceMath.applyCalibration(info.westernUnionRate, calibration)
        return RemittanceQuote(
            sendAmountUsd = sendAmountUsd,
            midMarketRate = info.officialTrm,
            westernUnion = if (rate > 0.0 && sendAmountUsd <= WU_MAX_QUOTE_USD) {
                ProviderQuote(
                    alias = WU_ALIAS,
                    displayName = "Western Union",
                    rate = rate,
                    fee = 0.0,
                    receivedCop = RemittanceMath.receivedCop(sendAmountUsd, 0.0, rate)
                )
            } else null,
            alternatives = emptyList(),
            calibration = calibration,
            fetchedAt = info.lastUpdated,
            source = QuoteSource.FALLBACK
        )
    }

    override suspend fun calibrateFromCounter(totalUsd: Double, feeUsd: Double, actualCop: Double) {
        val quote = _quoteFlow.value ?: return
        val wu = quote.westernUnion ?: return

        // Se compara contra la tasa SIN calibrar, si no el factor se realimenta.
        val cal = getCalibration()
        val rateApi = if (cal.factor > 0.0) wu.rate / cal.factor else wu.rate

        val observada = RemittanceMath.impliedRate(totalUsd, feeUsd, actualCop)
        val nueva = RemittanceMath.calibrate(cal, rateApi, observada)

        if (nueva.samples != cal.samples) {
            saveCalibration(nueva)
            quoteCache.clear()
            Log.d(TAG, "Calibracion Ecuador: factor=${nueva.factor} muestras=${nueva.samples}")
            _quoteFlow.value = quote.copy(
                calibration = nueva,
                westernUnion = wu.copy(
                    rate = RemittanceMath.applyCalibration(rateApi, nueva)
                )
            )
        }
    }

    // ------------------------------------------------------------------

    private fun RemittanceQuote.toExchangeRateInfo() = ExchangeRateInfo(
        baseCurrency = "USD",
        targetCurrency = "COP",
        officialTrm = midMarketRate,
        westernUnionRate = westernUnion?.rate ?: midMarketRate,
        spreadPercent = spreadPercent,
        lastUpdated = fetchedAt,
        isOfflineFallback = source == QuoteSource.FALLBACK,
        isRealWesternUnionRate = westernUnion != null && source != QuoteSource.FALLBACK
    )

    /** Reusa una cotizacion del mismo tramo para otro monto: la tasa no cambia. */
    private fun RemittanceQuote.recalculatedFor(monto: Double): RemittanceQuote = copy(
        westernUnion = westernUnion?.let {
            it.copy(receivedCop = RemittanceMath.receivedCop(monto, it.fee, it.rate))
        },
        alternatives = alternatives.map {
            it.copy(receivedCop = RemittanceMath.receivedCop(monto, it.fee, it.rate))
        }
    )
}
