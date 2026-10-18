package com.finanzas.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WiseComparisonResponseDto(
    @SerializedName("providers")
    val providers: List<WiseProviderDto>?,
    @SerializedName("sourceCurrency")
    val sourceCurrency: String?,
    @SerializedName("targetCurrency")
    val targetCurrency: String?,
    @SerializedName("amount")
    val amount: Double?,
    @SerializedName("amountType")
    val amountType: String?
)

data class WiseProviderDto(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("alias")
    val alias: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("quotes")
    val quotes: List<WiseQuoteDto>?
)

data class WiseQuoteDto(
    /** COP por 1 USD que aplica el operador. */
    @SerializedName("rate")
    val rate: Double?,
    /** Comision en USD. Se descuenta del envio ANTES de convertir. */
    @SerializedName("fee")
    val fee: Double?,
    /** Margen que declara la API. Se prefiere calcularlo contra la tasa mid-market. */
    @SerializedName("markup")
    val markup: Double?,
    /** COP que recibe el destinatario: (sendAmount - fee) * rate */
    @SerializedName("receivedAmount")
    val receivedAmount: Double?,
    /** Marca la cotizacion que la API considera tasa de mercado real. */
    @SerializedName("isConsideredMidMarketRate")
    val isConsideredMidMarketRate: Boolean?,
    @SerializedName("sourceCountry")
    val sourceCountry: String?,
    @SerializedName("targetCountry")
    val targetCountry: String?,
    @SerializedName("dateCollected")
    val dateCollected: String?
)
