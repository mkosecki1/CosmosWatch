package com.cosmoswatch.feature.apod.domain.usecase

import androidx.paging.PagingData
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.apod.domain.APOD_ARCHIVE_START_DATE
import com.cosmoswatch.feature.apod.domain.ApodArchiveFilter
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val NOW: Instant = Instant.parse("2026-09-03T12:00:00Z")
private val FIXED_CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)

class GetApodArchiveUseCaseTest {

    private val repository = mockk<ApodRepository>()
    private val useCase = GetApodArchiveUseCase(repository = repository, clock = FIXED_CLOCK)

    @Test
    fun `valid filter delegates to repository`() {
        val filter = ApodArchiveFilter(startDate = APOD_ARCHIVE_START_DATE)
        val pagingFlow = flowOf(PagingData.empty<ApodDomain>())
        every { repository.getArchive(filter) } returns pagingFlow

        val result = useCase(filter)

        assertEquals(AppResult.Success(pagingFlow), result)
        verify(exactly = 1) { repository.getArchive(filter) }
    }

    @Test
    fun `start date before archive start is rejected`() {
        val filter = ApodArchiveFilter(startDate = APOD_ARCHIVE_START_DATE.minusDays(1))

        val result = useCase(filter)

        assertEquals(AppResult.Failure(AppError.Validation("start_date_before_archive")), result)
        verify(exactly = 0) { repository.getArchive(any()) }
    }

    @Test
    fun `end date in the future is rejected`() {
        val filter = ApodArchiveFilter(endDate = LocalDate.now(FIXED_CLOCK).plusDays(1))

        val result = useCase(filter)

        assertEquals(AppResult.Failure(AppError.Validation("end_date_in_future")), result)
    }

    @Test
    fun `end date equal to today is accepted`() {
        val filter = ApodArchiveFilter(endDate = LocalDate.now(FIXED_CLOCK))
        every { repository.getArchive(filter) } returns flowOf(PagingData.empty())

        val result = useCase(filter)

        assertTrue(result is AppResult.Success)
    }

    @Test
    fun `start date after end date is rejected`() {
        val filter = ApodArchiveFilter(
            startDate = LocalDate.of(2020, 1, 10),
            endDate = LocalDate.of(2020, 1, 1),
        )

        val result = useCase(filter)

        assertEquals(AppResult.Failure(AppError.Validation("start_date_after_end_date")), result)
    }
}
