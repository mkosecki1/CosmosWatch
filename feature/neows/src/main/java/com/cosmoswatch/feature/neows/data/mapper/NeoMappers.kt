package com.cosmoswatch.feature.neows.data.mapper

import com.cosmoswatch.feature.neows.data.local.NeoArchiveEntity
import com.cosmoswatch.feature.neows.data.local.NeoUpcomingEntity
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

private fun NeoDomain.entryId(): String = "$id-$closeApproachDate"

fun NeoDomain.toUpcomingEntity(fetchedAtEpochMillis: Long): NeoUpcomingEntity = NeoUpcomingEntity(
    entryId = entryId(),
    id = id,
    name = name,
    closeApproachDate = closeApproachDate.toString(),
    isPotentiallyHazardous = isPotentiallyHazardous,
    isSentryObject = isSentryObject,
    missDistanceLunar = missDistanceLunar,
    relativeVelocityKmh = relativeVelocityKmh,
    estimatedDiameterMinMeters = estimatedDiameterMinMeters,
    estimatedDiameterMaxMeters = estimatedDiameterMaxMeters,
    fetchedAtEpochMillis = fetchedAtEpochMillis,
)

fun NeoUpcomingEntity.toDomain(): NeoDomain = NeoDomain(
    id = id,
    name = name,
    closeApproachDate = LocalDate.parse(closeApproachDate),
    isPotentiallyHazardous = isPotentiallyHazardous,
    isSentryObject = isSentryObject,
    missDistanceLunar = missDistanceLunar,
    relativeVelocityKmh = relativeVelocityKmh,
    estimatedDiameterMinMeters = estimatedDiameterMinMeters,
    estimatedDiameterMaxMeters = estimatedDiameterMaxMeters,
)

fun NeoDomain.toArchiveEntity(page: Int): NeoArchiveEntity = NeoArchiveEntity(
    entryId = entryId(),
    id = id,
    name = name,
    closeApproachDate = closeApproachDate.toString(),
    isPotentiallyHazardous = isPotentiallyHazardous,
    isSentryObject = isSentryObject,
    missDistanceLunar = missDistanceLunar,
    relativeVelocityKmh = relativeVelocityKmh,
    estimatedDiameterMinMeters = estimatedDiameterMinMeters,
    estimatedDiameterMaxMeters = estimatedDiameterMaxMeters,
    page = page,
)

fun NeoArchiveEntity.toDomain(): NeoDomain = NeoDomain(
    id = id,
    name = name,
    closeApproachDate = LocalDate.parse(closeApproachDate),
    isPotentiallyHazardous = isPotentiallyHazardous,
    isSentryObject = isSentryObject,
    missDistanceLunar = missDistanceLunar,
    relativeVelocityKmh = relativeVelocityKmh,
    estimatedDiameterMinMeters = estimatedDiameterMinMeters,
    estimatedDiameterMaxMeters = estimatedDiameterMaxMeters,
)
