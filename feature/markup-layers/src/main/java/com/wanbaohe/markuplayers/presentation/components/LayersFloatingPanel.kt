package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Visibility
import com.t8rin.imagetoolbox.core.resources.icons.VisibilityOff
import com.wanbaohe.core.ui.widget.enhanced.longPress
import com.wanbaohe.core.ui.widget.enhanced.press
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.container
import com.wanbaohe.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import kotlin.math.roundToInt
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.UnfoldMore

/**
 * 画布右侧浮动图层小面板(设计稿「图片创作」主界面):
 * 紧凑图层列表(长按拖拽排序)+ 背景层行(底图缩略图,可点 = 取消图层选中,
 * 滤镜/调节目标回到背景图)+ 底部操作行(删除/复制选中图层)。
 * 面板始终可打开:零图层时列表区显示空提示,背景层行保留。
 * 选中图层的不透明度滑杆在玻璃容器外(面板下方独立一行,无背景)。
 * 点标题区/展开按钮打开完整 [LayersSheet]。
 */
@Composable
internal fun LayersFloatingPanel(
    component: MarkupLayersComponent,
    onAddText: () -> Unit,
    onAddSticker: () -> Unit,
    onAddImage: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layers = component.layers

    Column(modifier = modifier.width(240.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassBackground(
                    style = GlassStyle.None,
                    shape = ShapeDefaults.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            PanelHeader(
                onAddText = onAddText,
                onAddSticker = onAddSticker,
                onAddImage = onAddImage,
                onExpand = onExpand
            )
            if (layers.isEmpty()) {
                Text(
                    text = stringResource(R.string.markup_layers_empty),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            // 背景层在列表内随图层一起拖拽排序
            CompactLayerList(component = component)
            PanelActionRow(component = component)
        }
        val selected = layers.firstOrNull { it.id == component.selectedLayerId }
        if (selected != null) {
            SelectedLayerOpacity(
                component = component,
                layerId = selected.id,
                alpha = selected.transform.alpha,
                enabled = !selected.transform.locked
            )
        }
    }
}

@Composable
private fun PanelHeader(
    onAddText: () -> Unit,
    onAddSticker: () -> Unit,
    onAddImage: () -> Unit,
    onExpand: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.markup_tool_layers),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .weight(1f)
                .clip(ShapeDefaults.small)
                .clickable(onClick = onExpand)
                .heightIn(min = 48.dp)
                .padding(vertical = 4.dp)
        )
        LayerIconButton(
            icon = MaterialIcons.Outlined.UnfoldMore,
            contentDescription = stringResource(R.string.markup_expand_layers),
            onClick = onExpand
        )
        var addMenuVisible by rememberSaveable { mutableStateOf(false) }
        Box {
            LayerIconButton(
                icon = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.markup_add_layer),
                onClick = { addMenuVisible = true },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AddLayerDropdownMenu(
                expanded = addMenuVisible,
                onDismiss = { addMenuVisible = false },
                onAddText = onAddText,
                onAddSticker = onAddSticker,
                onAddImage = onAddImage
            )
        }
    }
}

/** 紧凑图层列表:最多约 3 行,超出可滚;行 = 眼睛 + 缩略图 + 名称,整行长按拖拽排序,松手一次性提交新 z 序;
 * 背景层作为末位条目(默认)随列表一起排序 */
