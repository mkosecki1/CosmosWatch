package com.cosmoswatch.feature.apod.data.mapper

import com.cosmoswatch.feature.apod.data.local.ApodEntity
import com.cosmoswatch.feature.apod.data.remote.ApodDto
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import java.time.LocalDate

fun ApodDto.toEntity(fetchedAtEpochMillis: Long): ApodEntity = ApodEntity(
    date = date,
    title = title,
    explanation = explanation,
    imageUrl = url,
    hdImageUrl = hdurl,
    mediaType = mediaType,
    copyright = copyright,
    fetchedAtEpochMillis = fetchedAtEpochMillis,
)

fun ApodEntity.toDomain(): ApodDomain = ApodDomain(
    date = LocalDate.parse(date),
    title = title,
    explanation = explanation,
    imageUrl = imageUrl,
    hdImageUrl = hdImageUrl,
    mediaType = if (mediaType == "video") ApodMediaType.VIDEO else ApodMediaType.IMAGE,
    copyright = copyright,
)
