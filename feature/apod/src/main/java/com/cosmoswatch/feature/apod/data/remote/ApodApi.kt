package com.cosmoswatch.feature.apod.data.remote

import retrofit2.http.GET

interface ApodApi {
    @GET("planetary/apod")
    suspend fun getApod(): ApodDto
}
