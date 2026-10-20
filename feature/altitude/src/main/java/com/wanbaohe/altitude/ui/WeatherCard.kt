package com.wanbaohe.altitude.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSunny
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.core.weather.domain.model.HourlyForecast
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import com.weather.icons.QWeatherIcons
import com.weather.icons.getIconByCode

/**
 * 天气主卡。hourly 为空时不画逐时条，历史快照走这条路径。
 */
@Composable
internal fun WeatherCard(
    weather: WeatherInfo,
    hourly: List<HourlyForecast> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = tintedGlassCardColors(weatherTint(weather.icon)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "${weather.temp.ifEmpty { "--" }}°C",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = weather.text.ifEmpty { "--" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(CoreR.string.altitude_feels_like, weather.feelsLike.ifEmpty { "--" }),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Icon(
                    imageVector = weatherIcon(weather.icon),
                    contentDescription = weather.text,
                    tint = LocalContentColor.current,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        if (hourly.isNotEmpty()) {
            HourlyForecastRow(hourly)
        }
    }
}

@Composable
private fun HourlyForecastRow(hourly: List<HourlyForecast>) {
    val primary = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(hourly) { index, slot ->
            val current = index == 0
            val textColor = if (current) primary else onSurface
            GlassSurface(
                shape = RoundedCornerShape(16.dp),
                color = if (current) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
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
                        fontWeight = if (current) FontWeight.Bold else FontWeight.Normal
                    )
                    Icon(
                        imageVector = weatherIcon(slot.icon),
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
                    if (slot.precipProbability.isNotBlank() && slot.precipProbability != "0") {
                        Text(
                            text = stringResource(
                                CoreR.string.altitude_precip_probability,
                                slot.precipProbability
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun weatherTint(iconCode: String): Color {
    val scheme = MaterialTheme.colorScheme
    val code = iconCode.toIntOrNull() ?: return scheme.primaryContainer
    return when {
        code == 302 || code == 303 || code == 304 -> scheme.tertiaryContainer
        code in 300..399 -> scheme.secondaryContainer
        code in 400..499 -> scheme.tertiaryContainer
        code == 104 || code in 500..599 -> scheme.surfaceContainer
        else -> scheme.primaryContainer
    }
}

internal fun weatherIcon(code: String): ImageVector =
    QWeatherIcons.getIconByCode(code)
        ?: com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSunny
