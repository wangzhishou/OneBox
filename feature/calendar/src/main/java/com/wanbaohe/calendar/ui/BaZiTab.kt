package com.wanbaohe.calendar.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.CardDefaults
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.base.ui.utils.Animation.StaggeredAnimatedItem
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.calendar.R
import com.wanbaohe.calendar.data.CalendarUiState
import com.wanbaohe.calendar.data.CHINESE_HOUR_SLOTS
import com.wanbaohe.calendar.data.DaYunItem
import com.wanbaohe.calendar.data.LunarCalendarCalculator
import com.wanbaohe.calendar.data.formatLunarMonthDay
import com.wanbaohe.calendar.data.getChineseHourSlot
import java.time.LocalDate
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic

/**
 * 八字 Tab
 *
 * 显示：生辰八字四柱、大运走势、五行分布、流年详批
 */
@Composable
fun BaZiTab(
    state: CalendarUiState,
    onSelectDate: (Int, Int, Int) -> Unit,
    onSelectHour: (Int) -> Unit,
    onShare: () -> Unit,
    onAiBaZiClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val baZi = state.baZiData ?: return
    val baZiLunar = remember(state.baZiYear, state.baZiMonth, state.baZiDay) {
        LunarCalendarCalculator.solarToLunar(state.baZiYear, state.baZiMonth, state.baZiDay)
    }
    val selectedHourSlot = getChineseHourSlot(state.baZiHour)

    if (showDatePicker) {
        ChineseDatePickerDialog(
            initialDate = LocalDate.of(state.baZiYear, state.baZiMonth, state.baZiDay),
            onDateSelected = { date ->
                onSelectDate(date.year, date.monthValue, date.dayOfMonth)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            minYear = 1900,
            maxYear = 2100
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        StaggeredAnimatedItem(index = 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(
                            R.string.date_ymd_format,
                            state.baZiYear, state.baZiMonth, state.baZiDay
                        ),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${stringResource(R.string.lunar_label)} ${formatLunarMonthDay(baZiLunar.monthName, baZiLunar.dayName, baZiLunar.isLeapMonth)} ${selectedHourSlot.displayName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                        contentDescription = stringResource(R.string.select_bazi_date),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        StaggeredAnimatedItem(index = 1) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CHINESE_HOUR_SLOTS) { option ->
                    val isSelected = state.baZiHour == option.hour
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
                    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    val labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .glassBackground(
                                shape = RoundedCornerShape(12.dp),
                                color = bgColor
                            )
                            .clickable { onSelectHour(option.hour) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Column(horizontalAlignment = if (isSelected) Alignment.Start else Alignment.CenterHorizontally) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = labelColor
                                )
                                Text(
                                    text = option.timeRange,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── 生辰八字卡片 ──────────────────────────────────────
        StaggeredAnimatedItem(index = 2) {
            NatalChartCard(baZi = baZi)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 大运走势卡片 ──────────────────────────────────────
        StaggeredAnimatedItem(index = 3) {
            DaYunCard(daYunList = state.daYunList)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 五行分布卡片 ──────────────────────────────────────
        StaggeredAnimatedItem(index = 4) {
            WuXingCard(wuXingDist = baZi.wuXingDistribution)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 流年详批卡片 ──────────────────────────────────────
        StaggeredAnimatedItem(index = 5) {
            state.fortuneData?.let { fortune ->
                FortuneCard(fortune = fortune)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 底部 Wisdom 卡片 ──────────────────────────────────
        StaggeredAnimatedItem(index = 6) {
            WisdomCard()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── AI 解盘入口 ────────────────────────────────────────
        StaggeredAnimatedItem(index = 7) {
            AiBaZiCard(onClick = onAiBaZiClick)
        }

        Spacer(modifier = Modifier.height(16.dp))

        StaggeredAnimatedItem(index = 8) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FilledTonalButton(
                    onClick = onShare,
                    colors = AppTheme.colors.filledTonalButtonColors()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.share_current_tab),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}


/** 生辰八字卡片 */
@Composable
private fun NatalChartCard(
    baZi: com.wanbaohe.calendar.data.BaZiData
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.natal_chart_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.natal_chart),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${stringResource(R.string.day_master)}：${baZi.dayMaster}${getWuXingForGan(baZi.dayMaster)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${baZi.strength}·${baZi.favorableElements}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 十神行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(baZi.yearPillar, baZi.monthPillar, baZi.dayPillar, baZi.hourPillar).forEach {
                    Text(
                        text = it.shiShen,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (it.shiShen == "日主") MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 天干行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(baZi.yearPillar, baZi.monthPillar, baZi.dayPillar, baZi.hourPillar).forEach {
                    PillarBox(
                        text = it.tianGan,
                        isHighlight = it.shiShen == "日主"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 地支行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(baZi.yearPillar, baZi.monthPillar, baZi.dayPillar, baZi.hourPillar).forEach {
                    PillarBox(text = it.diZhi, isHighlight = false)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 柱名行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    stringResource(R.string.year_pillar),
                    stringResource(R.string.month_pillar),
                    stringResource(R.string.day_pillar),
                    stringResource(R.string.hour_pillar)
                ).forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(60.dp)
                    )
                }
            }
        }
    }
}

/** 八字方格 */
@Composable
private fun PillarBox(text: String, isHighlight: Boolean) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .glassBackground(
                shape = RoundedCornerShape(8.dp),
                color = if (isHighlight) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHighest
            )
            .then(
                if (!isHighlight) Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(8.dp)
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = if (isHighlight) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

/** 大运走势卡片 */
@Composable
private fun DaYunCard(daYunList: List<DaYunItem>) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.dayun_trend),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 干支方格行
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daYunList) { item ->
                    DaYunBox(item = item)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 当前大运说明
            daYunList.find { it.isCurrent }?.let { current ->
                Text(
                    text = stringResource(
                        R.string.current_dayun,
                        current.ganZhi,
                        getRandomDaYunDesc(current.ganZhi)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DaYunBox(item: DaYunItem) {
    val gan = if (item.ganZhi.length >= 2) item.ganZhi.substring(0, 1) else ""
    val zhi = if (item.ganZhi.length >= 2) item.ganZhi.substring(1, 2) else ""

    Column(
        modifier = Modifier
            .width(80.dp)
            .height(80.dp)
            .glassBackground(
                shape = RoundedCornerShape(12.dp),
                color = if (item.isCurrent) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surfaceContainerHighest
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${item.startYear} - ${item.startYear + 9}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (item.isCurrent) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = gan,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                color = if (item.isCurrent) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = zhi,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                color = if (item.isCurrent) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** 五行分布卡片 */
@Composable
private fun WuXingCard(wuXingDist: Map<String, Float>) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.wuxing_dist),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 五行列表
            val wuXingColors = mapOf(
                "水" to MaterialTheme.colorScheme.tertiary,
                "火" to MaterialTheme.colorScheme.error,
                "金" to MaterialTheme.colorScheme.secondary,
                "木" to MaterialTheme.colorScheme.primary,
                "土" to MaterialTheme.colorScheme.outline
            )
            val wuXingStrength = mapOf(
                0f..15f to stringResource(R.string.very_weak),
                15f..30f to stringResource(R.string.balanced),
                30f..100f to stringResource(R.string.extremely_strong)
            )

            wuXingDist.entries.sortedByDescending { it.value }.forEach { (element, value) ->
                val strength = wuXingStrength.entries.find { value in it.key }?.value
                    ?: stringResource(R.string.balanced)
                val color = wuXingColors[element] ?: MaterialTheme.colorScheme.primary

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$element ($strength)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = color,
                        modifier = Modifier.width(80.dp)
                    )
                    LinearProgressIndicator(
                        progress = { (value / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${value.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

/** 流年详批卡片 */
@Composable
private fun FortuneCard(fortune: com.wanbaohe.calendar.data.FortuneData) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.yearly_fortune),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.switch_year),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 年份与运势
            Row(verticalAlignment = Alignment.Top) {
                // 年份标签
                Box(
                    modifier = Modifier
                        .glassBackground(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = fortune.ganZhiYear.substring(0, 1),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = fortune.ganZhiYear.substring(1, 2),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${fortune.year}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 运势描述
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fortune.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fortune.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 标签
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FortuneTag(
                            text = "FORTUNE:${fortune.fortuneScore}%",
                            color = MaterialTheme.colorScheme.primary
                        )
                        FortuneTag(
                            text = "CAREER:${fortune.careerLevel}",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 月运势网格
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fortune.monthlyFortunes.forEach { mf ->
                    MonthFortuneCard(
                        fortune = mf,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FortuneTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, color, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun MonthFortuneCard(
    fortune: com.wanbaohe.calendar.data.MonthlyFortune,
    modifier: Modifier = Modifier
) {
    val tagColor = when (fortune.tagType) {
        "positive" -> MaterialTheme.colorScheme.primary
        "negative" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }

    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${fortune.monthDisplay}·${fortune.ganZhi}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = fortune.keyword,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = fortune.tag,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = tagColor
            )
        }
    }
}

/** Wisdom 卡片 */
@Composable
private fun WisdomCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.wisdom_cycles),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.wisdom_desc),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─── 工具函数 ────────────────────────────────────────────────

/** AI 解盘卡片 */
@Composable
private fun AiBaZiCard(onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .glassBackground(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.bazi_ai_interpret_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.bazi_ai_interpret_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            ) {
                Text(
                    text = stringResource(R.string.bazi_ai_interpret_btn),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private fun getWuXingForGan(gan: String): String = when (gan) {
    "甲", "乙" -> "木"
    "丙", "丁" -> "火"
    "戊", "己" -> "土"
    "庚", "辛" -> "金"
    "壬", "癸" -> "水"
    else -> ""
}

private fun getRandomDaYunDesc(ganZhi: String): String {
    val descs = listOf(
        "水势壮阔，利于事业开拓与资源广进",
        "火旺土生，财运稳健，宜守成发展",
        "木气生发，学业有成，事业上升期",
        "金水相生，贵人运佳，适合拓展人脉"
    )
    return descs[ganZhi.hashCode().and(0x3)]
}

