package com.cosmoswatch.feature.neows.domain

data class NeoUpcomingFilter(
    val hazardousOnly: Boolean = false,
    val closeOnly: Boolean = false,
    val largeOnly: Boolean = false,
    val sentryOnly: Boolean = false,
)

fun NeoDomain.matches(filter: NeoUpcomingFilter): Boolean =
    (!filter.hazardousOnly || isPotentiallyHazardous) &&
        (!filter.closeOnly || missDistanceLunar <= NEO_CLOSE_THRESHOLD_LD) &&
        (!filter.largeOnly || estimatedDiameterMinMeters >= NEO_LARGE_THRESHOLD_METERS) &&
        (!filter.sentryOnly || isSentryObject)
