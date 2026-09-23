package com.cosmoswatch.feature.donki.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DonkiTimelineEntity::class, DonkiRemoteKeyEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class DonkiDatabase : RoomDatabase() {
    abstract fun donkiTimelineDao(): DonkiTimelineDao
    abstract fun donkiRemoteKeyDao(): DonkiRemoteKeyDao
}
