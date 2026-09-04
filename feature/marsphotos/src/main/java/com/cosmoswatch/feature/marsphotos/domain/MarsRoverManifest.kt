package com.cosmoswatch.feature.marsphotos.domain

import java.time.LocalDate

data class MarsRoverManifest(
    val latestAvailableDate: LocalDate,
    val status: MarsRoverStatus,
)

enum class MarsRoverStatus {
    ACTIVE,
    COMPLETE,
}
