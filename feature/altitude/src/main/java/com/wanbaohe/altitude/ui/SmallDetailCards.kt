package com.wanbaohe.altitude.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import com.shifenmiao.core.R as CoreR

/**
 * 2×2 小型数据卡片网格 — Precipitation / Pressure / Dew Point / Cloud Cover
 */
@Composable
internal fun SmallDetailCards(
    weather: WeatherInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallCard(
                label = stringResource(CoreR.string.altitude_precipitation),
                value = weather.precip.ifEmpty { "0.0" },
                unit = "MM",
                modifier = Modifier.weight(1f)
            )
            SmallCard(
                label = stringResource(CoreR.string.altitude_pressure),
                value = weather.pressure.ifEmpty { "--" },
                unit = "HPA",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallCard(
                label = stringResource(CoreR.string.altitude_dew_point),
                value = weather.dew.ifEmpty { "--" },
                unit = "°C",
                modifier = Modifier.weight(1f)
            )
            SmallCard(
                label = stringResource(CoreR.string.altitude_cloud_cover),
                value = weather.cloud.ifEmpty { "--" },
                unit = "%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SmallCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    GlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

