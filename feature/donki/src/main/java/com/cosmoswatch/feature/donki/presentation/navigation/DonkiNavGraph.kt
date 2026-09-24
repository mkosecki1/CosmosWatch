package com.cosmoswatch.feature.donki.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cosmoswatch.feature.donki.presentation.timeline.DonkiTimelineRoute
import com.cosmoswatch.feature.donki.presentation.timeline.DonkiTimelineScreen

fun NavGraphBuilder.donkiNavGraph() {
    composable<DonkiTimelineRoute> {
        DonkiTimelineScreen()
    }
}
