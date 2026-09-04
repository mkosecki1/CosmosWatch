package com.cosmoswatch.feature.marsphotos.data.mapper

import com.cosmoswatch.feature.marsphotos.data.local.MarsPhotoEntity
import com.cosmoswatch.feature.marsphotos.data.local.MarsPhotoManifestEntity
import com.cosmoswatch.feature.marsphotos.data.remote.MarsPhotoDto
import com.cosmoswatch.feature.marsphotos.data.remote.MarsPhotoManifestDto
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoDomain
import com.cosmoswatch.feature.marsphotos.domain.MarsRover
import com.cosmoswatch.feature.marsphotos.domain.MarsRoverManifest
import com.cosmoswatch.feature.marsphotos.domain.MarsRoverStatus
import java.time.LocalDate

fun MarsPhotoDto.toEntity(page: Int, roverApiName: String): MarsPhotoEntity = MarsPhotoEntity(
    photoId = id,
    page = page,
    sol = sol,
    earthDate = earthDate,
    cameraName = camera.name,
    cameraFullName = camera.fullName,
    imageUrl = imgSrc,
    roverApiName = roverApiName,
)

fun MarsPhotoEntity.toDomain(): MarsPhotoDomain = MarsPhotoDomain(
    id = photoId,
    imageUrl = imageUrl,
    earthDate = LocalDate.parse(earthDate),
    sol = sol,
    cameraName = cameraName,
    cameraFullName = cameraFullName,
    rover = MarsRover.entries.first { it.apiName == roverApiName },
)

fun MarsPhotoManifestDto.toEntity(roverApiName: String, fetchedAtEpochMillis: Long): MarsPhotoManifestEntity =
    MarsPhotoManifestEntity(
        roverApiName = roverApiName,
        maxEarthDate = maxDate,
        status = status,
        fetchedAtEpochMillis = fetchedAtEpochMillis,
    )

fun MarsPhotoManifestEntity.toDomain(): MarsRoverManifest = MarsRoverManifest(
    latestAvailableDate = LocalDate.parse(maxEarthDate),
    status = if (status.equals("active", ignoreCase = true)) MarsRoverStatus.ACTIVE else MarsRoverStatus.COMPLETE,
)
