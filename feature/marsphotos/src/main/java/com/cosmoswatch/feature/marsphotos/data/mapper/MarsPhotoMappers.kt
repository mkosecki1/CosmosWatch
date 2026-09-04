package com.cosmoswatch.feature.marsphotos.data.mapper

import com.cosmoswatch.feature.marsphotos.data.local.MarsPhotoEntity
import com.cosmoswatch.feature.marsphotos.data.remote.MarsPhotoDto
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoDomain
import com.cosmoswatch.feature.marsphotos.domain.MarsRover
import java.time.LocalDate

fun MarsPhotoDto.toEntity(page: Int, roverApiName: String): MarsPhotoEntity = MarsPhotoEntity(
    photoId = id,
    page = page,
    sol = sol,
    earthDate = earthDate,
    cameraName = camera.name,
    imageUrl = imgSrc,
    roverApiName = roverApiName,
)

fun MarsPhotoEntity.toDomain(): MarsPhotoDomain = MarsPhotoDomain(
    id = photoId,
    imageUrl = imageUrl,
    earthDate = LocalDate.parse(earthDate),
    sol = sol,
    cameraName = cameraName,
    rover = MarsRover.entries.first { it.apiName == roverApiName },
)
