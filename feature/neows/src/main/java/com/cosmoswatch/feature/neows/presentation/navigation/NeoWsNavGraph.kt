package com.cosmoswatch.feature.neows.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cosmoswatch.feature.neows.presentation.archive.NeoArchiveRoute
import com.cosmoswatch.feature.neows.presentation.archive.NeoArchiveScreen
import com.cosmoswatch.feature.neows.presentation.upcoming.NeoUpcomingRoute
import com.cosmoswatch.feature.neows.presentation.upcoming.NeoUpcomingScreen

private const val NAV_TRANSITION_DURATION_MILLIS = 50

fun NavGraphBuilder.neoWsNavGraph(navController: NavController) {
    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(NAV_TRANSITION_DURATION_MILLIS)
        )
    }
    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(NAV_TRANSITION_DURATION_MILLIS)
        )
    }
    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(NAV_TRANSITION_DURATION_MILLIS)
        )
    }
    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(NAV_TRANSITION_DURATION_MILLIS)
        )
    }

    composable<NeoUpcomingRoute> {
        NeoUpcomingScreen(
            onArchiveClick = { navController.navigate(NeoArchiveRoute) },
        )
    }
    composable<NeoArchiveRoute>(
        enterTransition = enter,
        exitTransition = exit,
        popEnterTransition = popEnter,
        popExitTransition = popExit
    ) {
        NeoArchiveScreen(
            onBackClick = navController::popBackStack,
        )
    }
}
