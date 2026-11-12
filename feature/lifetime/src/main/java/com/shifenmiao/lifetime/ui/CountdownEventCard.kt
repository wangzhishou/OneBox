/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.lifetime.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.sectionGradient
import com.shifenmiao.common.components.sectionIconColor
import com.shifenmiao.common.components.sectionIconContainerColor
import com.shifenmiao.common.components.sectionThemeForIndex
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.domain.model.CountdownStatus
import com.shifenmiao.lifetime.util.localizedPresetEventName
import com.wanbaohe.core.ui.widget.glass.tintedGlassContentColor

/**
 * 倒数日卡片。
 *
 * 主题色：今天 → SECONDARY，已过期 → SURFACE，其余轮转。
 * 版式与纪念日卡共用 [LifeEntryCard]：大数字只放天数，方向由单位（天后 / 天前）表达，
 * 今天 / 明天 / 后天直接用词，不再出现"16 Days + 16 days to go"这种重复。
 */
@Composable
fun CountdownEventCard(
    status: CountdownStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    themeIndex: Int = 0,
) {
    val event = status.event
    val theme = when {
        status.isToday -> sectionThemeForIndex(1) // SECONDARY
        status.isPast -> sectionThemeForIndex(3)  // SURFACE
        else -> sectionThemeForIndex(themeIndex)
    }
    val containerColor = sectionGradient(theme)
    val accent = sectionIconColor(theme)
    val iconBg = sectionIconContainerColor(theme)
    val contentColor = tintedGlassContentColor(containerColor)

    val (value, unit) = countdownRelativeValue(status)

    LifeEntryCard(
        iconKey = event.iconKey,
        fallbackName = event.name,
        title = localizedPresetEventName(event.name, event.isPreset),
        value = value,
        unit = unit,
        dateLabel = formatDateWithWeekday(status.nextOccurrence),
        containerColor = containerColor,
        contentColor = contentColor,
        accent = accent,
        iconContainer = iconBg,
        onClick = onClick,
        modifier = modifier,
        badge = if (event.isFromHoliday) {
            { HolidayBadge() }
        } else {
            null
        },
    )
}

@Composable
private fun countdownRelativeValue(status: CountdownStatus): Pair<String, String?> {
    val days = status.daysUntil
    return when {
        status.isToday -> stringResource(R.string.lifetime_status_today) to null
        status.isPast -> days.toString() to stringResource(R.string.lifetime_unit_days_past)
        days == 1L -> stringResource(R.string.lifetime_status_tomorrow) to null
        days == 2L -> stringResource(R.string.lifetime_status_day_after_tomorrow) to null
        else -> days.toString() to stringResource(R.string.lifetime_unit_days_until)
    }
}

@Composable
private fun HolidayBadge() {
    val container = MaterialTheme.colorScheme.tertiaryContainer
    val onContainer = MaterialTheme.colorScheme.onTertiaryContainer
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = container,
    ) {
        Text(
            text = stringResource(R.string.lifetime_countdown_holiday_badge),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = onContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
