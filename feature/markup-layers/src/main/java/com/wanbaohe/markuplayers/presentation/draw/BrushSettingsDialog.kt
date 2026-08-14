package com.wanbaohe.markuplayers.presentation.draw

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.BrushColor
import com.t8rin.imagetoolbox.core.resources.icons.Draw
import com.t8rin.imagetoolbox.core.resources.icons.Highlighter
import com.t8rin.imagetoolbox.core.resources.icons.Pen
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSlider
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.BrushType
import kotlin.math.roundToInt

/**
 * 画笔参数设置 Dialog(与 [TextEditDialog] 同范式):
 * 笔型 chips、颜色行、粗细/不透明度滑杆、橡皮擦开关。
 *
 * 参数即改即生效(直接写 [DrawSessionState]),取消/确认都只关窗不回滚——
 * 画笔参数是会话级偏好,没有文字图层那样的内容历史语义。
 */
@Composable
internal fun BrushSettingsDialog(
    visible: Boolean,
    session: DrawSessionState,
    onDismiss: () -> Unit,
) {
    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.markup_brush_settings_title)) },
        text = { BrushSettingsContent(session = session) },
        dismissButton = {
            CancelButton(
                text = stringResource(R.string.markup_cancel),
                onClick = onDismiss
            )
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.markup_confirm),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun BrushSettingsContent(session: DrawSessionState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        PanelLabel(text = stringResource(R.string.markup_draw_brush_type))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            brushChips.forEach { chip ->
                BrushChip(
                    chip = chip,
                    selected = !session.isEraser && session.brush == chip.type,
                    onClick = { session.selectBrush(chip.type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        PanelLabel(text = stringResource(R.string.markup_draw_color))
        ColorSelectionRow(
            value = Color(session.colorInt),
            onValueChange = { session.selectColor(it.toArgb()) },
            allowAlpha = false
        )

        SettingSliderRow(
            label = stringResource(R.string.markup_draw_width),
            valueText = "${session.widthPx.roundToInt()}px"
        ) {
            EnhancedSlider(
                value = session.widthPx,
                onValueChange = { session.widthPx = it },
                valueRange = 2f..96f,
                drawContainer = false,
                modifier = Modifier.weight(1f)
            )
        }

        SettingSliderRow(
            label = stringResource(R.string.markup_draw_opacity),
            valueText = "${(session.alpha * 100).roundToInt()}%"
        ) {
            EnhancedSlider(
                value = session.alpha,
                onValueChange = { session.alpha = it },
                valueRange = 0.05f..1f,
                drawContainer = false,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.markup_draw_eraser_mode),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Spacer(Modifier.weight(1f))
            Switch(
                checked = session.isEraser,
                onCheckedChange = { checked ->
                    if (checked) session.enableEraser() else session.enableBrush()
                }
            )
        }
        Spacer(Modifier.height(4.dp))
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

@Composable
private fun SettingSliderRow(
    label: String,
    valueText: String,
    slider: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 56.dp)
        )
        slider()
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 44.dp)
        )
    }
}

private class BrushChipSpec(
    val type: BrushType,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
)

private val brushChips = listOf(
    BrushChipSpec(BrushType.Pencil, Icons.Outlined.Draw, R.string.markup_brush_pencil),
    BrushChipSpec(BrushType.Pen, Icons.Outlined.Pen, R.string.markup_brush_pen),
    BrushChipSpec(BrushType.Brush, Icons.Outlined.BrushColor, R.string.markup_brush_brush),
    BrushChipSpec(BrushType.Marker, Icons.Outlined.Highlighter, R.string.markup_brush_marker),
)

@Composable
private fun BrushChip(
    chip: BrushChipSpec,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
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
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = chip.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(chip.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1
        )
    }
}
