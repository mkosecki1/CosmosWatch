package com.cosmoswatch.feature.donki.presentation.timeline

import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.donki.domain.DonkiTimelineFilter

data class DonkiTimelineState(
    val filter: DonkiTimelineFilter,
    val filterError: AppError? = null,
)
