package com.cosmoswatch.feature.neows.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cosmoswatch.feature.neows.presentation.upcoming.NeoUpcomingRoute
import com.cosmoswatch.feature.neows.presentation.upcoming.NeoUpcomingScreen

fun NavGraphBuilder.neoWsNavGraph() {
    composable<NeoUpcomingRoute> {
        NeoUpcomingScreen()
    }
}
