package com.cosmoswatch.feature.marsphotos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mars_photo")
data class MarsPhotoEntity(
    @PrimaryKey val photoId: Int,
    val page: Int,
    val sol: Int,
    val earthDate: String,
    val cameraName: String,
    val imageUrl: String,
    val roverApiName: String,
)
