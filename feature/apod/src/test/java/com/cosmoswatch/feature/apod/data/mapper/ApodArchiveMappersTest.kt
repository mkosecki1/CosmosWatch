package com.cosmoswatch.feature.apod.data.mapper

import com.cosmoswatch.feature.apod.data.local.ApodArchiveEntity
import com.cosmoswatch.feature.apod.data.remote.ApodDto
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import com.cosmoswatch.feature.apod.domain.isPlayableVideo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

private const val PERMALINK = "https://science.nasa.gov/image-article/apod-2026-september-03-title/"
private const val ASSET = "https://assets.science.nasa.gov/dynamicimage/apod/image.jpg"

class ApodArchiveMappersTest {

    private val imageDto = ApodDto(
        date = "2026-09-03",
        title = "Stars &amp; Dust",
        explanation = "<strong>Explanation:</strong> The <a href=\"https://example.com\">Milky Way</a>\n  over\n" +
            "the city. <br><br><strong>Tomorrow's picture: </strong>Chasing the ISS",
        url = PERMALINK,
        hdurl = "$ASSET?w=2160&h=3840&fit=clip&crop=faces%2Cfocalpoint",
        mediaType = "image",
        copyright = "<a href=\"https://example.com/a\">Jane Doe</a>, <a href=\"https://example.com/b\">John Roe</a>",
    )

    private fun videoDto(mediaType: String, embedded: String) = imageDto.copy(
        mediaType = mediaType,
        hdurl = "https://assets.science.nasa.gov/dynamicimage/apod/frame.png?w=1080&h=1350&fit=clip",
        basicHtml = "<html><body><center>$embedded</center></body></html>",
    )

    @Test
    fun `image entry is cleaned up and gets a preview-sized url next to the hd one`() {
        val entity = imageDto.toArchiveEntity(page = 3)

        assertEquals(
            ApodArchiveEntity(
                date = "2026-09-03",
                title = "Stars & Dust",
                explanation = "The Milky Way over the city.",
                imageUrl = "$ASSET?crop=faces%2Cfocalpoint&w=1080&h=1080&fit=clip",
                hdImageUrl = "$ASSET?w=2160&h=3840&fit=clip&crop=faces%2Cfocalpoint",
                mediaType = "image",
                copyright = "Jane Doe, John Roe",
                thumbnailUrl = null,
                page = 3,
            ),
            entity,
        )
    }

    @Test
    fun `self-hosted video plays the embedded mp4 and uses the hd frame as thumbnail`() {
        val dto = videoDto("video", "<video controls><source src=\"https://assets.science.nasa.gov/clip.mp4\" type=\"video/mp4\"></video>")

        val domain = dto.toArchiveEntity(page = 0).toDomain()

        assertEquals(ApodMediaType.VIDEO, domain.mediaType)
        assertEquals("https://assets.science.nasa.gov/clip.mp4", domain.imageUrl)
        assertEquals("https://assets.science.nasa.gov/dynamicimage/apod/frame.png?w=1080&h=1080&fit=clip", domain.thumbnailUrl)
        assertNull(domain.hdImageUrl)
        assertTrue(domain.isPlayableVideo())
    }

    @Test
    fun `youtube embed gets an https scheme and is not played in-app`() {
        val dto = videoDto("video", "<iframe width=\"960\" src=\"//www.youtube.com/embed/abc123?rel=0\"></iframe>")

        val domain = dto.toArchiveEntity(page = 0).toDomain()

        assertEquals("https://www.youtube.com/embed/abc123?rel=0", domain.imageUrl)
        assertFalse(domain.isPlayableVideo())
    }

    @Test
    fun `interactive iframe entry is treated as an external video link`() {
        val dto = videoDto("iframe", "<iframe src=\"https://www.meteorshowers.org/view/perseids\"></iframe>")

        val entity = dto.toArchiveEntity(page = 0)

        assertEquals("video", entity.mediaType)
        assertEquals("https://www.meteorshowers.org/view/perseids", entity.imageUrl)
        assertFalse(entity.toDomain().isPlayableVideo())
    }

    @Test
    fun `video without an embedded source falls back to the article permalink`() {
        val dto = videoDto("video", "<p>No player here</p>")

        assertEquals(PERMALINK, dto.toArchiveEntity(page = 0).imageUrl)
    }

    @Test
    fun `blank copyright is mapped to null`() {
        assertNull(imageDto.copy(copyright = "").toArchiveEntity(page = 0).copyright)
    }

    @Test
    fun `archive entity maps to domain`() {
        val domain = imageDto.toArchiveEntity(page = 0).toDomain()

        assertEquals(
            ApodDomain(
                date = LocalDate.of(2026, 9, 3),
                title = "Stars & Dust",
                explanation = "The Milky Way over the city.",
                imageUrl = "$ASSET?crop=faces%2Cfocalpoint&w=1080&h=1080&fit=clip",
                hdImageUrl = "$ASSET?w=2160&h=3840&fit=clip&crop=faces%2Cfocalpoint",
                mediaType = ApodMediaType.IMAGE,
                copyright = "Jane Doe, John Roe",
                thumbnailUrl = null,
            ),
            domain,
        )
    }

    @Test
    fun `toRaw round-trips the media type used to filter the archive query`() {
        assertEquals("image", ApodMediaType.IMAGE.toRaw())
        assertEquals("video", ApodMediaType.VIDEO.toRaw())
    }
}
