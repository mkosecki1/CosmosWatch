package com.cosmoswatch.feature.donki.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface DonkiApi {
    @GET("get/FLR")
    suspend fun getFlares(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): List<FlrDto>

    @GET("get/CME")
    suspend fun getCmes(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): List<CmeDto>

    @GET("get/GST")
    suspend fun getStorms(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): List<GstDto>
}
