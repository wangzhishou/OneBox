package com.shifenmiao.base.ui.popup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.shifenmiao.base.ui.modifier.clickableWithoutRipple

class CenterBottomPositionProvider(
    private val screenWidth: Int
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val halfScreenWidth = screenWidth / 2
        val x = anchorBounds.center.x - halfScreenWidth
        val y = anchorBounds.bottom
        return IntOffset(x, y)
    }
}

@Composable
fun BottomPositionPopup(
    visible: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
    properties: PopupProperties = PopupProperties(),
    enterTransition: EnterTransition = slideInVertically(
        animationSpec = tween(150),
        initialOffsetY = { it }
    ),
    exitTransition: ExitTransition = slideOutVertically(
        animationSpec = tween(150),
        targetOffsetY = { it }
    ),
    content: @Composable () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val updatedContent by rememberUpdatedState(content)
    val positionProvider = remember { CenterBottomPositionProvider(screenWidth) }
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = properties,
        content = {
            AnimatedVisibility(
                visible = visible,
                enter = enterTransition,
                exit = exitTransition
            ) {
                updatedContent()
            }
        }
    )
}

@Composable
fun MaskedPopup(
    visible: Boolean = true,
    onDismissRequest: () -> Unit,
    properties: PopupProperties = PopupProperties(usePlatformDefaultWidth = false),
    enterTransition: EnterTransition = slideInVertically(
        animationSpec = tween(150),
        initialOffsetY = { it }
    ),
    exitTransition: ExitTransition = slideOutVertically(
        animationSpec = tween(150),
        targetOffsetY = { it }
    ),
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = enterTransition,
        exit = exitTransition
    ) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickableWithoutRipple {
                            onDismissRequest()
                        }
                )
                content()
            }
        }
    }
}