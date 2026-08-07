package com.shifenmiao.base.pullrefresh

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PullToRefreshLayout(
    modifier: Modifier = Modifier,
    pullRefreshLayoutState: PullToRefreshLayoutState,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    content: @Composable (pullToRefreshState: PullToRefreshState) -> Unit,
) {
    val refreshIndicatorState by pullRefreshLayoutState.refreshIndicatorState
    val timeElapsedSinceLastRefresh by pullRefreshLayoutState.lastRefreshText

    val pullToRefreshState = rememberPullToRefreshState()
    if (isRefreshing) {
        LaunchedEffect(true) {
            pullRefreshLayoutState.refresh()
        }
    } else {
        LaunchedEffect(true) {
            pullRefreshLayoutState.endRefresh()
        }
    }
    LaunchedEffect(key1 = pullToRefreshState.distanceFraction) {
        when {
            pullToRefreshState.distanceFraction >= 1 -> {
                pullRefreshLayoutState.updateRefreshState(RefreshIndicatorState.ReachedThreshold)
            }

            pullToRefreshState.distanceFraction > 0 -> {
                pullRefreshLayoutState.updateRefreshState(RefreshIndicatorState.PullingDown)
            }
        }
    }

    Column(
        modifier = modifier
            .pullToRefresh(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            )
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
    ) {
        PullToRefreshIndicator(
            isRefreshing = isRefreshing,
            indicatorState = refreshIndicatorState,
            pullToRefreshProgress = pullToRefreshState.distanceFraction,
            timeElapsed = timeElapsedSinceLastRefresh
        )
        content(pullToRefreshState)
    }
}