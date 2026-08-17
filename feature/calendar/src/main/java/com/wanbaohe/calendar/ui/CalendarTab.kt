package com.wanbaohe.calendar.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.utils.Animation.StaggeredAnimatedItem
import com.shifenmiao.base.ui.calendar.ChineseWeekdayHeader
import com.shifenmiao.base.ui.calendar.MonthCalendarGrid
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.calendar.R
import com.wanbaohe.calendar.data.CalendarUiState
import com.wanbaohe.calendar.data.CalendarDayInfo
import com.wanbaohe.calendar.data.FestivalType
import com.wanbaohe.calendar.data.FotoData
import com.wanbaohe.calendar.data.LunarCalendarCalculator
import com.wanbaohe.calendar.data.LunarDate
import com.wanbaohe.calendar.data.LunarTimeSlot
import com.wanbaohe.calendar.data.TaoData
import com.wanbaohe.calendar.data.UpcomingFestivalItem
import com.wanbaohe.icons.Aquarius
import com.wanbaohe.icons.Aries
import com.wanbaohe.icons.Autumn
import com.wanbaohe.icons.Cancer
import com.wanbaohe.icons.Capricorn
import com.wanbaohe.icons.Cold
import com.wanbaohe.icons.Dog
import com.wanbaohe.icons.Dragon
import com.wanbaohe.icons.DragonBoat
import com.wanbaohe.icons.Frost
import com.wanbaohe.icons.Gemini
import com.wanbaohe.icons.Goat
import com.wanbaohe.icons.Heat
import com.wanbaohe.icons.Holiday
import com.wanbaohe.icons.Horse
import com.wanbaohe.icons.Labor
import com.wanbaohe.icons.Lantern
import com.wanbaohe.icons.Leo
import com.wanbaohe.icons.Libra
import com.wanbaohe.icons.MidAutumn
import com.wanbaohe.icons.Monkey
import com.wanbaohe.icons.NationalDay
import com.wanbaohe.icons.NewYear
import com.wanbaohe.icons.Ox
import com.wanbaohe.icons.Pig
import com.wanbaohe.icons.Pisces
import com.wanbaohe.icons.Rabbit
import com.wanbaohe.icons.Rain
import com.wanbaohe.icons.Rat
import com.wanbaohe.icons.Rooster
import com.wanbaohe.icons.Sagittarius
import com.wanbaohe.icons.Scorpio
import com.wanbaohe.icons.Snake
import com.wanbaohe.icons.Snow
import com.wanbaohe.icons.Spring
import com.wanbaohe.icons.Summer
import com.wanbaohe.icons.Taurus
import com.wanbaohe.icons.Tiger
import com.wanbaohe.icons.TombSweeping
import com.wanbaohe.icons.Virgo
import com.wanbaohe.icons.Winter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Calendar
import java.util.GregorianCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronLeft
import com.t8rin.imagetoolbox.core.resources.icons.Compass
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAquarius
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAries
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutumn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCancer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCapricorn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCold
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDog
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDragon
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDragonBoat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEmojiFace
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFirstPage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFrost
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGemini
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGoat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHeat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHoliday
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHorse
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLabor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLantern
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLastPage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLeo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLibra
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMidAutumn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMonkey
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNationalDay
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNewYear
import com.t8rin.imagetoolbox.core.resources.icons.line.LineOx
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePets
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePig
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePisces
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRabbit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRain
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRooster
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSagittarius
import com.t8rin.imagetoolbox.core.resources.icons.line.LineScorpio
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSnake
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSnow
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSpring
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSummer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSunny
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTaurus
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTiger
import com.t8rin.imagetoolbox.core.resources.icons.line.LineToday
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTombSweeping
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVirgo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWinter

/**
 * 日历 Tab（合并了原来的详情 Tab 内容）
 *
 * 显示月历网格 + 选中日详情卡片 + 宜忌卡片 + 详细信息 + 节气卡片
 */
