package com.cosmoswatch.feature.apod.presentation.navigation

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

fun NavGraphBuilder.apodNavGraph(navController: NavController) {
    composable<ApodRoute> {
        ApodScreen(
            onArchiveClick = { navController.navigate(ApodArchiveRoute) },
            onImageClick = { imageUrl -> navController.navigate(ApodImageViewerRoute(imageUrl)) },
        )
    }
    composable<ApodArchiveRoute> {
        ApodArchiveScreen(
            onBackClick = navController::popBackStack,
            onEntryClick = { date -> navController.navigate(ApodArchiveDetailRoute(date.toString())) },
        )
    }
    composable<ApodArchiveDetailRoute> {
        ApodArchiveDetailScreen(
            onBackClick = navController::popBackStack,
            onImageClick = { imageUrl -> navController.navigate(ApodImageViewerRoute(imageUrl)) },
        )
    }
    composable<ApodImageViewerRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ApodImageViewerRoute>()
        ApodImageViewerScreen(
            imageUrl = route.imageUrl,
            onBackClick = navController::popBackStack,
        )
    }
}
