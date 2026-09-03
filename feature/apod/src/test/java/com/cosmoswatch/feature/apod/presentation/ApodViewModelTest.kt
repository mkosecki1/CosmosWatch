package com.cosmoswatch.feature.apod.presentation

import app.cash.turbine.test
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.core.testing.MainDispatcherExtension
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import com.cosmoswatch.feature.apod.domain.ApodRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.LocalDate

class ApodViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherExtension = MainDispatcherExtension()
    }

    private val sampleApod = ApodDomain(
        date = LocalDate.of(2026, 9, 3),
        title = "Title",
        explanation = "Explanation",
        imageUrl = "https://example.com/image.jpg",
        hdImageUrl = null,
        mediaType = ApodMediaType.IMAGE,
        copyright = null,
    )

    @Test
    fun `state starts with Loading then reflects Success`() = runTest {
        val repository = mockk<ApodRepository>()
        every { repository.getApod() } returns flow { emit(AppResult.Success(sampleApod)) }
        val viewModel = ApodViewModel(repository)

        viewModel.state.test {
            assertEquals(ApodState.Loading, awaitItem())
            assertEquals(ApodState.Success(sampleApod), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state reflects Failure as Error`() = runTest {
        val repository = mockk<ApodRepository>()
        every { repository.getApod() } returns flow { emit(AppResult.Failure(AppError.Network)) }
        val viewModel = ApodViewModel(repository)

        viewModel.state.test {
            assertEquals(ApodState.Loading, awaitItem())
            assertEquals(ApodState.Error(AppError.Network), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry intent resubscribes to the repository`() = runTest {
        val repository = mockk<ApodRepository>()
        var callCount = 0
        every { repository.getApod() } answers {
            callCount++
            flow { emit(AppResult.Success(sampleApod)) }
        }
        val viewModel = ApodViewModel(repository)

        viewModel.state.test {
            awaitItem()
            awaitItem()
            viewModel.onIntent(ApodIntent.Retry)
            awaitItem()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2, callCount)
    }
}
