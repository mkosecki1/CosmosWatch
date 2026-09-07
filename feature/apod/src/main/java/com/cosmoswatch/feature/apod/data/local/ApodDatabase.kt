package com.cosmoswatch.feature.apod.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ApodEntity::class, ApodArchiveEntity::class, ApodArchiveRemoteKeyEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ApodDatabase : RoomDatabase() {
    abstract fun apodDao(): ApodDao
    abstract fun apodArchiveDao(): ApodArchiveDao
    abstract fun apodArchiveRemoteKeyDao(): ApodArchiveRemoteKeyDao
}
