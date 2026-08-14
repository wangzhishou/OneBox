package com.wanbaohe.markuplayers.presentation.tools.ai

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Fullscreen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentCut
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHealing
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHighQuality
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStyle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWatermarking
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R

/**
 * 「AI 处理」底部面板(设计稿「AI处理Tab展开界面」):
 * 标题行(带 NEW 角标)+ 副标题 + 7 张功能卡(2 列网格,末卡通栏)。
 * 功能均为占位:点击统一 toast「敬请期待」,保持可点不置灰。
 */
@Composable
fun AiToolSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
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
                AiFeature.entries.toList().chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { feature ->
                            AiFeatureCard(
                                feature = feature,
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
    feature: AiFeature,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { AppToastHost.showToast(R.string.markup_coming_soon) }
            .padding(10.dp)
    ) {
        Icon(
            imageVector = feature.icon,
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
                text = stringResource(feature.titleRes),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1
            )
            Text(
                text = stringResource(feature.subtitleRes),
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
}

/** AI 占位功能卡;顺序即面板展示顺序,末位单卡自动通栏 */
private enum class AiFeature(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
) {
    Enhance(
        icon = Icons.Outlined.LineAutoFix,
        titleRes = R.string.markup_ai_enhance,
        subtitleRes = R.string.markup_ai_enhance_sub
    ),
    Cutout(
        icon = Icons.Outlined.LineContentCut,
        titleRes = R.string.markup_ai_cutout,
        subtitleRes = R.string.markup_ai_cutout_sub
    ),
    StyleTransfer(
        icon = Icons.Outlined.LineStyle,
        titleRes = R.string.markup_ai_style,
        subtitleRes = R.string.markup_ai_style_sub
    ),
    Repair(
        icon = Icons.Outlined.LineHealing,
        titleRes = R.string.markup_ai_repair,
        subtitleRes = R.string.markup_ai_repair_sub
    ),
    Outpaint(
        icon = Icons.Outlined.Fullscreen,
        titleRes = R.string.markup_ai_outpaint,
        subtitleRes = R.string.markup_ai_outpaint_sub
    ),
    Dewatermark(
        icon = Icons.Outlined.LineWatermarking,
        titleRes = R.string.markup_ai_dewatermark,
        subtitleRes = R.string.markup_ai_dewatermark_sub
    ),
    Upscale(
        icon = Icons.Outlined.LineHighQuality,
        titleRes = R.string.markup_ai_upscale,
        subtitleRes = R.string.markup_ai_upscale_sub
    ),
}
