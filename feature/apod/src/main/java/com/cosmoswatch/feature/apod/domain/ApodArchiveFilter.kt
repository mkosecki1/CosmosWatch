package com.cosmoswatch.feature.apod.domain

import java.time.LocalDate

val APOD_ARCHIVE_START_DATE: LocalDate = LocalDate.of(1995, 6, 16)

data class ApodArchiveFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)
