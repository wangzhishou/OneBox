/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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