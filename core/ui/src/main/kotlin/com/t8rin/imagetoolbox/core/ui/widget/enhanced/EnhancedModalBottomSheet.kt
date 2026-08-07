package com.t8rin.imagetoolbox.core.ui.widget.enhanced

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.animation.FancyTransitionEasing
import com.t8rin.imagetoolbox.core.ui.utils.helper.PredictiveBackObserver
import com.t8rin.imagetoolbox.core.ui.utils.provider.ProvideContainerDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.CornerSides
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.drawHorizontalStroke
import com.t8rin.imagetoolbox.core.ui.widget.modifier.materialShadow
import com.t8rin.imagetoolbox.core.ui.widget.modifier.only
import com.t8rin.modalsheet.ModalBottomSheetValue
import com.t8rin.modalsheet.ModalSheet
import com.t8rin.modalsheet.rememberModalBottomSheetState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun EnhancedModalBottomSheet(
    nestedScrollEnabled: Boolean = false,
    cancelable: Boolean = true,
    dragHandle: @Composable ColumnScope.() -> Unit = { EnhancedModalSheetDragHandle() },
    visible: Boolean,
    onDismiss: (Boolean) -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    EnhancedModalSheetImpl(
        cancelable = cancelable,
        nestedScrollEnabled = nestedScrollEnabled,
        dragHandle = dragHandle,
        visible = visible,
        onVisibleChange = onDismiss,
        content = sheetContent
    )
}

@Composable
fun EnhancedModalBottomSheet(
    nestedScrollEnabled: Boolean,
    cancelable: Boolean = true,
    confirmButton: @Composable RowScope.() -> Unit,
    dragHandle: @Composable ColumnScope.() -> Unit = { EnhancedModalSheetDragHandle() },
    title: @Composable () -> Unit,
    endConfirmButtonPadding: Dp = 16.dp,
    visible: Boolean,
    onDismiss: (Boolean) -> Unit,
    enableBackHandler: Boolean = true,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    EnhancedModalBottomSheet(
        nestedScrollEnabled = nestedScrollEnabled,
        cancelable = cancelable,
        confirmButton = confirmButton,
        dragHandle = dragHandle,
        title = title,
        endConfirmButtonPadding = endConfirmButtonPadding,
        visible = visible,
        onDismiss = onDismiss,
        enableBackHandler = enableBackHandler,
        enableGlass = true,
        sheetContent = sheetContent
    )
}

@Composable
fun EnhancedModalBottomSheet(
    nestedScrollEnabled: Boolean,
    cancelable: Boolean = true,
    confirmButton: @Composable RowScope.() -> Unit,
    dragHandle: @Composable ColumnScope.() -> Unit = { EnhancedModalSheetDragHandle() },
    title: @Composable () -> Unit,
    endConfirmButtonPadding: Dp = 16.dp,
    visible: Boolean,
    onDismiss: (Boolean) -> Unit,
    enableBackHandler: Boolean = true,
    enableGlass: Boolean,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    val settingsState = LocalSettingsState.current
    val useGlass = enableGlass && settingsState.isGlassAlphaEnabled

    EnhancedModalSheetImpl(
        cancelable = cancelable,
        nestedScrollEnabled = nestedScrollEnabled,
        dragHandle = dragHandle,
        visible = visible,
        onVisibleChange = onDismiss,
        enableBackHandler = enableBackHandler,
        content = {
            Column(
                modifier = Modifier.weight(1f, false),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = sheetContent
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (useGlass) {
                        Modifier.background(
                            color = EnhancedBottomSheetDefaults.barContainerColor,
                        )
                    } else {
                        Modifier.drawHorizontalStroke(true, autoElevation = 6.dp).background(
                            EnhancedBottomSheetDefaults.barContainerColor
                        )
                    })
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .padding(end = endConfirmButtonPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                title()
                Spacer(modifier = Modifier.weight(1f))
                confirmButton()
            }
        }
    )
}

