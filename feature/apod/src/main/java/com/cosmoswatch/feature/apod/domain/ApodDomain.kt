package com.cosmoswatch.feature.apod.domain

import java.time.LocalDate

data class ApodDomain(
    val date: LocalDate,
    val title: String,
    val explanation: String,
    val imageUrl: String,
    val hdImageUrl: String?,
    val mediaType: ApodMediaType,
    val copyright: String?,
)
