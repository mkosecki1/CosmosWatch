package com.cosmoswatch.feature.neows.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NeoArchiveDao {
    @Query(
        """
        SELECT * FROM neo_archive
        WHERE (:hazardousOnly = 0 OR isPotentiallyHazardous = 1)
          AND (:closeOnly = 0 OR missDistanceLunar <= :closeThresholdLd)
          AND (:largeOnly = 0 OR estimatedDiameterMinMeters >= :largeThresholdMeters)
          AND (:sentryOnly = 0 OR isSentryObject = 1)
        ORDER BY closeApproachDate DESC
        """,
    )
    fun pagingSource(
        hazardousOnly: Boolean,
        closeOnly: Boolean,
        closeThresholdLd: Double,
        largeOnly: Boolean,
        largeThresholdMeters: Double,
        sentryOnly: Boolean,
    ): PagingSource<Int, NeoArchiveEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<NeoArchiveEntity>)

    @Query("DELETE FROM neo_archive")
    suspend fun clearAll()

    @Query("DELETE FROM neo_archive WHERE page = :page")
    suspend fun deletePage(page: Int)
}
