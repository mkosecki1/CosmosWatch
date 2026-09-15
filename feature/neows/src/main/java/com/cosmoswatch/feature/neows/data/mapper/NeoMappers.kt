package com.cosmoswatch.feature.neows.data.mapper

import com.cosmoswatch.feature.neows.data.remote.NeoDto
import com.cosmoswatch.feature.neows.data.remote.NeoWsFeedResponse
import com.cosmoswatch.feature.neows.domain.NeoDomain
import java.time.LocalDate

fun NeoWsFeedResponse.toDomain(): List<NeoDomain> =
    nearEarthObjects.values.flatten().map { it.toDomain() }

fun NeoDto.toDomain(): NeoDomain {
    val closeApproach = closeApproachData.first()
    return NeoDomain(
        id = id,
        name = name,
        closeApproachDate = LocalDate.parse(closeApproach.closeApproachDate),
        isPotentiallyHazardous = isPotentiallyHazardousAsteroid,
        isSentryObject = isSentryObject,
        missDistanceLunar = closeApproach.missDistance.lunar.toDouble(),
        relativeVelocityKmh = closeApproach.relativeVelocity.kilometersPerHour.toDouble(),
        estimatedDiameterMinMeters = estimatedDiameter.meters.estimatedDiameterMin,
        estimatedDiameterMaxMeters = estimatedDiameter.meters.estimatedDiameterMax,
    )
}
