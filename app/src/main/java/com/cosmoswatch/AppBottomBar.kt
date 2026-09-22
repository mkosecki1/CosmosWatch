package com.cosmoswatch

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import com.cosmoswatch.core.ui.component.AppBottomNavItem
import com.cosmoswatch.feature.apod.presentation.daily.ApodRoute
import com.cosmoswatch.feature.neows.presentation.upcoming.NeoUpcomingRoute

@Composable
fun AppBottomBar(
    navController: NavHostController,
    currentBackStackEntry: NavBackStackEntry?,
    onHeightMeasured: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    Surface(
        modifier = modifier.onGloballyPositioned { coordinates ->
            onHeightMeasured(with(density) { coordinates.size.height.toDp() })
        },
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val apodLabel = stringResource(R.string.bottom_nav_apod)
            val neoWsLabel = stringResource(R.string.bottom_nav_neows)
            AppBottomNavItem(
                selected = currentBackStackEntry?.destination?.hasRoute(ApodRoute::class) == true,
                onClick = { navController.navigate(ApodRoute) { launchSingleTop = true } },
                label = apodLabel,
                icon = { tint -> Icon(Icons.Outlined.Image, contentDescription = apodLabel, tint = tint) },
            )
            AppBottomNavItem(
                selected = currentBackStackEntry?.destination?.hasRoute(NeoUpcomingRoute::class) == true,
                onClick = { navController.navigate(NeoUpcomingRoute) { launchSingleTop = true } },
                label = neoWsLabel,
                icon = { tint -> Icon(Icons.Outlined.Radar, contentDescription = neoWsLabel, tint = tint) },
            )
        }
    }
}
