package com.cosmoswatch.feature.apod.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeApodDao(initialEntity: ApodEntity? = null) : ApodDao {

    private val entity = MutableStateFlow(initialEntity)

    override fun observe(): Flow<ApodEntity?> = entity

    override suspend fun insertOrReplace(entity: ApodEntity) {
        this.entity.value = entity
    }
}
