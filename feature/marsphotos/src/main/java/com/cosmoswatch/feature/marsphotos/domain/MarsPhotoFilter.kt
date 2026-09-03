package com.cosmoswatch.feature.marsphotos.domain

import java.time.LocalDate

data class MarsPhotoFilter(
    val rover: MarsRover,
    val earthDate: LocalDate,
    val camera: String? = null,
)
