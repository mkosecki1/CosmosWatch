package com.cosmoswatch.feature.neows.presentation.archive

import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.neows.domain.NeoArchiveFilter

data class NeoArchiveState(
    val filter: NeoArchiveFilter = NeoArchiveFilter(),
    val filterError: AppError? = null,
)
