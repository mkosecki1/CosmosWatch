package com.cosmoswatch

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cosmoswatch.core.ui.component.LocalBottomBarPadding
import com.cosmoswatch.core.ui.theme.CosmosWatchTheme
import com.cosmoswatch.feature.apod.presentation.daily.ApodRoute
import com.cosmoswatch.feature.apod.presentation.navigation.apodNavGraph
import com.cosmoswatch.feature.neows.presentation.navigation.neoWsNavGraph
import com.cosmoswatch.feature.neows.presentation.upcoming.NeoUpcomingRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        setContent {
            CosmosWatchTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val density = LocalDensity.current
                var bottomNavHeight = remember { mutableStateOf(0.dp) }

                Box(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalBottomBarPadding provides bottomNavHeight.value) {
                        NavHost(
                            navController = navController,
                            startDestination = ApodRoute,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            apodNavGraph(navController)
                            neoWsNavGraph()
                        }
                    }
                    NavigationBar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .onGloballyPositioned { coordinates ->
                                bottomNavHeight.value = with(density) {
                                    coordinates.size.height.toDp()
                                }
                            },
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        NavigationBarItem(
                            selected = currentBackStackEntry?.destination?.hasRoute(ApodRoute::class) == true,
                            onClick = { navController.navigate(ApodRoute) { launchSingleTop = true } },
                            icon = {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "APOD"
                                )
                            },
                            label = { Text(text = "APOD") }
                        )
                        NavigationBarItem(
                            selected = currentBackStackEntry?.destination?.hasRoute(NeoUpcomingRoute::class) == true,
                            onClick = { navController.navigate(NeoUpcomingRoute) { launchSingleTop = true } },
                            icon = {
                                Icon(
                                    painter = painterResource(com.cosmoswatch.feature.neows.R.drawable.ic_asteroid),
                                    contentDescription = "NeoWs"
                                )
                            },
                            label = { Text(text = "NeoWs") }
                        )
                    }
                }
            }
        }
    }
}
