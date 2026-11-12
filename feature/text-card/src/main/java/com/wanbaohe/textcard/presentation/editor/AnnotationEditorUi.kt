/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.editor.BrushType
import com.shifenmiao.model.editor.ShapeKind
import com.shifenmiao.model.editor.ShapeSpec
import com.t8rin.imagetoolbox.core.resources.icons.Eraser
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.base.ui.button.PrimaryButton
import com.wanbaohe.base.ui.button.SecondaryButton
import com.wanbaohe.core.ui.widget.color_picker.ColorSelectionRow
import com.wanbaohe.core.ui.widget.editor.EditorDrawSessionState
import com.wanbaohe.core.ui.widget.editor.EditorRailTool
import com.wanbaohe.core.ui.widget.editor.EditorToolRail
import com.wanbaohe.core.ui.widget.editor.VerticalDraggable
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.enhanced.longPress
import com.wanbaohe.core.ui.widget.enhanced.press
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.core.ui.widget.glass.glassDense
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextCardAnnotation
import com.wanbaohe.textcard.domain.model.TextCardAnnotationType
import com.wanbaohe.textcard.domain.model.needsReview
import com.wanbaohe.textcard.presentation.editor.panels.PanelSettingRow
import com.wanbaohe.textcard.presentation.editor.panels.PanelSliderRow
import com.wanbaohe.textcard.presentation.screenLogic.TextCardAnnotationEditor
import com.wanbaohe.textcard.presentation.screenLogic.TextCardAnnotationPanel
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.roundToInt

@Composable
internal fun BoxScope.AnnotationToolRail(
    editor: TextCardAnnotationEditor,
    containerHeightPx: Float,
) {
    var showBrushSettings by remember { mutableStateOf(false) }
    val drawSession = editor.drawSession
    val tools = if (drawSession != null) {
        listOf(
            EditorRailTool("settings", Icons.Outlined.Tune, stringResource(R.string.textcard_brush_settings)),
            EditorRailTool(
                "eraser",
                com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Eraser,
                stringResource(R.string.textcard_eraser)
            ),
            EditorRailTool(
                "undo",
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo,
                stringResource(R.string.textcard_undo),
                enabled = drawSession.canUndo
            ),
            EditorRailTool(
                "redo",
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo,
                stringResource(R.string.textcard_redo),
                enabled = drawSession.canRedo
            ),
        )
    } else {
        listOf(
            EditorRailTool("text", Icons.Outlined.TextFields, stringResource(R.string.textcard_add_text)),
            EditorRailTool("draw", Icons.Outlined.Draw, stringResource(R.string.textcard_draw)),
            EditorRailTool("shape", Icons.Outlined.CropSquare, stringResource(R.string.textcard_add_shape)),
            EditorRailTool("layers", Icons.Outlined.Layers, stringResource(R.string.textcard_tab_layers)),
        )
    }
    val activeId = when {
        drawSession != null && drawSession.isEraser -> "eraser"
        drawSession != null -> null
        editor.textEditId != null -> "text"
        editor.panel == TextCardAnnotationPanel.Shape -> "shape"
        editor.panel == TextCardAnnotationPanel.Layers -> "layers"
        else -> null
    }
    VerticalDraggable(
        containerHeightPx = containerHeightPx,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 8.dp),
        consumeTap = true,
    ) {
        EditorToolRail(
            tools = tools,
            activeId = activeId,
            onToolClick = { tool ->
                when (tool.id) {
                    "text" -> editor.addText()
                    "draw" -> editor.startDrawing()
                    "shape" -> editor.openShapePanel()
                    "layers" -> editor.openLayers()
                    "settings" -> showBrushSettings = true
                    "eraser" -> {
                        if (drawSession?.isEraser == true) {
                            drawSession.enableBrush()
                        } else {
                            drawSession?.enableEraser()
                        }
                    }
                    "undo" -> drawSession?.undo()
                    "redo" -> drawSession?.redo()
                }
            }
        )
    }
    if (drawSession != null) {
        AnnotationBrushSettingsDialog(
            visible = showBrushSettings,
            session = drawSession,
            onDismiss = { showBrushSettings = false },
        )
    }
}

