package com.wanbaohe.markuplayers.presentation.tools.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassDense
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.AiImageOp

/**
 * 「AI 处理」底部面板(设计稿「AI处理Tab展开界面」):
 * 标题行(带 NEW 角标)+ 副标题 + [AiImageOp] 能力卡(2 列网格,玻璃背景,
 * 卡片右上角展示单次消耗积分)。
 * 点击卡片经 [onOpClick] 交给外部执行(登录/积分预检在外层完成):
 * 直出能力立即处理,[AiImageOp.needsRect] 的能力(图像修复)先进入框选模式。
 */
@Composable
fun AiToolSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    pointsCost: Int,
    onOpClick: (AiImageOp) -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        sheetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                AiSheetHeader()
                AiImageOp.entries.toList().chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { op ->
                            AiFeatureCard(
                                op = op,
                                pointsCost = pointsCost,
                                onClick = { onOpClick(op) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    )
}

@Composable
private fun AiSheetHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.markup_ai_sheet_title),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.markup_ai_badge_new),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier
                    .clip(ShapeDefaults.small)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
        Text(
            text = stringResource(R.string.markup_ai_sheet_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AiFeatureCard(
    op: AiImageOp,
    pointsCost: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(ShapeDefaults.default)
            // 与浮动面板同款:0.92 实色打底,玻璃层只保留边框/高光与一丝通透
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
                shape = ShapeDefaults.default
            )
            .glassDense(
                shape = ShapeDefaults.default,
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Icon(
                imageVector = op.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .clip(ShapeDefaults.small)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(8.dp)
                    .size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(op.nameRes),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                Text(
                    text = stringResource(op.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.LineChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        // 单次消耗积分角标(值跟随远程配置)
        Text(
            text = stringResource(R.string.markup_ai_points_badge, pointsCost),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .clip(ShapeDefaults.small)
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}
