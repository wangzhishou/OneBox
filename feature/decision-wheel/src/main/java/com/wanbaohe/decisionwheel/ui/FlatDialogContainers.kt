package com.wanbaohe.decisionwheel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme

/**
 * Ultra-flat scrim used across decision-wheel dialogs.
 *
 * Instead of using a semi-transparent black scrim (which feels heavy and non-flat),
 * we use a theme-based `surfaceContainerHighest` overlay.
 *
 * @param onDismiss Called when the user taps on the scrim.
 * @param alpha Opacity for the scrim layer.
 * @param content Dialog content displayed on top of the scrim.
 */
@Composable
internal fun FlatDialogScrim(
    onDismiss: () -> Unit,
    alpha: Float = 0.68f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha))
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
