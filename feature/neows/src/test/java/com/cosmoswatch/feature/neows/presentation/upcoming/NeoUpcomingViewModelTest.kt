package com.cosmoswatch.feature.neows.presentation.upcoming

import app.cash.turbine.test
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.core.testing.MainDispatcherExtension
import com.cosmoswatch.feature.neows.domain.NeoDomain
import com.cosmoswatch.feature.neows.domain.NeoUpcomingFilter
import com.cosmoswatch.feature.neows.domain.NeoWsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val NOW: Instant = Instant.parse("2026-09-03T12:00:00Z")
private val FIXED_CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)
private val TODAY: LocalDate = LocalDate.now(FIXED_CLOCK)

private fun sampleNeo(id: String, hazardous: Boolean) = NeoDomain(
    id = id,
    name = "Asteroid $id",
    closeApproachDate = TODAY,
    isPotentiallyHazardous = hazardous,
    isSentryObject = false,
    missDistanceLunar = 5.0,
    relativeVelocityKmh = 40000.0,
    estimatedDiameterMinMeters = 10.0,
    estimatedDiameterMaxMeters = 20.0,
)

class NeoUpcomingViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherExtension = MainDispatcherExtension()
    }

    private val hazardousNeo = sampleNeo("1", hazardous = true)
    private val safeNeo = sampleNeo("2", hazardous = false)

    private fun viewModel(repository: NeoWsRepository) = NeoUpcomingViewModel(repository = repository, clock = FIXED_CLOCK)

    @Test
    fun `state starts with Loading then reflects Success`() = runTest {
        val repository = mockk<NeoWsRepository>()
        every { repository.getUpcoming() } returns flow { emit(AppResult.Success(listOf(hazardousNeo, safeNeo))) }

        viewModel(repository).state.test {
            assertEquals(NeoUpcomingState.Loading, awaitItem())
            assertEquals(
                NeoUpcomingState.Success(neos = listOf(hazardousNeo, safeNeo), today = TODAY, filter = NeoUpcomingFilter()),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state reflects Failure as Error`() = runTest {
        val repository = mockk<NeoWsRepository>()
        every { repository.getUpcoming() } returns flow { emit(AppResult.Failure(AppError.Network)) }

        viewModel(repository).state.test {
            assertEquals(NeoUpcomingState.Loading, awaitItem())
            assertEquals(NeoUpcomingState.Error(AppError.Network), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry intent resubscribes to the repository`() = runTest {
        val repository = mockk<NeoWsRepository>()
        var callCount = 0
        every { repository.getUpcoming() } answers {
            callCount++
            flow { emit(AppResult.Success(listOf(hazardousNeo))) }
        }
        val viewModel = viewModel(repository)

        viewModel.state.test {
            awaitItem()
            awaitItem()
            viewModel.onIntent(NeoUpcomingIntent.Retry)
            awaitItem()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2, callCount)
    }

    @Test
    fun `FilterChanged intent filters the list without re-fetching`() = runTest {
        val repository = mockk<NeoWsRepository>()
        every { repository.getUpcoming() } returns flow { emit(AppResult.Success(listOf(hazardousNeo, safeNeo))) }
        val viewModel = viewModel(repository)

        viewModel.state.test {
            awaitItem()
            awaitItem()
            viewModel.onIntent(NeoUpcomingIntent.FilterChanged(NeoUpcomingFilter(hazardousOnly = true)))
            assertEquals(
                NeoUpcomingState.Success(
                    neos = listOf(hazardousNeo),
                    today = TODAY,
                    filter = NeoUpcomingFilter(hazardousOnly = true),
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ClearFilter intent resets the filter back to default`() = runTest {
        val repository = mockk<NeoWsRepository>()
        every { repository.getUpcoming() } returns flow { emit(AppResult.Success(listOf(hazardousNeo, safeNeo))) }
        val viewModel = viewModel(repository)

        viewModel.state.test {
            awaitItem()
            awaitItem()
            viewModel.onIntent(NeoUpcomingIntent.FilterChanged(NeoUpcomingFilter(hazardousOnly = true)))
            awaitItem()
            viewModel.onIntent(NeoUpcomingIntent.ClearFilter)
            assertEquals(
                NeoUpcomingState.Success(neos = listOf(hazardousNeo, safeNeo), today = TODAY, filter = NeoUpcomingFilter()),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
