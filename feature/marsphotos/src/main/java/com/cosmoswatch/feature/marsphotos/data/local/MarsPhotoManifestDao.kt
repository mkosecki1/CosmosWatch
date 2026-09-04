package com.cosmoswatch.feature.marsphotos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MarsPhotoManifestDao {
    @Query("SELECT * FROM mars_photo_manifest WHERE roverApiName = :roverApiName")
    suspend fun getManifest(roverApiName: String): MarsPhotoManifestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: MarsPhotoManifestEntity)
}
