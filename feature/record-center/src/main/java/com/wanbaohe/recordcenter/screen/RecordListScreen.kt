package com.wanbaohe.recordcenter.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.base.ui.StreamingMarkdownContent
import com.shifenmiao.base.ui.empty.EmptyStateGuide
import com.shifenmiao.base.ui.empty.EmptyStateGuideAction
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.aiHealthInsightPointsCost
import com.shifenmiao.database.recordcenter.entity.HealthRecordEntity
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.ui.widget.charts.LineTrendChart
import com.t8rin.imagetoolbox.core.ui.widget.charts.TrendSeries
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.component.RecordFieldStats
import com.wanbaohe.recordcenter.component.RecordInsightState
import com.wanbaohe.recordcenter.component.RecordListComponent
import com.wanbaohe.recordcenter.component.RecordRangeFilter
import com.wanbaohe.recordcenter.model.RecordFieldsCodec
import com.wanbaohe.recordcenter.registry.RecordField
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition
import com.wanbaohe.recordcenter.screen.util.formatChartDate
import com.wanbaohe.recordcenter.screen.util.formatRecordDateTime
import com.wanbaohe.recordcenter.screen.util.formatRecordValues

/**
 * 记录列表页 — 某一类型的趋势图、统计与明细,支持时间范围筛选与增删改。
 */
@Composable
fun RecordListScreen(component: RecordListComponent) {
    val definition = component.definition
    val records by component.records.collectAsState()
    // null = 首次查询未返回;false = 该类型一条记录都没有
    val hasAnyRecords by component.hasAnyRecords.collectAsState()
    val stats by component.stats.collectAsState()
    val rangeFilter by component.rangeFilter.collectAsState()
    val insightState by component.insightState.collectAsState()
    val profile by component.profile.collectAsState()

    var pendingDeleteRecordId by remember { mutableStateOf<String?>(null) }
    // AI 解读前未设置基础信息:先弹编辑层,保存后自动继续解读
    var showProfileSheet by remember { mutableStateOf(false) }
    var pendingInsight by remember { mutableStateOf(false) }

    // 登录 + 积分预检:通过后才真正生成(同其他 AI 能力)
    fun launchInsight() {
        ActionUtils.ensureLoginAndCheckPoints(
            source = RecordListComponent.POINTS_SOURCE,
            point = aiHealthInsightPointsCost(),
        ) {
            component.generateInsight()
        }
    }

    BaseScreen(
        title = {
            Text(
                text = definition?.let { stringResource(it.titleRes) }
                    ?: stringResource(R.string.record_center_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        onGoBack = component.onGoBack,
        actions = {
            if (definition != null) {
                IconButton(onClick = component::navigateToAddRecord) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.record_center_add_record),
                    )
                }
            }
        },
        supportGlassEffect = true,
        content = {
            if (definition == null) {
                // 未知记录类型:空态兜底,不崩溃
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.record_center_error_unknown_type, component.recordTypeKey),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                RecordListContent(
                    definition = definition,
                    records = records,
                    stats = stats,
                    rangeFilter = rangeFilter,
                    insightState = insightState,
                    hasAnyRecords = hasAnyRecords,
                    onRangeFilterChange = component::setRangeFilter,
                    onGenerateInsight = {
                        if (profile.isSet) {
                            launchInsight()
                        } else {
                            pendingInsight = true
                            showProfileSheet = true
                        }
                    },
                    onEditRecord = component::navigateToEditRecord,
                    onDeleteRecord = { pendingDeleteRecordId = it },
                    onAddRecord = component::navigateToAddRecord,
                    onGoAiChat = component::navigateToAiChat,
                )
            }
        },
    )

    HealthProfileSheet(
        visible = showProfileSheet,
        initial = profile,
        onSave = {
            component.saveProfile(it)
            showProfileSheet = false
            if (pendingInsight) {
                pendingInsight = false
                launchInsight()
            }
        },
        onDismiss = {
            showProfileSheet = false
            pendingInsight = false
        },
    )

    // 删除确认弹窗
    pendingDeleteRecordId?.let { recordId ->
        EnhancedAlertDialog(
            visible = true,
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            enableGlass = true,
            onDismissRequest = { pendingDeleteRecordId = null },
            title = { Text(text = stringResource(R.string.record_center_delete_confirm_title)) },
            text = { Text(text = stringResource(R.string.record_center_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.deleteRecord(recordId)
                        pendingDeleteRecordId = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.record_center_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteRecordId = null }) {
                    Text(text = stringResource(R.string.record_center_cancel))
                }
            },
        )
    }
}

