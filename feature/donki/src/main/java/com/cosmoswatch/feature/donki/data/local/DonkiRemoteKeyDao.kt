package com.cosmoswatch.feature.donki.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DonkiRemoteKeyDao {
    @Query("SELECT * FROM donki_remote_key WHERE id = ${DonkiRemoteKeyEntity.SINGLE_ROW_ID}")
    suspend fun getRemoteKey(): DonkiRemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: DonkiRemoteKeyEntity)
}
