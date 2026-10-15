package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Visibility
import com.t8rin.imagetoolbox.core.resources.icons.VisibilityOff
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSlider
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.longPress
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.press
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.t8rin.imagetoolbox.core.ui.widget.modifier.transparencyChecker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import android.net.Uri
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerBlendMode
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import kotlin.math.roundToInt
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedDropdownMenu

/** Phone-first layer management; detailed controls stay off the canvas. */
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
        dragHandle = {},
        onDismiss = { onDismiss() },
        title = {},
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
            SheetHeader(
                title = stringResource(R.string.markup_tool_layers),
                onClose = onDismiss
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.studio_layers_multiselect_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                if (component.selectedLayerIds.isNotEmpty()) {
                    LayerBatchActions(component)
                }
                if (component.layers.isEmpty()) {
                    Text(
                        text = stringResource(R.string.markup_layers_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                // 背景层在列表内随图层一起拖拽排序
                LayerReorderList(component = component)
                component.selectedLayer?.let { layer ->
                    SelectedLayerProperties(component = component, layer = layer)
                }
                LayerActionBar(component = component)
            }
        }
    )
}

/** 可拖拽排序列表的条目:普通图层或背景层(背景不在最底层时随列表移动) */
internal sealed interface LayerListEntry {
    val key: String

    data class LayerEntry(val layer: MarkupLayer) : LayerListEntry {
        override val key: String get() = layer.id
    }

    data object BackgroundEntry : LayerListEntry {
        override val key: String get() = "__background__"
    }
}

/** 松手提交:拆出图层新 z 序 + 背景位置(背景之后的条目数 = 渲染在背景下的图层数) */
internal fun commitLayerReorder(
    component: MarkupLayersComponent,
    displayList: List<LayerListEntry>,
) {
    val newLayers = displayList
        .filterIsInstance<LayerListEntry.LayerEntry>()
        .map { it.layer }
        .asReversed()
    val backgroundIndex = displayList.indexOfFirst { it is LayerListEntry.BackgroundEntry }
    val newBaseOrder = (displayList.size - 1 - backgroundIndex).coerceAtLeast(0)
    component.reorderLayersAndBase(newLayers, newBaseOrder)
}

/** 可拖拽排序的图层列表,显示倒序(顶层在上),松手时一次性提交新 z 序;背景层在列表底部(默认) */
@Composable
private fun LayerReorderList(component: MarkupLayersComponent) {
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .heightIn(max = 288.dp)
            .padding(horizontal = 16.dp)
    ) {
        itemsIndexed(
            items = displayList,
            key = { _, entry -> entry.key }
        ) { _, entry ->
            ReorderableItem(
                state = reorderState,
                key = entry.key
            ) { isDragging ->
                val dragHandleModifier = Modifier.longPressDraggableHandle(
                    onDragStarted = { haptics.longPress() },
                    onDragStopped = { commitLayerReorder(component, displayList) }
                )
                when (entry) {
                    is LayerListEntry.LayerEntry -> {
                        val layer = entry.layer
                        LayerRow(
                            layer = layer,
                            layers = layers,
                            isSelected = layer.id in component.selectedLayerIds,
                            isDragging = isDragging,
                            dragHandleModifier = dragHandleModifier,
                            onSelect = {
                                if (!layer.transform.locked) component.selectLayer(layer.id)
                            },
                            onToggleSelection = { component.toggleLayerSelection(layer.id) },
                            onToggleVisible = { component.toggleLayerVisible(layer.id) },
                            onToggleLocked = { component.toggleLayerLocked(layer.id) },
                        )
                    }

                    LayerListEntry.BackgroundEntry -> BackgroundTargetRow(
                        component = component,
                        isDragging = isDragging,
                        dragHandleModifier = dragHandleModifier
                    )
                }
            }
        }
    }
}

/** 背景层行:底图缩略图;可点 = 取消图层选中(滤镜/调节目标回到背景图),无选中时高亮。
 * 支持显隐切换、删除(替换为同尺寸透明底,可撤销)与长按拖拽调整 z 序 */
