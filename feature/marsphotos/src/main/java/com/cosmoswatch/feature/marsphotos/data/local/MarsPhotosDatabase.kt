package com.cosmoswatch.feature.marsphotos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MarsPhotoEntity::class,
        MarsPhotoRemoteKeyEntity::class,
        MarsPhotoManifestEntity::class,
    ],
    version = 1,
)
abstract class MarsPhotosDatabase : RoomDatabase() {
    abstract fun marsPhotoDao(): MarsPhotoDao
    abstract fun marsPhotoRemoteKeyDao(): MarsPhotoRemoteKeyDao
    abstract fun marsPhotoManifestDao(): MarsPhotoManifestDao
}
