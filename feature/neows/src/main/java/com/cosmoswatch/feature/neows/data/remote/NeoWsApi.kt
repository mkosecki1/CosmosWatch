package com.cosmoswatch.feature.neows.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface NeoWsApi {
    @GET("neo/rest/v1/feed")
    suspend fun getFeed(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): NeoWsFeedResponse
}
