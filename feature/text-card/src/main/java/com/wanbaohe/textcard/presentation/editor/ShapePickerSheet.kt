package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.ConfirmButton
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.CardShapeKind
import com.wanbaohe.textcard.domain.model.ShapeElementSpec
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent

/**
 * 形状选择弹层(对照图片创作形状工具的精简版):
 * 种类(矩形/圆形/三角/箭头/线条/星形)+ 颜色 + 填充开关,确认落地画布中心。
 */
@Composable
fun ShapePickerSheet(
    visible: Boolean,
    component: TextCardComponent,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    var kind by remember { mutableStateOf(CardShapeKind.Rectangle) }
    var colorArgb by remember { mutableStateOf(ShapeElementSpec.DEFAULT_SHAPE_COLOR) }
    var filled by remember { mutableStateOf(true) }

    EnhancedModalBottomSheet(
        visible = true,
        dragHandle = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.textcard_add_shape),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            )
        },
        onDismiss = { onDismiss() },
        sheetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
            ) {
                // 种类
                GlassSegmentedButtonRow(
                    options = CardShapeKind.entries.toList(),
                    selectedOption = kind,
                    onOptionSelected = { kind = it },
                    label = { option ->
                        Text(
                            text = stringResource(option.labelRes()),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
                // 颜色
                ColorSelectionRow(
                    value = Color(colorArgb),
                    onValueChange = { colorArgb = it.toArgb().toLong() and 0xFFFF_FFFFL },
                    allowAlpha = false
                )
                // 填充开关(线条强制描边,添加时组件侧兜底)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .container(shape = ShapeDefaults.default)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.textcard_shape_filled),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    GlassSwitch(
                        checked = filled,
                        onCheckedChange = { filled = it }
                    )
                }
                ConfirmButton(
                    onClick = {
                        component.addShapeElement(kind, colorArgb, filled)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }
        }
    )
}

private fun CardShapeKind.labelRes(): Int = when (this) {
    CardShapeKind.Rectangle -> R.string.textcard_shape_rect
    CardShapeKind.Circle -> R.string.textcard_shape_circle
    CardShapeKind.Triangle -> R.string.textcard_shape_triangle
    CardShapeKind.Arrow -> R.string.textcard_shape_arrow
    CardShapeKind.Line -> R.string.textcard_shape_line
    CardShapeKind.Star -> R.string.textcard_shape_star
}
