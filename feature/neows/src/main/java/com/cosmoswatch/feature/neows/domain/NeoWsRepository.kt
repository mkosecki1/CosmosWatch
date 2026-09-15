package com.cosmoswatch.feature.neows.domain

import androidx.paging.PagingData
import com.cosmoswatch.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow

interface NeoWsRepository {
    fun getUpcoming(): Flow<AppResult<List<NeoDomain>>>
    fun getArchive(): Flow<PagingData<NeoDomain>>
}
