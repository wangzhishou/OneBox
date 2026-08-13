package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Visibility
import com.t8rin.imagetoolbox.core.resources.icons.VisibilityOff
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.longPress
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.press
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.t8rin.imagetoolbox.core.ui.widget.text.TitleItem
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerBlendMode
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import kotlin.math.roundToInt
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen

/**
 * 图层面板(设计稿「图层管理 Tab」完整版):
 * 倒序图层列表(缩略图/显隐/锁定/不透明度/混合模式/长按拖拽排序)
 * + 底部五操作(添加/删除/合并/上移/下移)。
 */
@Composable
internal fun LayersSheet(
    visible: Boolean,
    component: MarkupLayersComponent,
    onDismiss: () -> Unit,
    onAddText: () -> Unit,
    onAddSticker: () -> Unit,
    onAddImage: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        title = {
            TitleItem(text = stringResource(R.string.markup_tool_layers))
        },
        confirmButton = {
            var addMenuVisible by rememberSaveable { mutableStateOf(false) }
            Box {
                EnhancedIconButton(onClick = { addMenuVisible = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.markup_add_layer)
                    )
                }
                AddLayerDropdownMenu(
                    expanded = addMenuVisible,
                    onDismiss = { addMenuVisible = false },
                    onAddText = onAddText,
                    onAddSticker = onAddSticker,
                    onAddImage = onAddImage
                )
            }
        },
        sheetContent = {
            if (component.layers.isEmpty()) {
                Text(
                    text = stringResource(R.string.markup_layers_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LayerReorderList(component = component)
            }
            LayerActionBar(
                component = component,
                onAddText = onAddText,
                onAddSticker = onAddSticker,
                onAddImage = onAddImage
            )
        }
    )
}

/** 可拖拽排序的图层列表,显示倒序(顶层在上),松手时一次性提交新 z 序 */
@Composable
private fun LayerReorderList(component: MarkupLayersComponent) {
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .heightIn(max = 380.dp)
            .padding(horizontal = 16.dp)
    ) {
        itemsIndexed(
            items = displayList,
            key = { _, layer -> layer.id }
        ) { _, layer ->
            ReorderableItem(
                state = reorderState,
                key = layer.id
            ) { isDragging ->
                LayerRow(
                    layer = layer,
                    layers = layers,
                    isSelected = layer.id == component.selectedLayerId,
                    isDragging = isDragging,
                    dragHandleModifier = Modifier.longPressDraggableHandle(
                        onDragStarted = { haptics.longPress() },
                        onDragStopped = {
                            component.reorderLayers(displayList.reversed())
                        }
                    ),
                    onSelect = {
                        if (!layer.transform.locked) component.selectLayer(layer.id)
                    },
                    onToggleVisible = { component.toggleLayerVisible(layer.id) },
                    onToggleLocked = { component.toggleLayerLocked(layer.id) },
                    onAlphaDragStart = component::beginLayerAlphaChange,
                    onAlphaChange = { component.updateLayerAlphaTransient(layer.id, it) },
                    onBlendModeChange = { component.setLayerBlendMode(layer.id, it) }
                )
            }
        }
    }
}

@Composable
private fun LayerRow(
    layer: MarkupLayer,
    layers: List<MarkupLayer>,
    isSelected: Boolean,
    isDragging: Boolean,
    dragHandleModifier: Modifier,
    onSelect: () -> Unit,
    onToggleVisible: () -> Unit,
    onToggleLocked: () -> Unit,
    onAlphaDragStart: () -> Unit,
    onAlphaChange: (Float) -> Unit,
    onBlendModeChange: (LayerBlendMode) -> Unit,
) {
    val locked = layer.transform.locked
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animateFloatAsState(if (isDragging) 1.02f else 1f).value)
            .container(
                shape = ShapeDefaults.large,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else MaterialTheme.colorScheme.surfaceContainer,
                resultPadding = 0.dp
            )
            .clickable(enabled = !locked, onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        LayerStateToggles(
            visible = layer.transform.visible,
            locked = locked,
            onToggleVisible = onToggleVisible,
            onToggleLocked = onToggleLocked
        )
        LayerThumbnail(
            layer = layer,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = layerDisplayName(layer, layers),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.markup_layer_opacity),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LayerOpacitySlider(
                    alpha = layer.transform.alpha,
                    enabled = !locked,
                    onDragStart = onAlphaDragStart,
                    onAlphaChange = onAlphaChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "${(layer.transform.alpha * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
        BlendModeDropdown(
            current = layer.transform.blendMode,
            enabled = !locked,
            onChange = onBlendModeChange
        )
        Icon(
            imageVector = MaterialIcons.Rounded.DragHandle,
            contentDescription = stringResource(R.string.markup_drag_reorder),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = dragHandleModifier.padding(4.dp)
        )
    }
}

