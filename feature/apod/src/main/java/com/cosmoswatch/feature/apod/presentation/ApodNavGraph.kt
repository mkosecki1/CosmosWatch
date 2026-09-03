package com.cosmoswatch.feature.apod.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object ApodRoute

fun NavGraphBuilder.apodNavGraph() {
    composable<ApodRoute> {
        ApodScreen()
    }
}
