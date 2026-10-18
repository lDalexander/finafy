package com.finanzas.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponseDto(
    @SerializedName("result")
    val result: String?,
    @SerializedName("base_code")
    val baseCode: String?,
    @SerializedName("time_last_update_unix")
    val timeLastUpdateUnix: Long?,
    @SerializedName("time_last_update_utc")
    val timeLastUpdateUtc: String?,
    @SerializedName("rates")
    val rates: Map<String, Double>?
)
