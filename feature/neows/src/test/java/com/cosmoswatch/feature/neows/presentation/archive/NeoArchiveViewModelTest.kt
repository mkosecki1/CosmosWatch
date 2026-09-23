package com.cosmoswatch.feature.neows.presentation.archive

import androidx.paging.PagingData
import app.cash.turbine.test
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.core.testing.MainDispatcherExtension
import com.cosmoswatch.feature.neows.domain.NeoArchiveFilter
import com.cosmoswatch.feature.neows.domain.usecase.GetNeoArchiveUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val NOW: Instant = Instant.parse("2026-09-03T12:00:00Z")
private val FIXED_CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)
private val TODAY: LocalDate = LocalDate.now(FIXED_CLOCK)

class NeoArchiveViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherExtension = MainDispatcherExtension()
    }

    private val useCase = mockk<GetNeoArchiveUseCase>()

    private fun viewModel() = NeoArchiveViewModel(getNeoArchiveUseCase = useCase, clock = FIXED_CLOCK)

    @Test
    fun `initial state uses an empty filter, today and no error`() = runTest {
        every { useCase(NeoArchiveFilter()) } returns AppResult.Success(flowOf(PagingData.empty()))

        viewModel().state.test {
            assertEquals(NeoArchiveState(filter = NeoArchiveFilter(), today = TODAY), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `FilterChanged intent updates the filter and clears a previous error`() = runTest {
        val validFilter = NeoArchiveFilter(hazardousOnly = true)
        every { useCase(NeoArchiveFilter()) } returns AppResult.Success(flowOf(PagingData.empty()))
        every { useCase(validFilter) } returns AppResult.Success(flowOf(PagingData.empty()))
        val viewModel = viewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(NeoArchiveIntent.FilterChanged(validFilter))
            assertEquals(NeoArchiveState(filter = validFilter, today = TODAY), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invalid filter surfaces the validation error without touching the paging flow`() = runTest {
        val invalidFilter = NeoArchiveFilter(endDate = TODAY.plusDays(1))
        every { useCase(NeoArchiveFilter()) } returns AppResult.Success(flowOf(PagingData.empty()))
        every { useCase(invalidFilter) } returns AppResult.Failure(AppError.Validation("end_date_not_before_today"))
        val viewModel = viewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(NeoArchiveIntent.FilterChanged(invalidFilter))
            val state = awaitItem()
            assertEquals(invalidFilter, state.filter)
            assertEquals(AppError.Validation("end_date_not_before_today"), state.filterError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ClearFilter intent resets the filter back to default`() = runTest {
        val someFilter = NeoArchiveFilter(sentryOnly = true)
        every { useCase(NeoArchiveFilter()) } returns AppResult.Success(flowOf(PagingData.empty()))
        every { useCase(someFilter) } returns AppResult.Success(flowOf(PagingData.empty()))
        val viewModel = viewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(NeoArchiveIntent.FilterChanged(someFilter))
            awaitItem()
            viewModel.onIntent(NeoArchiveIntent.ClearFilter)
            val state = awaitItem()
            assertEquals(NeoArchiveFilter(), state.filter)
            assertNull(state.filterError)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
