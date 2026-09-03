package com.cosmoswatch.feature.apod.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apod")
data class ApodEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val date: String,
    val title: String,
    val explanation: String,
    val imageUrl: String,
    val hdImageUrl: String?,
    val mediaType: String,
    val copyright: String?,
    val fetchedAtEpochMillis: Long,
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
