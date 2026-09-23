package com.cosmoswatch.feature.neows.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.MediatorResult
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cosmoswatch.feature.neows.data.local.NeoArchiveEntity
import com.cosmoswatch.feature.neows.data.local.NeoWsDatabase
import com.cosmoswatch.feature.neows.domain.NeoArchiveFilter
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalPagingApi::class)
@RunWith(AndroidJUnit4::class)
class NeoArchiveRemoteMediatorTest {

    private val clock: Clock = Clock.fixed(Instant.parse("2026-09-03T12:00:00Z"), ZoneOffset.UTC)
    private val requestedEndDates = mutableListOf<String>()
    private val api = FakeNeoWsApi { _, endDate ->
        requestedEndDates += endDate
        sampleFeedResponse(endDate)
    }
    private lateinit var database: NeoWsDatabase

    private val emptyPagingState = PagingState<Int, NeoArchiveEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20),
        leadingPlaceholderCount = 0,
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NeoWsDatabase::class.java)
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun sampleFeedResponse(date: String) = NeoWsFeedResponse(
        nearEarthObjects = mapOf(
            date to listOf(
                NeoDto(
                    id = "id-$date",
                    name = "Asteroid $date",
                    isPotentiallyHazardousAsteroid = false,
                    isSentryObject = false,
                    estimatedDiameter = EstimatedDiameterDto(meters = DiameterRangeDto(estimatedDiameterMin = 10.0, estimatedDiameterMax = 20.0)),
                    closeApproachData = listOf(
                        CloseApproachDto(
                            closeApproachDate = date,
                            relativeVelocity = RelativeVelocityDto(kilometersPerHour = "40000"),
                            missDistance = MissDistanceDto(lunar = "5.0"),
                        ),
                    ),
                ),
            ),
        ),
    )

    private fun mediator(filter: NeoArchiveFilter = NeoArchiveFilter()) =
        NeoArchiveRemoteMediator(api = api, database = database, clock = clock, filter = filter)

    private fun assertSuccess(expectedEndOfPagination: Boolean, result: MediatorResult) {
        assertTrue("expected MediatorResult.Success but was $result", result is MediatorResult.Success)
        assertEquals(expectedEndOfPagination, (result as MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refresh_startsTheWindowAtYesterday_notToday() = runTest {
        val result = mediator().load(LoadType.REFRESH, emptyPagingState)

        assertSuccess(expectedEndOfPagination = false, result = result)
        assertEquals(LocalDate.now(clock).minusDays(1).toString(), requestedEndDates.single())
        val remoteKey = database.neoArchiveRemoteKeyDao().getRemoteKey()
        assertEquals(0, remoteKey?.oldestCachedPage)
        assertEquals(0, remoteKey?.newestCachedPage)
        assertEquals(LocalDate.now(clock).minusDays(8).toString(), remoteKey?.nextEndDate)
    }

    @Test
    fun append_continuesFromTheRemoteKeysNextEndDate() = runTest {
        val remoteMediator = mediator()
        remoteMediator.load(LoadType.REFRESH, emptyPagingState)
        val nextEndDateAfterRefresh = database.neoArchiveRemoteKeyDao().getRemoteKey()?.nextEndDate

        val result = remoteMediator.load(LoadType.APPEND, emptyPagingState)

        assertSuccess(expectedEndOfPagination = false, result = result)
        assertEquals(nextEndDateAfterRefresh, requestedEndDates[1])
        val remoteKey = database.neoArchiveRemoteKeyDao().getRemoteKey()
        assertEquals(0, remoteKey?.oldestCachedPage)
        assertEquals(1, remoteKey?.newestCachedPage)
    }

    @Test
    fun endOfPaginationIsReachedOnceTheRangeHitsTheArchiveBoundary() = runTest {
        val filter = NeoArchiveFilter(startDate = LocalDate.now(clock).minusDays(3))

        val result = mediator(filter).load(LoadType.REFRESH, emptyPagingState)

        assertSuccess(expectedEndOfPagination = true, result = result)
        assertNull(database.neoArchiveRemoteKeyDao().getRemoteKey()?.nextEndDate)
    }

    @Test
    fun networkFailureIsReportedAsMediatorError() = runTest {
        api.rangeError = IOException()

        val result = mediator().load(LoadType.REFRESH, emptyPagingState)

        assertTrue(result is MediatorResult.Error)
    }

    @Test
    fun theOldestPageIsEvictedOnceTheCacheExceedsTwentyPages() = runTest {
        val remoteMediator = mediator()
        remoteMediator.load(LoadType.REFRESH, emptyPagingState)
        repeat(20) { remoteMediator.load(LoadType.APPEND, emptyPagingState) }

        val remoteKey = database.neoArchiveRemoteKeyDao().getRemoteKey()
        assertEquals(1, remoteKey?.oldestCachedPage)
        assertEquals(20, remoteKey?.newestCachedPage)
        assertEquals(21, requestedEndDates.size)
    }
}
