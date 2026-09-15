package com.cosmoswatch.feature.apod.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.MediatorResult
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cosmoswatch.feature.apod.data.local.ApodArchiveEntity
import com.cosmoswatch.feature.apod.data.local.ApodDatabase
import com.cosmoswatch.feature.apod.domain.ApodArchiveFilter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
class ApodArchiveRemoteMediatorTest {

    private val clock: Clock = Clock.fixed(Instant.parse("2026-09-03T12:00:00Z"), ZoneOffset.UTC)
    private val requestedEndDates = mutableListOf<String>()
    private val api = FakeApodApi { _, endDate ->
        requestedEndDates += endDate
        listOf(sampleDto(date = endDate))
    }
    private lateinit var database: ApodDatabase

    private val emptyPagingState = PagingState<Int, ApodArchiveEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20),
        leadingPlaceholderCount = 0,
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), ApodDatabase::class.java)
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun sampleDto(date: String) = ApodDto(
        date = date,
        title = "Title $date",
        explanation = "Explanation",
        url = "https://example.com/$date.jpg",
        hdurl = null,
        mediaType = "image",
        copyright = null,
    )

    private fun mediator(filter: ApodArchiveFilter = ApodArchiveFilter()) =
        ApodArchiveRemoteMediator(api = api, database = database, clock = clock, filter = filter)

    private fun assertSuccess(expectedEndOfPagination: Boolean, result: MediatorResult) {
        assertTrue("expected MediatorResult.Success but was $result", result is MediatorResult.Success)
        assertEquals(expectedEndOfPagination, (result as MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refresh_storesTheNewestWindowAsPageZero() = runTest {
        val result = mediator().load(LoadType.REFRESH, emptyPagingState)

        assertSuccess(expectedEndOfPagination = false, result = result)
        val remoteKey = database.apodArchiveRemoteKeyDao().getRemoteKey()
        assertEquals(0, remoteKey?.oldestCachedPage)
        assertEquals(0, remoteKey?.newestCachedPage)
        assertEquals(LocalDate.now(clock).minusDays(30).toString(), remoteKey?.nextEndDate)
    }

    @Test
    fun append_continuesFromTheRemoteKeysNextEndDate() = runTest {
        val remoteMediator = mediator()
        remoteMediator.load(LoadType.REFRESH, emptyPagingState)
        val nextEndDateAfterRefresh = database.apodArchiveRemoteKeyDao().getRemoteKey()?.nextEndDate

        val result = remoteMediator.load(LoadType.APPEND, emptyPagingState)

        assertSuccess(expectedEndOfPagination = false, result = result)
        assertEquals(nextEndDateAfterRefresh, requestedEndDates[1])
        val remoteKey = database.apodArchiveRemoteKeyDao().getRemoteKey()
        assertEquals(0, remoteKey?.oldestCachedPage)
        assertEquals(1, remoteKey?.newestCachedPage)
    }

    @Test
    fun endOfPaginationIsReachedOnceTheRangeHitsTheArchiveBoundary() = runTest {
        val filter = ApodArchiveFilter(startDate = LocalDate.now(clock).minusDays(14))

        val result = mediator(filter).load(LoadType.REFRESH, emptyPagingState)

        assertSuccess(expectedEndOfPagination = true, result = result)
        assertNull(database.apodArchiveRemoteKeyDao().getRemoteKey()?.nextEndDate)
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

        val remoteKey = database.apodArchiveRemoteKeyDao().getRemoteKey()
        assertEquals(1, remoteKey?.oldestCachedPage)
        assertEquals(20, remoteKey?.newestCachedPage)
        assertEquals(21, requestedEndDates.size)

        val evictedPageEntry = database.apodArchiveDao().observe(requestedEndDates[0]).first()
        val survivingPageEntry = database.apodArchiveDao().observe(requestedEndDates[20]).first()
        assertNull(evictedPageEntry)
        assertNotNull(survivingPageEntry)
    }
}
