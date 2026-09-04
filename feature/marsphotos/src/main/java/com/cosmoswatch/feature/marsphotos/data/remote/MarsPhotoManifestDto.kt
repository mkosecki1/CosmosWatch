package com.cosmoswatch.feature.marsphotos.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarsPhotoManifestResponseDto(
    @SerialName("photo_manifest") val photoManifest: MarsPhotoManifestDto,
)

@Serializable
data class MarsPhotoManifestDto(
    @SerialName("max_date") val maxDate: String,
    val status: String,
)
