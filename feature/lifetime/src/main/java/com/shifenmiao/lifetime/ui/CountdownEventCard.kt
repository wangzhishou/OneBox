package com.shifenmiao.lifetime.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.icon.IconAvatar
import com.shifenmiao.common.components.sectionGradient
import com.shifenmiao.common.components.sectionIconColor
import com.shifenmiao.common.components.sectionIconContainerColor
import com.shifenmiao.common.components.sectionOnColor
import com.shifenmiao.common.components.sectionThemeForIndex
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.domain.model.CountdownStatus
import com.shifenmiao.lifetime.util.countdownStatusLabel
import com.shifenmiao.lifetime.util.localizedPresetEventName
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem

/**
 * 倒数日卡片 —— 竖屏比例 (3:4 高宽比)。
 *
 * 主题色：按 [themeIndex] 在 PRIMARY / SECONDARY / TERTIARY / SURFACE 中轮转。
 *  - [CountdownStatus.isToday] → 强制 SECONDARY 主题（强调"就是今天"）
 *  - [CountdownStatus.isPast] → SURFACE 主题（弱化显示）
 *  - 其余 → 轮转
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
    val contentColor = sectionOnColor(theme)

    GlassCard(
        modifier = modifier
            .aspectRatio(CARD_ASPECT_RATIO),
        shape = OneBoxDesignSystem.sectionCardShape,
        containerAlpha = 0.22f,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top: icon + holiday badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                IconAvatar(
                    iconName = event.iconKey,
                    fallbackName = event.name,
                    size = 36.dp,
                    containerColor = iconBg,
                    tint = accent,
                    shape = RoundedCornerShape(10.dp),
                    iconSizeRatio = 0.6f,
                )
                if (event.isFromHoliday) {
                    HolidayBadge()
                }
            }

            // Middle: name + days + status label (centered)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = localizedPresetEventName(event.name, event.isPreset),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = if (status.isToday) "0" else status.daysUntil.toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                        ),
                        color = accent,
                    )
                    Text(
                        text = stringResource(R.string.lifetime_unit_days),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = accent.copy(alpha = 0.75f),
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }
                Text(
                    text = countdownStatusLabel(status),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.65f),
                    maxLines = 1,
                )
            }

            // Bottom: next-occurrence tag (left-aligned)
            DateTag(
                date = status.nextOccurrence?.let { formatShortDate(it) } ?: "—",
                accent = accent,
            )
        }
    }
}

private const val CARD_ASPECT_RATIO: Float = 3f / 4f

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
