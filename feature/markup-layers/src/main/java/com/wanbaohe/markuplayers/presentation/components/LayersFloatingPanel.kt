package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
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
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.longPress
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.press
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import kotlin.math.roundToInt
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material.icons.rounded.Lock

/**
 * 画布右侧浮动图层小面板(设计稿「图片创作」主界面):
 * 紧凑图层列表(长按拖拽排序)+ 背景装饰行 + 选中图层的不透明度滑杆
 * + 底部操作行(删除/复制选中图层)。
 * 点标题区/展开按钮打开完整 [LayersSheet]。仅在有图层时显示。
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
    if (layers.isEmpty()) return

    Column(
        modifier = modifier
            .width(160.dp)
            .clip(ShapeDefaults.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        PanelHeader(
            onAddText = onAddText,
            onAddSticker = onAddSticker,
            onAddImage = onAddImage,
            onExpand = onExpand
        )
        CompactLayerList(component = component)
        BackgroundRow()
        val selected = layers.firstOrNull { it.id == component.selectedLayerId }
        if (selected != null) {
            SelectedLayerOpacity(
                component = component,
                layerId = selected.id,
                alpha = selected.transform.alpha,
                enabled = !selected.transform.locked
            )
        }
        PanelActionRow(component = component)
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
                tint = MaterialTheme.colorScheme.onSurface
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

/** 紧凑图层列表:最多约 3 行,超出可滚;行 = 眼睛 + 缩略图 + 名称,整行长按拖拽排序,松手一次性提交新 z 序 */
@Composable
private fun CompactLayerList(component: MarkupLayersComponent) {
    val layers = component.layers
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
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.heightIn(max = 116.dp)
    ) {
        // 显示倒序(顶层在上)
        itemsIndexed(
            items = displayList,
            key = { _, layer -> layer.id }
        ) { _, layer ->
            ReorderableItem(
                state = reorderState,
                key = layer.id
            ) { _ ->
                CompactLayerRow(
                    layer = layer,
                    layers = layers,
                    isSelected = layer.id == component.selectedLayerId,
                    onSelect = { component.selectLayer(layer.id) },
                    onToggleVisible = { component.toggleLayerVisible(layer.id) },
                    dragModifier = Modifier.longPressDraggableHandle(
                        onDragStarted = { haptics.longPress() },
                        onDragStopped = {
                            component.reorderLayers(displayList.reversed())
                        }
                    )
                )
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
                } else MaterialTheme.colorScheme.surfaceContainer,
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
                MaterialTheme.colorScheme.onSurface
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

/** 背景行:装饰用,不可操作 */
@Composable
private fun BackgroundRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = MaterialIcons.Outlined.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(R.string.markup_layer_background),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        )
        Icon(
            imageVector = MaterialIcons.Rounded.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

/** 当前选中图层的不透明度 */
@Composable
private fun SelectedLayerOpacity(
    component: MarkupLayersComponent,
    layerId: String,
    alpha: Float,
    enabled: Boolean,
) {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(vertical = 4.dp)
    )
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
            text = "${(alpha * 100).roundToInt()}%",
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
    Spacer(Modifier.height(2.dp))
}

/** 底部操作行:删除/复制选中图层,无选中或选中项锁定时置灰 */
@Composable
private fun PanelActionRow(component: MarkupLayersComponent) {
    val selected = component.layers.firstOrNull { it.id == component.selectedLayerId }
    val canModify = selected != null && !selected.transform.locked
    val actionTint = if (canModify) {
        MaterialTheme.colorScheme.onSurface
    } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(vertical = 4.dp)
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        LayerIconButton(
            icon = Icons.Rounded.ContentCopy,
            contentDescription = stringResource(R.string.markup_duplicate_layer),
            onClick = { if (canModify) component.duplicateLayer(selected.id) },
            tint = actionTint
        )
        LayerIconButton(
            icon = Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.markup_delete_layer),
            onClick = { if (canModify) component.removeLayer(selected.id) },
            tint = actionTint
        )
    }
}
