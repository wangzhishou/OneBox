package com.wanbaohe.textcard.presentation.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.emoji.Emoji
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.longPress
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.press
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.t8rin.imagetoolbox.core.ui.widget.modifier.meshGradient
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.TextCardLayer
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff

/**
 * 图层面板(设计稿 05):固定三层(背景/文字/装饰),显示顺序顶层在上;
 * 行 = 缩略图标 + 名称 + 眼睛 + 锁 + 拖拽手柄(长按拖拽排序,松手一次性提交新 z 序)。
 * 点装饰行打开装饰选择 Sheet。
 */
@Composable
fun LayersPanel(
    component: TextCardComponent,
    onEditDecoration: () -> Unit,
) {
    PanelTitle(R.string.textcard_layers_title)

    val layers = component.layers
    // 显示倒序(顶层在上),提交时反转回 z 序
    var displayList by remember(layers) { mutableStateOf(layers.asReversed()) }
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    val reorderState = rememberReorderableLazyListState(
        lazyListState = listState,
        onMove = { from, to ->
            haptics.press()
            displayList = displayList.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    // 宿主在可滚动弹层内:LazyColumn 必须有界,避免拿到无限高度约束
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
    ) {
        itemsIndexed(
            items = displayList,
            key = { _, layer -> layer.nameRes }
        ) { _, layer ->
            ReorderableItem(
                state = reorderState,
                key = layer.nameRes,
                enabled = !layer.locked
            ) { _ ->
                LayerRow(
                    layer = layer,
                    component = component,
                    onToggleVisible = {
                        val index = layers.indexOf(layer)
                        if (index >= 0) component.toggleLayerVisible(index)
                    },
                    onToggleLocked = {
                        val index = layers.indexOf(layer)
                        if (index >= 0) component.toggleLayerLocked(index)
                    },
                    onClick = {
                        if (layer is TextCardLayer.Decoration) onEditDecoration()
                    },
                    dragModifier = if (layer.locked) {
                        Modifier
                    } else {
                        Modifier.longPressDraggableHandle(
                            onDragStarted = { haptics.longPress() },
                            onDragStopped = {
                                component.reorderLayers(displayList.asReversed())
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun LayerRow(
    layer: TextCardLayer,
    component: TextCardComponent,
    onToggleVisible: () -> Unit,
    onToggleLocked: () -> Unit,
    onClick: () -> Unit,
    dragModifier: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .container(
                shape = ShapeDefaults.large,
                resultPadding = 0.dp
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        LayerThumb(layer = layer, component = component)
        Text(
            text = stringResource(layer.nameRes),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )
        LayerAction(
            icon = if (layer.visible) {
                MaterialIcons.Outlined.Visibility
            } else MaterialIcons.Outlined.VisibilityOff,
            contentDescription = stringResource(R.string.textcard_layer_toggle_visible),
            enabled = !layer.locked,
            onClick = onToggleVisible
        )
        LayerAction(
            icon = if (layer.locked) {
                MaterialIcons.Outlined.Lock
            } else MaterialIcons.Outlined.LockOpen,
            contentDescription = stringResource(R.string.textcard_layer_toggle_locked),
            enabled = true,
            onClick = onToggleLocked
        )
        Icon(
            imageVector = MaterialIcons.Outlined.DragHandle,
            contentDescription = null,
            tint = if (layer.locked) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            } else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(start = 8.dp)
                .then(dragModifier)
        )
    }
}

/** 图层缩略图标:背景=当前背景 mini 预览,文字=T 图标,装饰=当前 emoji */
@Composable
private fun LayerThumb(
    layer: TextCardLayer,
    component: TextCardComponent,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(ShapeDefaults.small)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        when (layer) {
            is TextCardLayer.Background -> when (val bg = component.background) {
                is BackgroundSpec.Gradient -> Box(
                    modifier = Modifier
                        .size(36.dp)
                        .meshGradient(
                            points = bg.toPointPairs(),
                            resolutionX = MESH_RESOLUTION,
                            resolutionY = MESH_RESOLUTION
                        )
                )

                is BackgroundSpec.Image -> Picture(
                    model = bg.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    showTransparencyChecker = false,
                    modifier = Modifier.size(36.dp)
                )

                else -> Unit
            }

            is TextCardLayer.Text -> Icon(
                imageVector = MaterialIcons.Outlined.TextFields,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            is TextCardLayer.Decoration -> {
                val emojis = Emoji.allIcons()
                val uri = emojis.getOrNull(component.decoration.emojiIndex ?: -1)
                if (uri != null) {
                    Picture(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        showTransparencyChecker = false,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerAction(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = if (enabled) {
            MaterialTheme.colorScheme.onSurface
        } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .padding(start = 8.dp)
            .clip(ShapeDefaults.small)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(4.dp)
    )
}
