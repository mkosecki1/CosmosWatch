package com.cosmoswatch.feature.marsphotos.domain

import java.time.LocalDate

data class MarsPhotoDomain(
    val id: Int,
    val imageUrl: String,
    val earthDate: LocalDate,
    val sol: Int,
    val cameraName: String,
    val cameraFullName: String,
    val rover: MarsRover,
)
