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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppFontMode
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.base.ui.icon.IconAvatar
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import java.time.LocalDate
import java.util.Locale

/** 纪念日卡与倒数日卡常规字号共用比例，大字体下允许按内容增高。 */
internal val LifeCardAspectRatio: Float = 3f / 4f

internal const val LifeCardIconSize = 36

/**
 * 纪念日 / 倒数日通用卡片。
 *
 * 版式（自上而下）：图标 + 角标 → 标题 → 大数字 + 方向单位 → 日期胶囊。
 * 大数字只放"距离/已过"的数值，方向交给单位文案（天前 / 天后 / days ago …），
 * 不再像旧版那样大数字和下面一行"还有 16 天"重复表达同一件事。
 */
@Composable
internal fun LifeEntryCard(
    iconKey: String,
    fallbackName: String,
    title: String,
    value: String,
    unit: String?,
    dateLabel: String,
    containerColor: Color,
    contentColor: Color,
    accent: Color,
    iconContainer: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null,
) {
    val isLargeFont = AppTheme.uiProfile.fontMode != AppFontMode.Regular
    GlassCard(
        modifier = modifier.then(
            if (isLargeFont) Modifier.heightIn(min = 240.dp)
            else Modifier.aspectRatio(LifeCardAspectRatio)
        ),
        shape = OneBoxDesignSystem.sectionCardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                IconAvatar(
                    iconName = iconKey,
                    fallbackName = fallbackName,
                    size = LifeCardIconSize.dp,
                    containerColor = iconContainer,
                    tint = accent,
                    shape = RoundedCornerShape(10.dp),
                    iconSizeRatio = 0.6f,
                )
                badge?.invoke()
            }

            // 文字贴着图标往下排，不要用 SpaceBetween 把标题推到卡片中间
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                ),
                // 常规字号保留有限档 autoSize，大字体改为换行并允许卡片增高
                autoSize = if (isLargeFont) null else TextAutoSize.StepBased(
                    minFontSize = 16.sp,
                    maxFontSize = 30.sp,
                    stepSize = 1.sp,
                ),
                color = accent,
                maxLines = if (isLargeFont) Int.MAX_VALUE else 1,
                overflow = if (isLargeFont) TextOverflow.Clip else TextOverflow.Ellipsis,
            )
            if (!unit.isNullOrBlank()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = if (isLargeFont) Int.MAX_VALUE else 1,
                    overflow = if (isLargeFont) TextOverflow.Clip else TextOverflow.Ellipsis,
                )
            }

            if (isLargeFont) {
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            // 常规字号保留有限档 autoSize，大字体允许日期换行
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor.copy(alpha = 0.62f),
                maxLines = if (isLargeFont) Int.MAX_VALUE else 1,
                autoSize = if (isLargeFont) null else TextAutoSize.StepBased(
                    minFontSize = 8.sp,
                    maxFontSize = 11.sp,
                    stepSize = 0.5.sp,
                ),
                overflow = if (isLargeFont) TextOverflow.Clip else TextOverflow.Ellipsis,
            )
        }
    }
}

internal fun formatShortDate(date: LocalDate): String =
    "${date.year}/${date.monthValue.toString().padStart(2, '0')}/${date.dayOfMonth.toString().padStart(2, '0')}"

/**
 * 日期 + 星期：`2027/03/18 · 周四`。
 * 星期用系统/应用当前 Locale，不额外进资源。
 */
@Composable
internal fun formatDateWithWeekday(date: LocalDate?): String {
    if (date == null) return "—"
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val weekday = date.dayOfWeek.getDisplayName(
        java.time.format.TextStyle.SHORT,
        locale
    )
    return "${formatShortDate(date)} · $weekday"
}
