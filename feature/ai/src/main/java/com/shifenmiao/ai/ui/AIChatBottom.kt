package com.shifenmiao.ai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVerticalAlignBottom

@Composable
fun AIChatBottom(
    showTop: Boolean,
    bottomPadding: Dp = 80.dp,
    goToTop: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = bottomPadding,
                end = 28.dp
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        AnimatedGoToTopIcon(
            showTop = showTop,
            goToTop = goToTop
        )
    }
}

@Composable
fun BoxScope.AnimatedGoToTopIcon(
    showTop: Boolean,
    goToTop: () -> Unit
) {
    AnimatedVisibility(
        visible = showTop,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = 300)) +
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
    ) {
        GlassTonalIconButton(
            onClick = { goToTop.invoke() },
            modifier = Modifier
                .padding(bottom = AppTheme.dimens.paddingNormal)
                .align(Alignment.BottomEnd)
                .size(40.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVerticalAlignBottom,
                contentDescription = "go to top",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


