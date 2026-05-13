package com.example.meteopipli.data.remote

import retrofit2.http.GET

interface NoaaApi {
    @GET("products/noaa-planetary-k-index.json")
    suspend fun getKpIndex(): List<List<String>>
}