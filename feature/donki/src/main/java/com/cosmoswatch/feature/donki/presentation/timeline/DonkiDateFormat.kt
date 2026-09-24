package com.cosmoswatch.feature.donki.presentation.timeline

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val EVENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH)

internal fun Instant.toEventTimeText(zoneId: ZoneId = ZoneId.systemDefault()): String =
    atZone(zoneId).format(EVENT_TIME_FORMATTER)
