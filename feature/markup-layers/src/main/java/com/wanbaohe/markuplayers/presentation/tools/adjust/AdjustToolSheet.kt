package com.wanbaohe.markuplayers.presentation.tools.adjust

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import com.wanbaohe.markuplayers.presentation.components.EditorTargetSelector
import com.wanbaohe.markuplayers.presentation.components.SheetHeader
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import kotlin.math.roundToInt

/**
 * 「调色」底部 Tab 的独立面板:内容复用 [AdjustPanelContent]。
 * 作用目标 = 当前选中图层(写入图层 adjustments,进 undo 历史);
 * 未选中图层时作用于背景图;图层和背景图的改动均可撤销。
 * 预览经 colorFilter 实时生效,导出时按目标烘焙。
 */
@Composable
fun AdjustToolSheet(
    visible: Boolean,
    component: MarkupLayersComponent,
    onDismiss: () -> Unit,
) {
    val candidateLayerId = remember(visible) { component.selectedLayerId }
    EnhancedModalBottomSheet(
        visible = visible,
        dragHandle = {},
        onDismiss = { onDismiss() },
        sheetContent = {
            SheetHeader(
                title = stringResource(R.string.markup_tool_adjust),
                onClose = onDismiss
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                EditorTargetSelector(
                    component = component,
                    candidateLayerId = candidateLayerId
                )
                Spacer(Modifier.height(4.dp))
                AdjustPanelContent(component = component)
            }
        }
    )
}

/**
 * 基础调节面板内容(亮度/对比度/饱和度三条滑杆 + 重置),
 * 供「基础工具」Sheet 等容器组装复用。读写当前目标的调节状态
 * (选中图层 → 图层字段,无选中 → 背景图);图层目标的拖动开始经
 * [MarkupLayersComponent.beginAdjustmentsChange] 记一次 undo 快照,
 * 拖动中的连续变更走 transient(整段拖动 = 一步 undo)。
 */
@Composable
fun AdjustPanelContent(
    component: MarkupLayersComponent,
    modifier: Modifier = Modifier,
) {
    val adjustments = component.targetAdjustments
    val enabled = component.selectedLayer?.transform?.locked != true
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        AdjustSliderRow(
            icon = Icons.Outlined.LineSunny,
            label = stringResource(R.string.markup_adjust_brightness),
            value = adjustments.brightness,
            enabled = enabled,
            onDragStart = component::beginAdjustmentsChange,
            onValueChange = {
                component.updateBaseAdjustments(adjustments.copy(brightness = it))
            }
        )
        AdjustSliderRow(
            icon = Icons.Outlined.LineContrast,
            label = stringResource(R.string.markup_adjust_contrast),
            value = adjustments.contrast,
            enabled = enabled,
            onDragStart = component::beginAdjustmentsChange,
            onValueChange = {
                component.updateBaseAdjustments(adjustments.copy(contrast = it))
            }
        )
        AdjustSliderRow(
            icon = Icons.Outlined.LineWaterDrop,
            label = stringResource(R.string.markup_adjust_saturation),
            value = adjustments.saturation,
            enabled = enabled,
            onDragStart = component::beginAdjustmentsChange,
            onValueChange = {
                component.updateBaseAdjustments(adjustments.copy(saturation = it))
            }
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = component::resetBaseAdjustments,
                enabled = enabled && !adjustments.isNeutral,
                modifier = Modifier.heightIn(min = 48.dp)
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
    enabled: Boolean,
    onDragStart: () -> Unit,
    onValueChange: (Int) -> Unit,
) {
    // 拖动会话跟踪:首次变更记快照(beginAdjustmentsChange),抬起复位
    var dragging by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (value > 0) stringResource(R.string.studio_signed_value, value) else value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.width(40.dp)
            )
        }
        EnhancedSlider(
            value = value.toFloat(),
            onValueChange = {
                if (!dragging) {
                    dragging = true
                    onDragStart()
                }
                onValueChange(it.roundToInt())
            },
            onValueChangeFinished = { dragging = false },
            valueRange = -100f..100f,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        )
    }
}
