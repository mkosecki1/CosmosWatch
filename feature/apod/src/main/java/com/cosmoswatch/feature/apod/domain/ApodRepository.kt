package com.cosmoswatch.feature.apod.domain

import androidx.paging.PagingData
import com.cosmoswatch.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ApodRepository {
    fun getApod(): Flow<AppResult<ApodDomain>>
    fun getArchive(filter: ApodArchiveFilter = ApodArchiveFilter()): Flow<PagingData<ApodDomain>>
    fun observeArchiveEntry(date: LocalDate): Flow<ApodDomain?>
}
