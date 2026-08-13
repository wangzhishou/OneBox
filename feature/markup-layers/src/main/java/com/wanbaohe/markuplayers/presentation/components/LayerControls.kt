package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.theme.toColor
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSlider
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.presentation.render.LayerPreviewRenderers
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Category

/**
 * 图层缩略图:圆角透明棋盘格小盒内用预览渲染器画内容。
 * 文字按比例渲染在缩略图里会过小,改用固定字号显示;
 * 没有预览渲染器的类型(画笔)用类型图标兜底。
 */
@Composable
internal fun LayerThumbnail(
    layer: MarkupLayer,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(ShapeDefaults.small)
            .transparencyChecker(),
        contentAlignment = Alignment.Center
    ) {
        when (val type = layer.type) {
            is LayerType.Text -> {
                if (type.text.isBlank()) {
                    LayerTypeIcon(layer.type)
                } else {
                    Text(
                        text = type.text,
                        color = type.color.toColor(),
                        fontSize = (size.value * 0.32f).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }

            is LayerType.Sticker, is LayerType.Image, is LayerType.Shape -> {
                // 渲染器内容尺寸与画布成比例(贴纸 25% / 形状 30% / 图片 40%),放大让内容尽量填满盒子
                val canvasPx = with(LocalDensity.current) { size.toPx() }
                val scaleUp = if (type is LayerType.Sticker) 2f else 2.5f
                Box(modifier = Modifier.scale(scaleUp)) {
                    LayerPreviewRenderers.Content(
                        layer = layer,
                        canvasWidthPx = canvasPx,
                        canvasHeightPx = canvasPx
                    )
                }
            }

            else -> LayerTypeIcon(layer.type)
        }
    }
}

/** 无预览渲染器类型的兜底类型图标 */
@Composable
private fun LayerTypeIcon(type: LayerType) {
    val icon: ImageVector = when (type) {
        is LayerType.Draw -> MaterialIcons.Outlined.Brush
        else -> MaterialIcons.Outlined.Category
    }
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
    )
}

/** 图层类型的默认名资源 */
internal fun layerTypeNameRes(type: LayerType): Int = when (type) {
    is LayerType.Text -> R.string.markup_layer_kind_text
    is LayerType.Sticker -> R.string.markup_layer_kind_sticker
    is LayerType.Image -> R.string.markup_layer_kind_image
    is LayerType.Draw -> R.string.markup_layer_kind_draw
    is LayerType.Shape -> R.string.markup_layer_kind_shape
}

/** 图层显示名:空名时按类型给默认名 + 同类型序号(按 z 序从 1 数) */
@Composable
internal fun layerDisplayName(
    layer: MarkupLayer,
    layers: List<MarkupLayer>
): String {
    if (layer.name.isNotBlank()) return layer.name
    val kind = stringResource(layerTypeNameRes(layer.type))
    val ordinal = layers
        .take(layers.indexOfFirst { it.id == layer.id } + 1)
        .count { it.type::class == layer.type::class }
    return stringResource(R.string.markup_layer_default_name, kind, ordinal)
}

/** 「添加图层」下拉菜单:文字/贴纸/图片。锚点 Box 由调用方提供 */
@Composable
internal fun AddLayerDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onAddText: () -> Unit,
    onAddSticker: () -> Unit,
    onAddImage: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.markup_layer_kind_text)) },
            onClick = {
                onDismiss()
                onAddText()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.markup_layer_kind_sticker)) },
            onClick = {
                onDismiss()
                onAddSticker()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.markup_layer_kind_image)) },
            onClick = {
                onDismiss()
                onAddImage()
            }
        )
    }
}

/**
 * 不透明度滑杆:拖动开始经 [onDragStart] 记一次历史快照,
 * 拖动中的连续变更走 [onAlphaChange](不再入历史)。
 */
@Composable
internal fun LayerOpacitySlider(
    alpha: Float,
    enabled: Boolean,
    onDragStart: () -> Unit,
    onAlphaChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    EnhancedSlider(
        value = dragValue ?: alpha,
        onValueChange = {
            if (dragValue == null) onDragStart()
            dragValue = it
            onAlphaChange(it)
        },
        onValueChangeFinished = { dragValue = null },
        valueRange = 0f..1f,
        enabled = enabled,
        drawContainer = false,
        modifier = modifier
    )
}

/** 图层面板用的小号图标按钮(不占 48dp 最小触控区) */
@Composable
internal fun LayerIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}
