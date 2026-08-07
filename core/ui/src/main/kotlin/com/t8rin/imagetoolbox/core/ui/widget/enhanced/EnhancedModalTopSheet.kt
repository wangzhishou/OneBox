/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.core.ui.widget.enhanced

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.utils.animation.FancyTransitionEasing
import com.t8rin.imagetoolbox.core.ui.utils.helper.PredictiveBackObserver
import com.t8rin.imagetoolbox.core.ui.utils.provider.ProvideContainerDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.CornerSides
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.animateContentSizeNoClip
import com.t8rin.imagetoolbox.core.ui.widget.modifier.drawHorizontalStroke
import com.t8rin.imagetoolbox.core.ui.widget.modifier.only
import com.t8rin.imagetoolbox.core.ui.widget.utils.FullscreenPopup
import kotlinx.coroutines.delay
import com.t8rin.imagetoolbox.core.resources.icons.Close

/**
 * A modal top sheet that slides down from the top of the screen.
 * Similar to ModalBottomSheet but appears from the top.
 *
 * @param visible Whether the sheet is visible
 * @param onDismiss Callback when the sheet is dismissed
 * @param modifier Modifier for the scrim/background
 * @param sheetModifier Modifier for the sheet content
 * @param cancelable Whether the sheet can be dismissed by clicking outside
 * @param animationSpec Animation specification for show/hide transitions
 * @param shape The shape of the sheet
 * @param elevation The elevation of the sheet
 * @param containerColor The background color of the sheet
 * @param contentColor The content color of the sheet
 * @param scrimColor The color of the scrim overlay
 * @param enableBackHandler Whether to enable back button handling
 * @param maxWidth Maximum width of the sheet content (default 640.dp for tablet support)
 * @param content The content of the sheet
 */
