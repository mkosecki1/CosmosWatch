package com.cosmoswatch.feature.neows.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neo_archive_remote_key")
data class NeoArchiveRemoteKeyEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val nextEndDate: String?,
    val oldestCachedPage: Int,
    val newestCachedPage: Int,
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
