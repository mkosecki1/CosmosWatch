package com.cosmoswatch.feature.apod.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ApodDao {
    @Query("SELECT * FROM apod WHERE id = ${ApodEntity.SINGLE_ROW_ID}")
    fun observe(): Flow<ApodEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: ApodEntity)
}
