package com.wanbaohe.calendar.ui

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.base.ui.utils.Animation.StaggeredAnimatedItem
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.wanbaohe.calendar.R
import com.wanbaohe.calendar.data.CHINESE_HOUR_SLOTS
import com.wanbaohe.calendar.data.CalendarUiState
import com.wanbaohe.calendar.data.FotoData
import com.wanbaohe.calendar.data.LunarDate
import com.wanbaohe.calendar.data.TaoData
import com.wanbaohe.calendar.data.formatLunarMonthDay
import com.wanbaohe.calendar.data.getChineseHourSlot
import java.time.LocalDate
import java.util.GregorianCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwapVert

/**
 * 转换 Tab
 *
 * 公历↔农历日期互转
 */
@Composable
fun ConversionTab(
    state: CalendarUiState,
    onUpdateDate: (Int, Int, Int) -> Unit,
    onUpdateHour: (Int) -> Unit,
    onConvert: () -> Unit,
    onToggleMode: () -> Unit,
    onToggleLeapMonth: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        ChineseDatePickerDialog(
            initialDate = LocalDate.of(state.convertYear, state.convertMonth, state.convertDay),
            onDateSelected = { date ->
                onUpdateDate(date.year, date.monthValue, date.dayOfMonth)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            minYear = 1900,
            maxYear = 2100
        )
    }

    val colorScheme = MaterialTheme.colorScheme
    val pageBg = colorScheme.surface
    val cardBg = colorScheme.surfaceContainer
    val accentPrimary = colorScheme.primary
    val accentSecondary = colorScheme.tertiary

    val inputTitle = if (state.isConvertSolarToLunar) {
        stringResource(R.string.solar_date_input_title)
    } else {
        stringResource(R.string.lunar_date_input_title)
    }

    val yearValid = state.convertYear in 1900..2100
    val monthValid = state.convertMonth in 1..12
    val maxDay = maxSupportedDay(
        year = state.convertYear,
        month = state.convertMonth,
        isSolarInput = state.isConvertSolarToLunar
    )
    val dayValid = state.convertDay in 1..maxDay
    val canConvert = yearValid && monthValid && dayValid
    val selectedHourSlot = getChineseHourSlot(state.convertHour)

    val validationMessage = when {
        !yearValid -> stringResource(R.string.convert_year_range_error)
        !monthValid -> stringResource(R.string.convert_month_range_error)
        !dayValid -> stringResource(R.string.convert_day_range_error, maxDay)
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .glassBackground(color = pageBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        StaggeredAnimatedItem(index = 0) {
            Column {
                Text(
                    text = stringResource(R.string.convert_top_caption),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.convert_page_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.convert_page_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        StaggeredAnimatedItem(index = 1) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBg
                )
            ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(22.dp)
                            .glassBackground(
                                shape = RoundedCornerShape(1.5.dp),
                                color = accentSecondary
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.input_date_en),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = accentPrimary,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = inputTitle,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                                contentDescription = stringResource(R.string.select_convert_date),
                                tint = accentPrimary
                            )
                        }
                    }
                    TextButton(onClick = onToggleMode) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwapVert,
                            contentDescription = null,
                            tint = accentPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isConvertSolarToLunar) {
                                stringResource(R.string.switch_to_lunar)
                            } else {
                                stringResource(R.string.switch_to_solar)
                            },
                            color = accentPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                DateInputRow(
                    year = state.convertYear,
                    month = state.convertMonth,
                    day = state.convertDay,
                    onUpdateDate = onUpdateDate,
                    isYearError = !yearValid,
                    isMonthError = !monthValid,
                    isDayError = !dayValid
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.current_time_slot_label) + " ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedHourSlot.displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " ${selectedHourSlot.timeRange}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CHINESE_HOUR_SLOTS) { option ->
                        FilledTonalButton(
                            onClick = { onUpdateHour(option.hour) },
                            modifier = Modifier.height(52.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = if (state.convertHour == option.hour) {
                                AppTheme.colors.filledTonalButtonColors()
                            } else {
                                ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = option.timeRange,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                if (!state.isConvertSolarToLunar) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.leap_month_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurface
                        )
                        GlassSwitch(
                            checked = state.isConvertLunarLeapMonth,
                            onCheckedChange = { onToggleLeapMonth() }
                        )
                    }
                }

                validationMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                GlassButton(
                    onClick = onConvert,
                    enabled = canConvert,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentPrimary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.convert_calculate_action),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        }

        Spacer(modifier = Modifier.height(16.dp))

        state.convertResult?.let { result ->
            StaggeredAnimatedItem(index = 2) {
                Column {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardBg
                        )
                    ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.convert_result_en),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = accentSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildHeadline(state = state, result = result),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val festivalText = listOfNotNull(result.lunarFestival, result.solarFestival)
                        .distinct()
                        .joinToString("、")
                        .ifBlank { stringResource(R.string.no_festival_today) }

                    if (!state.isConvertSolarToLunar) {
                        val solarResult = state.convertSolarResult
                        ConvertResultRow(
                            label = stringResource(R.string.solar_date),
                            value = if (solarResult != null) {
                                stringResource(
                                    R.string.full_solar_date_format,
                                    solarResult.year, solarResult.month, solarResult.day,
                                    solarResult.weekDay
                                )
                            } else {
                                stringResource(R.string.convert_failed)
                            }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ResultGridItem(
                            label = stringResource(R.string.ganzhi_label_long),
                            value = "${result.ganZhiYear} ${result.ganZhiMonth} ${result.ganZhiDay}",
                            modifier = Modifier.weight(1f)
                        )
                        ResultGridItem(
                            label = stringResource(R.string.zodiac_label),
                            value = result.zodiac,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ResultGridItem(
                            label = stringResource(R.string.solar_term_today_label),
                            value = result.solarTerm ?: stringResource(R.string.none_label),
                            modifier = Modifier.weight(1f)
                        )
                        ResultGridItem(
                            label = stringResource(R.string.chong_sha_label),
                            value = listOf(result.chong, result.sha)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                                .ifBlank { stringResource(R.string.none_label) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ConvertResultRow(
                        label = stringResource(R.string.constellation_label),
                        value = result.constellation
                    )
                    state.convertTimeSlot?.let { slot ->
                        Spacer(modifier = Modifier.height(8.dp))
                        ConvertResultRow(
                            label = stringResource(R.string.current_time_slot_label),
                            value = "${slot.ganZhi} ${slot.timeRange}"
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ConvertResultRow(label = stringResource(R.string.festival_info_label), value = festivalText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 佛历 & 道历 卡片 ──
            val solarYear = if (state.isConvertSolarToLunar) {
                state.convertYear
            } else {
                state.convertSolarResult?.year ?: state.convertYear
            }

            BuddhistCalendarCard(
                solarYear = solarYear,
                fotoData = state.convertFotoData,
                cardBg = cardBg
            )
            Spacer(modifier = Modifier.height(12.dp))
            DaoistCalendarCard(
                solarYear = solarYear,
                taoData = state.convertTaoData,
                cardBg = cardBg
            )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        StaggeredAnimatedItem(index = 3) {
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

// ── 佛历卡片 ──────────────────────────────────────────────
@Composable
private fun BuddhistCalendarCard(
    solarYear: Int,
    fotoData: FotoData?,
    cardBg: androidx.compose.ui.graphics.Color
) {
    val buddhistYear = fotoData?.year ?: (solarYear + 544)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(22.dp)
                        .glassBackground(
                            shape = RoundedCornerShape(1.5.dp),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.buddhist_calendar),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.tertiary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (fotoData != null) {
                    "${stringResource(R.string.buddhist_calendar)} ${fotoData.yearInChinese}年${fotoData.monthInChinese}月${fotoData.dayInChinese}"
                } else {
                    stringResource(R.string.buddhist_year_value, buddhistYear)
                },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResultGridItem(
                    label = stringResource(R.string.buddhist_year_label),
                    value = stringResource(R.string.year_value_format, buddhistYear),
                    modifier = Modifier.weight(1f)
                )
                ResultGridItem(
                    label = stringResource(R.string.buddhist_era_label),
                    value = stringResource(R.string.gregorian_era_offset, solarYear, 544),
                    modifier = Modifier.weight(1f)
                )
            }

            // ── lunar-java 丰富数据区 ──
            if (fotoData != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultGridItem(
                        label = stringResource(R.string.buddhist_date_label),
                        value = "${fotoData.monthInChinese}月${fotoData.dayInChinese}",
                        modifier = Modifier.weight(1f)
                    )
                    ResultGridItem(
                        label = stringResource(R.string.buddhist_xiu_label),
                        value = if (fotoData.xiu.isNotBlank()) {
                            "${fotoData.xiu}（${fotoData.xiuLuck}）"
                        } else {
                            stringResource(R.string.none_data)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 斋日标签
                val zhaiTags = buildList {
                    if (fotoData.isMonthZhai) add(stringResource(R.string.foto_month_zhai))
                    if (fotoData.isDayZhaiShuoWang) add(stringResource(R.string.foto_zhai_shuo_wang))
                    if (fotoData.isDayZhaiSix) add(stringResource(R.string.foto_zhai_six))
                    if (fotoData.isDayZhaiTen) add(stringResource(R.string.foto_zhai_ten))
                    if (fotoData.isDayZhaiGuanYin) add(stringResource(R.string.foto_zhai_guan_yin))
                    if (fotoData.isDayYangGong) add(stringResource(R.string.foto_yang_gong))
                }
                if (zhaiTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultGridItem(
                        label = stringResource(R.string.buddhist_zhai_label),
                        value = zhaiTags.joinToString("、")
                    )
                }

                // 因果犯忌
                if (fotoData.festivals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultGridItem(
                        label = stringResource(R.string.buddhist_festivals_label),
                        value = fotoData.festivals.joinToString("；") { f ->
                            buildString {
                                append(f.name)
                                if (f.result.isNotBlank()) append("（${f.result}）")
                                if (f.remark.isNotBlank()) append(" ${f.remark}")
                            }
                        }
                    )
                }

                // 纪念日
                if (fotoData.otherFestivals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultGridItem(
                        label = stringResource(R.string.buddhist_memorial_label),
                        value = fotoData.otherFestivals.joinToString("、")
                    )
                }

                // 星宿歌诀
                if (fotoData.xiuSong.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "☸ ${fotoData.xiuSong}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.buddhist_calendar_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── 道历卡片 ──────────────────────────────────────────────
@Composable
private fun DaoistCalendarCard(
    solarYear: Int,
    taoData: TaoData?,
    cardBg: androidx.compose.ui.graphics.Color
) {
    val daoistYear = taoData?.year ?: (solarYear + 2697)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(22.dp)
                        .glassBackground(
                            shape = RoundedCornerShape(1.5.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.daoist_calendar),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (taoData != null) {
                    "${stringResource(R.string.daoist_calendar)} ${taoData.yearInChinese}年${taoData.monthInChinese}月${taoData.dayInChinese}"
                } else {
                    stringResource(R.string.daoist_year_value, daoistYear)
                },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResultGridItem(
                    label = stringResource(R.string.daoist_year_label),
                    value = stringResource(R.string.year_value_format, daoistYear),
                    modifier = Modifier.weight(1f)
                )
                ResultGridItem(
                    label = stringResource(R.string.daoist_era_label),
                    value = stringResource(R.string.gregorian_era_offset, solarYear, 2697),
                    modifier = Modifier.weight(1f)
                )
            }

            // ── lunar-java 丰富数据区 ──
            if (taoData != null) {
                Spacer(modifier = Modifier.height(12.dp))

                ResultGridItem(
                    label = stringResource(R.string.daoist_date_label),
                    value = "${taoData.monthInChinese}月${taoData.dayInChinese}"
                )

                // 特殊日标签
                val specialTags = buildList {
                    if (taoData.isDaySanHui) add(stringResource(R.string.tao_san_hui))
                    if (taoData.isDaySanYuan) add(stringResource(R.string.tao_san_yuan))
                    if (taoData.isDayBaJie) add(stringResource(R.string.tao_ba_jie))
                    if (taoData.isDayWuLa) add(stringResource(R.string.tao_wu_la))
                    if (taoData.isDayBaHui) add(stringResource(R.string.tao_ba_hui))
                    if (taoData.isDayWu) add(stringResource(R.string.tao_wu_day))
                    if (taoData.isDayTianShe) add(stringResource(R.string.tao_tian_she))
                }
                if (specialTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultGridItem(
                        label = stringResource(R.string.daoist_special_label),
                        value = specialTags.joinToString("、")
                    )
                }

                // 道教节日
                if (taoData.festivals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultGridItem(
                        label = stringResource(R.string.daoist_festivals_label),
                        value = taoData.festivals.joinToString("；") { f ->
                            if (f.remark.isNotBlank()) "${f.name}[${f.remark}]" else f.name
                        }
                    )
                }

                // 完整纪年字符串
                if (taoData.fullString.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "☯ ${taoData.fullString}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.daoist_calendar_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DateInputRow(
    year: Int,
    month: Int,
    day: Int,
    onUpdateDate: (Int, Int, Int) -> Unit,
    isYearError: Boolean,
    isMonthError: Boolean,
    isDayError: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassOutlinedTextField(
            value = "$year",
            onValueChange = { newVal ->
                newVal.toIntOrNull()?.let {
                    onUpdateDate(it, month, day)
                }
            },
            label = { Text(stringResource(R.string.year_label_en_zh)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = isYearError,
            colors = AppTheme.colors.getOutlinedTextFieldColors().copy(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                focusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
            ),
            modifier = Modifier.weight(1f)
        )
        GlassOutlinedTextField(
            value = "$month",
            onValueChange = { newVal ->
                newVal.toIntOrNull()?.let {
                    onUpdateDate(year, it, day)
                }
            },
            label = { Text(stringResource(R.string.month_label_en_zh)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = isMonthError,
            colors = AppTheme.colors.getOutlinedTextFieldColors().copy(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                focusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
            ),
            modifier = Modifier.weight(1f)
        )
        GlassOutlinedTextField(
            value = "$day",
            onValueChange = { newVal ->
                newVal.toIntOrNull()?.let {
                    onUpdateDate(year, month, it)
                }
            },
            label = { Text(stringResource(R.string.day_label_en_zh)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = isDayError,
            colors = AppTheme.colors.getOutlinedTextFieldColors().copy(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                focusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

private fun maxSupportedDay(year: Int, month: Int, isSolarInput: Boolean): Int {
    if (month !in 1..12) return if (isSolarInput) 31 else 30
    if (!isSolarInput) return 30
    if (year !in 1..9999) return 31
    return GregorianCalendar(year, month - 1, 1)
        .getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
}

@Composable
private fun ResultGridItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun buildHeadline(state: CalendarUiState, result: LunarDate): String {
    return if (state.isConvertSolarToLunar) {
        "${stringResource(R.string.lunar_label)} ${result.ganZhiYear}年 ${formatLunarMonthDay(result.monthName, result.dayName, result.isLeapMonth)}"
    } else {
        state.convertSolarResult?.let {
            val leapPrefix = if (state.isConvertLunarLeapMonth) {
                "${stringResource(R.string.leap_prefix)}"
            } else {
                ""
            }
            "${stringResource(R.string.solar_date)} ${stringResource(R.string.date_ymd_format, it.year, it.month, it.day)} ($leapPrefix${state.convertMonth}月${state.convertDay}日)"
        } ?: stringResource(R.string.convert_failed)
    }
}

@Composable
private fun ConvertResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
