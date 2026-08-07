package com.shifenmiao.lifetime.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconAvatar
import com.shifenmiao.common.components.sectionGradient
import com.shifenmiao.common.components.sectionIconColor
import com.shifenmiao.common.components.sectionIconContainerColor
import com.shifenmiao.common.components.sectionOnColor
import com.shifenmiao.common.components.sectionThemeForIndex
import com.shifenmiao.lifetime.domain.model.MilestoneStatus
import com.shifenmiao.lifetime.util.milestoneStatusLabel
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import java.time.LocalDate

/**
 * 纪念日卡片 —— 竖屏比例 (3:4 高宽比)。
 *
 * 主题色：按 [themeIndex] 在 PRIMARY / SECONDARY / TERTIARY / SURFACE 中轮转。
 *  - 已达成（[MilestoneStatus.isReached]）→ 强制使用 SECONDARY 主题以突出
 *  - 其余 → 轮转
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
    val contentColor = sectionOnColor(theme)

    GlassCard(
        modifier = modifier
            .aspectRatio(CARD_ASPECT_RATIO),
        shape = OneBoxDesignSystem.sectionCardShape,
        containerAlpha = 0.22f,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = containerColor
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.cardPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top: icon + name + status label (centered)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
            ) {
                IconAvatar(
                    iconName = milestone.iconKey,
                    fallbackName = milestone.name,
                    size = 48.dp,
                    containerColor = iconBg,
                    tint = iconTint,
                    shape = RoundedCornerShape(OneBoxDesignSystem.smallRadius),
                    iconSizeRatio = 0.6f,
                )

                Text(
                    text = milestone.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = milestoneStatusLabel(status),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = contentColor.copy(alpha = 0.72f),
                )
            }

            // Bottom: target date tag (left-aligned)
            DateTag(
                date = milestone.targetDate?.let { formatShortDate(it) } ?: "—",
                accent = iconTint,
            )
        }
    }
}

private const val CARD_ASPECT_RATIO: Float = 3f / 4f  // 宽度:高度 = 3:4

internal fun formatShortDate(date: LocalDate): String =
    "${date.year}/${date.monthValue.toString().padStart(2, '0')}/${date.dayOfMonth.toString().padStart(2, '0')}"

/**
 * 左侧带颜色 tag 包裹的时间标签 —— 替代横线分隔。
 *  - 圆角小药丸
 *  - 背景：accent 颜色 18% 透明
 *  - 文字：accent 颜色，semibold
 */
@Composable
internal fun DateTag(date: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = accent.copy(alpha = 0.18f),
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = accent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}
