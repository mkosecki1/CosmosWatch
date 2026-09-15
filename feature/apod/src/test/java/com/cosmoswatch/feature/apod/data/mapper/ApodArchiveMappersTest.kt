package com.cosmoswatch.feature.apod.data.mapper

import com.cosmoswatch.feature.apod.data.local.ApodArchiveEntity
import com.cosmoswatch.feature.apod.data.remote.ApodDto
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ApodArchiveMappersTest {

    private val dto = ApodDto(
        date = "2026-09-03",
        title = "Title",
        explanation = "Explanation",
        url = "https://example.com/image.jpg",
        hdurl = "https://example.com/image_hd.jpg",
        mediaType = "video",
        copyright = "NASA",
        thumbnailUrl = "https://example.com/thumb.jpg",
    )

    @Test
    fun `toArchiveEntity carries the page and blanks out an empty thumbnail`() {
        val blankThumbnailDto = dto.copy(thumbnailUrl = " ")

        val entity = blankThumbnailDto.toArchiveEntity(page = 3)

        assertEquals(
            ApodArchiveEntity(
                date = "2026-09-03",
                title = "Title",
                explanation = "Explanation",
                imageUrl = "https://example.com/image.jpg",
                hdImageUrl = "https://example.com/image_hd.jpg",
                mediaType = "video",
                copyright = "NASA",
                thumbnailUrl = null,
                page = 3,
            ),
            entity,
        )
    }

    @Test
    fun `archive entity maps video media type to domain`() {
        val entity = dto.toArchiveEntity(page = 0)

        assertEquals(
            ApodDomain(
                date = LocalDate.of(2026, 9, 3),
                title = "Title",
                explanation = "Explanation",
                imageUrl = "https://example.com/image.jpg",
                hdImageUrl = "https://example.com/image_hd.jpg",
                mediaType = ApodMediaType.VIDEO,
                copyright = "NASA",
                thumbnailUrl = "https://example.com/thumb.jpg",
            ),
            entity.toDomain(),
        )
    }

    @Test
    fun `archive entity maps any non-video media type to image`() {
        val entity = dto.copy(mediaType = "image").toArchiveEntity(page = 0)

        assertEquals(ApodMediaType.IMAGE, entity.toDomain().mediaType)
    }

    @Test
    fun `toRaw round-trips the media type used to filter the archive query`() {
        assertEquals("image", ApodMediaType.IMAGE.toRaw())
        assertEquals("video", ApodMediaType.VIDEO.toRaw())
    }
}
