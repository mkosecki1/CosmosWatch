package com.cosmoswatch.feature.marsphotos.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cosmoswatch.feature.marsphotos.presentation.detail.MarsPhotoDetailRoute
import com.cosmoswatch.feature.marsphotos.presentation.detail.MarsPhotoDetailScreen
import com.cosmoswatch.feature.marsphotos.presentation.list.MarsPhotosRoute
import com.cosmoswatch.feature.marsphotos.presentation.list.MarsPhotosScreen

fun NavGraphBuilder.marsPhotosNavGraph(navController: NavController) {
    composable<MarsPhotosRoute> {
        MarsPhotosScreen(
            onPhotoClick = { photoId -> navController.navigate(MarsPhotoDetailRoute(photoId)) },
        )
    }
    composable<MarsPhotoDetailRoute> {
        MarsPhotoDetailScreen(onBackClick = navController::popBackStack)
    }
}