/** 行首的显隐/锁定两个小开关 */
@Composable
private fun LayerStateToggles(
    visible: Boolean,
    locked: Boolean,
    onToggleVisible: () -> Unit,
    onToggleLocked: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LayerIconButton(
            icon = if (visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
            contentDescription = stringResource(R.string.markup_layer_toggle_visible),
            onClick = onToggleVisible,
            tint = if (visible) {
                MaterialTheme.colorScheme.onSurface
            } else MaterialTheme.colorScheme.onSurfaceVariant
        )
        LayerIconButton(
            icon = if (locked) MaterialIcons.Rounded.Lock else MaterialIcons.Rounded.LockOpen,
            contentDescription = stringResource(R.string.markup_layer_toggle_lock),
            onClick = onToggleLocked,
            tint = if (locked) {
                MaterialTheme.colorScheme.primary
            } else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 混合模式下拉:本期只有「正常」,结构预留扩展 */
@Composable
private fun BlendModeDropdown(
    current: LayerBlendMode,
    enabled: Boolean,
    onChange: (LayerBlendMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(ShapeDefaults.small)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = blendModeName(current),
                style = MaterialTheme.typography.labelSmall
            )
            Icon(
                imageVector = MaterialIcons.Rounded.ExpandMore,
                contentDescription = stringResource(R.string.markup_layer_blend_mode),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LayerBlendMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(blendModeName(mode)) },
                    onClick = {
                        expanded = false
                        onChange(mode)
                    }
                )
            }
        }
    }
}

@Composable
private fun blendModeName(mode: LayerBlendMode): String = when (mode) {
    LayerBlendMode.Normal -> stringResource(R.string.markup_blend_normal)
}

/** 底部五操作:添加/删除/合并/上移/下移,无选中或选中项锁定时对应按钮置灰 */
@Composable
private fun LayerActionBar(
    component: MarkupLayersComponent,
    onAddText: () -> Unit,
    onAddSticker: () -> Unit,
    onAddImage: () -> Unit,
) {
    val layers = component.layers
    val selectedIndex = layers.indexOfFirst { it.id == component.selectedLayerId }
    val selected = layers.getOrNull(selectedIndex)
    val canModify = selected != null && !selected.transform.locked
    val canMergeDown = canModify && layers.take(selectedIndex).any { it.transform.visible }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        var addMenuVisible by rememberSaveable { mutableStateOf(false) }
        Box {
            LayerActionButton(
                icon = Icons.Outlined.Add,
                labelRes = R.string.markup_add_layer,
                enabled = true,
                onClick = { addMenuVisible = true }
            )
            AddLayerDropdownMenu(
                expanded = addMenuVisible,
                onDismiss = { addMenuVisible = false },
                onAddText = onAddText,
                onAddSticker = onAddSticker,
                onAddImage = onAddImage
            )
        }
        LayerActionButton(
            icon = Icons.Outlined.Delete,
            labelRes = R.string.markup_delete_layer,
            enabled = canModify,
            onClick = { selected?.let { component.removeLayer(it.id) } }
        )
        LayerActionButton(
            icon = MaterialIcons.Outlined.Merge,
            labelRes = R.string.markup_merge_layer,
            enabled = canMergeDown,
            onClick = { selected?.let { component.mergeLayerDown(it.id) } }
        )
        LayerActionButton(
            icon = MaterialIcons.Outlined.KeyboardArrowUp,
            labelRes = R.string.markup_move_layer_up,
            enabled = canModify && selectedIndex < layers.lastIndex,
            onClick = { selected?.let { component.moveLayerUp(it.id) } }
        )
        LayerActionButton(
            icon = MaterialIcons.Outlined.KeyboardArrowDown,
            labelRes = R.string.markup_move_layer_down,
            enabled = canModify && selectedIndex > 0,
            onClick = { selected?.let { component.moveLayerDown(it.id) } }
        )
    }
}

@Composable
private fun LayerActionButton(
    icon: ImageVector,
    labelRes: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(ShapeDefaults.default)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(labelRes),
            tint = contentColor,
            modifier = Modifier.size(20.dp)
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
