package com.cosmoswatch.feature.neows.domain

import java.time.LocalDate

data class NeoDomain(
    val id: String,
    val name: String,
    val closeApproachDate: LocalDate,
    val isPotentiallyHazardous: Boolean,
    val isSentryObject: Boolean,
    val missDistanceLunar: Double,
    val relativeVelocityKmh: Double,
    val estimatedDiameterMinMeters: Double,
    val estimatedDiameterMaxMeters: Double,
)
