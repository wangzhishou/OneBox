package com.wanbaohe.markuplayers.presentation.tools.shape

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.FreeArrow
import com.t8rin.imagetoolbox.core.resources.icons.Line
import com.t8rin.imagetoolbox.core.resources.icons.RadioButtonUnchecked
import com.t8rin.imagetoolbox.core.resources.icons.Square
import com.t8rin.imagetoolbox.core.resources.icons.Star
import com.t8rin.imagetoolbox.core.resources.icons.Triangle
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSlider
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.ShapeKind
import com.wanbaohe.markuplayers.domain.model.ShapeSpec
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import kotlin.math.roundToInt

/**
 * 形状工具底部面板(设计稿「形状图形工具」):
 * 基础形状 6 选 1、填充/描边分段开关、颜色行、圆角(仅矩形)与描边粗细(描边模式)滑杆。
 *
 * 未选中形状图层时:面板值为「下一个新形状」的默认样式,点形状按钮创建并选中图层;
 * 已选中形状图层时:面板显示该图层 spec,任何修改实时应用到该图层
 * (滑杆拖动经 beginShapeSpecChange/updateShapeSpecTransient 只记一次历史快照)。
 */
@Composable
fun ShapeToolSheet(
    visible: Boolean,
    component: MarkupLayersComponent,
    defaultSpec: ShapeSpec,
    onDefaultSpecChange: (ShapeSpec) -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        sheetContent = {
            ShapeToolPanel(
                component = component,
                defaultSpec = defaultSpec,
                onDefaultSpecChange = onDefaultSpecChange
            )
        }
    )
}

@Composable
private fun ShapeToolPanel(
    component: MarkupLayersComponent,
    defaultSpec: ShapeSpec,
    onDefaultSpecChange: (ShapeSpec) -> Unit,
) {
    val selectedLayer = component.layers.firstOrNull { it.id == component.selectedLayerId }
    val editingSpec = (selectedLayer?.type as? LayerType.Shape)?.spec
    val displaySpec = editingSpec ?: defaultSpec

    // 离散修改(形状种类/填充样式/颜色):正常记一次历史
    val applySpec: (ShapeSpec) -> Unit = { newSpec ->
        onDefaultSpecChange(newSpec)
        if (selectedLayer != null && editingSpec != null) {
            component.updateShapeSpec(selectedLayer.id, newSpec)
        }
    }
    // 滑杆拖动:开始时记一次快照,拖动中只改值不入历史
    val beginDrag: () -> Unit = {
        if (editingSpec != null) component.beginShapeSpecChange()
    }
    val applyTransient: (ShapeSpec) -> Unit = { newSpec ->
        onDefaultSpecChange(newSpec)
        if (selectedLayer != null && editingSpec != null) {
            component.updateShapeSpecTransient(selectedLayer.id, newSpec)
        }
    }
    val onKindClick: (ShapeKind) -> Unit = { kind ->
        // 切形状时保留颜色/样式等参数,尺寸换成该形状的默认比例
        val kindDefault = ShapeSpec.default(kind)
        val newSpec = displaySpec.copy(
            kind = kind,
            widthRatio = kindDefault.widthRatio,
            heightRatio = kindDefault.heightRatio
        )
        if (editingSpec == null) {
            onDefaultSpecChange(newSpec)
            component.addLayer(MarkupLayer(type = LayerType.Shape(newSpec)))
        } else {
            applySpec(newSpec)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PanelLabel(text = stringResource(R.string.markup_shape_basic_shapes))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            shapeKindItems.forEach { item ->
                ShapeKindButton(
                    item = item,
                    selected = displaySpec.kind == item.kind,
                    onClick = { onKindClick(item.kind) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        PanelLabel(text = stringResource(R.string.markup_shape_style))
        StyleSegmentedRow(
            filled = displaySpec.filled,
            onFilledChange = { applySpec(displaySpec.copy(filled = it)) }
        )

        ColorSelectionRow(
            value = Color(displaySpec.color),
            onValueChange = { applySpec(displaySpec.copy(color = it.toArgb())) },
            allowAlpha = false
        )

        ShapeSliderRow(
            label = stringResource(R.string.markup_shape_corner_radius),
            value = displaySpec.cornerRadiusRatio,
            valueRange = 0f..0.1f,
            enabled = displaySpec.kind == ShapeKind.Rectangle,
            onBeginDrag = beginDrag,
            onValueChange = { applyTransient(displaySpec.copy(cornerRadiusRatio = it)) }
        )
        ShapeSliderRow(
            label = stringResource(R.string.markup_shape_stroke_width),
            value = displaySpec.strokeWidthRatio,
            valueRange = 0.002f..0.05f,
            enabled = !displaySpec.filled || displaySpec.kind == ShapeKind.Line,
            onBeginDrag = beginDrag,
            onValueChange = { applyTransient(displaySpec.copy(strokeWidthRatio = it)) }
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PanelLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private class ShapeKindItem(
    val kind: ShapeKind,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
)

private val shapeKindItems = listOf(
    ShapeKindItem(ShapeKind.Rectangle, Icons.Rounded.Square, R.string.markup_shape_kind_rectangle),
    ShapeKindItem(
        ShapeKind.Circle,
        Icons.Rounded.RadioButtonUnchecked,
        R.string.markup_shape_kind_circle
    ),
    ShapeKindItem(ShapeKind.Triangle, Icons.Outlined.Triangle, R.string.markup_shape_kind_triangle),
    ShapeKindItem(ShapeKind.Arrow, Icons.Rounded.FreeArrow, R.string.markup_shape_kind_arrow),
    ShapeKindItem(ShapeKind.Line, Icons.Rounded.Line, R.string.markup_shape_kind_line),
    ShapeKindItem(ShapeKind.Star, Icons.Outlined.Star, R.string.markup_shape_kind_star),
)

@Composable
private fun ShapeKindButton(
    item: ShapeKindItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .then(
                if (selected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = ShapeDefaults.default
                    )
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1
        )
    }
}

/** 填充/描边分段开关 */
@Composable
private fun StyleSegmentedRow(
    filled: Boolean,
    onFilledChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeDefaults.default)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StyleSegment(
            text = stringResource(R.string.markup_shape_style_filled),
            selected = filled,
            onClick = { onFilledChange(true) },
            modifier = Modifier.weight(1f)
        )
        StyleSegment(
            text = stringResource(R.string.markup_shape_style_stroke),
            selected = !filled,
            onClick = { onFilledChange(false) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StyleSegment(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier
            .clip(ShapeDefaults.small)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    )
}

/**
 * 形状参数滑杆:拖动开始经 [onBeginDrag] 记一次历史快照,
 * 拖动中的连续变更走 [onValueChange](不再入历史)。
 */
@Composable
private fun ShapeSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onBeginDrag: () -> Unit,
    onValueChange: (Float) -> Unit,
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val displayValue = dragValue ?: value
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 64.dp)
        )
        EnhancedSlider(
            value = displayValue,
            onValueChange = {
                if (dragValue == null) onBeginDrag()
                dragValue = it
                onValueChange(it)
            },
            onValueChangeFinished = { dragValue = null },
            valueRange = valueRange,
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${(displayValue * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 44.dp)
        )
    }
}
