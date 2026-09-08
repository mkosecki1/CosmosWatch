package com.cosmoswatch.feature.apod.presentation.archive

import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.apod.domain.ApodArchiveFilter

data class ApodArchiveState(
    val filter: ApodArchiveFilter = ApodArchiveFilter(),
    val filterError: AppError? = null,
)
