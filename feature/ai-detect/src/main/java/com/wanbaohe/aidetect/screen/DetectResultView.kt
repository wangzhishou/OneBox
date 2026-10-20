package com.wanbaohe.aidetect.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassCircularProgressIndicator
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedButton
import com.wanbaohe.aidetect.R
import com.wanbaohe.aidetect.service.AiDetectService
import java.util.Locale

/**
 * 检测结果卡片(文本/图片 tab 与历史详情共用):
 * 大号环形进度 + 中心百分比 + 「AI 生成概率」+ 判定文案 + 重新检测/查看详情按钮。
 */
@Composable
fun DetectResultCard(
    probability: Float,
    isDetailExpanded: Boolean,
    onRecheck: () -> Unit,
    onToggleDetail: () -> Unit,
    modifier: Modifier = Modifier,
    showActions: Boolean = true,
    detailContent: @Composable ColumnScope.() -> Unit = {},
) {
    val verdictKey = AiDetectService.verdictKey(probability)
    val color = verdictColor(verdictKey)

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center,
            ) {
                GlassCircularProgressIndicator(
                    progress = { probability.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    color = color,
                    strokeWidth = 12.dp,
                )
                Text(
                    text = formatPercent(probability),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.ai_detect_probability_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = verdictText(verdictKey),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (showActions) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassOutlinedButton(onClick = onRecheck) {
                        Text(stringResource(R.string.ai_detect_recheck))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    GlassOutlinedButton(onClick = onToggleDetail) {
                        Text(
                            stringResource(
                                if (isDetailExpanded) R.string.ai_detect_collapse_detail
                                else R.string.ai_detect_view_detail
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isDetailExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                ) {
                    detailContent()
                }
            }
        }
    }
}

/** 文本检测详情:分段标签 + 标签占比 */
@Composable
fun TextDetectDetailContent(
    segments: List<com.shifenmiao.model.aidetect.AiDetectSegmentLabel>,
    labelsRatio: Map<String, Float>,
) {
    if (labelsRatio.isNotEmpty()) {
        Text(
            text = stringResource(R.string.ai_detect_labels_ratio_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabelRatioRow(
            label = stringResource(R.string.ai_detect_segment_human),
            ratio = labelsRatio["0"] ?: 0f,
        )
        LabelRatioRow(
            label = stringResource(R.string.ai_detect_segment_ai),
            ratio = labelsRatio["1"] ?: 0f,
        )
        LabelRatioRow(
            label = stringResource(R.string.ai_detect_segment_suspected),
            ratio = labelsRatio["2"] ?: 0f,
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    segments.forEach { segment ->
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = segmentLabelText(segment.label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = segmentLabelColor(segment.label),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatPercent(segment.conf),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = segment.text,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun LabelRatioRow(label: String, ratio: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = formatPercent(ratio),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun segmentLabelText(label: Int): String = stringResource(
    when (label) {
        0 -> R.string.ai_detect_segment_human
        1 -> R.string.ai_detect_segment_ai
        else -> R.string.ai_detect_segment_suspected
    }
)

@Composable
private fun segmentLabelColor(label: Int) = when (label) {
    0 -> MaterialTheme.colorScheme.onPrimaryContainer
    1 -> MaterialTheme.colorScheme.onErrorContainer
    else -> MaterialTheme.colorScheme.onTertiaryContainer
}

internal fun formatPercent(value: Float): String =
    String.format(Locale.US, "%.1f%%", value * 100)

/** 居中的空态/加载态提示 */
@Composable
fun CenteredHint(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
