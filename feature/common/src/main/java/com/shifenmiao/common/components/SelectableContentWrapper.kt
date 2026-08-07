package com.shifenmiao.common.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.ui.utils.capturable.CaptureController
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalImageShareProvider
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTextSnippet

/**
 * A reusable wrapper composable that provides long-press context actions for its content:
 * - **Copy**: copies [textContent] to the clipboard.
 * - **Select Text**: wraps content in [SelectionContainer] for text selection.
 * - **Share as Image**: captures the content as a bitmap and shares it via [ImageShareProvider].
 *
 * Place this in any screen to give the wrapped content copy / select / share-as-image capabilities.
 *
 * @param textContent The plain-text representation used for the "Copy" action. If null, copy is disabled.
 * @param modifier Modifier applied to the outer container.
 * @param content The composable content to wrap.
 */
@Composable
fun SelectableContentWrapper(
    textContent: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shareProvider = LocalImageShareProvider.current
    val captureController: CaptureController = rememberCaptureController()
    val scope = rememberCoroutineScope()

    var showActionBar by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    // Dismiss action bar on normal tap when visible
                    if (showActionBar) showActionBar = false
                },
                onLongClick = {
                    if (!selectionMode) {
                        showActionBar = !showActionBar
                    }
                },
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            )
    ) {
        // Capturable content area — only the content is captured, not the action bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .capturable(captureController)
        ) {
            if (selectionMode) {
                SelectionContainer {
                    content()
                }
            } else {
                content()
            }
        }

        // Action bar — shown on long press
        ContentActionBar(
            visible = showActionBar,
            enableCopy = textContent != null,
            onCopy = {
                textContent?.let { Clipboard.copy(it) }
            },
            onSelectText = {
                selectionMode = true
            },
            onShareImage = {
                scope.launch {
                    try {
                        val bitmap: Bitmap = captureController.captureAsync().await().asAndroidBitmap()
                        shareProvider.shareImage(
                            imageInfo = ImageInfo(
                                width = bitmap.width,
                                height = bitmap.height,
                                imageFormat = ImageFormat.Png.Lossless
                            ),
                            image = bitmap,
                            onComplete = {}
                        )
                    } catch (_: Throwable) {
                        // Capture may fail in rare edge cases — silently ignore
                    }
                }
            },
            onHide = { showActionBar = false },
            modifier = Modifier.align(Alignment.TopEnd)
        )

        // "Done" chip when in selection mode — allows user to exit selection
        if (selectionMode) {
            SelectionModeDoneChip(
                onDone = { selectionMode = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )
        }
    }
}

// ───────────────────────────────────────────────────────────────────
// Internal components
// ───────────────────────────────────────────────────────────────────

/**
 * Floating action bar with Copy / Select Text / Share as Image buttons.
 * Uses the same animation pattern as ActionBar in GenericCard.
 */
@Composable
private fun ContentActionBar(
    visible: Boolean,
    enableCopy: Boolean,
    onCopy: () -> Unit,
    onSelectText: () -> Unit,
    onShareImage: () -> Unit,
    onHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var clickedIndex by remember { mutableStateOf<Int?>(null) }

    data class Action(
        val icon: ImageVector,
        val description: String,
        val onClick: () -> Unit,
        val enabled: Boolean = true,
    )

    val actions = listOf(
        Action(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
            description = stringResource(R.string.copy_url),
            onClick = onCopy,
            enabled = enableCopy,
        ),
        Action(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTextSnippet,
            description = stringResource(R.string.select_text),
            onClick = onSelectText,
        ),
        Action(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
            description = stringResource(R.string.share_as_image),
            onClick = onShareImage,
        ),
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ),
        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions.forEachIndexed { index, action ->
                if (!action.enabled) return@forEachIndexed

                val isClicked = clickedIndex == index
                val scale by animateFloatAsState(
                    targetValue = if (isClicked) 0.85f else 1f,
                    animationSpec = tween(150),
                    label = "actionScale"
                )

                GlassTonalIconButton(
                    onClick = {
                        clickedIndex = index
                        action.onClick()
                        scope.launch {
                            delay(200.milliseconds)
                            onHide()
                            delay(100.milliseconds)
                            clickedIndex = null
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .scale(scale),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.description,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (index < actions.lastIndex) {
                    Spacer(modifier = Modifier.size(4.dp))
                }
            }
        }
    }
}

/**
 * Small "Done" chip shown when selection mode is active, allowing the user to exit.
 */
@Composable
private fun SelectionModeDoneChip(
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onDone,
        label = {
            Text(
                text = stringResource(R.string.done),
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        modifier = modifier
    )
}

