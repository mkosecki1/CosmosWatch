package com.cosmoswatch.feature.apod.data.mapper

import com.cosmoswatch.feature.apod.data.local.ApodArchiveEntity
import com.cosmoswatch.feature.apod.data.local.ApodEntity
import com.cosmoswatch.feature.apod.data.remote.ApodDto
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.time.LocalDate

private const val RAW_IMAGE = "image"
private const val RAW_VIDEO = "video"
private const val PREVIEW_MAX_SIZE_PX = "1080"
private const val EXPLANATION_PREFIX = "Explanation:"
private const val TOMORROW_TEASER = "Tomorrow's picture:"
private val EMBEDDED_MEDIA_SRC_REGEX = Regex("""<(?:source|iframe)\b[^>]*\bsrc="([^"]+)"""", RegexOption.IGNORE_CASE)

fun ApodDto.toArchiveEntity(page: Int): ApodArchiveEntity = ApodArchiveEntity(
    date = date,
    title = title.htmlToPlainText(),
    explanation = cleanExplanation(),
    imageUrl = mediaUrl(),
    hdImageUrl = hdImageUrl(),
    mediaType = rawMediaType(),
    copyright = cleanCopyright(),
    thumbnailUrl = thumbnailUrl(),
    page = page,
)

fun ApodArchiveEntity.toDomain(): ApodDomain = ApodDomain(
    date = LocalDate.parse(date),
    title = title,
    explanation = explanation,
    imageUrl = imageUrl,
    hdImageUrl = hdImageUrl,
    mediaType = if (mediaType == RAW_VIDEO) ApodMediaType.VIDEO else ApodMediaType.IMAGE,
    copyright = copyright,
    thumbnailUrl = thumbnailUrl,
)

fun ApodDto.toEntity(fetchedAtEpochMillis: Long): ApodEntity = ApodEntity(
    date = date,
    title = title.htmlToPlainText(),
    explanation = cleanExplanation(),
    imageUrl = mediaUrl(),
    hdImageUrl = hdImageUrl(),
    mediaType = rawMediaType(),
    copyright = cleanCopyright(),
    thumbnailUrl = thumbnailUrl(),
    fetchedAtEpochMillis = fetchedAtEpochMillis,
)

fun ApodEntity.toDomain(): ApodDomain = ApodDomain(
    date = LocalDate.parse(date),
    title = title,
    explanation = explanation,
    imageUrl = imageUrl,
    hdImageUrl = hdImageUrl,
    mediaType = if (mediaType == RAW_VIDEO) ApodMediaType.VIDEO else ApodMediaType.IMAGE,
    copyright = copyright,
    thumbnailUrl = thumbnailUrl,
)

fun ApodMediaType.toRaw(): String = when (this) {
    ApodMediaType.IMAGE -> RAW_IMAGE
    ApodMediaType.VIDEO -> RAW_VIDEO
}

private fun ApodDto.isImage(): Boolean = mediaType == RAW_IMAGE

private fun ApodDto.rawMediaType(): String = if (isImage()) RAW_IMAGE else RAW_VIDEO

private fun ApodDto.validHdUrl(): String? = hdurl?.takeIf { it.startsWith("http") }

private fun ApodDto.mediaUrl(): String = if (isImage()) {
    validHdUrl()?.let(::previewSized) ?: url
} else {
    embeddedMediaUrl() ?: url
}

private fun ApodDto.hdImageUrl(): String? = if (isImage()) validHdUrl() else null

private fun ApodDto.thumbnailUrl(): String? = if (isImage()) null else validHdUrl()?.let(::previewSized)

private fun ApodDto.embeddedMediaUrl(): String? = basicHtml
    ?.let { EMBEDDED_MEDIA_SRC_REGEX.find(it)?.groupValues?.get(1) }
    ?.let { if (it.startsWith("//")) "https:$it" else it }
    ?.takeIf { it.startsWith("http") }

private fun previewSized(url: String): String = url.toHttpUrlOrNull()
    ?.newBuilder()
    ?.setQueryParameter("w", PREVIEW_MAX_SIZE_PX)
    ?.setQueryParameter("h", PREVIEW_MAX_SIZE_PX)
    ?.setQueryParameter("fit", "clip")
    ?.build()
    ?.toString()
    ?: url

private fun ApodDto.cleanExplanation(): String = explanation.htmlToPlainText()
    .removePrefix(EXPLANATION_PREFIX)
    .substringBefore(TOMORROW_TEASER)
    .trim()

private fun ApodDto.cleanCopyright(): String? = copyright?.htmlToPlainText()?.takeIf { it.isNotBlank() }
