package com.cosmoswatch.feature.neows.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NeoUpcomingDao {
    @Query("SELECT * FROM neo_upcoming ORDER BY closeApproachDate ASC")
    fun observe(): Flow<List<NeoUpcomingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<NeoUpcomingEntity>)

    @Query("DELETE FROM neo_upcoming")
    suspend fun clearAll()
}
