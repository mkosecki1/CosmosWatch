package com.cosmoswatch.feature.donki.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DonkiTimelineDao {
    @Query(
        """
        SELECT * FROM donki_timeline
        WHERE (
            (type = 'FLARE' AND :showFlares)
            OR (type = 'CME' AND :showCmes)
            OR (type = 'STORM' AND :showStorms)
        )
        AND (:significantOnly = 0 OR type != 'FLARE' OR severity != 'NONE')
        AND (:earthDirectedOnly = 0 OR type != 'CME' OR cmeIsEarthDirected = 1)
        ORDER BY eventTimeEpochMillis DESC
        """,
    )
    fun pagingSource(
        showFlares: Boolean,
        showCmes: Boolean,
        showStorms: Boolean,
        significantOnly: Boolean,
        earthDirectedOnly: Boolean,
    ): PagingSource<Int, DonkiTimelineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DonkiTimelineEntity>)

    @Query("DELETE FROM donki_timeline")
    suspend fun clearAll()

    @Query("DELETE FROM donki_timeline WHERE page = :page")
    suspend fun deletePage(page: Int)
}
