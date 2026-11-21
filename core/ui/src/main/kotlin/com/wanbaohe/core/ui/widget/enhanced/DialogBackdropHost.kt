package com.wanbaohe.core.ui.widget.enhanced

import android.os.Build
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState

internal class DialogBackdrop {
    val owners = mutableStateListOf<() -> Float>()
}

private val LocalAllowBackdropBlur = staticCompositionLocalOf { true }

@Composable
fun EditorPreviewBlurScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAllowBackdropBlur provides false, content = content)
}

internal val LocalDialogBackdrop = staticCompositionLocalOf<DialogBackdrop?> { null }

@Composable
internal fun overlayBackdrop(enabled: Boolean = true): DialogBackdrop? =
    if (enabled && LocalSettingsState.current.isGlassAlphaEnabled) LocalDialogBackdrop.current else null

@Composable
fun isOverlayGlassAvailable(): Boolean = overlayBackdrop() != null

/** 同一绘制树内的浮层只模糊其下方内容，不包含面板和关闭按钮。 */
@Composable
fun Modifier.behindCreatePanel(expanded: Boolean): Modifier {
    if (!isOverlayGlassAvailable()) return this
    val radius = animateDpAsState(if (expanded) 8.dp else 0.dp, tween(if (expanded) 300 else 250))
    return if (radius.value > 0.dp) blur(radius.value) else this
}

fun Modifier.floatingPanelTint(shape: Shape, color: Color): Modifier =
    overlayTint(shape, color, OverlaySurface.Dialog)

@Composable
internal fun HoldOverlayBlur(backdrop: DialogBackdrop?, progress: () -> Float = { 1f }) {
    if (backdrop == null || !LocalAllowBackdropBlur.current) return
    val latest = rememberUpdatedState(progress)
    DisposableEffect(backdrop) {
        val owner = { latest.value() }
        backdrop.owners.add(owner)
        onDispose { backdrop.owners.remove(owner) }
    }
}

internal enum class OverlaySurface(val opacity: Float) {
    Dialog(.88f), Sheet(.94f), Menu(.96f)
}

internal fun Modifier.overlayTint(shape: Shape, color: Color, surface: OverlaySurface) =
    drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        onDrawBehind { drawOutline(outline, color, alpha = surface.opacity) }
    }

internal const val OverlayScrimAlpha = .28f

/** 只处理页面像素；挂在 decorView 上的弹窗不在这个绘制层里。 */
@Composable
fun DialogBackdropHost(content: @Composable () -> Unit) {
    val available = Build.VERSION.SDK_INT >= 31 && LocalView.current.isHardwareAccelerated
    val backdrop = remember(available) { if (available) DialogBackdrop() else null }
    CompositionLocalProvider(LocalDialogBackdrop provides backdrop) {
        val fraction = backdrop?.owners?.maxOfOrNull { it() } ?: 0f
        Box(
            modifier = Modifier.fillMaxSize().then(if (fraction > 0f) Modifier.blur(8.dp * fraction) else Modifier),
            propagateMinConstraints = true
        ) { content() }
    }
}
