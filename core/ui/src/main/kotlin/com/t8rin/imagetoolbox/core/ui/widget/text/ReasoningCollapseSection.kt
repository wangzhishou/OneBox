package com.t8rin.imagetoolbox.core.ui.widget.text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwitchAccess
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle

/**
 * 深度思考内容折叠区:整体包在 Glass 背景块里,折叠时是一行标题块,展开时背景块随内容撑开。
 * 折叠态会展示最新一行思考内容;[isStreaming] 为 true(仍在思考)时该行走马灯滚动,
 * 以提示正在生成。展开/收起状态走全局开关 [AppSharedStorage.isExpandedReasoningChat],
 * 与 AI 助手的思考卡片保持一致。内容为空白时不占位。
 */
@Composable
fun ReasoningCollapseSection(
    reasoning: String,
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    if (reasoning.isBlank()) return
    val expanded by AppSharedStorage.isExpandedReasoningChat.collectAsState()
    // 折叠态预览:取最后一行非空内容(对齐 AI 助手的 buildReasoningPreview 策略)
    val preview = remember(reasoning) {
        reasoning.replace("\r\n", "\n")
            .split('\n')
            .lastOrNull { it.isNotBlank() }
            ?.trim()
            ?.take(120)
            .orEmpty()
    }
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = contentColor,
        ),
        style = GlassStyle.Medium,
        borderWidth = 0.7.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { AppSharedStorage.saveIsExpandedReasoningChat(!expanded) }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineSwitchAccess,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.ai_reasoning),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                    maxLines = 1,
                )
                if (!expanded && preview.isNotEmpty()) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (isStreaming) Modifier.marquee() else Modifier),
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Icon(
                    imageVector = if (expanded) {
                        Icons.Outlined.LineExpandLess
                    } else {
                        Icons.Outlined.LineExpandMore
                    },
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp),
                )
            }
            if (expanded) {
                Text(
                    text = reasoning,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
