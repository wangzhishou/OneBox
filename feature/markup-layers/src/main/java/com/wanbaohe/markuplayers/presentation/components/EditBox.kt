package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wanbaohe.core.ui.widget.editor.BoxResizeConfig
import com.wanbaohe.core.ui.widget.editor.EditorLayerBox
import com.wanbaohe.core.ui.widget.editor.EditorLayerBoxLabels
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerTransform

@Composable
fun BoxWithConstraintsScope.EditBox(
    transform: LayerTransform,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTransformEnd: (LayerTransform) -> Unit,
    modifier: Modifier = Modifier,
    onTransformStart: () -> Unit = {},
    onTransformChange: ((LayerTransform) -> Unit)? = null,
    onEditRequest: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    tapSelectable: Boolean = true,
    gesturesEnabled: Boolean = true,
    showHandles: Boolean = true,
    canvasScale: Float = 1f,
    resizeConfig: BoxResizeConfig? = null,
    content: @Composable BoxScope.() -> Unit,
) = EditorLayerBox(
    transform = transform,
    isSelected = isSelected,
    onSelect = onSelect,
    onTransformEnd = onTransformEnd,
    labels = EditorLayerBoxLabels(
        scale = stringResource(R.string.studio_layer_scale),
        rotate = stringResource(R.string.studio_layer_rotate),
        resizeWidth = stringResource(R.string.studio_layer_resize_width),
        delete = stringResource(R.string.markup_delete_layer),
    ),
    modifier = modifier,
    onTransformStart = onTransformStart,
    onTransformChange = onTransformChange,
    onEditRequest = onEditRequest,
    onDelete = onDelete,
    tapSelectable = tapSelectable,
    gesturesEnabled = gesturesEnabled,
    showHandles = showHandles,
    canvasScale = canvasScale,
    resizeConfig = resizeConfig,
    content = content,
)
