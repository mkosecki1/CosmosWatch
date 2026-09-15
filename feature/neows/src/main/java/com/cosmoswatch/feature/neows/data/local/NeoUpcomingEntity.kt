package com.cosmoswatch.feature.neows.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neo_upcoming")
data class NeoUpcomingEntity(
    @PrimaryKey val entryId: String,
    val id: String,
    val name: String,
    val closeApproachDate: String,
    val isPotentiallyHazardous: Boolean,
    val isSentryObject: Boolean,
    val missDistanceLunar: Double,
    val relativeVelocityKmh: Double,
    val estimatedDiameterMinMeters: Double,
    val estimatedDiameterMaxMeters: Double,
    val fetchedAtEpochMillis: Long,
)
