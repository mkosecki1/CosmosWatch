package com.cosmoswatch.feature.apod.domain

import com.cosmoswatch.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow

interface ApodRepository {
    fun getApod(): Flow<AppResult<ApodDomain>>
}
