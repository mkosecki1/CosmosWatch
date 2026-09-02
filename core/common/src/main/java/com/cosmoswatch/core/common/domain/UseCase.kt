package com.cosmoswatch.core.common.domain

import com.cosmoswatch.core.common.result.AppResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

abstract class UseCase<in P, R>(private val dispatcher: CoroutineDispatcher) {

    suspend operator fun invoke(params: P): AppResult<R> = withContext(dispatcher) {
        execute(params)
    }

    protected abstract suspend fun execute(params: P): AppResult<R>
}
