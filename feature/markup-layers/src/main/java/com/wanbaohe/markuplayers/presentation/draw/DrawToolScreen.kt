package com.wanbaohe.markuplayers.presentation.draw

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Eraser
import com.t8rin.imagetoolbox.core.resources.icons.FreeDraw
import com.t8rin.imagetoolbox.core.resources.icons.Save
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFrontHand
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBar
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.transparencyChecker
import com.t8rin.imagetoolbox.core.ui.widget.text.marquee
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.StrokePoint
import com.wanbaohe.markuplayers.presentation.render.LayerPreviewRenderers
import com.wanbaohe.markuplayers.presentation.render.drawStroke
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import kotlin.math.min
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

/**
 * 画笔全屏工具页(设计稿「绘画工具」):
 * 顶栏(返回=完成/撤销/重做)+ 画布(底图 + 已有图层静态渲染 + 绘画覆盖层)+
 * 常驻底部设置面板 + 底部操作栏(画笔/橡皮擦/浏览/撤销/重做/完成)。
 * 会话状态只在页内 remember,完成时才 addLayer。
 */
@Composable
fun DrawToolScreen(component: MarkupLayersComponent) {
    val session = remember { DrawSessionState() }
    val onFinish = {
        if (session.strokes.isNotEmpty()) {
            component.addLayer(
                MarkupLayer(type = LayerType.Draw(strokes = session.strokes))
            )
        }
        component.setActiveTool(null)
    }
    BackHandler(onBack = onFinish)

    Column(modifier = Modifier.fillMaxSize()) {
        DrawTopBar(session = session, onBack = onFinish)
        DrawCanvas(
            component = component,
            session = session,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        DrawSettingsPanel(session = session)
        DrawBottomBar(session = session, onDone = onFinish)
    }
}

@Composable
private fun DrawTopBar(
    session: DrawSessionState,
    onBack: () -> Unit,
) {
    EnhancedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.markup_draw_title),
                modifier = Modifier.marquee()
            )
        },
        navigationIcon = {
            EnhancedIconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.markup_back)
                )
            }
        },
        actions = {
            EnhancedIconButton(onClick = session::undo, enabled = session.canUndo) {
                Icon(
                    imageVector = Icons.Outlined.LineUndo,
                    contentDescription = stringResource(R.string.markup_undo)
                )
            }
            EnhancedIconButton(onClick = session::redo, enabled = session.canRedo) {
                Icon(
                    imageVector = Icons.Outlined.LineRedo,
                    contentDescription = stringResource(R.string.markup_redo)
                )
            }
        }
    )
}