@Composable
fun EnhancedModalBottomSheet(
    nestedScrollEnabled: Boolean = false,
    cancelable: Boolean = true,
    confirmButton: (@Composable RowScope.() -> Unit)? = null,
    dragHandle: @Composable ColumnScope.() -> Unit = { EnhancedModalSheetDragHandle() },
    title: (@Composable RowScope.() -> Unit)? = null,
    visible: Boolean,
    onDismiss: (Boolean) -> Unit,
    enableBackHandler: Boolean = true,
    enableBottomContentWeight: Boolean = true,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    EnhancedModalBottomSheet(
        nestedScrollEnabled = nestedScrollEnabled,
        cancelable = cancelable,
        confirmButton = confirmButton,
        dragHandle = dragHandle,
        title = title,
        visible = visible,
        onDismiss = onDismiss,
        enableBackHandler = enableBackHandler,
        enableBottomContentWeight = enableBottomContentWeight,
        enableGlass = true,
        sheetContent = sheetContent
    )
}

@Composable
fun EnhancedModalBottomSheet(
    nestedScrollEnabled: Boolean = false,
    cancelable: Boolean = true,
    confirmButton: (@Composable RowScope.() -> Unit)? = null,
    dragHandle: @Composable ColumnScope.() -> Unit = { EnhancedModalSheetDragHandle() },
    title: (@Composable RowScope.() -> Unit)? = null,
    visible: Boolean,
    onDismiss: (Boolean) -> Unit,
    enableBackHandler: Boolean = true,
    enableBottomContentWeight: Boolean = true,
    enableGlass: Boolean,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    val settingsState = LocalSettingsState.current
    val useGlass = enableGlass && settingsState.isGlassAlphaEnabled

    EnhancedModalSheetImpl(
        cancelable = cancelable,
        nestedScrollEnabled = nestedScrollEnabled,
        dragHandle = dragHandle,
        visible = visible,
        onVisibleChange = onDismiss,
        enableBackHandler = enableBackHandler,
        content = {
            Column(
                modifier = Modifier.weight(1f, false),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = sheetContent
            )
            if (confirmButton != null && title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (useGlass) Modifier else Modifier.drawHorizontalStroke(true, autoElevation = 6.dp))
                        .background(if (useGlass) Color.Transparent else EnhancedBottomSheetDefaults.barContainerColor)
                        .navigationBarsPadding()
                        .padding(vertical = 12.dp)
                        .then(
                            if (enableBottomContentWeight) Modifier.padding(end = 16.dp)
                            else Modifier
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.then(
                            if (enableBottomContentWeight) {
                                Modifier.weight(1f)
                            } else Modifier
                        )
                    ) {
                        title()
                    }
                    confirmButton()
                }
            }
        }
    )
}


