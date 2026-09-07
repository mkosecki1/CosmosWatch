package com.cosmoswatch.feature.apod.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ApodArchiveDao {
    @Query("SELECT * FROM apod_archive ORDER BY date DESC")
    fun pagingSource(): PagingSource<Int, ApodArchiveEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ApodArchiveEntity>)

    @Query("DELETE FROM apod_archive")
    suspend fun clearAll()

    @Query("DELETE FROM apod_archive WHERE page = :page")
    suspend fun deletePage(page: Int)
}
