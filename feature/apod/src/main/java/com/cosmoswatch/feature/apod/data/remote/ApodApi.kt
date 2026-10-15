package com.cosmoswatch.feature.apod.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ApodApi {
    @GET("apod-basic")
    suspend fun getPage(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
    ): List<ApodDto>
}
