package com.cosmoswatch.feature.donki.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface DonkiRepository {
    fun getTimeline(filter: DonkiTimelineFilter = DonkiTimelineFilter()): Flow<PagingData<DonkiEventDomain>>
}
