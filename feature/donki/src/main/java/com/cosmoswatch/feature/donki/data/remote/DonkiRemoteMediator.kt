package com.cosmoswatch.feature.donki.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.cosmoswatch.feature.donki.data.local.DonkiDatabase
import com.cosmoswatch.feature.donki.data.local.DonkiRemoteKeyEntity
import com.cosmoswatch.feature.donki.data.local.DonkiTimelineEntity
import com.cosmoswatch.feature.donki.data.mapper.toDomain
import com.cosmoswatch.feature.donki.data.mapper.toEntity
import com.cosmoswatch.feature.donki.domain.DONKI_ARCHIVE_START_DATE
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain
import com.cosmoswatch.feature.donki.domain.DonkiTimelineFilter
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.LocalDate

private const val WINDOW_DAYS = 30L

@OptIn(ExperimentalPagingApi::class)
class DonkiRemoteMediator(
    private val api: DonkiApi,
    private val database: DonkiDatabase,
    private val clock: Clock,
    private val filter: DonkiTimelineFilter,
) : RemoteMediator<Int, DonkiTimelineEntity>() {

    private val timelineDao = database.donkiTimelineDao()
    private val remoteKeyDao = database.donkiRemoteKeyDao()
    private val boundaryStartDate = DONKI_ARCHIVE_START_DATE

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, DonkiTimelineEntity>,
    ): MediatorResult {
        val currentKey = remoteKeyDao.getRemoteKey()
        val range = rangeToLoad(loadType, currentKey)
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        return try {
            val events = fetchRange(range)
            val endOfPaginationReached = range.start <= boundaryStartDate
            persistPage(loadType, events, range.start, endOfPaginationReached)
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        } catch (e: SerializationException) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun fetchRange(range: DateRange): List<DonkiEventDomain> {
        val startDate = range.start.toString()
        val endDate = range.end.toString()
        val flares = api.getFlares(startDate, endDate).map { it.toDomain() }
        val cmes = api.getCmes(startDate, endDate).map { it.toDomain() }
        val storms = api.getStorms(startDate, endDate).map { it.toDomain() }
        return flares + cmes + storms
    }

    private fun rangeToLoad(loadType: LoadType, currentKey: DonkiRemoteKeyEntity?): DateRange? = when (loadType) {
        LoadType.PREPEND -> null
        LoadType.REFRESH -> {
            val end = filter.endDate ?: LocalDate.now(clock)
            val start = maxOf(end.minusDays(WINDOW_DAYS - 1), boundaryStartDate)
            DateRange(start = start, end = end)
        }
        LoadType.APPEND -> {
            val nextEndDate = currentKey?.nextEndDate?.let(LocalDate::parse) ?: return null
            val start = maxOf(nextEndDate.minusDays(WINDOW_DAYS - 1), boundaryStartDate)
            DateRange(start = start, end = nextEndDate)
        }
    }

    private suspend fun persistPage(
        loadType: LoadType,
        events: List<DonkiEventDomain>,
        rangeStart: LocalDate,
        endOfPaginationReached: Boolean,
    ) = database.withTransaction {
        if (loadType == LoadType.REFRESH) {
            timelineDao.clearAll()
        }
        timelineDao.insertAll(events.map { it.toEntity() })
        remoteKeyDao.insertOrReplace(
            DonkiRemoteKeyEntity(
                nextEndDate = if (endOfPaginationReached) null else rangeStart.minusDays(1).toString(),
            ),
        )
    }

    private data class DateRange(val start: LocalDate, val end: LocalDate)
}
