package com.shifenmiao.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * A loading overlay that displays a circular progress indicator centered on the screen
 * with a semi-transparent background to block interaction with content below it.
 *
 * @param modifier The modifier to be applied to the overlay
 * @param backgroundColor The background color of the overlay (defaults to black with 50% transparency)
 * @param contentAlignment The alignment of the progress indicator within the overlay
 */
@Composable
fun LoadingOverlay(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
    contentAlignment: Alignment = Alignment.Center
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Consume clicks to prevent interaction with content underneath */ }
            .alpha(0.9f)
            .background(backgroundColor)
            .zIndex(Float.MAX_VALUE),
        contentAlignment = contentAlignment
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}