@Composable
private fun EnhancedModalSheetImpl(
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    dragHandle: @Composable ColumnScope.() -> Unit = { EnhancedModalSheetDragHandle() },
    nestedScrollEnabled: Boolean = false,
    animationSpec: AnimationSpec<Float> = EnhancedBottomSheetDefaults.animationSpec,
    sheetModifier: Modifier = Modifier,
    cancelable: Boolean = true,
    skipHalfExpanded: Boolean = true,
    shape: Shape = ShapeDefaults.extremeLarge.only(CornerSides.Top),
    elevation: Dp = 0.dp,
    containerColor: Color = EnhancedBottomSheetDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    scrimColor: Color = EnhancedBottomSheetDefaults.scrimColor,
    enableBackHandler: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val animatedPredictiveBackProgress by animateFloatAsState(predictiveBackProgress)

    val resetPredictiveBack: () -> Unit = { predictiveBackProgress = 0f }

    // Reset the predictive-back scale as soon as the sheet starts hiding. The previous
    // version delayed this by 300ms to hide the scale-up animation behind the slide-down,
    // but that meant a fast "close → re-open" within 300ms would re-launch this effect
    // and cancel the pending reset, leaving predictiveBackProgress at 1.0 and the sheet
    // showing up at 0.8× scale — which the user reports as "the second click does nothing".
    // Letting the scale-up run concurrently with the slide-down is the lesser visual cost.
    LaunchedEffect(visible) {
        if (!visible) {
            resetPredictiveBack()
        }
    }

    // Hold cancelable flag internally and set to true when modal sheet is dismissed via "visible" property in
    // non-cancellable modal sheet. This ensures that "confirmValueChange" will return true when sheet is set to hidden
    // state.
    val internalCancelable = remember { mutableStateOf(cancelable) }
    val sheetState = rememberModalBottomSheetState(
        skipHalfExpanded = skipHalfExpanded,
        initialValue = ModalBottomSheetValue.Hidden,
        animationSpec = animationSpec,
        confirmValueChange = {
            if (it == ModalBottomSheetValue.Hidden && !internalCancelable.value) {
                return@rememberModalBottomSheetState false
            }
            true
        },
    )
    val scope = rememberCoroutineScope()
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(visible, cancelable) {
        if (visible) {
            internalCancelable.value = cancelable
            isAnimating = true
            scope.launch {
                sheetState.show()
                isAnimating = false
            }
        } else {
            internalCancelable.value = true
            isAnimating = true
            scope.launch {
                sheetState.hide()
                isAnimating = false
            }
        }
    }

    LaunchedEffect(sheetState.currentValue, sheetState.targetValue, sheetState.progress) {
        delay(600.milliseconds)
        if (sheetState.progress == 1f && sheetState.currentValue == sheetState.targetValue) {
            val newVisible = sheetState.isVisible
            if (newVisible != visible) {
                onVisibleChange(newVisible)
            }
        }
    }

    if (!visible && sheetState.currentValue == sheetState.targetValue && !sheetState.isVisible && !isAnimating) return

    val settingsState = LocalSettingsState.current

    ProvideContainerDefaults(
        color = EnhancedBottomSheetDefaults.contentContainerColor
    ) {
        ModalSheet(
            sheetState = sheetState,
            onDismiss = {
                if (cancelable) {
                    onVisibleChange(false)
                }
            },
            dragHandle = dragHandle,
            nestedScrollEnabled = nestedScrollEnabled,
            sheetModifier = sheetModifier
                .statusBarsPadding()
                .graphicsLayer {
                    val sheetOffset = 0f
                    val sheetHeight = size.height
                    if (!sheetOffset.isNaN() && !sheetHeight.isNaN() && sheetHeight != 0f) {
                        val progress = animatedPredictiveBackProgress
                        scaleX = calculatePredictiveBackScaleX(progress)
                        scaleY = calculatePredictiveBackScaleY(progress)
                        transformOrigin = TransformOrigin(
                            pivotFractionX = 0.5f,
                            pivotFractionY = (sheetOffset + sheetHeight) / sheetHeight
                        )
                    }
                }
                // Single shadow pass. elevation stays constant; the inner animateDpAsState
                // inside materialShadow handles the 16dp ↔ 0dp transition when
                // drawContainerShadows toggles, so we don't need an outer Animatable here.
                .materialShadow(
                    shape = shape,
                    elevation = 16.dp,
                    enabled = settingsState.drawContainerShadows,
                    isClipped = true
                )
                .clip(shape),
            modifier = modifier,
            shape = shape,
            elevation = elevation,
            containerColor = containerColor,
            contentColor = contentColor,
            scrimColor = scrimColor,
            content = {
                PredictiveBackObserver(
                    onProgress = { progress ->
                        predictiveBackProgress = progress
                    },
                    // onClean's only job now is to translate a completed back-gesture into
                    // onVisibleChange(false). The 400ms delay + resetPredictiveBack() were
                    // redundant with the LaunchedEffect(visible) above and actively harmful
                    // for fast close→reopen: the parent state was set visible=false here
                    // (re-arming the 400ms wait), and if the user re-opened within 400ms the
                    // reset was skipped, leaving the sheet at 0.8× scale.
                    onClean = { isCompleted ->
                        if (isCompleted) {
                            onVisibleChange(false)
                        }
                    },
                    enabled = visible && enableBackHandler
                )
                content()
            },
        )
    }
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

object EnhancedBottomSheetDefaults {
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
        durationMillis = 300,
        easing = FancyTransitionEasing
    )

    val dragHandleHeight: Dp = 4.dp

}