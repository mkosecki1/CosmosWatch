package com.cosmoswatch.feature.marsphotos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MarsPhotoManifestDao {
    @Query("SELECT * FROM mars_photo_manifest WHERE roverApiName = :roverApiName")
    fun observeManifest(roverApiName: String): Flow<MarsPhotoManifestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: MarsPhotoManifestEntity)
}
