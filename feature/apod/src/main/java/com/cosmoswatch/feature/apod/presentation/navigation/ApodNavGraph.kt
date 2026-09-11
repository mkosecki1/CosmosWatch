package com.cosmoswatch.feature.apod.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.cosmoswatch.feature.apod.presentation.archive.ApodArchiveRoute
import com.cosmoswatch.feature.apod.presentation.archive.ApodArchiveScreen
import com.cosmoswatch.feature.apod.presentation.archivedetail.ApodArchiveDetailRoute
import com.cosmoswatch.feature.apod.presentation.archivedetail.ApodArchiveDetailScreen
import com.cosmoswatch.feature.apod.presentation.daily.ApodRoute
import com.cosmoswatch.feature.apod.presentation.daily.ApodScreen
import com.cosmoswatch.feature.apod.presentation.imageviewer.ApodImageViewerRoute
import com.cosmoswatch.feature.apod.presentation.imageviewer.ApodImageViewerScreen

private const val NAV_TRANSITION_DURATION_MILLIS =50
fun NavGraphBuilder.apodNavGraph(navController: NavController) {
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

    composable<ApodRoute> {
        ApodScreen(
            onArchiveClick = { navController.navigate(ApodArchiveRoute) },
            onImageClick = { imageUrl -> navController.navigate(ApodImageViewerRoute(imageUrl)) },
        )
    }
    composable<ApodArchiveRoute>(
        enterTransition = enter,
        exitTransition = exit,
        popEnterTransition = popEnter,
        popExitTransition = popExit
    ) {
        ApodArchiveScreen(
            onBackClick = navController::popBackStack,
            onEntryClick = { date -> navController.navigate(ApodArchiveDetailRoute(date.toString())) },
        )
    }
    composable<ApodArchiveDetailRoute>(
        enterTransition = enter,
        exitTransition = exit,
        popEnterTransition = popEnter,
        popExitTransition = popExit
    ) {
        ApodArchiveDetailScreen(
            onBackClick = navController::popBackStack,
            onImageClick = { imageUrl -> navController.navigate(ApodImageViewerRoute(imageUrl)) },
        )
    }
    composable<ApodImageViewerRoute>(
        enterTransition = enter,
        exitTransition = exit,
        popEnterTransition = popEnter,
        popExitTransition = popExit
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<ApodImageViewerRoute>()
        ApodImageViewerScreen(
            imageUrl = route.imageUrl,
            onBackClick = navController::popBackStack,
        )
    }
}
