package com.cosmoswatch.feature.apod.data.repository

import app.cash.turbine.test
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.apod.data.local.ApodEntity
import com.cosmoswatch.feature.apod.data.local.FakeApodDao
import com.cosmoswatch.feature.apod.data.mapper.toDomain
import com.cosmoswatch.feature.apod.data.mapper.toEntity
import com.cosmoswatch.feature.apod.data.remote.ApodApi
import com.cosmoswatch.feature.apod.data.remote.ApodDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

private val NOW: Instant = Instant.parse("2026-09-03T12:00:00Z")
private val FIXED_CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)

private val SAMPLE_DTO = ApodDto(
    date = "2026-09-03",
    title = "Title",
    explanation = "Explanation",
    url = "https://example.com/image.jpg",
    hdurl = null,
    mediaType = "image",
    copyright = null,
)

private fun sampleEntity(fetchedAt: Instant) = ApodEntity(
    date = "2026-09-02",
    title = "Cached title",
    explanation = "Cached explanation",
    imageUrl = "https://example.com/cached.jpg",
    hdImageUrl = null,
    mediaType = "image",
    copyright = null,
    fetchedAtEpochMillis = fetchedAt.toEpochMilli(),
)

class ApodRepositoryImplTest {

    private val api = mockk<ApodApi>()

    private fun repository(dao: FakeApodDao) = ApodRepositoryImpl(api = api, dao = dao, clock = FIXED_CLOCK)

    @Test
    fun `no cache and successful fetch emits Success and stores in dao`() = runTest {
        coEvery { api.getApod() } returns SAMPLE_DTO
        val dao = FakeApodDao()

        repository(dao).getApod().test {
            assertEquals(AppResult.Success(SAMPLE_DTO.toEntity(NOW.toEpochMilli()).toDomain()), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fresh cache within TTL is served without hitting the network`() = runTest {
        val dao = FakeApodDao(initialEntity = sampleEntity(fetchedAt = NOW.minus(Duration.ofHours(1))))

        repository(dao).getApod().test {
            assertEquals(
                AppResult.Success(sampleEntity(NOW.minus(Duration.ofHours(1))).toDomain()),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { api.getApod() }
    }

    @Test
    fun `cache older than TTL triggers refresh and emits fresh data`() = runTest {
        coEvery { api.getApod() } returns SAMPLE_DTO
        val dao = FakeApodDao(initialEntity = sampleEntity(fetchedAt = NOW.minus(Duration.ofHours(13))))

        repository(dao).getApod().test {
            assertEquals(AppResult.Success(SAMPLE_DTO.toEntity(NOW.toEpochMilli()).toDomain()), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stale cache with failed refresh falls back to cached data`() = runTest {
        coEvery { api.getApod() } throws IOException()
        val staleFetchedAt = NOW.minus(Duration.ofHours(13))
        val dao = FakeApodDao(initialEntity = sampleEntity(fetchedAt = staleFetchedAt))

        repository(dao).getApod().test {
            assertEquals(AppResult.Success(sampleEntity(staleFetchedAt).toDomain()), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `no cache and network failure emits Network failure`() = runTest {
        coEvery { api.getApod() } throws IOException()
        val dao = FakeApodDao()

        repository(dao).getApod().test {
            assertEquals(AppResult.Failure(AppError.Network), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `no cache and server error emits Server failure without retrying`() = runTest {
        val httpException = HttpException(Response.error<Any>(500, "".toResponseBody(null)))
        coEvery { api.getApod() } throws httpException
        val dao = FakeApodDao()

        repository(dao).getApod().test {
            assertEquals(AppResult.Failure(AppError.Server(500)), awaitItem())
            awaitComplete()
        }
        coVerify(exactly = 1) { api.getApod() }
    }

    @Test
    fun `transient network failures are retried before succeeding`() = runTest {
        coEvery { api.getApod() } throws IOException() andThenThrows IOException() andThen SAMPLE_DTO
        val dao = FakeApodDao()

        repository(dao).getApod().test {
            assertEquals(AppResult.Success(SAMPLE_DTO.toEntity(NOW.toEpochMilli()).toDomain()), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 3) { api.getApod() }
    }
}
