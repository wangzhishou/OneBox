package com.wanbaohe.altitude.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSunny

/**
 * 天气卡片 — 温度 / 天气描述 / 体感温度 / 逐时预报占位
 */
@Composable
internal fun WeatherCard(
    weather: WeatherInfo,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    // 温度
                    Text(
                        text = "${weather.temp}°C",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                    // 天气描述
                    Text(
                        text = weather.text.ifEmpty { "--" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = primary
                    )
                    Spacer(Modifier.height(4.dp))
                    // 体感温度
                    Text(
                        text = stringResource(CoreR.string.altitude_feels_like, weather.feelsLike),
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurface.copy(alpha = 0.5f)
                    )
                }
                // 天气图标占位
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSunny,
                        contentDescription = null,
                        tint = primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // 逐时预报占位行
            HourlyForecastPlaceholder(currentTemp = weather.temp)
        }
    }
}

/**
 * 逐时预报占位行（当前仅展示模拟数据样式）
 */
@Composable
private fun HourlyForecastPlaceholder(currentTemp: String) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val tempVal = currentTemp.toIntOrNull() ?: 18

    // 模拟 5 个时间点
    data class HourSlot(val time: String, val temp: Int, val isCurrent: Boolean)
    val slots = listOf(
        HourSlot("15:00", tempVal, false),
        HourSlot("16:00", tempVal + 1, true),
        HourSlot("17:00", tempVal - 2, false),
        HourSlot("18:00", tempVal - 1, false),
        HourSlot("19:00", tempVal - 3, false)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(slots) { _, slot ->
            val bgColor = if (slot.isCurrent) primary.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
            val textColor = if (slot.isCurrent) primary else onSurface.copy(alpha = 0.6f)

            GlassSurface(
                shape = RoundedCornerShape(16.dp),
                color = bgColor,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = slot.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        fontWeight = if (slot.isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                    Icon(
                        imageVector = if (slot.isCurrent) Icons.Default.WbSunny else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudStorage,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${slot.temp}°",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }
        }
    }
}