@Composable
private fun BackgroundTargetRow(
    component: MarkupLayersComponent,
    isDragging: Boolean,
    dragHandleModifier: Modifier,
) {
    val isTarget = component.selectedLayerId == null
    val visible = component.baseVisible
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animateFloatAsState(if (isDragging) 1.02f else 1f).value)
            .container(
                shape = ShapeDefaults.large,
                color = if (isTarget) {
                    MaterialTheme.colorScheme.primaryContainer
                } else MaterialTheme.colorScheme.surfaceContainerLow,
                resultPadding = 0.dp
            )
            .clickable { component.selectLayer(null) }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Picture(
            model = component.displayBitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(ShapeDefaults.small)
                .transparencyChecker()
                .graphicsLayer { alpha = if (visible) 1f else 0.4f }
        )
        Text(
            text = stringResource(R.string.markup_layer_background),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        LayerIconButton(
            icon = if (visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
            contentDescription = stringResource(R.string.markup_layer_toggle_visible),
            onClick = component::toggleBaseVisible
        )
        LayerIconButton(
            icon = Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.studio_layers_delete),
            onClick = component::removeBaseBackground
        )
        Box(
            modifier = dragHandleModifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = MaterialIcons.Rounded.DragHandle,
                contentDescription = stringResource(R.string.markup_drag_reorder),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
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
    onToggleSelection: () -> Unit,
    onToggleVisible: () -> Unit,
    onToggleLocked: () -> Unit,
) {
    val locked = layer.transform.locked
    val displayName = layerDisplayName(layer, layers)
    val selectDescription = stringResource(R.string.studio_layer_select, displayName)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animateFloatAsState(if (isDragging) 1.02f else 1f).value)
            .container(
                shape = ShapeDefaults.large,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else MaterialTheme.colorScheme.surfaceContainerLow,
                resultPadding = 0.dp
            )
            .clickable(enabled = !locked, onClick = onSelect)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Checkbox(
            checked = isSelected,
            enabled = !locked,
            onCheckedChange = { onToggleSelection() },
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = selectDescription }
        )
        LayerThumbnail(
            layer = layer,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (locked) {
                Text(
                    text = stringResource(R.string.studio_layer_locked),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LayerStateToggles(
            visible = layer.transform.visible,
            locked = locked,
            onToggleVisible = onToggleVisible,
            onToggleLocked = onToggleLocked
        )
        Box(
            modifier = dragHandleModifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = MaterialIcons.Rounded.DragHandle,
                contentDescription = stringResource(R.string.markup_drag_reorder),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
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
                MaterialTheme.colorScheme.onSurfaceVariant
            } else MaterialTheme.colorScheme.onSurfaceVariant
        )
        LayerIconButton(
            icon = if (locked) MaterialIcons.Rounded.Lock else MaterialIcons.Rounded.LockOpen,
            contentDescription = stringResource(R.string.markup_layer_toggle_lock),
            onClick = onToggleLocked,
            tint = if (locked) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Only one properties dropdown is present; rows never open nested menus. */
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
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable(enabled = enabled) { expanded = true }
                .heightIn(min = 48.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
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
        EnhancedDropdownMenu(
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
    LayerBlendMode.Multiply -> stringResource(R.string.studio_blend_multiply)
    LayerBlendMode.Screen -> stringResource(R.string.studio_blend_screen)
    LayerBlendMode.Overlay -> stringResource(R.string.studio_blend_overlay)
    LayerBlendMode.Darken -> stringResource(R.string.studio_blend_darken)
    LayerBlendMode.Lighten -> stringResource(R.string.studio_blend_lighten)
}

@Composable
private fun LayerBatchActions(component: MarkupLayersComponent) {
    val selected = component.layers.filter { it.id in component.selectedLayerIds }
    val editable = selected.filterNot { it.transform.locked }
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.studio_layers_selected, selected.size),
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            LayerActionButton(
                icon = Icons.Rounded.ContentCopy,
                labelRes = R.string.studio_layers_duplicate,
                enabled = editable.isNotEmpty(),
                onClick = component::duplicateSelectedLayers
            )
            LayerActionButton(
                icon = Icons.Outlined.Delete,
                labelRes = R.string.studio_layers_delete,
                enabled = editable.isNotEmpty(),
                onClick = component::removeSelectedLayers
            )
            LayerActionButton(
                icon = MaterialIcons.Outlined.GroupWork,
                labelRes = R.string.studio_layers_group,
                enabled = editable.size > 1,
                onClick = component::groupSelectedLayers
            )
            LayerActionButton(
                icon = MaterialIcons.Outlined.LinkOff,
                labelRes = R.string.studio_layers_ungroup,
                enabled = editable.any { it.groupId != null },
                onClick = component::ungroupSelectedLayers
            )
        }
    }
}

@Composable
private fun SelectedLayerProperties(component: MarkupLayersComponent, layer: MarkupLayer) {
    var expanded by rememberSaveable(layer.id) { mutableStateOf(false) }
    var showRename by rememberSaveable(layer.id) { mutableStateOf(false) }
    var name by rememberSaveable(layer.id) { mutableStateOf("") }
    var nudgeStep by rememberSaveable { mutableStateOf(1) }
    val displayName = layerDisplayName(layer, component.layers)
    val enabled = !layer.transform.locked
    DisposableEffect(layer.id) {
        onDispose { component.endLayerTransformChange() }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.studio_layer_properties))
            Icon(
                imageVector = if (expanded) MaterialIcons.Outlined.KeyboardArrowUp
                else MaterialIcons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        if (expanded) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    enabled = enabled,
                    onClick = {
                        name = displayName
                        showRename = true
                    }
                ) {
                    Text(stringResource(R.string.studio_layer_rename))
                }
            }
            if (layer.type is LayerType.Image) {
                val replacePicker = rememberImagePicker { uri: Uri ->
                    component.replaceImageLayerAsset(layer.id, uri)
                }
                TextButton(
                    enabled = enabled,
                    onClick = { replacePicker.pickImage() },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.studio_layer_replace_image))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.markup_layer_opacity),
                    style = MaterialTheme.typography.labelMedium
                )
                LayerOpacitySlider(
                    alpha = layer.transform.alpha,
                    enabled = enabled,
                    onDragStart = component::beginLayerAlphaChange,
                    onAlphaChange = { component.updateLayerAlphaTransient(layer.id, it) },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Text(
                    text = stringResource(R.string.studio_percent, (layer.transform.alpha * 100).roundToInt()),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.markup_layer_blend_mode),
                    style = MaterialTheme.typography.labelMedium
                )
                BlendModeDropdown(
                    current = layer.transform.blendMode,
                    enabled = enabled,
                    onChange = { component.setLayerBlendMode(layer.id, it) }
                )
            }
            Text(
                text = stringResource(R.string.studio_layer_align),
                style = MaterialTheme.typography.labelLarge
            )
            Row {
                TextButton(
                    enabled = enabled,
                    onClick = { component.alignLayer(layer.id, horizontal = true, position = 0.5f) },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text(stringResource(R.string.studio_layer_align_x)) }
                TextButton(
                    enabled = enabled,
                    onClick = { component.alignLayer(layer.id, horizontal = false, position = 0.5f) },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text(stringResource(R.string.studio_layer_align_y)) }
            }
            Text(
                text = stringResource(R.string.studio_layer_nudge),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(1, 10).forEach { step ->
                    EnhancedChip(
                        selected = nudgeStep == step,
                        onClick = { nudgeStep = step },
                        selectedColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) { Text(stringResource(R.string.studio_layer_nudge_step, step)) }
                }
                Spacer(Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                LayerIconButton(
                    icon = MaterialIcons.Outlined.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.studio_layer_nudge_left),
                    enabled = enabled,
                    onClick = { component.nudgeLayer(layer.id, -nudgeStep, 0) }
                )
                LayerIconButton(
                    icon = MaterialIcons.Outlined.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.studio_layer_nudge_up),
                    enabled = enabled,
                    onClick = { component.nudgeLayer(layer.id, 0, -nudgeStep) }
                )
                LayerIconButton(
                    icon = MaterialIcons.Outlined.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.studio_layer_nudge_down),
                    enabled = enabled,
                    onClick = { component.nudgeLayer(layer.id, 0, nudgeStep) }
                )
                LayerIconButton(
                    icon = MaterialIcons.Outlined.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.studio_layer_nudge_right),
                    enabled = enabled,
                    onClick = { component.nudgeLayer(layer.id, nudgeStep, 0) }
                )
            }
            Row {
                TextButton(
                    enabled = enabled,
                    onClick = { component.flipLayer(layer.id, horizontal = true) },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text(stringResource(R.string.markup_crop_flip_horizontal)) }
                TextButton(
                    enabled = enabled,
                    onClick = { component.flipLayer(layer.id, horizontal = false) },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text(stringResource(R.string.markup_crop_flip_vertical)) }
            }
            TransformPropertySlider(
                label = stringResource(R.string.studio_layer_scale_label),
                valueText = stringResource(R.string.studio_scale_value, layer.transform.scale),
                value = layer.transform.scale.coerceIn(0.1f, 10f),
                valueRange = 0.1f..10f,
                enabled = enabled,
                onStart = component::beginLayerTransformChange,
                onChange = {
                    component.updateLayerTransformTransient(layer.id, layer.transform.copy(scale = it))
                },
                onEnd = {
                    component.layers.firstOrNull { it.id == layer.id }?.let {
                        component.updateLayerTransformTransient(it.id, component.snapLayerTransform(it.transform))
                    }
                    component.endLayerTransformChange()
                }
            )
            val rotation = ((layer.transform.rotation + 180f) % 360f + 360f) % 360f - 180f
            TransformPropertySlider(
                label = stringResource(R.string.studio_layer_rotation_label),
                valueText = stringResource(R.string.studio_degrees_value, rotation),
                value = rotation,
                valueRange = -180f..180f,
                enabled = enabled,
                onStart = component::beginLayerTransformChange,
                onChange = {
                    component.updateLayerTransformTransient(layer.id, layer.transform.copy(rotation = it))
                },
                onEnd = {
                    component.layers.firstOrNull { it.id == layer.id }?.let {
                        component.updateLayerTransformTransient(it.id, component.snapLayerTransform(it.transform))
                    }
                    component.endLayerTransformChange()
                }
            )
            Text(
                text = stringResource(R.string.studio_layer_transform_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    EnhancedAlertDialog(
        visible = showRename,
        onDismissRequest = { showRename = false },
        title = { Text(stringResource(R.string.studio_layer_rename)) },
        text = {
            GlassOutlinedTextField(
                value = name,
                onValueChange = { name = it.take(120) },
                label = { Text(stringResource(R.string.studio_layer_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            CancelButton(
                text = stringResource(R.string.markup_cancel),
                onClick = { showRename = false }
            )
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.markup_confirm),
                enabled = name.isNotBlank(),
                onClick = {
                    component.renameLayer(layer.id, name.trim())
                    showRename = false
                }
            )
        }
    )
}

@Composable
private fun TransformPropertySlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onStart: () -> Unit,
    onChange: (Float) -> Unit,
    onEnd: () -> Unit,
) {
    var dragging by remember { mutableStateOf(false) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            Text(text = valueText, style = MaterialTheme.typography.labelMedium)
        }
        EnhancedSlider(
            value = value,
            enabled = enabled,
            valueRange = valueRange,
            onValueChange = {
                if (!dragging) {
                    dragging = true
                    onStart()
                }
                onChange(it)
            },
            onValueChangeFinished = {
                dragging = false
                onEnd()
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        )
    }
}

/** Z-order controls operate on the primary layer; batch actions are separate. */
@Composable
private fun LayerActionBar(
    component: MarkupLayersComponent,
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
        MaterialTheme.colorScheme.onSurfaceVariant
    } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
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
