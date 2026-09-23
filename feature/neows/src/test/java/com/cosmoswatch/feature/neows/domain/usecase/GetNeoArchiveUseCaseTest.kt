package com.cosmoswatch.feature.neows.domain.usecase

import androidx.paging.PagingData
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.neows.domain.NeoArchiveFilter
import com.cosmoswatch.feature.neows.domain.NeoDomain
import com.cosmoswatch.feature.neows.domain.NeoWsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val NOW: Instant = Instant.parse("2026-09-03T12:00:00Z")
private val FIXED_CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)

class GetNeoArchiveUseCaseTest {

    private val repository = mockk<NeoWsRepository>()
    private val useCase = GetNeoArchiveUseCase(repository = repository, clock = FIXED_CLOCK)

    @Test
    fun `valid filter delegates to repository`() {
        val filter = NeoArchiveFilter(hazardousOnly = true)
        val pagingFlow = flowOf(PagingData.empty<NeoDomain>())
        every { repository.getArchive(filter) } returns pagingFlow

        val result = useCase(filter)

        assertEquals(AppResult.Success(pagingFlow), result)
        verify(exactly = 1) { repository.getArchive(filter) }
    }

    @Test
    fun `end date in the future is rejected`() {
        val filter = NeoArchiveFilter(endDate = LocalDate.now(FIXED_CLOCK).plusDays(1))

        val result = useCase(filter)

        assertEquals(AppResult.Failure(AppError.Validation("end_date_not_before_today")), result)
        verify(exactly = 0) { repository.getArchive(any()) }
    }

    @Test
    fun `end date equal to today is rejected because Upcoming already covers today`() {
        val filter = NeoArchiveFilter(endDate = LocalDate.now(FIXED_CLOCK))

        val result = useCase(filter)

        assertEquals(AppResult.Failure(AppError.Validation("end_date_not_before_today")), result)
        verify(exactly = 0) { repository.getArchive(any()) }
    }

    @Test
    fun `start date after end date is rejected`() {
        val filter = NeoArchiveFilter(
            startDate = LocalDate.of(2026, 8, 20),
            endDate = LocalDate.of(2026, 8, 10),
        )

        val result = useCase(filter)

        assertEquals(AppResult.Failure(AppError.Validation("start_date_after_end_date")), result)
    }
}
