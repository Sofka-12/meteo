package com.example.meteopipli.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NoaaRetrofitClient {
    private const val BASE_URL = "https://services.swpc.noaa.gov/"

    val api: NoaaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NoaaApi::class.java)
    }
}