@Composable
fun EnhancedModalTopSheet(
    visible: Boolean,
    onDismiss: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    sheetModifier: Modifier = Modifier,
    cancelable: Boolean = true,
    animationSpec: AnimationSpec<Float> = EnhancedTopSheetDefaults.animationSpec,
    shape: Shape = ShapeDefaults.extremeLarge.only(CornerSides.Bottom),
    elevation: Dp = 0.dp,
    containerColor: Color = EnhancedTopSheetDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    scrimColor: Color = EnhancedTopSheetDefaults.scrimColor,
    enableBackHandler: Boolean = true,
    maxWidth: Dp = 640.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Animation state
    var isAnimating by remember { mutableStateOf(false) }
    var hasBeenVisible by remember { mutableStateOf(false) }

    // Animate visibility
    val animatedProgress by animateFloatAsState(
        targetValue = if (visible && hasBeenVisible) 1f else 0f,
        animationSpec = animationSpec,
        label = "topSheetProgress"
    )

    LaunchedEffect(visible) {
        if (visible) {
            if (!hasBeenVisible) {
                // First time showing: delay to ensure animation starts from 0f
                delay(50) // Small delay to ensure composition is ready
                hasBeenVisible = true
            }
            isAnimating = true
            delay(600)
            isAnimating = false
        } else {
            isAnimating = true
            delay(600)
            isAnimating = false
        }
    }

    // Don't render if never been visible or animation complete
    if (!hasBeenVisible) return
    if (!visible && !isAnimating && animatedProgress == 0f) return

    var predictiveBackProgress by remember {
        mutableFloatStateOf(0f)
    }
    val animatedPredictiveBackProgress by animateFloatAsState(predictiveBackProgress)

    LaunchedEffect(visible) {
        if (!visible) {
            delay(300L)
            predictiveBackProgress = 0f
        }
    }

    // Use Popup to ensure fullscreen coverage
    FullscreenPopup(
        onDismiss = {
            if (cancelable) {
                onDismiss(false)
            }
        }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    scrimColor.copy(alpha = scrimColor.alpha * animatedProgress)
                )
                .clickable(
                    onClick = {
                        if (cancelable) {
                            onDismiss(false)
                        }
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            ProvideContainerDefaults(
                color = EnhancedTopSheetDefaults.contentContainerColor
            ) {
                Surface(
                    modifier = sheetModifier
                        .widthIn(max = maxWidth)
                        .fillMaxWidth()
                        .graphicsLayer {
                            val sheetHeight = size.height
                            if (!sheetHeight.isNaN() && sheetHeight != 0f) {
                                // Slide from top animation
                                translationY = -sheetHeight * (1f - animatedProgress)

                                // Predictive back animation
                                val progress = animatedPredictiveBackProgress
                                scaleX = calculatePredictiveBackScaleX(progress)
                                scaleY = calculatePredictiveBackScaleY(progress)
                                transformOrigin = TransformOrigin(
                                    pivotFractionX = 0.5f,
                                    pivotFractionY = 0f
                                )
                            }
                        }
                        .clip(shape)
                        .animateContentSizeNoClip(spring())
                        .clickable(
                            onClick = {},
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    shape = shape,
                    color = containerColor,
                    contentColor = contentColor,
                    tonalElevation = elevation,
                    shadowElevation = elevation
                ) {
                    Column(
                        modifier = Modifier.statusBarsPadding()
                    ) {
                        PredictiveBackObserver(
                            onProgress = { progress ->
                                predictiveBackProgress = progress
                            },
                            onClean = { isCompleted ->
                                if (isCompleted) {
                                    onDismiss(false)
                                    delay(400)
                                }
                                predictiveBackProgress = 0f
                            },
                            enabled = visible && enableBackHandler
                        )
                        content()
                    }
                }
            }
        }
    }
}

/**
 * Enhanced Modal Top Sheet with title and confirm button.
 *
 * @param visible Whether the sheet is visible
 * @param onDismiss Callback when the sheet is dismissed
 * @param title The title content
 * @param cancelButton The cancel button content (displayed at bottom left of confirm button)
 * @param confirmButton The confirm button content (displayed at bottom)
 * @param cancelable Whether the sheet can be dismissed by clicking outside
 * @param enableBackHandler Whether to enable back button handling
 * @param maxContentHeight Maximum height for the content area (default 400.dp)
 * @param sheetContent The main content of the sheet
 */
@Composable
fun EnhancedModalTopSheet(
    visible: Boolean,
    onDismiss: (Boolean) -> Unit,
    title: @Composable RowScope.() -> Unit,
    cancelButton: (@Composable RowScope.() -> Unit)? = null,
    confirmButton: (@Composable RowScope.() -> Unit)? = null,
    cancelable: Boolean = true,
    enableBackHandler: Boolean = true,
    maxContentHeight: Dp = 600.dp,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    EnhancedModalTopSheet(
        visible = visible,
        onDismiss = onDismiss,
        cancelable = cancelable,
        enableBackHandler = enableBackHandler,
        content = {
            // Title bar with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawHorizontalStroke(false, autoElevation = 6.dp)
                    .background(EnhancedTopSheetDefaults.barContainerColor)
                    .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    title()
                }
                // Close button
                IconButton(
                    onClick = { onDismiss(false) }
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = "关闭",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Content with height limit
            Column(
                modifier = Modifier
                    .weight(1f, false)
                    .heightIn(max = maxContentHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = sheetContent
            )

            // Bottom button area
            if (confirmButton != null || cancelButton != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawHorizontalStroke(true, autoElevation = 6.dp)
                        .background(EnhancedTopSheetDefaults.barContainerColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (cancelButton != null) {
                        cancelButton()
                        Spacer(modifier = Modifier.widthIn(min = 8.dp))
                    }
                    if (confirmButton != null) {
                        confirmButton()
                    }
                }
            }
        }
    )
}

private fun GraphicsLayerScope.calculatePredictiveBackScaleX(progress: Float): Float {
    val width = size.width
    return if (width.isNaN() || width == 0f) {
        1f
    } else {
        (1f - progress).coerceAtLeast(0.8f)
    }
}

private fun GraphicsLayerScope.calculatePredictiveBackScaleY(progress: Float): Float {
    val height = size.height
    return if (height.isNaN() || height == 0f) {
        1f
    } else {
        (1f - progress).coerceAtLeast(0.8f)
    }
}

object EnhancedTopSheetDefaults {

    val barContainerColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.surface

    val containerColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.surface

    val contentContainerColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.surface

    val scrimColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.scrim.copy(0.32f)

    val animationSpec: AnimationSpec<Float> = tween(
        durationMillis = 600,
        easing = FancyTransitionEasing
    )
}
