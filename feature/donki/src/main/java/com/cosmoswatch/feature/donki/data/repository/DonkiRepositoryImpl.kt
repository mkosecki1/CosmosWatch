package com.cosmoswatch.feature.donki.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cosmoswatch.feature.donki.data.local.DonkiDatabase
import com.cosmoswatch.feature.donki.data.mapper.toDomain
import com.cosmoswatch.feature.donki.data.remote.DonkiApi
import com.cosmoswatch.feature.donki.data.remote.DonkiRemoteMediator
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain
import com.cosmoswatch.feature.donki.domain.DonkiRepository
import com.cosmoswatch.feature.donki.domain.DonkiTimelineFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

private const val TIMELINE_PAGE_SIZE = 20

class DonkiRepositoryImpl @Inject constructor(
    private val api: DonkiApi,
    private val database: DonkiDatabase,
    private val clock: Clock,
) : DonkiRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getTimeline(filter: DonkiTimelineFilter): Flow<PagingData<DonkiEventDomain>> = Pager(
        config = PagingConfig(pageSize = TIMELINE_PAGE_SIZE, enablePlaceholders = false),
        remoteMediator = DonkiRemoteMediator(api = api, database = database, clock = clock, filter = filter),
        pagingSourceFactory = {
            database.donkiTimelineDao().pagingSource(
                showFlares = filter.showFlares,
                showCmes = filter.showCmes,
                showStorms = filter.showStorms,
                significantOnly = filter.significantOnly,
                earthDirectedOnly = filter.earthDirectedOnly,
            )
        },
    ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
}
