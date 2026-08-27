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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.emoji.Emoji
import com.t8rin.imagetoolbox.core.resources.icons.FreeDraw
import com.t8rin.imagetoolbox.core.resources.icons.Star
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRestartAlt
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedLoadingIndicator
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.longPress
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.press
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.t8rin.imagetoolbox.core.ui.widget.modifier.meshGradient
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.ElementLayer
import com.wanbaohe.textcard.domain.model.ImageElementStatus
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff

/**
 * 图层面板(设计稿 05 演进):每个元素一层——文字块/装饰可排序(长按拖拽)、
 * 显隐(眼睛)、锁定、删除(文字至少保留一块);背景钉在最底,仅显隐。
 * 显示顺序顶层在上(列表倒序),松手一次性提交新 z 序。
 */
@Composable
fun LayersPanel(
    component: TextCardComponent,
) {
    PanelTitle(R.string.textcard_layers_title)

    val layers = component.elementLayers
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
            .heightIn(max = 320.dp)
    ) {
        itemsIndexed(
            items = displayList,
            key = { _, layer -> layer.elementId }
        ) { _, layer ->
            ReorderableItem(
                state = reorderState,
                key = layer.elementId,
                enabled = !layer.locked
            ) { _ ->
                ElementLayerRow(
                    layer = layer,
                    component = component,
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
        // 背景层钉在列表底部(最底层),仅显隐,不参与排序
        item(key = "background") {
            BackgroundLayerRow(component = component)
        }
    }
}

@Composable
private fun ElementLayerRow(
    layer: ElementLayer,
    component: TextCardComponent,
    dragModifier: Modifier,
) {
    val canDelete = when (layer.kind) {
        ElementLayer.Kind.Text -> component.textBlocks.size > 1
        ElementLayer.Kind.Decoration -> true
        ElementLayer.Kind.Image -> true
        ElementLayer.Kind.Shape -> true
        ElementLayer.Kind.Draw -> true
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .container(
                shape = ShapeDefaults.large,
                resultPadding = 0.dp
            )
            .clickable(enabled = !layer.locked) {
                component.selectElement(layer.elementId)
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        LayerThumb(layer = layer, component = component)
        Text(
            text = layerName(layer, component),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
            onClick = { component.toggleLayerVisible(layer.elementId) }
        )
        LayerAction(
            icon = if (layer.locked) {
                MaterialIcons.Outlined.Lock
            } else MaterialIcons.Outlined.LockOpen,
            contentDescription = stringResource(R.string.textcard_layer_toggle_locked),
            enabled = true,
            onClick = { component.toggleLayerLocked(layer.elementId) }
        )
        // 重置位置:误拖出可视区域时一键回初始位置(文字回基准位/装饰回角落/图片回铺满)
        LayerAction(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRestartAlt,
            contentDescription = stringResource(R.string.textcard_layer_reset_position),
            enabled = !layer.locked,
            onClick = { component.resetElementTransform(layer.elementId) }
        )
        LayerAction(
            icon = MaterialIcons.Outlined.Delete,
            contentDescription = stringResource(R.string.textcard_delete_selected),
            enabled = canDelete && !layer.locked,
            onClick = { component.removeElement(layer.elementId) }
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

/** 元素层名称:文字块取内容首行前几个字,装饰/图片/形状/画笔用通用名 */
@Composable
private fun layerName(layer: ElementLayer, component: TextCardComponent): String =
    when (layer.kind) {
        ElementLayer.Kind.Text -> {
            val block = component.textBlocks.find { it.id == layer.elementId }
            block?.content?.lineSequence()?.firstOrNull()?.take(6)
                ?.ifEmpty { null }
                ?: stringResource(R.string.textcard_layer_text)
        }

        ElementLayer.Kind.Decoration -> stringResource(R.string.textcard_layer_decoration)
        ElementLayer.Kind.Image -> stringResource(R.string.textcard_layer_image)
        ElementLayer.Kind.Shape -> stringResource(R.string.textcard_layer_shape)
        ElementLayer.Kind.Draw -> stringResource(R.string.textcard_draw)
    }

/** 背景层行:当前背景 mini 预览 + 眼睛,钉在列表底部不可排序/删除 */
@Composable
private fun BackgroundLayerRow(component: TextCardComponent) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .container(shape = ShapeDefaults.large, resultPadding = 0.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(ShapeDefaults.small)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            when (val bg = component.background) {
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
        }
        Text(
            text = stringResource(R.string.textcard_layer_background),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )
        LayerAction(
            icon = if (component.backgroundVisible) {
                MaterialIcons.Outlined.Visibility
            } else MaterialIcons.Outlined.VisibilityOff,
            contentDescription = stringResource(R.string.textcard_layer_toggle_visible),
            enabled = true,
            onClick = component::toggleBackgroundVisible
        )
    }
}

/** 图层缩略图标:文字=T 图标,装饰=对应 emoji 贴纸,图片=内容缩略图,形状/画笔=类型图标 */
@Composable
private fun LayerThumb(
    layer: ElementLayer,
    component: TextCardComponent,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(ShapeDefaults.small)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        when (layer.kind) {
            ElementLayer.Kind.Text -> Icon(
                imageVector = MaterialIcons.Outlined.TextFields,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ElementLayer.Kind.Decoration -> {
                val emojis = Emoji.allIcons()
                val decoration = component.decorations.find { it.id == layer.elementId }
                val model = decoration?.assetPath?.let { "file:///android_asset/$it" }
                    ?: emojis.getOrNull(decoration?.emojiIndex ?: -1)
                if (model != null) {
                    Picture(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        showTransparencyChecker = false,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            ElementLayer.Kind.Image -> {
                val element = component.imageElements.find { it.id == layer.elementId }
                when (element?.status) {
                    ImageElementStatus.Ready -> Picture(
                        model = element.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        showTransparencyChecker = false,
                        modifier = Modifier.size(36.dp)
                    )

                    ImageElementStatus.Loading -> EnhancedLoadingIndicator(
                        modifier = Modifier.size(20.dp)
                    )

                    ImageElementStatus.Error -> Icon(
                        imageVector = MaterialIcons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )

                    null -> Unit
                }
            }

            ElementLayer.Kind.Shape -> Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ElementLayer.Kind.Draw -> Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.FreeDraw,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
