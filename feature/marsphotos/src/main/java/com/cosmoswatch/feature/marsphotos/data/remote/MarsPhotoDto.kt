package com.cosmoswatch.feature.marsphotos.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarsPhotosResponseDto(
    val photos: List<MarsPhotoDto>,
)

@Serializable
data class MarsPhotoDto(
    val id: Int,
    val sol: Int,
    val camera: MarsCameraDto,
    @SerialName("img_src") val imgSrc: String,
    @SerialName("earth_date") val earthDate: String,
)

@Serializable
data class MarsCameraDto(
    val name: String,
    @SerialName("full_name") val fullName: String,
)
