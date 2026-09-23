package com.cosmoswatch.feature.neows.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNeoUpcomingDao(initialEntities: List<NeoUpcomingEntity> = emptyList()) : NeoUpcomingDao {

    private val entities = MutableStateFlow(initialEntities.sortedBy { it.closeApproachDate })

    override fun observe(): Flow<List<NeoUpcomingEntity>> = entities

    override suspend fun insertAll(entities: List<NeoUpcomingEntity>) {
        this.entities.value = (this.entities.value + entities)
            .associateBy { it.entryId }
            .values
            .sortedBy { it.closeApproachDate }
    }

    override suspend fun clearAll() {
        entities.value = emptyList()
    }
}
