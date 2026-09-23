package com.cosmoswatch.feature.donki.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DonkiTimelineDao {
    @Query("SELECT * FROM donki_timeline ORDER BY eventTimeEpochMillis DESC")
    fun pagingSource(): PagingSource<Int, DonkiTimelineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DonkiTimelineEntity>)

    @Query("DELETE FROM donki_timeline")
    suspend fun clearAll()

    @Query("DELETE FROM donki_timeline WHERE page = :page")
    suspend fun deletePage(page: Int)
}
