package com.cosmoswatch.feature.apod.presentation.archivedetail

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import com.cosmoswatch.feature.apod.domain.ApodRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ApodArchiveDetailViewModelTest {

    private val repository = mockk<ApodRepository>()

    private val sampleApod = ApodDomain(
        date = LocalDate.of(2026, 9, 3),
        title = "Title",
        explanation = "Explanation",
        imageUrl = "https://example.com/image.jpg",
        hdImageUrl = null,
        mediaType = ApodMediaType.IMAGE,
        copyright = null,
        thumbnailUrl = null,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): ApodArchiveDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("date" to "2026-09-03"))
        return ApodArchiveDetailViewModel(repository = repository, savedStateHandle = savedStateHandle)
    }

    @Test
    fun state_startsWithLoadingThenReflectsTheObservedEntry() = runTest {
        every { repository.observeArchiveEntry(LocalDate.of(2026, 9, 3)) } returns flowOf(sampleApod)

        viewModel().state.test {
            assertEquals(ApodArchiveDetailState.Loading, awaitItem())
            assertEquals(ApodArchiveDetailState.Content(sampleApod), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun state_missingEntryMapsToNotAvailable() = runTest {
        every { repository.observeArchiveEntry(LocalDate.of(2026, 9, 3)) } returns flowOf(null)

        viewModel().state.test {
            assertEquals(ApodArchiveDetailState.Loading, awaitItem())
            assertEquals(ApodArchiveDetailState.NotAvailable, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
