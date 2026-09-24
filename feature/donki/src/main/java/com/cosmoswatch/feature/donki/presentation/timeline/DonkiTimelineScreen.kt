package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.cosmoswatch.core.ui.component.LocalBottomBarPadding
import com.cosmoswatch.feature.donki.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonkiTimelineScreen(
    modifier: Modifier = Modifier,
    viewModel: DonkiTimelineViewModel = hiltViewModel()
) {
    val timeline = viewModel.timeline.collectAsLazyPagingItems()
    val bottomBarPadding = LocalBottomBarPadding.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.donki_timeline_title)) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 12.dp, bottom = bottomBarPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(count = timeline.itemCount, key = timeline.itemKey { it.id }) { index ->
                timeline[index]?.let { event ->
                    DonkiEventTile(event = event)
                }
            }
        }
    }
}