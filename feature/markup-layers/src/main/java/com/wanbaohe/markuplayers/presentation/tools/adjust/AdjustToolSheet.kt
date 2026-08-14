package com.wanbaohe.markuplayers.presentation.tools.adjust

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContrast
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSunny
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWaterDrop
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSlider
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import kotlin.math.roundToInt

/**
 * 「调色」底部 Tab 的独立面板:内容复用 [AdjustPanelContent],
 * 与「基础工具」面板里的调节滑杆读写同一份 component.baseAdjustments 状态,
 * 预览经 colorFilter 实时生效,导出时统一烘焙。
 */
@Composable
fun AdjustToolSheet(
    visible: Boolean,
    component: MarkupLayersComponent,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        sheetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.markup_tool_adjust),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                AdjustPanelContent(component = component)
            }
        }
    )
}

/**
 * 基础调节面板内容(亮度/对比度/饱和度三条滑杆 + 重置),
 * 供「基础工具」Sheet 等容器组装复用。改动实时写入 component,
 * 预览经 colorFilter 即时生效;不进图层 undo 历史,重置走这里。
 */
@Composable
fun AdjustPanelContent(
    component: MarkupLayersComponent,
    modifier: Modifier = Modifier,
) {
    val adjustments = component.baseAdjustments
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        AdjustSliderRow(
            icon = Icons.Outlined.LineSunny,
            label = stringResource(R.string.markup_adjust_brightness),
            value = adjustments.brightness,
            onValueChange = {
                component.updateBaseAdjustments(adjustments.copy(brightness = it))
            }
        )
        AdjustSliderRow(
            icon = Icons.Outlined.LineContrast,
            label = stringResource(R.string.markup_adjust_contrast),
            value = adjustments.contrast,
            onValueChange = {
                component.updateBaseAdjustments(adjustments.copy(contrast = it))
            }
        )
        AdjustSliderRow(
            icon = Icons.Outlined.LineWaterDrop,
            label = stringResource(R.string.markup_adjust_saturation),
            value = adjustments.saturation,
            onValueChange = {
                component.updateBaseAdjustments(adjustments.copy(saturation = it))
            }
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = component::resetBaseAdjustments,
                enabled = !adjustments.isNeutral
            ) {
                Text(stringResource(R.string.markup_reset))
            }
        }
    }
}

@Composable
private fun AdjustSliderRow(
    icon: ImageVector,
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(76.dp)
        )
        EnhancedSlider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = -100f..100f,
            drawContainer = false,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (value > 0) "+$value" else "$value",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp)
        )
    }
}
