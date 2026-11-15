package com.shifenmiao.ai.a2ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.skeleton.shimmerLoading

@Composable
internal fun A2uiSkeletonPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        A2uiSkeletonBar(modifier = Modifier.width(120.dp).height(16.dp))
        A2uiSkeletonBar(modifier = Modifier.fillMaxWidth().height(12.dp))
        A2uiSkeletonBar(modifier = Modifier.fillMaxWidth(0.75f).height(12.dp))
        A2uiSkeletonBar(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp))
        Spacer(modifier = Modifier.height(4.dp))
        A2uiSkeletonBar(
            modifier = Modifier.align(Alignment.End).width(80.dp).height(32.dp),
        )
    }
}

@Composable
private fun A2uiSkeletonBar(modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer)
            .shimmerLoading(),
    )
}
