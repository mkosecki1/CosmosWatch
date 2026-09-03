package com.cosmoswatch.feature.apod.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApodDto(
    val date: String,
    val title: String,
    val explanation: String,
    val url: String,
    val hdurl: String? = null,
    @SerialName("media_type") val mediaType: String,
    val copyright: String? = null,
)
