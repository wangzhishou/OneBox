package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.editor.EditorLayerBox
import com.wanbaohe.core.ui.widget.editor.EditorLayerBoxLabels
import com.wanbaohe.core.ui.widget.editor.drawEditorShape
import com.wanbaohe.core.ui.widget.editor.drawEditorStroke
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextCardAnnotation
import com.wanbaohe.textcard.domain.model.TextCardAnnotationType
import com.wanbaohe.textcard.presentation.screenLogic.TextCardAnnotationEditor

@Composable
internal fun BoxWithConstraintsScope.CardAnnotationLayer(
    annotation: TextCardAnnotation,
    editor: TextCardAnnotationEditor,
    editingState: TextFieldState?,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    interactive: Boolean,
    onEditingSync: () -> Unit,
    onOpenStylePanel: () -> Unit,
    onClearSelectionStyles: () -> Unit,
    selectionToolbarVisible: Boolean,
) {
    val density = LocalDensity.current
    val isEditing = editingState != null && annotation.type is TextCardAnnotationType.Text
    EditorLayerBox(
        transform = annotation.transform,
        isSelected = interactive && editor.isActive && editor.selectedId == annotation.id,
        onSelect = { editor.select(annotation.id) },
        onTransformEnd = { transform ->
            editor.updateTransform(annotation.id, transform)
            editor.endTransform()
        },
        labels = EditorLayerBoxLabels(
            scale = stringResource(R.string.textcard_scale_text),
            rotate = stringResource(R.string.textcard_rotate_text),
            resizeWidth = stringResource(R.string.textcard_resize_text_width),
            delete = stringResource(R.string.textcard_delete_selected),
        ),
        onTransformStart = editor::beginTransform,
        onTransformChange = { editor.updateTransform(annotation.id, it) },
        onEditRequest = when (annotation.type) {
            is TextCardAnnotationType.Text -> {
                { editor.beginTextEdit(annotation.id) }
            }
            is TextCardAnnotationType.Shape -> {
                {
                    editor.select(annotation.id)
                    editor.openShapePanel()
                }
            }
            is TextCardAnnotationType.Draw -> null
        },
        onDelete = { editor.delete(annotation.id) },
        gesturesEnabled = interactive && editor.isActive && !isEditing && editor.drawSession == null,
        showHandles = interactive && editor.isActive && !isEditing && editor.drawSession == null,
        tapSelectable = interactive && editor.isActive && annotation.type !is TextCardAnnotationType.Draw,
        resizeConfig = when (val type = annotation.type) {
            is TextCardAnnotationType.Text -> com.wanbaohe.core.ui.widget.editor.BoxResizeConfig(
                minWidthPx = canvasWidthPx * 0.12f,
                maxWidthPx = canvasWidthPx * 0.95f,
                minHeightPx = 1f,
                maxHeightPx = canvasHeightPx,
                onGestureStart = editor::beginTransform,
                onResize = { width, _, left, top ->
                    editor.updateTextBlock(type.block.id) {
                        it.copy(
                            widthRatio = width / canvasWidthPx,
                            widthManuallySet = true,
                        )
                    }
                    editor.updateTransform(
                        annotation.id,
                        annotation.transform.copy(
                            centerX = ((left + width / 2f) / canvasWidthPx).coerceIn(0f, 1f),
                            centerY = (top / canvasHeightPx).coerceIn(0f, 1f),
                        )
                    )
                },
            )
            is TextCardAnnotationType.Shape -> {
                val contentPadding = with(density) { 24.dp.toPx() }
                com.wanbaohe.core.ui.widget.editor.BoxResizeConfig(
                    minWidthPx = canvasWidthPx * 0.06f + contentPadding,
                    maxWidthPx = canvasWidthPx * 0.95f + contentPadding,
                    minHeightPx = 1f,
                    maxHeightPx = canvasHeightPx,
                    onGestureStart = editor::beginTransform,
                    onResize = { width, height, left, top ->
                        editor.updateShapeSpec(
                            type.spec.copy(widthRatio = (width - contentPadding) / canvasWidthPx)
                        )
                        editor.updateTransform(
                            annotation.id,
                            annotation.transform.copy(
                                centerX = ((left + width / 2f) / canvasWidthPx).coerceIn(0f, 1f),
                                centerY = ((top + height / 2f) / canvasHeightPx).coerceIn(0f, 1f),
                            )
                        )
                    },
                )
            }
            is TextCardAnnotationType.Draw -> null
        },
    ) {
        when (val type = annotation.type) {
            is TextCardAnnotationType.Text -> {
                val block = type.block
                val width = with(density) { (block.widthRatio * canvasWidthPx).toDp() }
                if (isEditing) {
                    InPlaceTextEditor(
                        block = block,
                        baseSizePx = canvasWidthPx * block.baseSizeRatio,
                        state = editingState,
                        onSync = onEditingSync,
                        onOpenStylePanel = onOpenStylePanel,
                        onClearSelectionStyles = onClearSelectionStyles,
                        selectionToolbarVisible = selectionToolbarVisible,
                        modifier = Modifier.width(width)
                    )
                } else {
                    CardText(
                        block = block,
                        baseSizePx = canvasWidthPx * block.baseSizeRatio,
                        canvasWidthPx = canvasWidthPx,
                        onTextLayout = {},
                        modifier = Modifier.width(width)
                    )
                }
            }
            is TextCardAnnotationType.Draw -> {
                Canvas(
                    modifier = Modifier
                        .size(
                            width = with(density) { canvasWidthPx.toDp() },
                            height = with(density) { canvasHeightPx.toDp() }
                        )
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                ) {
                    type.strokes.forEach { drawEditorStroke(it, size.width, size.height) }
                }
            }
            is TextCardAnnotationType.Shape -> {
                val spec = type.spec
                Canvas(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(
                            width = with(density) { (spec.widthRatio * canvasWidthPx).toDp() },
                            height = with(density) { (spec.heightRatio * canvasHeightPx).toDp() }
                        )
                ) {
                    drawEditorShape(
                        spec = spec,
                        strokeWidthPx = (spec.strokeWidthRatio * canvasWidthPx).coerceAtLeast(1f),
                        cornerRadiusPx = spec.cornerRadiusRatio * canvasWidthPx
                    )
                }
            }
        }
    }
}