@Composable
private fun DrawCanvas(
    component: MarkupLayersComponent,
    session: DrawSessionState,
    modifier: Modifier = Modifier,
) {
    val zoomState = rememberZoomState(maxScale = 10f)
    Box(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        // 浏览模式才启用缩放/平移,绘画模式禁用避免手势冲突
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zoomable(zoomState = zoomState, zoomEnabled = session.isPanMode),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = component.bitmap ?: return@Box
            val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val fitScale = min(
                    constraints.maxWidth / bitmap.width.toFloat(),
                    constraints.maxHeight / bitmap.height.toFloat()
                )
                val canvasWidthPx = bitmap.width * fitScale
                val canvasHeightPx = bitmap.height * fitScale
                val density = LocalDensity.current

                Box(
                    modifier = Modifier.size(
                        width = with(density) { canvasWidthPx.toDp() },
                        height = with(density) { canvasHeightPx.toDp() }
                    )
                ) {
                    Picture(
                        model = imageBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .matchParentSize()
                            .clipToBounds()
                            .transparencyChecker()
                    )
                    StaticLayers(layers = component.layers)
                    DrawOverlay(
                        session = session,
                        canvasWidthPx = canvasWidthPx,
                        canvasHeightPx = canvasHeightPx
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.StaticLayers(layers: List<MarkupLayer>) {
    BoxWithConstraints(modifier = Modifier.matchParentSize()) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()
        layers.forEach { layer ->
            key(layer.id) {
                StaticLayer(
                    layer = layer,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                )
            }
        }
    }
}

/** 静态渲染已有图层:套用 transform,但不挂 EditBox 的选择/变换手势 */
@Composable
private fun BoxWithConstraintsScope.StaticLayer(
    layer: MarkupLayer,
    canvasWidth: Float,
    canvasHeight: Float,
) {
    val transform = layer.transform
    if (!transform.visible) return
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .graphicsLayer {
                scaleX = transform.scale
                scaleY = transform.scale
                rotationZ = transform.rotation
                translationX = (transform.centerX - 0.5f) * canvasWidth
                translationY = (transform.centerY - 0.5f) * canvasHeight
                alpha = transform.alpha
            },
        contentAlignment = Alignment.Center
    ) {
        LayerPreviewRenderers.Content(
            layer = layer,
            canvasWidthPx = canvasWidth,
            canvasHeightPx = canvasHeight
        )
    }
}

/** 绘画覆盖层:会话笔画(含进行中笔画),Offscreen 隔离使橡皮擦只清会话笔画 */
@Composable
private fun DrawOverlay(
    session: DrawSessionState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
) {
    val density = LocalDensity.current
    Canvas(
        modifier = Modifier
            .size(
                width = with(density) { canvasWidthPx.toDp() },
                height = with(density) { canvasHeightPx.toDp() }
            )
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawGesture(session, canvasWidthPx, canvasHeightPx)
    ) {
        session.strokes.forEach { drawStroke(it, size.width, size.height) }
        session.inProgressStroke(size.width)?.let {
            drawStroke(it, size.width, size.height)
        }
    }
}

/** 单指绘画手势:记录归一化采样点;第二指落下即收笔,避免与缩放手势互相污染 */
private fun Modifier.drawGesture(
    session: DrawSessionState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
): Modifier = pointerInput(canvasWidthPx, canvasHeightPx, session.isPanMode) {
    if (session.isPanMode) return@pointerInput
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (down.isConsumed) return@awaitEachGesture
        session.beginStroke()
        session.appendPoint(down.position.normalized(canvasWidthPx, canvasHeightPx))
        try {
            while (true) {
                val event = awaitPointerEvent()
                if (event.changes.count { it.pressed } > 1) break
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break
                if (change.positionChanged()) {
                    session.appendPoint(change.position.normalized(canvasWidthPx, canvasHeightPx))
                    change.consume()
                }
            }
        } finally {
            // 手势被取消(如模式切换导致 pointerInput 重启)也要收笔结算,
            // 避免进行中的笔画残留成不落层的"幽灵"预览
            session.finishStroke(canvasWidthPx)
        }
    }
}

private fun Offset.normalized(width: Float, height: Float) = StrokePoint(x / width, y / height)

@Composable
private fun DrawBottomBar(
    session: DrawSessionState,
    onDone: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        DrawBarItem(
            icon = Icons.Rounded.FreeDraw,
            labelRes = R.string.markup_draw_brush_mode,
            active = !session.isEraser && !session.isPanMode,
            onClick = session::enableBrush,
            modifier = Modifier.weight(1f)
        )
        DrawBarItem(
            icon = Icons.Rounded.Eraser,
            labelRes = R.string.markup_draw_eraser_mode,
            active = session.isEraser,
            onClick = session::enableEraser,
            modifier = Modifier.weight(1f)
        )
        DrawBarItem(
            icon = Icons.Outlined.LineFrontHand,
            labelRes = R.string.markup_draw_pan_mode,
            active = session.isPanMode,
            onClick = session::togglePanMode,
            modifier = Modifier.weight(1f)
        )
        DrawBarItem(
            icon = Icons.Outlined.LineUndo,
            labelRes = R.string.markup_undo,
            active = false,
            enabled = session.canUndo,
            onClick = session::undo,
            modifier = Modifier.weight(1f)
        )
        DrawBarItem(
            icon = Icons.Outlined.LineRedo,
            labelRes = R.string.markup_redo,
            active = false,
            enabled = session.canRedo,
            onClick = session::redo,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        EnhancedButton(
            onClick = onDone,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Outlined.Save,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.markup_draw_done))
        }
    }
}

@Composable
private fun DrawBarItem(
    icon: ImageVector,
    labelRes: Int,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(
                if (active) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else Color.Transparent
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(labelRes),
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1
        )
    }
}
