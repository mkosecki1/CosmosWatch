package com.cosmoswatch.feature.marsphotos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mars_photo_remote_key")
data class MarsPhotoRemoteKeyEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val nextPage: Int?,
    val oldestCachedPage: Int,
    val newestCachedPage: Int,
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
