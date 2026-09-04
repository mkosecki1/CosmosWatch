package com.cosmoswatch.feature.marsphotos.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MarsPhotosApi {
    @GET("mars-photos/api/v1/rovers/{rover}/photos")
    suspend fun getPhotos(
        @Path("rover") rover: String,
        @Query("earth_date") earthDate: String,
        @Query("camera") camera: String?,
        @Query("page") page: Int,
    ): MarsPhotosResponseDto

    @GET("mars-photos/api/v1/manifests/{rover}")
    suspend fun getManifest(
        @Path("rover") rover: String,
    ): MarsPhotoManifestResponseDto
}
