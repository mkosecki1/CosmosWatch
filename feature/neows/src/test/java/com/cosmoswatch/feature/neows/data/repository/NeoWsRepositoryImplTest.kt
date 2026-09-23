package com.cosmoswatch.feature.neows.data.repository

import androidx.room.withTransaction
import app.cash.turbine.test
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.neows.data.local.FakeNeoUpcomingDao
import com.cosmoswatch.feature.neows.data.local.NeoUpcomingEntity
import com.cosmoswatch.feature.neows.data.local.NeoWsDatabase
import com.cosmoswatch.feature.neows.data.mapper.toDomain
import com.cosmoswatch.feature.neows.data.remote.CloseApproachDto
import com.cosmoswatch.feature.neows.data.remote.DiameterRangeDto
import com.cosmoswatch.feature.neows.data.remote.EstimatedDiameterDto
import com.cosmoswatch.feature.neows.data.remote.MissDistanceDto
import com.cosmoswatch.feature.neows.data.remote.NeoDto
import com.cosmoswatch.feature.neows.data.remote.NeoWsApi
import com.cosmoswatch.feature.neows.data.remote.NeoWsFeedResponse
import com.cosmoswatch.feature.neows.data.remote.RelativeVelocityDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
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

private const val WINDOW_1_START = "2026-09-03"
private const val WINDOW_1_END = "2026-09-09"
private const val WINDOW_2_START = "2026-09-10"
private const val WINDOW_2_END = "2026-09-16"

private fun sampleDto(id: String, closeApproachDate: String) = NeoDto(
    id = id,
    name = "Asteroid $id",
    isPotentiallyHazardousAsteroid = false,
    isSentryObject = false,
    estimatedDiameter = EstimatedDiameterDto(meters = DiameterRangeDto(estimatedDiameterMin = 10.0, estimatedDiameterMax = 20.0)),
    closeApproachData = listOf(
        CloseApproachDto(
            closeApproachDate = closeApproachDate,
            relativeVelocity = RelativeVelocityDto(kilometersPerHour = "40000"),
            missDistance = MissDistanceDto(lunar = "5.0"),
        ),
    ),
)

private fun feedResponse(id: String, date: String) =
    NeoWsFeedResponse(nearEarthObjects = mapOf(date to listOf(sampleDto(id, date))))

private fun sampleEntity(id: String, closeApproachDate: String, fetchedAt: Instant) = NeoUpcomingEntity(
    entryId = "$id-$closeApproachDate",
    id = id,
    name = "Asteroid $id",
    closeApproachDate = closeApproachDate,
    isPotentiallyHazardous = false,
    isSentryObject = false,
    missDistanceLunar = 5.0,
    relativeVelocityKmh = 40000.0,
    estimatedDiameterMinMeters = 10.0,
    estimatedDiameterMaxMeters = 20.0,
    fetchedAtEpochMillis = fetchedAt.toEpochMilli(),
)

class NeoWsRepositoryImplTest {

    private val api = mockk<NeoWsApi>()
    private val database = mockk<NeoWsDatabase>(relaxed = true)

    init {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { database.withTransaction(any<suspend () -> Any?>()) } coAnswers { secondArg<suspend () -> Any?>().invoke() }
    }

    private fun repository(dao: FakeNeoUpcomingDao) =
        NeoWsRepositoryImpl(api = api, dao = dao, database = database, clock = FIXED_CLOCK)

    private fun mockBothWindowsSuccess() {
        coEvery { api.getFeed(WINDOW_1_START, WINDOW_1_END) } returns feedResponse("1", WINDOW_1_START)
        coEvery { api.getFeed(WINDOW_2_START, WINDOW_2_END) } returns feedResponse("2", WINDOW_2_START)
    }

    @Test
    fun `no cache and successful fetch chunks the 14-day window into two requests and stores the merge`() = runTest {
        mockBothWindowsSuccess()
        val dao = FakeNeoUpcomingDao()

        repository(dao).getUpcoming().test {
            val expected = listOf(
                sampleEntity("1", WINDOW_1_START, NOW).toDomain(),
                sampleEntity("2", WINDOW_2_START, NOW).toDomain(),
            )
            assertEquals(AppResult.Success(expected), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { api.getFeed(WINDOW_1_START, WINDOW_1_END) }
        coVerify(exactly = 1) { api.getFeed(WINDOW_2_START, WINDOW_2_END) }
    }

    @Test
    fun `fresh cache within TTL is served without hitting the network`() = runTest {
        val fetchedAt = NOW.minus(Duration.ofHours(1))
        val dao = FakeNeoUpcomingDao(initialEntities = listOf(sampleEntity("1", WINDOW_1_START, fetchedAt)))

        repository(dao).getUpcoming().test {
            assertEquals(AppResult.Success(listOf(sampleEntity("1", WINDOW_1_START, fetchedAt).toDomain())), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { api.getFeed(any(), any()) }
    }

    @Test
    fun `cache older than TTL triggers refresh and emits fresh data`() = runTest {
        mockBothWindowsSuccess()
        val staleFetchedAt = NOW.minus(Duration.ofHours(13))
        val dao = FakeNeoUpcomingDao(initialEntities = listOf(sampleEntity("old", WINDOW_1_START, staleFetchedAt)))

        repository(dao).getUpcoming().test {
            val expected = listOf(
                sampleEntity("1", WINDOW_1_START, NOW).toDomain(),
                sampleEntity("2", WINDOW_2_START, NOW).toDomain(),
            )
            assertEquals(AppResult.Success(expected), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stale cache with failed refresh falls back to cached data`() = runTest {
        coEvery { api.getFeed(any(), any()) } throws IOException()
        val staleFetchedAt = NOW.minus(Duration.ofHours(13))
        val dao = FakeNeoUpcomingDao(initialEntities = listOf(sampleEntity("old", WINDOW_1_START, staleFetchedAt)))

        repository(dao).getUpcoming().test {
            assertEquals(
                AppResult.Success(listOf(sampleEntity("old", WINDOW_1_START, staleFetchedAt).toDomain())),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `no cache and network failure emits Network failure`() = runTest {
        coEvery { api.getFeed(any(), any()) } throws IOException()
        val dao = FakeNeoUpcomingDao()

        repository(dao).getUpcoming().test {
            assertEquals(AppResult.Failure(AppError.Network), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `no cache and server error emits Server failure without retrying`() = runTest {
        val httpException = HttpException(Response.error<Any>(500, "".toResponseBody(null)))
        coEvery { api.getFeed(any(), any()) } throws httpException
        val dao = FakeNeoUpcomingDao()

        repository(dao).getUpcoming().test {
            assertEquals(AppResult.Failure(AppError.Server(500)), awaitItem())
            awaitComplete()
        }
        coVerify(exactly = 1) { api.getFeed(any(), any()) }
    }

    @Test
    fun `transient network failures on a window are retried before succeeding`() = runTest {
        coEvery { api.getFeed(WINDOW_1_START, WINDOW_1_END) } throws IOException() andThenThrows
            IOException() andThen feedResponse("1", WINDOW_1_START)
        coEvery { api.getFeed(WINDOW_2_START, WINDOW_2_END) } returns feedResponse("2", WINDOW_2_START)
        val dao = FakeNeoUpcomingDao()

        repository(dao).getUpcoming().test {
            val expected = listOf(
                sampleEntity("1", WINDOW_1_START, NOW).toDomain(),
                sampleEntity("2", WINDOW_2_START, NOW).toDomain(),
            )
            assertEquals(AppResult.Success(expected), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 3) { api.getFeed(WINDOW_1_START, WINDOW_1_END) }
    }
}