@Composable
private fun RecordListContent(
    definition: RecordTypeDefinition,
    records: List<HealthRecordEntity>,
    stats: List<RecordFieldStats>,
    rangeFilter: RecordRangeFilter,
    insightState: RecordInsightState,
    hasAnyRecords: Boolean?,
    onRangeFilterChange: (RecordRangeFilter) -> Unit,
    onGenerateInsight: () -> Unit,
    onEditRecord: (String) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onAddRecord: () -> Unit,
    onGoAiChat: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (hasAnyRecords) {
            // 首次查询未返回:先留白,避免"空态 → 内容"闪一下
            null -> Unit

            // 一条记录都没有:不显示时间范围筛选与 AI 解读,直接给空态引导
            false -> item {
                EmptyStateGuide(
                    icon = definition.icon,
                    title = stringResource(R.string.record_center_empty_title),
                    description = stringResource(R.string.record_center_empty_description),
                    actions = listOf(
                        EmptyStateGuideAction(
                            icon = Icons.Outlined.Add,
                            title = stringResource(R.string.record_center_add_manually),
                            description = stringResource(R.string.record_center_add_manually_description),
                            onClick = onAddRecord,
                        ),
                        EmptyStateGuideAction(
                            icon = Icons.Outlined.LineAutoFix,
                            title = stringResource(R.string.record_center_empty_go_ai),
                            description = stringResource(R.string.record_center_empty_go_ai_description),
                            onClick = onGoAiChat,
                            emphasized = true,
                        ),
                    ),
                    modifier = Modifier.fillParentMaxSize(),
                )
            }

            true -> {
                item {
                    RangeFilterRow(
                        selected = rangeFilter,
                        onSelect = onRangeFilterChange,
                    )
                }

                if (records.isEmpty()) {
                    // 有记录,只是当前范围内没有:保留筛选 Bar 让用户切回其他范围,同样不显示 AI 解读
                    item {
                        EmptyStateGuide(
                            icon = definition.icon,
                            title = stringResource(R.string.record_center_empty_range_title),
                            description = stringResource(R.string.record_center_empty_range_description),
                            actions = listOf(
                                EmptyStateGuideAction(
                                    icon = Icons.Outlined.Add,
                                    title = stringResource(R.string.record_center_add_manually),
                                    description = stringResource(R.string.record_center_add_manually_description),
                                    onClick = onAddRecord,
                                ),
                            ),
                            modifier = Modifier.fillParentMaxSize(),
                        )
                    }
                } else {
                    item {
                        TrendChartCard(definition = definition, records = records)
                    }
                    if (stats.isNotEmpty()) {
                        item {
                            StatsCard(definition = definition, stats = stats)
                        }
                    }

                    // AI 解读卡片:放在数据可视化(趋势图/统计)之下、记录列表之上,上下留白加大
                    item {
                        Box(modifier = Modifier.padding(vertical = 8.dp)) {
                            AiInsightCard(
                                state = insightState,
                                onGenerate = onGenerateInsight,
                            )
                        }
                    }

                    items(
                        items = records,
                        key = { it.id },
                    ) { record ->
                        RecordRow(
                            definition = definition,
                            record = record,
                            onClick = { onEditRecord(record.id) },
                            onDelete = { onDeleteRecord(record.id) },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun RangeFilterRow(
    selected: RecordRangeFilter,
    onSelect: (RecordRangeFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            RecordRangeFilter.DAYS_7 to stringResource(R.string.record_center_range_7d),
            RecordRangeFilter.DAYS_30 to stringResource(R.string.record_center_range_30d),
            RecordRangeFilter.ALL to stringResource(R.string.record_center_range_all),
        ).forEach { (filter, label) ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(text = label) },
            )
        }
    }
}

/** 趋势图:每个图表字段一条序列,时间正序 */
@Composable
private fun TrendChartCard(
    definition: RecordTypeDefinition,
    records: List<HealthRecordEntity>,
) {
    val seriesColors = listOf(
        MaterialTheme.colorScheme.onPrimaryContainer,
        MaterialTheme.colorScheme.onTertiaryContainer,
        MaterialTheme.colorScheme.error,
    )
    val ascending = remember(records) { records.sortedBy { it.happenedAt } }
    val decoded = remember(ascending) { ascending.map { RecordFieldsCodec.decode(it.fieldsJson) } }

    val series = definition.chartFieldKeys.mapIndexedNotNull { index, fieldKey ->
        val field = definition.fields.firstOrNull { it.key == fieldKey } ?: return@mapIndexedNotNull null
        TrendSeries(
            label = stringResource(field.labelRes),
            color = seriesColors[index % seriesColors.size],
            points = decoded.map { it[fieldKey] },
        )
    }
    val xLabels = ascending.map { formatChartDate(it.happenedAt) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderWidth = 0.dp,
        containerAlpha = 0.4f,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LineTrendChart(
                series = series,
                xLabels = xLabels,
                modifier = Modifier.fillMaxWidth(),
            )
            if (series.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    series.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            LegendDot(color = item.color)
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * AI 解读卡片:手动触发生成,不落库。
 * 仅在当前范围内有记录时渲染(无记录时整个块由列表页隐藏)。
 * Idle → 提示 + 生成按钮;Loading → 进度;Content → 正文 + 重新解读;Error → 错误 + 重试。
 */
@Composable
private fun AiInsightCard(
    state: RecordInsightState,
    onGenerate: () -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderWidth = 0.dp,
        containerAlpha = 0.4f,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineMagic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.record_center_ai_insight),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            when (state) {
                RecordInsightState.Idle -> {
                    Text(
                        text = stringResource(R.string.record_center_ai_insight_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GlassTonalButton(
                        onClick = onGenerate,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(R.string.record_center_ai_insight_generate))
                    }
                    Text(
                        text = stringResource(
                            R.string.record_center_ai_insight_points_hint,
                            aiHealthInsightPointsCost(),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }

                RecordInsightState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(24.dp),
                        )
                        Text(
                            text = stringResource(R.string.record_center_ai_insight_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                is RecordInsightState.Content -> {
                    StreamingMarkdownContent(content = state.text)
                    GlassOutlinedButton(
                        onClick = onGenerate,
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text(text = stringResource(R.string.record_center_ai_insight_regenerate))
                    }
                }

                is RecordInsightState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    GlassTonalButton(
                        onClick = onGenerate,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(R.string.record_center_ai_insight_retry))
                    }
                }
            }
        }
    }
}

/** 统计行:每个图表字段一行 最新/平均/最高/最低 */
@Composable
private fun StatsCard(
    definition: RecordTypeDefinition,
    stats: List<RecordFieldStats>,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderWidth = 0.dp,
        containerAlpha = 0.4f,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            stats.forEach { stat ->
                val field = definition.fields.firstOrNull { it.key == stat.fieldKey }
                    ?: return@forEach
                StatRow(field = field, stat = stat)
            }
        }
    }
}

@Composable
private fun StatRow(field: RecordField, stat: RecordFieldStats) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(field.labelRes) +
                if (field.unit.isNotEmpty()) " (${field.unit})" else "",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCell(
                label = stringResource(R.string.record_center_stats_latest),
                value = stat.latest,
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = stringResource(R.string.record_center_stats_average),
                value = stat.average,
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = stringResource(R.string.record_center_stats_max),
                value = stat.max,
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = stringResource(R.string.record_center_stats_min),
                value = stat.min,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCell(label: String, value: Float?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value?.let(RecordFieldsCodec::formatStatsValue) ?: "--",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RecordRow(
    definition: RecordTypeDefinition,
    record: HealthRecordEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderWidth = 0.dp,
        containerAlpha = 0.4f,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = formatRecordValues(definition, record.fieldsJson),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!record.note.isNullOrBlank()) {
                    Text(
                        text = record.note.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = formatRecordDateTime(record.happenedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.record_center_action_delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
