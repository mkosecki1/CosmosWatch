package com.cosmoswatch.feature.donki.domain

import java.time.Instant

enum class DonkiSeverity { SEVERE, MODERATE, NONE }

data class KpObservationDomain(
    val time: Instant,
    val kp: Double,
)

sealed interface DonkiEventDomain {
    val id: String
    val eventTime: Instant
    val severity: DonkiSeverity
    val linkUrl: String
    val linkedEventIds: List<String>

    data class Flare(
        override val id: String,
        override val eventTime: Instant,
        override val severity: DonkiSeverity,
        override val linkUrl: String,
        override val linkedEventIds: List<String>,
        val classType: String,
        val sourceLocation: String?,
        val activeRegionNum: Int?,
    ) : DonkiEventDomain

    data class CmeEjection(
        override val id: String,
        override val eventTime: Instant,
        override val severity: DonkiSeverity,
        override val linkUrl: String,
        override val linkedEventIds: List<String>,
        val note: String,
        val speedKmS: Double?,
        val isEarthDirected: Boolean,
        val estimatedArrivalTime: Instant?,
        val forecastKp: Double?,
    ) : DonkiEventDomain

    data class GeomagneticStorm(
        override val id: String,
        override val eventTime: Instant,
        override val severity: DonkiSeverity,
        override val linkUrl: String,
        override val linkedEventIds: List<String>,
        val kpIndexSeries: List<KpObservationDomain>,
        val peakKp: Double,
    ) : DonkiEventDomain
}
