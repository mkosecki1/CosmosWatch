package com.cosmoswatch.feature.neows.domain

import java.time.LocalDate

const val NEO_CLOSE_THRESHOLD_LD = 1.0
const val NEO_LARGE_THRESHOLD_METERS = 140.0

data class NeoArchiveFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val hazardousOnly: Boolean = false,
    val closeOnly: Boolean = false,
    val largeOnly: Boolean = false,
    val sentryOnly: Boolean = false,
)
