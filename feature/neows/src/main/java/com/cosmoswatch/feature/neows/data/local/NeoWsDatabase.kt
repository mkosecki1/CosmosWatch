package com.cosmoswatch.feature.neows.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NeoUpcomingEntity::class, NeoArchiveEntity::class, NeoArchiveRemoteKeyEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class NeoWsDatabase : RoomDatabase() {
    abstract fun neoUpcomingDao(): NeoUpcomingDao
    abstract fun neoArchiveDao(): NeoArchiveDao
    abstract fun neoArchiveRemoteKeyDao(): NeoArchiveRemoteKeyDao
}
