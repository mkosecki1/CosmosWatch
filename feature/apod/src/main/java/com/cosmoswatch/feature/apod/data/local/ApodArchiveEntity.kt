package com.cosmoswatch.feature.apod.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apod_archive")
data class ApodArchiveEntity(
    @PrimaryKey val date: String,
    val title: String,
    val explanation: String,
    val imageUrl: String,
    val hdImageUrl: String?,
    val mediaType: String,
    val copyright: String?,
    val page: Int,
)
