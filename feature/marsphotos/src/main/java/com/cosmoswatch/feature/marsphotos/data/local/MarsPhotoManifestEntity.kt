package com.cosmoswatch.feature.marsphotos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mars_photo_manifest")
data class MarsPhotoManifestEntity(
    @PrimaryKey val roverApiName: String,
    val maxEarthDate: String,
    val fetchedAtEpochMillis: Long,
)
