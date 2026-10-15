package com.cosmoswatch.feature.donki.domain

import java.time.LocalDate

val DONKI_ARCHIVE_START_DATE: LocalDate = LocalDate.of(2010, 4, 3)

data class DonkiTimelineFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val flaresSelected: Boolean = false,
    val cmesSelected: Boolean = false,
    val stormsSelected: Boolean = false,
    val significantOnly: Boolean = false,
    val earthDirectedOnly: Boolean = false,
)
