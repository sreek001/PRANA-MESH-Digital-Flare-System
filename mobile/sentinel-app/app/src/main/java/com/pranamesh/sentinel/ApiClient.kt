package com.pranamesh.sentinel

import android.util.Log
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class DistressReport(
    val device_id: String,
    val lat: Float,
    val lon: Float,
    val status: Int,
    val battery: Int,
    val timestamp: Long,
    val sentinel_id: String
)

interface PranaMeshApi {
    @POST("/report")
    fun reportSignal(@Body report: DistressReport): Call<Void>
}

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    // Backend URL - Cloud Run deployment
    private const val BASE_URL = "https://prana-mesh-backend-8152165795.us-central1.run.app"

    val api: PranaMeshApi by lazy {
        Log.d(TAG, "Initializing Retrofit with base URL: $BASE_URL")
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(PranaMeshApi::class.java)
    }
}
