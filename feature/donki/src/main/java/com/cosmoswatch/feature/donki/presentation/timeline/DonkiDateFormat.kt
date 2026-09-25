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
