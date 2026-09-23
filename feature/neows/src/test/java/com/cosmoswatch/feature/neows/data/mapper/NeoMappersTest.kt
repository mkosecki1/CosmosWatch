package com.cosmoswatch.feature.neows.data.mapper

import com.cosmoswatch.feature.neows.data.remote.CloseApproachDto
import com.cosmoswatch.feature.neows.data.remote.DiameterRangeDto
import com.cosmoswatch.feature.neows.data.remote.EstimatedDiameterDto
import com.cosmoswatch.feature.neows.data.remote.MissDistanceDto
import com.cosmoswatch.feature.neows.data.remote.NeoDto
import com.cosmoswatch.feature.neows.data.remote.NeoWsFeedResponse
import com.cosmoswatch.feature.neows.data.remote.RelativeVelocityDto
import com.cosmoswatch.feature.neows.domain.NeoDomain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

private val SAMPLE_DTO = NeoDto(
    id = "2000433",
    name = "433 Eros",
    isPotentiallyHazardousAsteroid = true,
    isSentryObject = false,
    estimatedDiameter = EstimatedDiameterDto(meters = DiameterRangeDto(estimatedDiameterMin = 90.0, estimatedDiameterMax = 210.0)),
    closeApproachData = listOf(
        CloseApproachDto(
            closeApproachDate = "2026-09-03",
            relativeVelocity = RelativeVelocityDto(kilometersPerHour = "45000.5"),
            missDistance = MissDistanceDto(lunar = "12.3"),
        ),
    ),
)

private val SAMPLE_DOMAIN = NeoDomain(
    id = "2000433",
    name = "433 Eros",
    closeApproachDate = LocalDate.of(2026, 9, 3),
    isPotentiallyHazardous = true,
    isSentryObject = false,
    missDistanceLunar = 12.3,
    relativeVelocityKmh = 45000.5,
    estimatedDiameterMinMeters = 90.0,
    estimatedDiameterMaxMeters = 210.0,
)

class NeoMappersTest {

    @Test
    fun `dto maps to domain using the first close approach entry`() {
        assertEquals(SAMPLE_DOMAIN, SAMPLE_DTO.toDomain())
    }

    @Test
    fun `feed response flattens the per-date map into a single list`() {
        val response = NeoWsFeedResponse(
            nearEarthObjects = mapOf(
                "2026-09-03" to listOf(SAMPLE_DTO),
                "2026-09-04" to listOf(SAMPLE_DTO.copy(id = "2000434")),
            ),
        )

        val result = response.toDomain()

        assertEquals(2, result.size)
        assertEquals(setOf("2000433", "2000434"), result.map { it.id }.toSet())
    }

    @Test
    fun `upcoming entity round-trips back to the same domain model`() {
        val entity = SAMPLE_DOMAIN.toUpcomingEntity(fetchedAtEpochMillis = 1_000L)

        assertEquals("2000433-2026-09-03", entity.entryId)
        assertEquals(1_000L, entity.fetchedAtEpochMillis)
        assertEquals(SAMPLE_DOMAIN, entity.toDomain())
    }

    @Test
    fun `archive entity round-trips back to the same domain model and carries the page`() {
        val entity = SAMPLE_DOMAIN.toArchiveEntity(page = 3)

        assertEquals("2000433-2026-09-03", entity.entryId)
        assertEquals(3, entity.page)
        assertEquals(SAMPLE_DOMAIN, entity.toDomain())
    }
}
