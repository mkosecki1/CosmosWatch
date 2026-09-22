package com.cosmoswatch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cosmoswatch.core.ui.component.LocalBottomBarPadding
import com.cosmoswatch.feature.apod.presentation.daily.ApodRoute
import com.cosmoswatch.feature.apod.presentation.imageviewer.ApodImageViewerRoute
import com.cosmoswatch.feature.apod.presentation.navigation.apodNavGraph
import com.cosmoswatch.feature.neows.presentation.navigation.neoWsNavGraph

@Composable
fun CosmosWatchApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    var bottomNavHeight by remember { mutableStateOf(0.dp) }
    val isImageViewerActive = currentBackStackEntry?.destination?.hasRoute(ApodImageViewerRoute::class) == true

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalBottomBarPadding provides bottomNavHeight) {
            NavHost(
                navController = navController,
                startDestination = ApodRoute,
                modifier = Modifier.fillMaxSize()
            ) {
                apodNavGraph(navController)
                neoWsNavGraph()
            }
        }
        if (!isImageViewerActive) {
            AppBottomBar(
                navController = navController,
                currentBackStackEntry = currentBackStackEntry,
                onHeightMeasured = { bottomNavHeight = it },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
