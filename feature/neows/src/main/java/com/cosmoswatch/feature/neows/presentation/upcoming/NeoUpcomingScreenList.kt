package com.cosmoswatch.feature.neows.presentation.upcoming

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import com.cosmoswatch.core.ui.component.ToggleChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cosmoswatch.core.ui.component.ClearFilterChip
import com.cosmoswatch.core.ui.component.EmptyState
import com.cosmoswatch.core.ui.component.LocalBottomBarPadding
import com.cosmoswatch.feature.neows.R
import com.cosmoswatch.feature.neows.domain.NeoUpcomingFilter

@Composable
fun NeoUpcomingScreenList(
    innerPadding: PaddingValues,
    state: NeoUpcomingState.Success,
    onIntent: (NeoUpcomingIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomContentPadding = LocalBottomBarPadding.current

    Column(modifier = modifier.padding(innerPadding)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClearFilterChip(
                contentDescription = stringResource(R.string.neo_clear_filter),
                onClick = { onIntent(NeoUpcomingIntent.ClearFilter) },
            )
            ToggleChip(
                selected = state.filter.hazardousOnly,
                onClick = { onIntent(NeoUpcomingIntent.FilterChanged(state.filter.copy(hazardousOnly = !state.filter.hazardousOnly))) },
                label = stringResource(R.string.neo_filter_hazardous)
            )
            ToggleChip(
                selected = state.filter.closeOnly,
                onClick = { onIntent(NeoUpcomingIntent.FilterChanged(state.filter.copy(closeOnly = !state.filter.closeOnly))) },
                label = stringResource(R.string.neo_filter_close)
            )
            ToggleChip(
                selected = state.filter.largeOnly,
                onClick = { onIntent(NeoUpcomingIntent.FilterChanged(state.filter.copy(largeOnly = !state.filter.largeOnly))) },
                label = stringResource(R.string.neo_filter_large)
            )
            ToggleChip(
                selected = state.filter.sentryOnly,
                onClick = { onIntent(NeoUpcomingIntent.FilterChanged(state.filter.copy(sentryOnly = !state.filter.sentryOnly))) },
                label = stringResource(R.string.neo_filter_sentry)
            )
        }
        if (state.neos.isEmpty()) {
            EmptyState(
                message = stringResource(R.string.neo_upcoming_empty),
                actionLabel = if (state.filter != NeoUpcomingFilter()) stringResource(R.string.neo_clear_filter) else null,
                onAction = if (state.filter != NeoUpcomingFilter()) ({ onIntent(NeoUpcomingIntent.ClearFilter) }) else null
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 12.dp, bottom = bottomContentPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = state.neos, key = {it.id}) { neoItem ->
                    NeoTile(neoDomain = neoItem, today = state.today)
                }
            }

        }
    }
}
