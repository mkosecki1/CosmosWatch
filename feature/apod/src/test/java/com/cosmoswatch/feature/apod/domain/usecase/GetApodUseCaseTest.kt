package com.cosmoswatch.feature.apod.domain.usecase

import app.cash.turbine.test
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import com.cosmoswatch.feature.apod.domain.ApodRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class GetApodUseCaseTest {

    private val repository = mockk<ApodRepository>()
    private val useCase = GetApodUseCase(repository)

    @Test
    fun `invoke delegates to repository getApod`() = runTest {
        val apod = ApodDomain(
            date = LocalDate.of(2026, 9, 3),
            title = "Title",
            explanation = "Explanation",
            imageUrl = "https://example.com/image.jpg",
            hdImageUrl = null,
            mediaType = ApodMediaType.IMAGE,
            copyright = null,
        )
        every { repository.getApod() } returns flowOf(AppResult.Success(apod))

        useCase().test {
            assertEquals(AppResult.Success(apod), awaitItem())
            awaitComplete()
        }
    }
}
