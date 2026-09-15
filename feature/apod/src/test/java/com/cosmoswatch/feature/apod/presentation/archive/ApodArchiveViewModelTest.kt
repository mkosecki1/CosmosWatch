package com.cosmoswatch.feature.apod.presentation.archive

import androidx.paging.PagingData
import app.cash.turbine.test
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.core.testing.MainDispatcherExtension
import com.cosmoswatch.feature.apod.domain.ApodArchiveFilter
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import com.cosmoswatch.feature.apod.domain.usecase.GetApodArchiveUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.LocalDate

class ApodArchiveViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherExtension = MainDispatcherExtension()
    }

    private val useCase = mockk<GetApodArchiveUseCase>()

    @Test
    fun `initial state uses an empty filter and no error`() = runTest {
        every { useCase(ApodArchiveFilter()) } returns AppResult.Success(flowOf(PagingData.empty()))
        val viewModel = ApodArchiveViewModel(useCase)

        viewModel.state.test {
            assertEquals(ApodArchiveState(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `FilterChanged intent updates the filter and clears a previous error`() = runTest {
        val validFilter = ApodArchiveFilter(mediaType = ApodMediaType.VIDEO)
        every { useCase(ApodArchiveFilter()) } returns AppResult.Success(flowOf(PagingData.empty()))
        every { useCase(validFilter) } returns AppResult.Success(flowOf(PagingData.empty()))
        val viewModel = ApodArchiveViewModel(useCase)

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(ApodArchiveIntent.FilterChanged(validFilter))
            assertEquals(ApodArchiveState(filter = validFilter), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invalid filter surfaces the validation error without touching the paging flow`() = runTest {
        val invalidFilter = ApodArchiveFilter(startDate = LocalDate.of(1990, 1, 1))
        every { useCase(ApodArchiveFilter()) } returns AppResult.Success(flowOf(PagingData.empty()))
        every { useCase(invalidFilter) } returns AppResult.Failure(AppError.Validation("start_date_before_archive"))
        val viewModel = ApodArchiveViewModel(useCase)

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(ApodArchiveIntent.FilterChanged(invalidFilter))
            val state = awaitItem()
            assertEquals(invalidFilter, state.filter)
            assertEquals(AppError.Validation("start_date_before_archive"), state.filterError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ClearFilter intent resets the filter back to default`() = runTest {
        val someFilter = ApodArchiveFilter(mediaType = ApodMediaType.IMAGE)
        every { useCase(ApodArchiveFilter()) } returns AppResult.Success(flowOf(PagingData.empty()))
        every { useCase(someFilter) } returns AppResult.Success(flowOf(PagingData.empty()))
        val viewModel = ApodArchiveViewModel(useCase)

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(ApodArchiveIntent.FilterChanged(someFilter))
            awaitItem()
            viewModel.onIntent(ApodArchiveIntent.ClearFilter)
            val state = awaitItem()
            assertEquals(ApodArchiveFilter(), state.filter)
            assertNull(state.filterError)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
