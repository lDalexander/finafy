package com.finanzas.app.data.remote.api

import com.finanzas.app.data.remote.dto.ExchangeRateResponseDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface ExchangeRateApi {

    @GET("v6/latest/{baseCurrency}")
    suspend fun getLatestRates(
        @Path("baseCurrency") baseCurrency: String = "USD"
    ): Response<ExchangeRateResponseDto>

    companion object {
        private const val BASE_URL = "https://open.er-api.com/"

        fun create(): ExchangeRateApi {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ExchangeRateApi::class.java)
        }
    }
}
