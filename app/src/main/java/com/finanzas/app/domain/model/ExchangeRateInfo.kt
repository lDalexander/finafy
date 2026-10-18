package com.finanzas.app.domain.model

/**
 * Representa la tasa de cambio con el margen de comisión y cotización de Western Union vs TRM oficial.
 *
 * Fórmula de Fallback: Tasa Efectiva = TRM Oficial * (1 - Margen%)
 */
data class ExchangeRateInfo(
    val baseCurrency: String = "USD",
    val targetCurrency: String = "COP",
    val officialTrm: Double = 4150.0,
    val westernUnionRate: Double = 4067.0,
    val spreadPercent: Double = 2.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isOfflineFallback: Boolean = false,
    val isRealWesternUnionRate: Boolean = false
) {
    // Alias para conveniencia de integración
    val trmOficial: Double get() = officialTrm
    val tasaWesternUnion: Double get() = westernUnionRate
    val spreadAplicado: Double get() = spreadPercent

    /**
     * Tasa de cambio final aplicada: tasa real de Western Union si está disponible,
     * o cálculo con spread aplicado sobre la TRM oficial.
     */
    val effectiveRate: Double
        get() = if (isRealWesternUnionRate && westernUnionRate > 0.0) {
            westernUnionRate
        } else {
            officialTrm * (1.0 - (spreadPercent / 100.0))
        }

    /**
     * Calcula el monto a recibir en moneda destino a partir de un monto en USD.
     */
    fun calculateTargetAmount(amountUsd: Double): Double {
        return amountUsd * effectiveRate
    }

    /**
     * Calcula la tasa implícita si el usuario ingresa manualmente el monto en COP.
     */
    fun calculateImplicitRate(amountUsd: Double, targetCop: Double): Double {
        if (amountUsd <= 0) return effectiveRate
        return targetCop / amountUsd
    }
}