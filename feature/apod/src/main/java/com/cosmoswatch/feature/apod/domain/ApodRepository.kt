package com.cosmoswatch.feature.apod.domain

import androidx.paging.PagingData
import com.cosmoswatch.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow

interface ApodRepository {
    fun getApod(): Flow<AppResult<ApodDomain>>
    fun getArchive(filter: ApodArchiveFilter = ApodArchiveFilter()): Flow<PagingData<ApodDomain>>
}
