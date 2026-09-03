package com.cosmoswatch.feature.apod.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ApodEntity::class], version = 1)
abstract class ApodDatabase : RoomDatabase() {
    abstract fun apodDao(): ApodDao
}
