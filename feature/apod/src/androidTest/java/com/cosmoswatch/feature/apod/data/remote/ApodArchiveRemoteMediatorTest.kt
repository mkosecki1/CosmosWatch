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
    private val today: LocalDate = LocalDate.now(clock)
    private val requestedPages = mutableListOf<Int>()
    private val api = FakeApodApi { page, perPage ->
        requestedPages += page
        val newestIndex = (page - 1) * perPage
        (newestIndex until newestIndex + perPage).map { index -> sampleDto(today.minusDays(index.toLong())) }
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

    private fun sampleDto(date: LocalDate) = ApodDto(
        date = date.toString(),
        title = "Title $date",
        explanation = "Explanation",
        url = "https://example.com/$date",
        hdurl = "https://example.com/$date.jpg",
        mediaType = "image",
        copyright = null,
    )

    private fun mediator(filter: ApodArchiveFilter = ApodArchiveFilter()) =
        ApodArchiveRemoteMediator(api = api, database = database, clock = clock, filter = filter)

    private suspend fun cachedEntry(date: LocalDate) = database.apodArchiveDao().observe(date.toString()).first()

    private fun assertSuccess(expectedEndOfPagination: Boolean, result: MediatorResult) {
        assertTrue("expected MediatorResult.Success but was $result", result is MediatorResult.Success)
        assertEquals(expectedEndOfPagination, (result as MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refresh_storesTheNewestRemotePageAsCachePageZero() = runTest {
        val result = mediator().load(LoadType.REFRESH, emptyPagingState)

        assertSuccess(expectedEndOfPagination = false, result = result)
        assertEquals(listOf(1), requestedPages)
        val remoteKey = database.apodArchiveRemoteKeyDao().getRemoteKey()
        assertEquals(0, remoteKey?.oldestCachedPage)
        assertEquals(0, remoteKey?.newestCachedPage)
        assertEquals(2, remoteKey?.nextRemotePage)
    }

    @Test
    fun append_continuesFromTheRemoteKeysNextRemotePage() = runTest {
        val remoteMediator = mediator()
        remoteMediator.load(LoadType.REFRESH, emptyPagingState)

        val result = remoteMediator.load(LoadType.APPEND, emptyPagingState)

        assertSuccess(expectedEndOfPagination = false, result = result)
        assertEquals(listOf(1, 2), requestedPages)
        val remoteKey = database.apodArchiveRemoteKeyDao().getRemoteKey()
        assertEquals(0, remoteKey?.oldestCachedPage)
        assertEquals(1, remoteKey?.newestCachedPage)
        assertEquals(3, remoteKey?.nextRemotePage)
    }

    @Test
    fun refresh_withEndDateStartsOnePageEarlyAndSkipsNewerEntries() = runTest {
        val endDate = today.minusDays(60)

        mediator(ApodArchiveFilter(endDate = endDate)).load(LoadType.REFRESH, emptyPagingState)

        assertEquals(listOf(2, 3), requestedPages)
        assertNull(cachedEntry(endDate.plusDays(1)))
        assertNotNull(cachedEntry(endDate))
    }

    @Test
    fun refresh_keepsFetchingWhenAWholePageIsNewerThanTheEndDate() = runTest {
        val endDate = today.minusDays(30)

        mediator(ApodArchiveFilter(endDate = endDate)).load(LoadType.REFRESH, emptyPagingState)

        assertEquals(listOf(1, 2), requestedPages)
        assertNotNull(cachedEntry(endDate))
    }

    @Test
    fun endOfPaginationIsReachedOnceAPageHitsTheStartDate() = runTest {
        val startDate = today.minusDays(14)

        val result = mediator(ApodArchiveFilter(startDate = startDate)).load(LoadType.REFRESH, emptyPagingState)

        assertSuccess(expectedEndOfPagination = true, result = result)
        assertNull(database.apodArchiveRemoteKeyDao().getRemoteKey()?.nextRemotePage)
        assertNotNull(cachedEntry(startDate))
        assertNull(cachedEntry(startDate.minusDays(1)))
    }

    @Test
    fun endOfPaginationIsReachedOnAShortLastPage() = runTest {
        val shortPageApi = FakeApodApi { _, _ -> listOf(sampleDto(today)) }
        val remoteMediator = ApodArchiveRemoteMediator(shortPageApi, database, clock, ApodArchiveFilter())

        assertSuccess(expectedEndOfPagination = true, result = remoteMediator.load(LoadType.REFRESH, emptyPagingState))
    }

    @Test
    fun networkFailureIsReportedAsMediatorError() = runTest {
        api.pageError = IOException()

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
        assertEquals(21, requestedPages.size)
        assertNull(cachedEntry(today))
        assertNotNull(cachedEntry(today.minusDays(20L * 25)))
    }
}
