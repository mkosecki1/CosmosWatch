package com.cosmoswatch.feature.apod.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ApodApi {
    @GET("planetary/apod")
    suspend fun getApod(@Query("date") date: String): ApodDto
}