@Composable
fun CalendarTab(
    state: CalendarUiState,
    onSelectDate: (Int, Int, Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onReset: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
    ) {// 选中日详情大卡片
        StaggeredAnimatedItem(index = 0) {
            state.lunarDate?.let { lunar ->
                SelectedDayCard(
                    lunar = lunar,
                    solarYear = state.currentYear,
                    solarMonth = state.currentMonth,
                    solarDay = state.selectedDay,
                    weekDay = LunarCalendarCalculator.getWeekDayName(
                        state.currentYear, state.currentMonth, state.selectedDay
                    ),
                    lunarMonthName = LunarCalendarCalculator.normalizeLunarMonthName(
                        lunar.monthName,
                        lunar.isLeapMonth
                    ),
                    lunarDayName = lunar.dayName,
                    ganZhiMonth = lunar.ganZhiMonth,
                    ganZhiDay = lunar.ganZhiDay,
                    zodiac = lunar.zodiac,
                    currentTimeSlot = getCurrentTimeSlot(state.timeSlots),
                    onReset = onReset
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        StaggeredAnimatedItem(index = 1) {
            Column {
                MonthNavigator(
                    year = state.currentYear,
                    month = state.currentMonth,
                    selectedDay = state.selectedDay,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onSelectDate = onSelectDate
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 星期标题行
                WeekHeader()
                Spacer(modifier = Modifier.height(4.dp))

                // 月历网格
                CalendarGrid(
                    days = state.calendarDays,
                    selectedDay = state.selectedDay,
                    currentMonth = state.currentMonth,
                    currentYear = state.currentYear,
                    onDayClick = { dayInfo ->
                        onSelectDate(dayInfo.solarYear, dayInfo.solarMonth, dayInfo.solarDay)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        // 宜忌双卡片
        StaggeredAnimatedItem(index = 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                YiJiCard(
                    title = stringResource(R.string.yi),
                    items = state.yiJi.yi,
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckCircleOutline,
                    accentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    chipContainerColor = MaterialTheme.colorScheme.surface,
                    chipContentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                YiJiCard(
                    title = stringResource(R.string.ji),
                    items = state.yiJi.ji,
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    accentColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    chipContainerColor = MaterialTheme.colorScheme.errorContainer,
                    chipContentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── 详细信息卡片（原 DetailTab 内容整合） ──────────────────
        StaggeredAnimatedItem(index = 3) {
            state.lunarDate?.let { lunar ->
                DetailInfoCard(lunar = lunar)
            }
        }

        if (state.timeSlots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            StaggeredAnimatedItem(index = 4) {
                TimeSlotsCard(timeSlots = state.timeSlots)
            }
        }

        // ── 佛历 & 道历卡片 ──────────────────────────────────
        if (state.fotoData != null || state.taoData != null) {
            Spacer(modifier = Modifier.height(12.dp))
            StaggeredAnimatedItem(index = 5) {
                FotoTaoSection(
                    fotoData = state.fotoData,
                    taoData = state.taoData,
                    solarYear = state.currentYear
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.upcomingFestivalItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            StaggeredAnimatedItem(index = 6) {
                UpcomingFestivalSection(items = state.upcomingFestivalItems)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        StaggeredAnimatedItem(index = 7) {
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
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.share_current_tab),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp)) // 底部留出 FAB 空间
    }
}

@Composable
private fun TimeSlotsCard(timeSlots: List<LunarTimeSlot>) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.current_time_slot_label),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                timeSlots.forEach { slot ->
                    TimeSlotItem(
                        slot = slot,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSlotItem(
    slot: LunarTimeSlot,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.width(260.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = slot.ganZhi.ifBlank { "--" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = slot.timeRange,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (slot.tianShenType.isNotBlank()) {
                    Badge(text = slot.tianShenType)
                }
                if (slot.luck.isNotBlank()) {
                    Badge(text = slot.luck)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniInfoItem(label = stringResource(R.string.time_slot_deity), value = slot.tianShen.ifBlank { "-" })
                MiniInfoItem(label = stringResource(R.string.chong_label), value = slot.chong.ifBlank { "-" })
                MiniInfoItem(label = stringResource(R.string.sha_label), value = slot.sha.ifBlank { "-" })
            }

            Spacer(modifier = Modifier.weight(1f, fill = true))

            if (slot.yi.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${stringResource(R.string.yi)} ${slot.yi.joinToString("、")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (slot.ji.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.ji)} ${slot.ji.joinToString("、")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun Badge(text: String) {
    Box(
        modifier = Modifier
            .glassBackground(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

// ── 佛历 & 道历卡片 ──────────────────────────────────────────────
@Composable
private fun FotoTaoSection(
    fotoData: FotoData?,
    taoData: TaoData?,
    solarYear: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        fotoData?.let { foto ->
            FotoCard(foto = foto, solarYear = solarYear)
        }
        taoData?.let { tao ->
            TaoCard(tao = tao, solarYear = solarYear)
        }
    }
}

@Composable
private fun FotoCard(foto: FotoData, solarYear: Int) {
    val warmContainer = MaterialTheme.colorScheme.tertiaryContainer
    val onWarmContainer = MaterialTheme.colorScheme.onTertiaryContainer

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── 标题行：大号图标 + 名称 + 年份标签 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☸",
                    style = MaterialTheme.typography.titleLarge,
                    color = onWarmContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.buddhist_calendar),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .glassBackground(
                            shape = RoundedCornerShape(12.dp),
                            color = warmContainer
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.buddhist_year_value, foto.year),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = onWarmContainer
                    )
                }
            }

            // ── 日期 + 纪元 两列 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FotoTaoInfoBlock(
                    title = stringResource(R.string.buddhist_date_label),
                    value = "${foto.monthInChinese}月${foto.dayInChinese}",
                    accentColor = warmContainer,
                    onAccentColor = onWarmContainer,
                    modifier = Modifier.weight(1f)
                )
                FotoTaoInfoBlock(
                    title = stringResource(R.string.buddhist_era_label),
                    value = stringResource(R.string.gregorian_era_offset, solarYear, 544),
                    accentColor = warmContainer,
                    onAccentColor = onWarmContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── 星宿 + 因果犯忌 并排 ──
            val hasXiu = foto.xiu.isNotBlank()
            val hasFestivals = foto.festivals.isNotEmpty()
            if (hasXiu || hasFestivals) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (hasXiu) {
                        FotoTaoInfoBlock(
                            title = stringResource(R.string.buddhist_xiu_label),
                            value = "${foto.xiu}（${foto.xiuLuck}）",
                            accentColor = MaterialTheme.colorScheme.primaryContainer,
                            onAccentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (hasFestivals) {
                        val first = foto.festivals.first()
                        val value = buildString {
                            append(first.name)
                            if (first.result.isNotBlank()) append(" — ${first.result}")
                            if (foto.festivals.size > 1) append(stringResource(R.string.and_more_items, foto.festivals.size))
                        }
                        FotoTaoInfoBlock(
                            title = stringResource(R.string.buddhist_festivals_label),
                            value = value,
                            accentColor = MaterialTheme.colorScheme.errorContainer,
                            onAccentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── 因果犯忌详情（当条目 >1 或有备注时展开） ──
            if (hasFestivals && (foto.festivals.size > 1 || foto.festivals.any { it.remark.isNotBlank() })) {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    foto.festivals.forEach { f ->
                        FotoTaoDetailItem(
                            title = f.name,
                            subtitle = f.result,
                            remark = f.remark
                        )
                    }
                }
            }

            // ── 斋日标签 ──
            val zhaiTags = buildList {
                if (foto.isMonthZhai) add(stringResource(R.string.foto_month_zhai))
                if (foto.isDayZhaiShuoWang) add(stringResource(R.string.foto_zhai_shuo_wang))
                if (foto.isDayZhaiSix) add(stringResource(R.string.foto_zhai_six))
                if (foto.isDayZhaiTen) add(stringResource(R.string.foto_zhai_ten))
                if (foto.isDayZhaiGuanYin) add(stringResource(R.string.foto_zhai_guan_yin))
                if (foto.isDayYangGong) add(stringResource(R.string.foto_yang_gong))
            }
            if (zhaiTags.isNotEmpty()) {
                FotoTaoTagSection(
                    title = stringResource(R.string.buddhist_zhai_label),
                    tags = zhaiTags,
                    containerColor = warmContainer,
                    contentColor = onWarmContainer
                )
            }

            // ── 纪念日 ──
            if (foto.otherFestivals.isNotEmpty()) {
                FotoTaoTagSection(
                    title = stringResource(R.string.buddhist_memorial_label),
                    tags = foto.otherFestivals,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // ── 星宿歌诀 ──
            if (foto.xiuSong.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassBackground(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = foto.xiuSong,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaoCard(tao: TaoData, solarYear: Int) {
    val coolContainer = MaterialTheme.colorScheme.primaryContainer
    val onCoolContainer = MaterialTheme.colorScheme.onPrimaryContainer

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── 标题行 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☯",
                    style = MaterialTheme.typography.titleLarge,
                    color = onCoolContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.daoist_calendar),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .glassBackground(
                            shape = RoundedCornerShape(12.dp),
                            color = coolContainer
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.daoist_year_value, tao.year),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = onCoolContainer
                    )
                }
            }

            // ── 日期 + 纪元 两列 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FotoTaoInfoBlock(
                    title = stringResource(R.string.daoist_date_label),
                    value = "${tao.monthInChinese}月${tao.dayInChinese}",
                    accentColor = coolContainer,
                    onAccentColor = onCoolContainer,
                    modifier = Modifier.weight(1f)
                )
                FotoTaoInfoBlock(
                    title = stringResource(R.string.daoist_era_label),
                    value = stringResource(R.string.gregorian_era_offset, solarYear, 2697),
                    accentColor = coolContainer,
                    onAccentColor = onCoolContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── 特殊日标签 ──
            val specialTags = buildList {
                if (tao.isDaySanHui) add(stringResource(R.string.tao_san_hui))
                if (tao.isDaySanYuan) add(stringResource(R.string.tao_san_yuan))
                if (tao.isDayBaJie) add(stringResource(R.string.tao_ba_jie))
                if (tao.isDayWuLa) add(stringResource(R.string.tao_wu_la))
                if (tao.isDayBaHui) add(stringResource(R.string.tao_ba_hui))
                if (tao.isDayWu) add(stringResource(R.string.tao_wu_day))
                if (tao.isDayTianShe) add(stringResource(R.string.tao_tian_she))
            }
            if (specialTags.isNotEmpty()) {
                FotoTaoTagSection(
                    title = stringResource(R.string.daoist_special_label),
                    tags = specialTags,
                    containerColor = coolContainer,
                    contentColor = onCoolContainer
                )
            }

            // ── 道教节日 ──
            if (tao.festivals.isNotEmpty()) {
                Column {
                    Text(
                        text = stringResource(R.string.daoist_festivals_label),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    tao.festivals.forEach { f ->
                        FotoTaoDetailItem(
                            title = f.name,
                            subtitle = "",
                            remark = f.remark
                        )
                    }
                }
            }

            // ── 完整纪年 ──
            if (tao.fullString.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassBackground(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = tao.fullString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

/** 信息区块：标题 + 大号值 + 彩色竖线装饰 */
@Composable
private fun FotoTaoInfoBlock(
    title: String,
    value: String,
    accentColor: androidx.compose.ui.graphics.Color,
    onAccentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassBackground(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .glassBackground(
                        shape = RoundedCornerShape(1.5.dp),
                        color = accentColor
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = onAccentColor
                )
            }
        }
    }
}

/** 详情条目：标题 + 副标题 + 备注，卡片式展示 */
@Composable
private fun FotoTaoDetailItem(
    title: String,
    subtitle: String,
    remark: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .glassBackground(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (remark.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = remark,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/** 标题 + FlowRow 彩色标签 */
@Composable
private fun FotoTaoTagSection(
    title: String,
    tags: List<String>,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .glassBackground(
                            shape = RoundedCornerShape(10.dp),
                            color = containerColor
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor
                    )
                }
            }
        }
    }
}

// ── 详细信息卡片 ──────────────────────────────────────────────
@Composable
private fun DetailInfoCard(lunar: LunarDate) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.jishen_direction),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DirectionChip(
                    label = stringResource(R.string.xi_shen),
                    direction = lunar.xiShen,
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEmojiFace,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary
                )
                DirectionChip(
                    label = stringResource(R.string.cai_shen),
                    direction = lunar.caiShen,
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSunny,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary
                )
                DirectionChip(
                    label = stringResource(R.string.fu_shen),
                    direction = lunar.fuShen,
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Compass,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 第一行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val liFa = listOfNotNull(
                    if (lunar.jianChu.isNotBlank()) "${lunar.jianChu}日" else null,
                    if (lunar.star28.isNotBlank()) "${lunar.star28}宿" else null,
                    lunar.naYin.takeIf { it.isNotBlank() }
                )
                InfoColumnSection(stringResource(R.string.calendar_elements), liFa, modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight())

                val shenSha = listOfNotNull(
                    if (lunar.chong.isNotBlank()) "冲${lunar.chong}" else null,
                    if (lunar.sha.isNotBlank()) "煞${lunar.sha}" else null,
                    lunar.zhiShen.takeIf { it.isNotBlank() }
                )
                InfoColumnSection(
                    stringResource(R.string.chong_sha_zhishen),
                    shenSha,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            // 第二行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val pengZu = listOfNotNull(
                    lunar.pengZuGan.takeIf { it.isNotBlank() },
                    lunar.pengZuZhi.takeIf { it.isNotBlank() }
                )
                InfoColumnSection(
                    stringResource(R.string.pengzu_taboos),
                    pengZu,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                val taiShen = listOfNotNull(
                    lunar.taiShen.takeIf { it.isNotBlank() },
                    lunar.nineStar.takeIf { it.isNotBlank() }
                )
                InfoColumnSection(
                    stringResource(R.string.taishen_nine_star),
                    taiShen,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            // 第三行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val jiShen = lunar.dayJiShen.split(" ").filter { it.isNotBlank() }
                InfoColumnSection(
                    stringResource(R.string.jishen_yiqu),
                    jiShen,
                    isJi = true,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                val xiongSha = lunar.dayXiongSha.split(" ").filter { it.isNotBlank() }
                InfoColumnSection(
                    stringResource(R.string.xiongshen_yiji),
                    xiongSha,
                    isJi = false,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoColumnSection(
    title: String,
    items: List<String>,
    isJi: Boolean? = null,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Spacer(modifier = modifier)
        return
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .glassBackground(
                            color = when (isJi) {
                                true -> MaterialTheme.colorScheme.primaryContainer
                                false -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.labelMedium,
                        color = when (isJi) {
                            true -> MaterialTheme.colorScheme.onPrimaryContainer
                            false -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun DetailMiniInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .glassBackground(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PillTextContent(content: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .glassBackground(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = content.ifBlank { "-" },
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MonthNavigator(
    year: Int,
    month: Int,
    selectedDay: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (Int, Int, Int) -> Unit
) {
    var showYearMenu by remember { mutableStateOf(false) }
    var inputYear by remember { mutableStateOf(year.toString()) }
    val years = remember { (1900..2100).toList() }

    fun applyYear(newYear: Int) {
        if (newYear !in 1900..2100) return
        val maxDay = daysInMonth(newYear, month)
        onSelectDate(newYear, month, selectedDay.coerceAtMost(maxDay))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { applyYear(year - 1) }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFirstPage,
                    contentDescription = stringResource(R.string.previous_year),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronLeft,
                    contentDescription = stringResource(R.string.previous_month),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showYearMenu = true }
            ) {
                Text(
                    text = stringResource(R.string.year_month_format, year, month),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDropDown,
                    contentDescription = stringResource(R.string.switch_year),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = showYearMenu,
                onDismissRequest = { showYearMenu = false }
            ) {
                Column(modifier = Modifier.width(220.dp)) {
                    Column(
                        modifier = Modifier
                            .height(240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        years.forEach { y ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.year_value_format, y),
                                        color = if (y == year) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    applyYear(y)
                                    inputYear = y.toString()
                                    showYearMenu = false
                                }
                            )
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputYear,
                            onValueChange = { inputYear = it.filter(Char::isDigit).take(4) },
                            label = { Text(stringResource(R.string.year_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = AppTheme.colors.getOutlinedTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                inputYear.toIntOrNull()?.let { newYear ->
                                    applyYear(newYear)
                                    showYearMenu = false
                                }
                            }
                        ) {
                            Text(text = stringResource(R.string.confirm))
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                    contentDescription = stringResource(R.string.next_month),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = { applyYear(year + 1) }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLastPage,
                    contentDescription = stringResource(R.string.next_year),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun daysInMonth(year: Int, month: Int): Int {
    return GregorianCalendar(year, month - 1, 1)
        .getActualMaximum(Calendar.DAY_OF_MONTH)
}

@Composable
private fun InfoChip(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .glassBackground(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MiniInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DirectionChip(
    label: String,
    direction: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .glassBackground(
                    shape = RoundedCornerShape(12.dp),
                    color = containerColor
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            modifier = Modifier
                .glassBackground(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            text = direction,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── 星期标题行 ──────────────────────────────────────────────

@Composable
private fun WeekHeader() {
    ChineseWeekdayHeader(firstDayOfWeek = DayOfWeek.SUNDAY)
}

// ── 月历网格 ──────────────────────────────────────────────

@Composable
private fun CalendarGrid(
    days: List<CalendarDayInfo>,
    selectedDay: Int,
    currentMonth: Int,
    currentYear: Int,
    onDayClick: (CalendarDayInfo) -> Unit
) {
    val dayInfoByDate = remember(days) {
        days.associateBy { LocalDate.of(it.solarYear, it.solarMonth, it.solarDay) }
    }
    val selectedDate = remember(currentYear, currentMonth, selectedDay) {
        LocalDate.of(currentYear, currentMonth, selectedDay)
    }
    MonthCalendarGrid(
        yearMonth = YearMonth.of(currentYear, currentMonth),
        selectedDate = selectedDate,
        firstDayOfWeek = DayOfWeek.SUNDAY,
        onDateClick = { date -> dayInfoByDate[date]?.let(onDayClick) },
        isDateEnabled = { it in dayInfoByDate },
    ) { day ->
        dayInfoByDate[day.date]?.let { dayInfo ->
            CalendarDayCell(
                dayInfo = dayInfo,
                isSelected = day.isSelected,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

// ── 单个日期格子 ──────────────────────────────────────────

@Composable
private fun CalendarDayCell(
    dayInfo: CalendarDayInfo,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        dayInfo.isToday -> MaterialTheme.colorScheme.primary
        !dayInfo.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val subTextColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
        dayInfo.solarTerm != null -> MaterialTheme.colorScheme.primary
        dayInfo.lunarFestival != null -> MaterialTheme.colorScheme.primary
        dayInfo.solarFestival != null -> MaterialTheme.colorScheme.tertiary
        dayInfo.isToday -> MaterialTheme.colorScheme.primary
        !dayInfo.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (isSelected) Modifier.glassBackground(shape = CircleShape, color = MaterialTheme.colorScheme.primary)
                else if (dayInfo.isLegalHoliday) Modifier.glassBackground(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer.copy(
                        alpha = 0.4f
                    )
                )
                else if (dayInfo.isToday) Modifier.glassBackground(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.12f
                    )
                )
                else Modifier
            )
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${dayInfo.solarDay}",
                style = MaterialTheme.typography.titleSmall,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dayInfo.lunarDayName,
                style = MaterialTheme.typography.bodySmall,
                color = subTextColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (dayInfo.isLegalHoliday && dayInfo.holidayBadge != null) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    Box(
                        modifier = Modifier
                            .padding(end = 2.dp, top = 2.dp)
                            .glassBackground(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.error
                            )
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = dayInfo.holidayBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}

// ── 选中日详情大卡片 ──────────────────────────────────────

@Composable
private fun SelectedDayCard(
    solarYear: Int,
    solarMonth: Int,
    solarDay: Int,
    weekDay: String,
    lunarMonthName: String,
    lunarDayName: String,
    ganZhiMonth: String,
    ganZhiDay: String,
    zodiac: String,
    currentTimeSlot: String,
    lunar: LunarDate,
    onReset: () -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    val iconWatermarkAlpha = if (darkTheme) 0.08f else 0.04f
    val today = Calendar.getInstance()
    val isToday = solarYear == today.get(Calendar.YEAR) &&
            solarMonth == today.get(Calendar.MONTH) + 1 &&
            solarDay == today.get(Calendar.DAY_OF_MONTH)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // 公历 + 月日·星期
                Column {
                    Text(
                        text = stringResource(
                            R.string.full_solar_date_format,
                            solarYear, solarMonth, solarDay, weekDay
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = lunarMonthName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 大号农历日名
                Text(
                    text = lunarDayName,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 干支月·干支日
                    Text(
                        text = "${ganZhiMonth}月·${ganZhiDay}日·${currentTimeSlot}时",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isToday,
                            enter = fadeIn(animationSpec = tween(220)) + scaleIn(
                                animationSpec = tween(
                                    220
                                )
                            ),
                            exit = fadeOut(animationSpec = tween(180)) + scaleOut(
                                animationSpec = tween(
                                    180
                                )
                            )
                        ) {
                            FilledTonalIconButton(
                                onClick = onReset,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineToday,
                                    contentDescription = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.reset),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(12.dp))
            }

            // 叠加一层生肖图标水印
            Icon(
                imageVector = zodiacIconFor(zodiac),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = iconWatermarkAlpha),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(160.dp)
            )

            // 节日与生肖星座
            val festivalsList = mutableListOf<Pair<String, FestivalType>>()
            lunar.solarTerm?.takeIf { it.isNotBlank() }
                ?.let { festivalsList.add(it to FestivalType.SolarTerm) }
            lunar.lunarFestivals.filter { it.isNotBlank() }
                .forEach { festivalsList.add(it to FestivalType.LunarFestival) }
            lunar.solarFestivals.filter { it.isNotBlank() }
                .forEach { festivalsList.add(it to FestivalType.SolarFestival) }
            lunar.legalHolidayName?.takeIf { it.isNotBlank() }
                ?.let { festivalsList.add(it to FestivalType.LegalHoliday) }
            val festivals = festivalsList.distinctBy { it.first }

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.TopEnd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (festivals.isNotEmpty()) {
                    festivals.forEach { (fest, type) ->
                        Row(
                            modifier = Modifier
                                .glassBackground(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = festivalIconFor(fest, type),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = fest,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                InfoChip(icon = zodiacIconFor(zodiac), label = stringResource(R.string.zodiac_chip, zodiac))
                InfoChip(
                    icon = constellationIconFor(lunar.constellation),
                    label = stringResource(R.string.constellation_chip, lunar.constellation.removeSuffix("座"))
                )
            }
        }
    }
}

// ── 宜忌小卡片 ──────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun YiJiCard(
    title: String,
    items: List<String>,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    chipContainerColor: Color,
    chipContentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .glassBackground(
                                color = chipContainerColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.titleSmall,
                            color = chipContentColor
                        )
                    }
                }
            }
        }
    }
}

// ── 节气卡片 ──────────────────────────────────────────────

@Composable
private fun UpcomingFestivalSection(items: List<UpcomingFestivalItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.upcoming_festivals_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        items.forEach { item ->
            UpcomingFestivalItemCard(item = item)
        }
    }
}

@Composable
private fun UpcomingFestivalItemCard(item: UpcomingFestivalItem) {
    val icon = festivalIconFor(item)
    val typeLabel = when (item.type) {
        FestivalType.SolarTerm -> stringResource(R.string.solar_term_label)
        FestivalType.LunarFestival -> stringResource(R.string.lunar_festival_label)
        FestivalType.SolarFestival -> stringResource(R.string.intl_festival_label)
        FestivalType.LegalHoliday -> stringResource(R.string.legal_holiday_label)
    }
    val daysLabel = when {
        item.daysUntil <= 0 -> stringResource(R.string.today)
        else -> stringResource(R.string.days_until, item.daysUntil)
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .glassBackground(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.name}（${item.dateText}）",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = daysLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun getCurrentTimeSlot(timeSlots: List<LunarTimeSlot>): String {
    if (timeSlots.isEmpty()) return "--"
    val now = Calendar.getInstance()
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

    val current = timeSlots.firstOrNull { slot ->
        val range = slot.timeRange.split("-")
        if (range.size != 2) return@firstOrNull false
        val start = parseHmMinutes(range[0]) ?: return@firstOrNull false
        val end = parseHmMinutes(range[1]) ?: return@firstOrNull false
        nowMinutes in start..end
    }

    return current?.ganZhi?.takeIf { it.isNotBlank() } ?: "--"
}

private fun parseHmMinutes(hm: String): Int? {
    val parts = hm.trim().split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    return h * 60 + m
}


private fun festivalIconFor(item: UpcomingFestivalItem): ImageVector {
    return festivalIconFor(item.name, item.type)
}

private fun festivalIconFor(name: String, type: FestivalType): ImageVector {
    return when (type) {
        FestivalType.SolarTerm -> {
            when {
                name.contains("立春") || name.contains("惊蛰") || name.contains("春分") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSpring
                name.contains("雨水") || name.contains("谷雨") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRain
                name.contains("清明") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTombSweeping
                name.contains("立夏") || name.contains("小满") || name.contains("芒种") || name.contains(
                    "夏至"
                ) -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSummer

                name.contains("暑") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHeat
                name.contains("立秋") || name.contains("白露") || name.contains("秋分") || name.contains(
                    "寒露"
                ) -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutumn

                name.contains("霜降") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFrost
                name.contains("立冬") || name.contains("冬至") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWinter
                name.contains("雪") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSnow
                name.contains("寒") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCold
                else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSunny
            }
        }

        FestivalType.LunarFestival -> {
            when {
                name.contains("春") || name.contains("除夕") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNewYear
                name.contains("元宵") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLantern
                name.contains("端午") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDragonBoat
                name.contains("中秋") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMidAutumn
                name.contains("重阳") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutumn
                name.contains("腊八") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCold
                else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic
            }
        }

        FestivalType.SolarFestival -> {
            when {
                name.contains("元旦") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNewYear
                name.contains("妇女节") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSpring
                name.contains("植树节") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSpring
                name.contains("青年节") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSpring
                name.contains("儿童节") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSummer
                name.contains("建军节") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSummer
                name.contains("教师节") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutumn
                else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar
            }
        }

        FestivalType.LegalHoliday -> {
            when {
                name.contains("春节") || name.contains("元旦") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNewYear
                name.contains("清明") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTombSweeping
                name.contains("劳动") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLabor
                name.contains("端午") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDragonBoat
                name.contains("中秋") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMidAutumn
                name.contains("国庆") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNationalDay
                else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHoliday
            }
        }
    }
}

private fun zodiacIconFor(zodiac: String): ImageVector {
    return when (zodiac.trim()) {
        "鼠" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRat
        "牛" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineOx
        "虎" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTiger
        "兔" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRabbit
        "龙" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDragon
        "蛇" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSnake
        "马" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHorse
        "羊" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGoat
        "猴" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMonkey
        "鸡" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRooster
        "狗" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDog
        "猪" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePig
        else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePets
    }
}

private fun constellationIconFor(constellation: String): ImageVector {
    return when (constellation.trim().removeSuffix("座")) {
        "白羊" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAries
        "金牛" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTaurus
        "双子" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGemini
        "巨蟹" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCancer
        "狮子" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLeo
        "处女" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVirgo
        "天秤" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLibra
        "天蝎" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineScorpio
        "射手" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSagittarius
        "摩羯" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCapricorn
        "水瓶" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAquarius
        "双鱼" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePisces
        else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar
    }
}
