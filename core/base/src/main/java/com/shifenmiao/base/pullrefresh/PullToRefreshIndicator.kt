package com.shifenmiao.base.pullrefresh

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import kotlin.math.roundToInt


@Composable
fun PullToRefreshIndicator(
    indicatorState: RefreshIndicatorState,
    pullToRefreshProgress: Float,
    timeElapsed: String,
    positionalThreshold: Dp = PullToRefreshDefaults.PositionalThreshold,
    isRefreshing: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .height(
                (pullToRefreshProgress * 100)
                    .roundToInt()
                    .coerceAtMost(positionalThreshold.value.toInt()).dp,
            )
            .padding(15.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(indicatorState.messageRes),
                style = MaterialTheme.typography.labelMedium,
            )
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(R.string.last_updated, timeElapsed),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}