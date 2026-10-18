package com.finanzas.app.data.remote.api

import com.finanzas.app.data.remote.dto.WiseComparisonResponseDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * API publica de comparaciones de Wise. Devuelve, para un corredor y un monto,
 * la cotizacion de varios operadores (Western Union, Xoom, Instarem) y la tasa
 * de mercado real.
 *
 * IMPORTANTE - [sendAmount] no es decorativo: la tasa de Western Union es
 * ESCALONADA por monto. Medido en USD->COP:
 *     <= 500 USD  -> margen ~3,0% bajo mid-market
 *     >= 600 USD  -> margen ~1,4% bajo mid-market
 *     >  7000 USD -> WU deja de cotizar (la respuesta no lo incluye)
 * Consultar siempre con 100 fijo daba una tasa equivocada para giros grandes.
 *
 * LIMITACION CONOCIDA: con sourceCountry=EC la respuesta no incluye Western
 * Union. Las cotizaciones de WU son con origen EE.UU. Se usan como referencia
 * y el desvio real desde Ecuador lo aprende RemittanceMath.calibrate.
 */
interface WiseComparisonApi {

    @GET("v4/comparisons/")
    suspend fun getComparisons(
        @Query("sourceCurrency") sourceCurrency: String = "USD",
        @Query("targetCurrency") targetCurrency: String = "COP",
        @Query("sendAmount") sendAmount: Double = 100.0
    ): Response<WiseComparisonResponseDto>

    companion object {
        private const val BASE_URL = "https://api.wise.com/"

        fun create(): WiseComparisonApi {
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
                .create(WiseComparisonApi::class.java)
        }
    }
}
