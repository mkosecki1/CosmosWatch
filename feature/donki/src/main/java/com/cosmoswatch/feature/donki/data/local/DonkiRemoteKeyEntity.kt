package com.cosmoswatch.feature.donki.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donki_remote_key")
data class DonkiRemoteKeyEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val nextEndDate: String?,
    val oldestCachedPage: Int,
    val newestCachedPage: Int,
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
