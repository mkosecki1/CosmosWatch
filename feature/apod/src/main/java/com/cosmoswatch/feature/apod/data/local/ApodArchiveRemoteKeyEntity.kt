package com.cosmoswatch.feature.apod.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apod_archive_remote_key")
data class ApodArchiveRemoteKeyEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val nextEndDate: String?,
    val oldestCachedPage: Int,
    val newestCachedPage: Int,
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
