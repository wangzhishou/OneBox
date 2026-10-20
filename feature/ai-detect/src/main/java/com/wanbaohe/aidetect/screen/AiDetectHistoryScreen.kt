package com.wanbaohe.aidetect.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.database.aidetect.entity.AiDetectRecordEntity
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDeleteForever
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDescription
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.aidetect.R
import com.wanbaohe.aidetect.component.AiDetectComponent
import com.wanbaohe.aidetect.service.AiDetectService
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 检测历史页(模块内页面):列表 + 清空全部 + 单条删除 + 点击看详情 */
@Composable
fun AiDetectHistoryScreen(component: AiDetectComponent) {
    val records by component.history.collectAsState()
    var isClearDialogVisible by remember { mutableStateOf(false) }

    BaseScreen(
        title = stringResource(R.string.ai_detect_history),
        onGoBack = component::handleBack,
        isShowDefaultActions = true,
        supportGlassEffect = true,
        actions = {
            if (records.isNotEmpty()) {
                IconButton(onClick = { isClearDialogVisible = true }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDeleteForever,
                        contentDescription = stringResource(R.string.ai_detect_clear_all),
                    )
                }
            }
        },
    ) {
        if (records.isEmpty()) {
            CenteredHint(
                text = stringResource(R.string.ai_detect_history_empty),
                modifier = Modifier.padding(32.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(records, key = { it.id }) { record ->
                    HistoryRecordItem(
                        record = record,
                        onClick = { component.openHistoryDetail(record) },
                        onDelete = { component.deleteRecord(record.id) },
                    )
                }
            }
        }
    }

    if (isClearDialogVisible) {
        AlertDialog(
            onDismissRequest = { isClearDialogVisible = false },
            title = { Text(stringResource(R.string.ai_detect_clear_all)) },
            text = { Text(stringResource(R.string.ai_detect_clear_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.clearHistory()
                        isClearDialogVisible = false
                    }
                ) {
                    Text(stringResource(R.string.ai_detect_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { isClearDialogVisible = false }) {
                    Text(stringResource(R.string.ai_detect_cancel))
                }
            },
        )
    }
}

@Composable
private fun HistoryRecordItem(
    record: AiDetectRecordEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (record.type == AiDetectService.TYPE_IMAGE && record.inputSummary.isNotBlank()) {
                AsyncImage(
                    model = File(record.inputSummary),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = if (record.type == AiDetectService.TYPE_IMAGE) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription
                    },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (record.type == AiDetectService.TYPE_IMAGE) {
                        stringResource(R.string.ai_detect_type_image)
                    } else {
                        record.inputSummary
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatRecordTime(record.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatPercent(record.probability),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = verdictColor(record.verdict),
                )
                Text(
                    text = verdictText(record.verdict),
                    style = MaterialTheme.typography.labelSmall,
                    color = verdictColor(record.verdict),
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDeleteForever,
                    contentDescription = stringResource(R.string.ai_detect_delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** 历史详情页:复用结果卡片,文本显示分段标签、图片显示置信度说明与缩略图 */
@Composable
fun AiDetectHistoryDetailScreen(component: AiDetectComponent) {
    val detail by component.historyDetail.collectAsState()

    BaseScreen(
        title = stringResource(R.string.ai_detect_detail_title),
        onGoBack = component::handleBack,
        isShowDefaultActions = true,
        supportGlassEffect = true,
    ) {
        val current = detail
        if (current == null) {
            CenteredHint(
                text = stringResource(R.string.ai_detect_history_empty),
                modifier = Modifier.padding(32.dp),
            )
            return@BaseScreen
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            val record = current.record
            if (record.type == AiDetectService.TYPE_IMAGE && record.inputSummary.isNotBlank()) {
                AsyncImage(
                    model = File(record.inputSummary),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            DetectResultCard(
                probability = record.probability,
                isDetailExpanded = true,
                onRecheck = {},
                onToggleDetail = {},
                showActions = false,
            ) {
                if (record.type == AiDetectService.TYPE_TEXT) {
                    TextDetectDetailContent(
                        segments = current.segments,
                        labelsRatio = current.labelsRatio,
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.ai_detect_image_confidence_detail,
                            (record.probability * 100).toInt(),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

private fun formatRecordTime(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
