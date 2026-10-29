package com.cosmoswatch.feature.donki.presentation.timeline

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val EVENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH)

internal fun Instant.toEventTimeText(zoneId: ZoneId = ZoneId.systemDefault()): String =
    atZone(zoneId).format(EVENT_TIME_FORMATTER)

private val UTC_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm 'UTC'", Locale.ENGLISH)

internal fun Instant.toUtcTimeText(): String =
    atZone(ZoneOffset.UTC).format(UTC_TIME_FORMATTER)

private val ARRIVAL_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, HH:mm 'UTC'", Locale.ENGLISH)

internal fun Instant.toArrivalTimeText(): String =
    atZone(ZoneOffset.UTC).format(ARRIVAL_TIME_FORMATTER)

private val DAY_FORMATTER = DateTimeFormatter.ofPattern("d", Locale.ENGLISH)
private val MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
private val FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

internal fun dateRangeText(start: Instant, end: Instant): String {
    val startDate = start.atZone(ZoneOffset.UTC).toLocalDate()
    val endDate = end.atZone(ZoneOffset.UTC).toLocalDate()
    return when {
        startDate == endDate -> endDate.format(FULL_DATE_FORMATTER)
        startDate.year != endDate.year ->
            "${startDate.format(FULL_DATE_FORMATTER)} – ${endDate.format(FULL_DATE_FORMATTER)}"
        startDate.month != endDate.month ->
            "${startDate.format(MONTH_DAY_FORMATTER)} – ${endDate.format(FULL_DATE_FORMATTER)}"
        else -> "${startDate.format(MONTH_DAY_FORMATTER)}–${endDate.format(DAY_FORMATTER)}, ${endDate.year}"
    }
}
