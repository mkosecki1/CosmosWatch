package com.cosmoswatch.feature.neows.presentation.archive

import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.neows.domain.NeoArchiveFilter
import java.time.LocalDate

data class NeoArchiveState(
    val filter: NeoArchiveFilter,
    val today: LocalDate,
    val filterError: AppError? = null,
)
