package com.cosmoswatch.feature.donki.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donki_timeline")
data class DonkiTimelineEntity(
    @PrimaryKey val id: String,
    val type: String,
    val eventTimeEpochMillis: Long,
    val severity: String,
    val linkUrl: String,
    val linkedEventIds: String,
    val page: Int,
    val flareClassType: String? = null,
    val flareSourceLocation: String? = null,
    val flareActiveRegionNum: Int? = null,
    val cmeNote: String? = null,
    val cmeSpeedKmS: Double? = null,
    val cmeIsEarthDirected: Boolean? = null,
    val cmeEstimatedArrivalEpochMillis: Long? = null,
    val cmeForecastKp: Double? = null,
    val stormPeakKp: Double? = null,
    val stormKpSeriesJson: String? = null,
)
