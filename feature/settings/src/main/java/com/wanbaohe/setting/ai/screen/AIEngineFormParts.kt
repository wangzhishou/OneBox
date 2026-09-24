package com.wanbaohe.setting.ai.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.settings.R

// 分组卡片容器(无标题分组), 模型服务新增/编辑页共用
@Composable
internal fun SettingCard(content: @Composable () -> Unit) {
    OneBoxSectionCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            content()
        }
    }
}

// 可折叠分组头: 中号加粗标题 + 行尾旋转箭头, 整行可点; summary 用于收起时展示当前取值
@Composable
internal fun CollapsibleSectionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    summary: String? = null,
) {
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!summary.isNullOrBlank()) {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )
        } else {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        }
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .rotate(arrowRotation),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 网格/横向列表通用选择卡片(模型/协议/鉴权共用):选中高亮(primary 描边 + 对勾),未选中细描边 */
@Composable
internal fun SelectGridCard(
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f)
        },
        border = if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        },
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
internal fun protocolLabel(protocol: AiRequestProtocol): String {
    return when (protocol) {
        AiRequestProtocol.OPENAI_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_openai)
        AiRequestProtocol.RESPONSES_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_responses)
        AiRequestProtocol.ANTHROPIC_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_anthropic)
        AiRequestProtocol.OWN_PROXY -> stringResource(R.string.ai_engine_protocol_proxy)
        // 仅云端协议出现在本选择器；
        // LOCAL_ON_DEVICE 由独立的"本地模型管理"页处理（Phase 2）。
        AiRequestProtocol.LOCAL_ON_DEVICE -> stringResource(R.string.ai_engine_protocol_local_on_device)
        AiRequestProtocol.JEV -> stringResource(R.string.ai_engine_protocol_jev)
        AiRequestProtocol.PIKAFISH -> stringResource(R.string.ai_engine_protocol_pikafish)
    }
}

@Composable
internal fun authTypeLabel(authType: AuthType): String {
    return when (authType) {
        AuthType.BEARER -> stringResource(R.string.ai_engine_auth_type_bearer)
        AuthType.API_KEY -> stringResource(R.string.ai_engine_auth_type_api_key)
        AuthType.NONE -> stringResource(R.string.ai_engine_auth_type_none)
    }
}
