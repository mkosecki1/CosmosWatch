package com.cosmoswatch.core.common.result

sealed interface AppError {
    data object Network : AppError
    data class Server(val code: Int?) : AppError
    data class Unknown(val cause: Throwable? = null) : AppError
    data class Validation(val reason: String) : AppError
}
