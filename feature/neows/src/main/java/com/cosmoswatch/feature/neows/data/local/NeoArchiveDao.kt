package com.cosmoswatch.feature.neows.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NeoArchiveDao {
    @Query("SELECT * FROM neo_archive ORDER BY closeApproachDate DESC")
    fun pagingSource(): PagingSource<Int, NeoArchiveEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<NeoArchiveEntity>)

    @Query("DELETE FROM neo_archive")
    suspend fun clearAll()

    @Query("DELETE FROM neo_archive WHERE page = :page")
    suspend fun deletePage(page: Int)
}
