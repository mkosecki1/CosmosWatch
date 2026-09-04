package com.cosmoswatch.feature.marsphotos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MarsPhotoRemoteKeyDao {
    @Query("SELECT * FROM mars_photo_remote_key WHERE id = ${MarsPhotoRemoteKeyEntity.SINGLE_ROW_ID}")
    suspend fun getRemoteKey(): MarsPhotoRemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(key: MarsPhotoRemoteKeyEntity)

    @Query("DELETE FROM mars_photo_remote_key")
    suspend fun clear()
}
