package com.cosmoswatch.feature.apod.domain.usecase

import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetApodUseCase @Inject constructor(
    private val repository: ApodRepository,
) {
    operator fun invoke(): Flow<AppResult<ApodDomain>> = repository.getApod()
}
