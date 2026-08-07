package com.shifenmiao.base.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color

@Composable
fun DisableContainer(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.alpha(
            animateFloatAsState(
                if (enabled) 1f else 0.5f
            ).value
        )
    ) {
        content()
        if (!enabled) {
            Surface(
                color = Color.Transparent,
                modifier = Modifier.matchParentSize()
            ) {}
        }
    }
}