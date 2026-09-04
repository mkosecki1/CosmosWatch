package com.cosmoswatch.feature.marsphotos.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MarsPhotoDao {
    @Query("SELECT * FROM mars_photo ORDER BY page ASC, photoId ASC")
    fun pagingSource(): PagingSource<Int, MarsPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<MarsPhotoEntity>)

    @Query("DELETE FROM mars_photo")
    suspend fun clearAll()

    @Query("DELETE FROM mars_photo WHERE page = :page")
    suspend fun deletePage(page: Int)
}
