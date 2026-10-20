package com.wanbaohe.altitude.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWaterDrop
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.core.weather.domain.model.AirQuality
import com.wanbaohe.core.weather.domain.model.DailyOutlook
import com.wanbaohe.core.weather.domain.model.LifeIndex
import com.wanbaohe.core.weather.domain.model.PlaceWeather
import com.wanbaohe.core.weather.domain.model.WeatherAlertItem
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 仪表盘里的天气块：实况、小时、逐日、空气、预警、生活指数。
 */
@Composable
internal fun WeatherBoard(
    place: PlaceWeather,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        place.alerts.forEach { alert ->
            AlertBanner(alert)
        }
        if (place.minutelySummary.isNotBlank()) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = tintedGlassCardColors(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Text(
                    text = place.minutelySummary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        WeatherCard(weather = place.current, hourly = place.hourly)
        WeatherDetailsCard(weather = place.current)
        SmallDetailCards(weather = place.current)
        if (place.daily.isNotEmpty()) {
            DailyForecastCard(place.daily)
        }
        place.air?.let { air ->
            if (air.aqi.isNotBlank() || air.category.isNotBlank()) {
                AirQualityCard(air)
            }
        }
        if (place.indices.isNotEmpty()) {
            LifeIndexRow(place.indices)
        }
        Text(
            text = stringResource(CoreR.string.altitude_weather_source),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AlertBanner(alert: WeatherAlertItem) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = tintedGlassCardColors(MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = alert.headline,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (alert.instruction.isNotBlank()) {
                Text(
                    text = alert.instruction,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun DailyForecastCard(days: List<DailyOutlook>) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(CoreR.string.altitude_daily),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            days.forEach { day ->
                DailyRow(day)
            }
        }
    }
}

@Composable
private fun DailyRow(day: DailyOutlook) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val numberStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum",
    )
    val precipRaw = day.precipProbability.takeIf { it.isNotBlank() && it != "0" }
    val precip = precipRaw
        ?.let { stringResource(CoreR.string.altitude_precip_probability, it) }
        .orEmpty()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = weekdayLabel(day.date),
            modifier = Modifier.width(44.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = weatherIcon(day.iconDay),
            contentDescription = day.textDay,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = day.textDay,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        // 降水概率带个水滴图标：只写 "98%" 夹在天气图标和温度中间看不出是什么
        Row(
            modifier = Modifier.width(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            if (precipRaw != null) {
                Icon(
                    imageVector = Icons.Outlined.LineWaterDrop,
                    contentDescription = stringResource(CoreR.string.altitude_precipitation),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = precip,
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
            }
        }
        Text(
            text = stringResource(CoreR.string.altitude_temp_value, day.tempMin),
            modifier = Modifier.width(52.dp),
            style = numberStyle,
            color = onSurface,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
        Text(
            text = "/",
            modifier = Modifier.width(16.dp),
            style = numberStyle,
            color = onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = stringResource(CoreR.string.altitude_temp_value, day.tempMax),
            modifier = Modifier.width(52.dp),
            style = numberStyle,
            color = onSurface,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
    }
}

@Composable
private fun AirQualityCard(air: AirQuality) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(CoreR.string.altitude_air_quality),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = listOf(air.aqi, air.category).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (air.primaryPollutant.isNotBlank()) {
                Text(
                    text = stringResource(CoreR.string.altitude_primary_pollutant, air.primaryPollutant),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (air.advice.isNotBlank()) {
                Text(
                    text = air.advice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun LifeIndexRow(indices: List<LifeIndex>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(CoreR.string.altitude_life_indices),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 4.dp),
        ) {
            items(indices) { index ->
                GlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = index.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                        )
                        Text(
                            text = index.category,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun weekdayLabel(date: String): String {
    if (date.length < 10) return date
    val locale = LocalConfiguration.current.locales[0]
    return runCatching {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date.take(10)) ?: return date
        SimpleDateFormat("EEE", locale).format(parsed)
    }.getOrDefault(date)
}