@Composable
internal fun AnnotationBottomBar(
    component: TextCardComponent,
    onDoneText: () -> Unit,
) {
    val editor = component.annotationEditor
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassDense(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = when {
                    editor.drawSession != null -> stringResource(
                        R.string.textcard_annotation_draw_mode,
                        component.currentPage + 1
                    )
                    editor.textEditId != null -> stringResource(R.string.textcard_annotation_edit_text)
                    else -> stringResource(R.string.textcard_annotation_mode, component.currentPage + 1)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (editor.drawSession != null) {
                SecondaryButton(
                    text = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.cancel),
                    onClick = editor::cancelDrawing
                )
                PrimaryButton(text = stringResource(R.string.textcard_draw_done), onClick = editor::finishDrawing)
            } else {
                PrimaryButton(
                    text = stringResource(R.string.textcard_draw_done),
                    onClick = {
                        if (editor.textEditId != null) onDoneText() else editor.finish()
                    }
                )
            }
        }
        if (editor.reviewCount > 0) {
            Text(
                text = stringResource(R.string.textcard_annotation_review_count, editor.reviewCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
internal fun AnnotationPanels(component: TextCardComponent) {
    val editor = component.annotationEditor
    EnhancedModalBottomSheet(
        visible = editor.panel != null,
        title = stringResource(
            if (editor.panel == TextCardAnnotationPanel.Shape) {
                R.string.textcard_add_shape
            } else {
                R.string.textcard_tab_layers
            }
        ),
        onDismiss = { editor.dismissPanel() },
        onClose = editor::dismissPanel,
        nestedScrollEnabled = true,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 440.dp)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (editor.panel) {
                TextCardAnnotationPanel.Shape -> ShapeAnnotationPanel(editor)
                TextCardAnnotationPanel.Layers -> AnnotationLayersPanel(component)
                null -> Unit
            }
        }
    }
}

@Composable
private fun ShapeAnnotationPanel(editor: TextCardAnnotationEditor) {
    val selectedSpec = (editor.selectedAnnotation?.type as? TextCardAnnotationType.Shape)?.spec
    val spec = selectedSpec ?: editor.shapeSpec
    var continuous by remember { mutableStateOf(false) }
    fun updateContinuous(updated: ShapeSpec) {
        if (!continuous) {
            editor.beginTransform()
            continuous = true
        }
        editor.updateShapeSpec(updated)
    }
    fun finishContinuous() {
        if (continuous) {
            editor.endTransform()
            continuous = false
        }
    }

    Text(stringResource(R.string.textcard_shape_kind), style = MaterialTheme.typography.titleSmall)
    GlassSegmentedButtonRow(
        options = listOf(ShapeKind.Rectangle, ShapeKind.Circle, ShapeKind.Arrow, ShapeKind.Line),
        selectedOption = spec.kind,
        onOptionSelected = { kind ->
            val updated = ShapeSpec.default(kind).copy(
                color = spec.color,
                filled = spec.filled,
                cornerRadiusRatio = spec.cornerRadiusRatio,
                strokeWidthRatio = spec.strokeWidthRatio,
            )
            if (selectedSpec == null) editor.addShape(updated) else editor.updateShapeSpec(updated)
        },
        label = { kind ->
            Text(
                stringResource(
                    when (kind) {
                        ShapeKind.Rectangle -> R.string.textcard_shape_rect
                        ShapeKind.Circle -> R.string.textcard_shape_circle
                        ShapeKind.Arrow -> R.string.textcard_shape_arrow
                        ShapeKind.Line -> R.string.textcard_shape_line
                        else -> R.string.textcard_add_shape
                    }
                )
            )
        },
        modifier = Modifier.fillMaxWidth(),
    )
    GlassSegmentedButtonRow(
        options = listOf(true, false),
        selectedOption = spec.filled,
        onOptionSelected = { filled -> editor.updateShapeSpec(spec.copy(filled = filled)) },
        label = { filled ->
            Text(stringResource(if (filled) R.string.textcard_shape_filled else R.string.textcard_shape_stroke))
        },
        modifier = Modifier.fillMaxWidth(),
    )
    PanelSettingRow(stringResource(R.string.textcard_text_color)) {
        ColorSelectionRow(
            value = Color(spec.color),
            onValueChange = { editor.updateShapeSpec(spec.copy(color = it.toArgb())) },
            allowAlpha = false,
            modifier = Modifier.weight(1f),
        )
    }
    PanelSliderRow(
        label = stringResource(R.string.textcard_shape_corner_radius),
        value = spec.cornerRadiusRatio,
        valueRange = 0f..0.1f,
        valueText = "${(spec.cornerRadiusRatio * 100).roundToInt()}%",
        enabled = spec.kind == ShapeKind.Rectangle,
        onValueChange = { updateContinuous(spec.copy(cornerRadiusRatio = it)) },
        onValueChangeFinished = ::finishContinuous,
    )
    PanelSliderRow(
        label = stringResource(R.string.textcard_draw_width),
        value = spec.strokeWidthRatio,
        valueRange = 0.002f..0.05f,
        valueText = "${(spec.strokeWidthRatio * 100).roundToInt()}%",
        enabled = !spec.filled || spec.kind == ShapeKind.Line,
        onValueChange = { updateContinuous(spec.copy(strokeWidthRatio = it)) },
        onValueChangeFinished = ::finishContinuous,
    )
}

@Composable
private fun AnnotationLayersPanel(component: TextCardComponent) {
    val editor = component.annotationEditor
    val layers = editor.currentAnnotations.asReversed()
    if (layers.isEmpty()) {
        Text(
            text = stringResource(R.string.textcard_annotation_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Text(
        text = stringResource(R.string.textcard_layers_reorder_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    var displayList by remember(layers) { mutableStateOf(layers) }
    val reorderState = rememberReorderableLazyListState(
        lazyListState = listState,
        onMove = { from, to ->
            haptics.press()
            displayList = displayList.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
    )
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        itemsIndexed(displayList, key = { _, item -> item.id }) { _, annotation ->
            ReorderableItem(state = reorderState, key = annotation.id) { _ ->
                val dragModifier = Modifier.draggableHandle(
                    onDragStarted = { haptics.press() },
                    onDragStopped = {
                        val targetIndex = displayList.indexOfFirst { it.id == annotation.id }
                        if (targetIndex >= 0) {
                            editor.moveAnnotation(
                                annotation.id,
                                displayList.lastIndex - targetIndex
                            )
                        }
                    },
                )
                val selected = editor.selectedId == annotation.id
                GlassCard(
                    onClick = { editor.select(annotation.id) },
                    colors = if (selected) {
                        tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer)
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        IconButton(onClick = { editor.toggleVisible(annotation.id) }) {
                            Icon(
                                imageVector = if (annotation.transform.visible) {
                                    Icons.Outlined.Visibility
                                } else {
                                    Icons.Outlined.VisibilityOff
                                },
                                contentDescription = stringResource(R.string.textcard_layer_toggle_visible),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = annotation.displayName(),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (component.layout?.needsReview(annotation) == true) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = stringResource(R.string.textcard_annotation_needs_review),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { editor.toggleLocked(annotation.id) }) {
                            Icon(
                                imageVector = if (annotation.transform.locked) {
                                    Icons.Outlined.Lock
                                } else {
                                    Icons.Outlined.LockOpen
                                },
                                contentDescription = stringResource(R.string.textcard_layer_toggle_locked),
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .then(dragModifier),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DragIndicator,
                                contentDescription = stringResource(R.string.textcard_layers_reorder_hint),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
    val selectedId = editor.selectedId
    val pageCount = component.layout?.pages?.size ?: 0
    if (selectedId != null && pageCount > 1) {
        var pageText by remember { mutableStateOf("") }
        val target = pageText.toIntOrNull()?.minus(1)?.takeIf { it in 0 until pageCount }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.textcard_move_annotation_page),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            GlassOutlinedTextField(
                value = pageText,
                onValueChange = { pageText = it.filter(Char::isDigit).take(4) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(88.dp),
            )
            TextButton(
                enabled = target != null,
                onClick = {
                    target?.let {
                        editor.moveToPage(selectedId, it)
                        editor.select(null)
                        pageText = ""
                    }
                },
            ) {
                Text(stringResource(R.string.textcard_apply))
            }
        }
    }
}

@Composable
private fun AnnotationBrushSettingsDialog(
    visible: Boolean,
    session: EditorDrawSessionState,
    onDismiss: () -> Unit,
) {
    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.textcard_brush_settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.textcard_brush_type), style = MaterialTheme.typography.titleSmall)
                GlassSegmentedButtonRow(
                    options = listOf(BrushType.Pencil, BrushType.Marker),
                    selectedOption = if (session.brush == BrushType.Marker) {
                        BrushType.Marker
                    } else {
                        BrushType.Pencil
                    },
                    onOptionSelected = session::selectBrush,
                    label = { type ->
                        Text(
                            stringResource(
                                if (type == BrushType.Pencil) {
                                    R.string.textcard_brush_pencil
                                } else {
                                    R.string.textcard_brush_marker
                                }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                PanelSettingRow(stringResource(R.string.textcard_brush_color)) {
                    ColorSelectionRow(
                        value = Color(session.colorInt),
                        onValueChange = { session.selectColor(it.toArgb()) },
                        allowAlpha = false,
                        modifier = Modifier.weight(1f),
                    )
                }
                PanelSliderRow(
                    label = stringResource(R.string.textcard_draw_width),
                    value = session.widthPx,
                    valueRange = 2f..96f,
                    valueText = "${session.widthPx.roundToInt()}px",
                    onValueChange = { session.widthPx = it },
                )
                PanelSliderRow(
                    label = stringResource(R.string.textcard_draw_opacity),
                    value = session.alpha,
                    valueRange = 0.05f..1f,
                    valueText = "${(session.alpha * 100).roundToInt()}%",
                    onValueChange = { session.alpha = it },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.textcard_eraser),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    GlassSwitch(
                        checked = session.isEraser,
                        onCheckedChange = { checked ->
                            if (checked) session.enableEraser() else session.enableBrush()
                        },
                    )
                }
            }
        },
        dismissButton = { CancelButton(onClick = onDismiss) },
        confirmButton = { ConfirmButton(onClick = onDismiss) },
    )
}

@Composable
private fun TextCardAnnotation.displayName(): String = when (val type = type) {
    is TextCardAnnotationType.Text -> type.block.content.lineSequence().firstOrNull()
        ?.take(16)
        ?.takeIf { it.isNotBlank() }
        ?: type.block.content.take(16).ifBlank { stringResource(R.string.textcard_layer_text) }
    is TextCardAnnotationType.Draw -> stringResource(R.string.textcard_draw)
    is TextCardAnnotationType.Shape -> stringResource(R.string.textcard_layer_shape)
}