@Composable
private fun CompactLayerList(component: MarkupLayersComponent) {
    val layers = component.layers
    val baseOrder = component.baseOrder.coerceIn(0, layers.size)
    var displayList by remember(layers, baseOrder) {
        mutableStateOf(
            buildList<LayerListEntry> {
                layers.asReversed().forEach { add(LayerListEntry.LayerEntry(it)) }
                add(layers.size - baseOrder, LayerListEntry.BackgroundEntry)
            }
        )
    }
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
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.heightIn(max = 168.dp)
    ) {
        // 显示倒序(顶层在上)
        itemsIndexed(
            items = displayList,
            key = { _, entry -> entry.key }
        ) { _, entry ->
            ReorderableItem(
                state = reorderState,
                key = entry.key
            ) { _ ->
                val dragModifier = Modifier.longPressDraggableHandle(
                    onDragStarted = { haptics.longPress() },
                    onDragStopped = { commitLayerReorder(component, displayList) }
                )
                when (entry) {
                    is LayerListEntry.LayerEntry -> {
                        val layer = entry.layer
                        CompactLayerRow(
                            layer = layer,
                            layers = layers,
                            isSelected = layer.id in component.selectedLayerIds,
                            onSelect = { component.selectLayer(layer.id) },
                            onToggleVisible = { component.toggleLayerVisible(layer.id) },
                            dragModifier = dragModifier
                        )
                    }

                    LayerListEntry.BackgroundEntry -> CompactBackgroundRow(
                        component = component,
                        dragModifier = dragModifier
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactLayerRow(
    layer: MarkupLayer,
    layers: List<MarkupLayer>,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleVisible: () -> Unit,
    dragModifier: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .container(
                shape = ShapeDefaults.small,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else MaterialTheme.colorScheme.surfaceContainerLow,
                resultPadding = 0.dp
            )
            .clickable(
                enabled = !layer.transform.locked,
                onClick = onSelect
            )
            .then(dragModifier)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        LayerIconButton(
            icon = if (layer.transform.visible) {
                Icons.Rounded.Visibility
            } else Icons.Rounded.VisibilityOff,
            contentDescription = stringResource(R.string.markup_layer_toggle_visible),
            onClick = onToggleVisible,
            tint = if (layer.transform.visible) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else MaterialTheme.colorScheme.onSurfaceVariant
        )
        LayerThumbnail(layer = layer, size = 28.dp)
        Text(
            text = layerDisplayName(layer, layers),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

/** 紧凑背景层行:眼睛 + 底图缩略图 + 名称 + 删除;可点 = 取消图层选中(滤镜/调节目标回到背景图),
 * 无选中图层时高亮;整行长按拖拽调整背景 z 序,删除 = 替换为同尺寸透明底(可撤销) */
@Composable
private fun CompactBackgroundRow(
    component: MarkupLayersComponent,
    dragModifier: Modifier,
) {
    val isTarget = component.selectedLayerId == null
    val visible = component.baseVisible
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .container(
                shape = ShapeDefaults.small,
                color = if (isTarget) {
                    MaterialTheme.colorScheme.primaryContainer
                } else Color.Transparent,
                resultPadding = 0.dp
            )
            .clickable { component.selectLayer(null) }
            .then(dragModifier)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        LayerIconButton(
            icon = if (visible) {
                Icons.Rounded.Visibility
            } else Icons.Rounded.VisibilityOff,
            contentDescription = stringResource(R.string.markup_layer_toggle_visible),
            onClick = component::toggleBaseVisible
        )
        Picture(
            model = component.displayBitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(28.dp)
                .clip(ShapeDefaults.small)
                .transparencyChecker()
                .graphicsLayer { alpha = if (visible) 1f else 0.4f }
        )
        Text(
            text = stringResource(R.string.markup_layer_background),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        )
        LayerIconButton(
            icon = Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.studio_layers_delete),
            onClick = component::removeBaseBackground
        )
    }
}

/** 当前选中图层的不透明度:面板玻璃容器外、下方独立一行,无背景容器 */
@Composable
private fun SelectedLayerOpacity(
    component: MarkupLayersComponent,
    layerId: String,
    alpha: Float,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.markup_layer_opacity),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.studio_percent, (alpha * 100).roundToInt()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LayerOpacitySlider(
            alpha = alpha,
            enabled = enabled,
            onDragStart = component::beginLayerAlphaChange,
            onAlphaChange = { component.updateLayerAlphaTransient(layerId, it) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** 底部操作行:删除/复制选中图层,无选中或选中项锁定时置灰 */
@Composable
private fun PanelActionRow(component: MarkupLayersComponent) {
    val canModify = component.layers.any {
        it.id in component.selectedLayerIds && !it.transform.locked
    }
    val actionTint = if (canModify) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        LayerIconButton(
            icon = Icons.Rounded.ContentCopy,
            contentDescription = stringResource(R.string.studio_layers_duplicate),
            onClick = component::duplicateSelectedLayers,
            tint = actionTint,
            enabled = canModify
        )
        LayerIconButton(
            icon = Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.studio_layers_delete),
            onClick = component::removeSelectedLayers,
            tint = actionTint,
            enabled = canModify
        )
    }
}
