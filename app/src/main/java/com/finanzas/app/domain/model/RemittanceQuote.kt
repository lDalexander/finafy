package com.finanzas.app.domain.model

/**
 * Cotizacion de un giro USD -> COP para un monto concreto.
 *
 * MODELO DE COSTO (verificado contra la API de comparaciones de Wise):
 *
 *     recibido_COP = (total_enviado_USD - comision_USD) * tasa
 *
 * Es decir, el operador descuenta la comision ANTES de convertir. No es
 * "monto * tasa, y aparte pago la comision".
 *
 * Y hay DOS costos, no uno:
 *   1. la comision explicita (fee)
 *   2. el margen sobre la tasa: el operador convierte a una tasa peor que la
 *      de mercado (mid-market). Ese margen suele pesar mas que la comision.
 */
data class ProviderQuote(
    val alias: String,
    val displayName: String,
    /** COP por 1 USD que aplica este operador. */
    val rate: Double,
    /** Comision en USD, descontada del envio antes de convertir. */
    val fee: Double,
    /** COP que recibe el destinatario. */
    val receivedCop: Double,
    /** true solo para la cotizacion que la API marca como tasa de mercado. */
    val isMidMarket: Boolean = false
)

/** De donde salio la cotizacion que se esta mostrando. */
enum class QuoteSource {
    /** Respuesta fresca de la API. */
    LIVE,

    /** Cache local todavia vigente. */
    CACHE,

    /** Sin red y sin cache util: estimacion con la ultima tasa conocida. */
    FALLBACK
}

/**
 * Ajuste Ecuador.
 *
 * La API de comparaciones solo publica cotizaciones de Western Union con
 * origen EE.UU. (con sourceCountry=EC, WU desaparece de la respuesta).
 * Girando desde Ecuador la tarifa real difiere.
 *
 * Por eso la app aprende el desvio: cada vez que se registra un giro con el
 * valor en COP realmente entregado en ventanilla, se calcula
 *
 *     factor_observado = tasa_real_ventanilla / tasa_WU_de_la_API
 *
 * y se promedia exponencialmente con las observaciones anteriores.
 * Las estimaciones futuras se corrigen con ese factor.
 */
data class RemittanceCalibration(
    val factor: Double = 1.0,
    val samples: Int = 0,
    val lastUpdated: Long = 0L
) {
    val isCalibrated: Boolean get() = samples > 0

    /** Desvio respecto a la cotizacion de la API, en porcentaje. */
    val deviationPercent: Double get() = (factor - 1.0) * 100.0
}

data class RemittanceQuote(
    /** Monto total que sale de la cuenta, comision incluida. */
    val sendAmountUsd: Double,
    /** Tasa de mercado real (mid-market), sin margen de nadie. */
    val midMarketRate: Double,
    /** Cotizacion de Western Union ya corregida por [calibration]. Null si WU no cotiza este monto. */
    val westernUnion: ProviderQuote?,
    /** Otros operadores de la misma consulta, ordenados de mejor a peor. */
    val alternatives: List<ProviderQuote> = emptyList(),
    val calibration: RemittanceCalibration = RemittanceCalibration(),
    val fetchedAt: Long = System.currentTimeMillis(),
    val source: QuoteSource = QuoteSource.FALLBACK
) {
    /** La cotizacion de referencia: WU si existe, si no la de mercado. */
    val effectiveRate: Double
        get() = westernUnion?.rate ?: midMarketRate

    val effectiveFee: Double
        get() = westernUnion?.fee ?: 0.0

    /** Margen de WU bajo la tasa de mercado, en porcentaje. */
    val spreadPercent: Double
        get() = if (midMarketRate > 0.0 && westernUnion != null) {
            ((midMarketRate - westernUnion.rate) / midMarketRate) * 100.0
        } else 0.0

    /** La mejor alternativa por COP recibidos, si supera a WU. */
    val bestAlternative: ProviderQuote?
        get() = alternatives
            .filter { !it.isMidMarket }
            .maxByOrNull { it.receivedCop }
            ?.takeIf { alt -> westernUnion == null || alt.receivedCop > westernUnion.receivedCop }

    /** COP extra que se recibirian usando [bestAlternative] en vez de WU. */
    val savingsVsBestAlternative: Double
        get() {
            val wu = westernUnion ?: return 0.0
            val alt = bestAlternative ?: return 0.0
            return alt.receivedCop - wu.receivedCop
        }
}
