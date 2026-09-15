package com.cosmoswatch.feature.neows.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NeoArchiveRemoteKeyDao {
    @Query("SELECT * FROM neo_archive_remote_key WHERE id = ${NeoArchiveRemoteKeyEntity.SINGLE_ROW_ID}")
    suspend fun getRemoteKey(): NeoArchiveRemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: NeoArchiveRemoteKeyEntity)
}
