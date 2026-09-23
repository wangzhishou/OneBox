package com.wanbaohe.xiangqi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMemory
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePsychology
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource

/**
 * 象棋专用走棋 AI 选择面板。
 *
 * 只列决策/引擎类来源 +「快速工作模型」，与聊天模型选择器分离；
 * 新增开源象棋引擎时扩展 [XiangqiAiSource.RemoteEngine.presets] 即可出现在此列表。
 */
@Composable
fun XiangqiAiPickerBottomSheet(
    visible: Boolean,
    selected: XiangqiAiSource,
    workingModelTitle: String,
    title: String,
    onSelected: (XiangqiAiSource) -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        dragHandle = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppTheme.dimens.spaceLarge, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.xiangqi_ai_picker_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))

            XiangqiAiSource.presets.forEach { source ->
                SourceRow(
                    source = source,
                    selected = source == selected,
                    workingModelTitle = workingModelTitle,
                    onClick = {
                        onSelected(source)
                        onDismiss()
                    },
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SourceRow(
    source: XiangqiAiSource,
    selected: Boolean,
    workingModelTitle: String,
    onClick: () -> Unit,
) {
    val title = when (source) {
        XiangqiAiSource.WorkingModel -> stringResource(R.string.xiangqi_ai_source_working_model)
        XiangqiAiSource.Jev -> stringResource(R.string.xiangqi_ai_source_jev)
        is XiangqiAiSource.RemoteEngine -> when (source.engineId) {
            XiangqiAiSource.RemoteEngine.PIKAFISH -> stringResource(R.string.xiangqi_ai_source_pikafish)
            else -> source.engineId
        }
    }
    val subtitle = when (source) {
        XiangqiAiSource.WorkingModel -> workingModelTitle
        XiangqiAiSource.Jev -> stringResource(R.string.xiangqi_ai_source_jev_desc)
        is XiangqiAiSource.RemoteEngine -> stringResource(R.string.xiangqi_ai_source_engine_desc)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SourceTags(source = source)
        }
        if (source is XiangqiAiSource.RemoteEngine) {
            Icon(
                imageVector = Icons.Outlined.LineMemory,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (source == XiangqiAiSource.Jev) {
            Icon(
                imageVector = Icons.Outlined.LinePsychology,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
private fun SourceTags(source: XiangqiAiSource) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp),
    ) {
        if (source.requiresLogin) {
            SourceTag(stringResource(R.string.xiangqi_ai_tag_login), Color(0xFFF08A5D))
        }
        if (source.requiresPoints) {
            SourceTag(stringResource(R.string.xiangqi_ai_tag_points), Color(0xFF4F46E5))
        }
        if (!source.requiresLogin && !source.requiresPoints) {
            SourceTag(stringResource(R.string.xiangqi_ai_tag_free), Color(0xFF3D8B7A))
        }
    }
}

@Composable
private fun SourceTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.14f),
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
            ),
            color = color.copy(alpha = 0.92f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
