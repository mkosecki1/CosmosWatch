package com.cosmoswatch.feature.neows.presentation.upcoming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosmoswatch.core.ui.component.ErrorState
import com.cosmoswatch.core.ui.component.LoadingState
import com.cosmoswatch.core.ui.component.LocalBottomBarPadding
import com.cosmoswatch.feature.neows.R
import com.cosmoswatch.feature.neows.presentation.common.toMessage

@Composable
fun NeoUpcomingScreen(
    modifier: Modifier = Modifier,
    viewModel: NeoUpcomingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NeoUpcomingScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NeoUpcomingScreenContent(
    state: NeoUpcomingState,
    onIntent: (NeoUpcomingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val bottomContentPadding = LocalBottomBarPadding.current

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.neo_upcoming_title)) },
                actions = {},
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->

        when (state) {
            is NeoUpcomingState.Loading -> LoadingState(modifier = Modifier.padding(innerPadding).fillMaxSize())
            is NeoUpcomingState.Error -> ErrorState(
                message = state.error.toMessage(),
                modifier = Modifier.padding(innerPadding).fillMaxWidth(),
                retryLabel = stringResource(R.string.neo_retry),
                onRetry = { onIntent(NeoUpcomingIntent.Retry) }
            )
            is NeoUpcomingState.Success -> LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 12.dp, bottom = bottomContentPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = state.neos, key = {it.id}) { neoItem ->
                    NeoTile(neoDomain = neoItem)
                }
            }
        }

    }
}
