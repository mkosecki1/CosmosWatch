package com.cosmoswatch.feature.apod.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ApodArchiveRemoteKeyDao {
    @Query("SELECT * FROM apod_archive_remote_key WHERE id = ${ApodArchiveRemoteKeyEntity.SINGLE_ROW_ID}")
    suspend fun getRemoteKey(): ApodArchiveRemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: ApodArchiveRemoteKeyEntity)
}
