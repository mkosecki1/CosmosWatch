package com.cosmoswatch.feature.apod.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ApodApi {
    @GET("planetary/apod")
    suspend fun getApod(@Query("thumbs") thumbs: Boolean = true): ApodDto

    @GET("planetary/apod")
    suspend fun getApodRange(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("thumbs") thumbs: Boolean = true,
    ): List<ApodDto>
}
