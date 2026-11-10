package com.shifenmiao.lifetime.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.components.sectionGradient
import com.shifenmiao.common.components.sectionIconColor
import com.shifenmiao.common.components.sectionIconContainerColor
import com.shifenmiao.common.components.sectionThemeForIndex
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.domain.model.MilestoneStatus
import com.wanbaohe.core.ui.widget.glass.tintedGlassContentColor

/**
 * 纪念日卡片。
 *
 * 主题色：按 [themeIndex] 在 PRIMARY / SECONDARY / TERTIARY / SURFACE 中轮转；
 * 已达成（[MilestoneStatus.isReached]）强制 SECONDARY 突出。
 * 版式与倒数日卡共用 [LifeEntryCard]，两张卡在列表里尺寸一致。
 */
@Composable
fun PersonalMilestoneCard(
    status: MilestoneStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    themeIndex: Int = 0,
) {
    val milestone = status.milestone
    val theme = if (status.isReached) {
        sectionThemeForIndex(1) // SECONDARY
    } else {
        sectionThemeForIndex(themeIndex)
    }
    val containerColor = sectionGradient(theme)
    val iconTint = sectionIconColor(theme)
    val iconBg = sectionIconContainerColor(theme)
    val contentColor = tintedGlassContentColor(containerColor)

    // 距离 / 已过的相对时间；方向交给单位文案，避免"7 天"读不出前后
    val (value, unit) = milestoneRelativeValue(status)

    LifeEntryCard(
        iconKey = milestone.iconKey,
        fallbackName = milestone.name,
        title = milestone.name,
        value = value,
        unit = unit,
        dateLabel = formatDateWithWeekday(milestone.targetDate),
        containerColor = containerColor,
        contentColor = contentColor,
        accent = iconTint,
        iconContainer = iconBg,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun milestoneRelativeValue(status: MilestoneStatus): Pair<String, String?> {
    val daysUntil = status.daysUntil
    val daysSince = status.daysSince
    return when {
        status.isReached && daysSince == 0L ->
            stringResource(R.string.lifetime_status_today) to null

        status.isReached && daysSince >= 365 ->
            (daysSince / 365).toString() to stringResource(R.string.lifetime_unit_years_past)

        status.isReached && daysSince >= 30 ->
            (daysSince / 30).toString() to stringResource(R.string.lifetime_unit_months_past)

        status.isReached ->
            daysSince.toString() to stringResource(R.string.lifetime_unit_days_past)

        daysUntil == 1L ->
            stringResource(R.string.lifetime_status_tomorrow) to null

        daysUntil == 2L ->
            stringResource(R.string.lifetime_status_day_after_tomorrow) to null

        daysUntil > 365 ->
            (daysUntil / 365).toString() to stringResource(R.string.lifetime_unit_years_until)

        daysUntil > 30 ->
            (daysUntil / 30).toString() to stringResource(R.string.lifetime_unit_months_until)

        else ->
            daysUntil.toString() to stringResource(R.string.lifetime_unit_days_until)
    }
}
