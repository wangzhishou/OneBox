package com.wanbaohe.period.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEmojiFace
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHealing
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStickyNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWaterDrop
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.period.R
import com.wanbaohe.period.component.PeriodDetailComponent
import com.wanbaohe.period.component.PeriodDetailUi
import com.wanbaohe.period.model.PeriodColors
import com.wanbaohe.period.model.PeriodStatus
import com.wanbaohe.period.model.periodColors
import com.wanbaohe.period.screen.component.PeriodFlowDrops
import com.wanbaohe.period.screen.component.PeriodInfoIcon
import com.wanbaohe.period.screen.component.PeriodMoodFace
import com.wanbaohe.period.screen.component.flowLabel
import com.wanbaohe.period.screen.component.moodLabel
import com.wanbaohe.period.screen.component.periodFullDate
import com.wanbaohe.period.screen.component.periodShortDate
import com.wanbaohe.period.screen.component.periodWeekday
import com.wanbaohe.period.screen.component.symptomLabel
import java.time.format.TextStyle

@Composable
fun PeriodDetailScreen(
    component: PeriodDetailComponent,
) {
    val state by component.uiState.collectAsState()
    val colors = periodColors()

    BaseScreen(
        title = stringResource(R.string.period_detail_title),
        onGoBack = component.onGoBack,
        actions = {
            IconButton(onClick = component::openEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.period_edit_title),
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (!state.isLoaded) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.period_loading),
                        color = colors.onSurfaceVariant,
                    )
                }
                return@Column
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HeroCard(colors = colors, state = state)
                DetailInfoRow(
                    icon = { PeriodInfoIcon(icon = Icons.Outlined.LineWaterDrop, colors = colors) },
                    title = stringResource(R.string.period_field_flow),
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        PeriodFlowDrops(level = state.flowIntensity, colors = colors)
                        Text(
                            text = flowLabel(state.flowIntensity),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
                if (state.symptoms.isNotEmpty()) {
                    DetailInfoRow(
                        icon = { PeriodInfoIcon(icon = Icons.Outlined.LineHealing, colors = colors) },
                        title = stringResource(R.string.period_field_symptoms),
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            state.symptoms.forEach { symptom ->
                                Text(
                                    text = symptomLabel(symptom),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.accent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(colors.accentSoft)
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
                DetailInfoRow(
                    icon = { PeriodInfoIcon(icon = Icons.Outlined.LineEmojiFace, colors = colors) },
                    title = stringResource(R.string.period_field_mood),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PeriodMoodFace(
                            mood = state.mood,
                            color = colors.accent,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = moodLabel(state.mood),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface,
                        )
                    }
                }
                if (!state.note.isNullOrBlank()) {
                    DetailInfoRow(
                        icon = { PeriodInfoIcon(icon = Icons.Outlined.LineStickyNote, colors = colors) },
                        title = stringResource(R.string.period_field_note),
                        alignTop = true,
                    ) {
                        Text(
                            text = state.note.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
                TimelineCard(colors = colors, state = state)
            }
        }
    }
}

// ── 顶部渐变英雄卡 ─────────────────────────────────

@Composable
private fun HeroCard(colors: PeriodColors, state: PeriodDetailUi) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.accentContainer)
            .padding(20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${state.recordDate.periodFullDate()} " +
                        state.recordDate.periodWeekday(TextStyle.FULL),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onAccentContainer.copy(alpha = 0.85f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.cycleDay?.let {
                        stringResource(R.string.period_cycle_day, it)
                    } ?: stringResource(R.string.period_cycle_day_unknown),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onAccentContainer,
                )
                Spacer(modifier = Modifier.height(8.dp))
                HeroStatusBadge(colors = colors, status = state.status)
                Spacer(modifier = Modifier.height(12.dp))
                state.nextPeriodStart?.let { next ->
                    Text(
                        text = stringResource(R.string.period_next_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onAccentContainer.copy(alpha = 0.75f),
                    )
                    Text(
                        text = "${next.periodShortDate()} ${next.periodWeekday()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onAccentContainer,
                    )
                    state.daysUntilNext?.let { days ->
                        Text(
                            text = stringResource(R.string.period_days_until, days),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onAccentContainer.copy(alpha = 0.75f),
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineWaterDrop,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(44.dp),
                )
            }
        }
    }
}

@Composable
private fun HeroStatusBadge(colors: PeriodColors, status: PeriodStatus) {
    val label = when (status) {
        PeriodStatus.PERIOD -> stringResource(R.string.period_status_period)
        PeriodStatus.OVULATION -> stringResource(R.string.period_status_ovulation)
        PeriodStatus.NORMAL -> stringResource(R.string.period_status_normal)
    }
    Text(
        text = label,
        color = colors.onAccent,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colors.accent)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

// ── 信息行 ─────────────────────────────────────────

@Composable
private fun DetailInfoRow(
    icon: @Composable () -> Unit,
    title: String,
    alignTop: Boolean = false,
    content: @Composable () -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = if (alignTop) Alignment.Top else Alignment.CenterVertically,
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .width(64.dp)
                    .padding(top = if (alignTop) 8.dp else 0.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = if (alignTop) Alignment.TopStart else Alignment.CenterEnd,
            ) {
                content()
            }
        }
    }
}

// ── 时间线 ─────────────────────────────────────────

private data class TimelineItem(
    val label: String,
    val date: String,
    val highlight: Boolean,
)

@Composable
private fun TimelineCard(colors: PeriodColors, state: PeriodDetailUi) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.period_timeline_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
            val cycleStart = if (state.cycleDay != null) {
                state.recordDate.minusDays((state.cycleDay - 1).toLong())
            } else {
                state.recordDate
            }
            val items = buildList {
                add(
                    TimelineItem(
                        label = stringResource(R.string.period_timeline_start),
                        date = "${cycleStart.periodShortDate()} ${cycleStart.periodWeekday()}",
                        highlight = false,
                    )
                )
                add(
                    TimelineItem(
                        label = state.cycleDay?.let {
                            stringResource(R.string.period_cycle_day, it)
                        } ?: stringResource(R.string.period_cycle_day_unknown),
                        date = "${state.recordDate.periodShortDate()} ${state.recordDate.periodWeekday()}",
                        highlight = true,
                    )
                )
                state.predictedPeriodEnd?.let { end ->
                    add(
                        TimelineItem(
                            label = stringResource(R.string.period_timeline_end),
                            date = "${end.periodShortDate()} ${end.periodWeekday()}",
                            highlight = false,
                        )
                    )
                }
            }
            items.forEachIndexed { index, item ->
                TimelineRow(item = item, colors = colors, isLast = index == items.lastIndex)
            }
        }
    }
}

@Composable
private fun TimelineRow(
    item: TimelineItem,
    colors: PeriodColors,
    isLast: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(if (item.highlight) 14.dp else 12.dp)
                    .clip(CircleShape)
                    .background(if (item.highlight) colors.accent else colors.accentSoft),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(colors.accentSoft),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (item.highlight) FontWeight.SemiBold else FontWeight.Normal,
                color = if (item.highlight) colors.accent else colors.onSurface,
            )
            Text(
                text = item.date,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
