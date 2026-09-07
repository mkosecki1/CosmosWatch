package com.cosmoswatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.cosmoswatch.core.ui.theme.CosmosWatchTheme
import com.cosmoswatch.feature.apod.presentation.ApodRoute
import com.cosmoswatch.feature.apod.presentation.apodNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CosmosWatchTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = ApodRoute) {
                    apodNavGraph()
                }
            }
        }
    }
}
