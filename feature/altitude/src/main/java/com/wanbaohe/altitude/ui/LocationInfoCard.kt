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

package com.wanbaohe.altitude.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.core.R as CoreR
import com.wanbaohe.core.weather.domain.model.CityInfo
import kotlin.math.abs

/**
 * 位置信息卡片 — Administrative Area / Timezone / UTC Offset / Region
 *
 * NOTE: CityInfo fields are declared non-null in Kotlin but can be null at runtime
 * when deserialized by Gson (which bypasses Kotlin constructors). Guard with .orEmpty().
 */
@Suppress("UNNECESSARY_SAFE_CALL", "KotlinRedundantDiagnosticSuppress")
@Composable
internal fun LocationInfoCard(
    cityInfo: CityInfo,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val cityField = safeText { cityInfo.name }
    val districtField2 = safeText { cityInfo.adm2 }
    val districtField1 = safeText { cityInfo.adm1 }
    val cityName = cityField.ifEmpty { cityField }.ifEmpty { "--" }
    val regionText = buildString {
        val parts = listOf(districtField1, safeText { cityInfo.country })
            .filter { it.isNotBlank() && it != cityName }
        append(parts.joinToString(", ").ifEmpty { "--" })
    }
    val adminText = districtField2.ifEmpty { districtField1 }.ifEmpty { cityField }.ifEmpty { "--" }
    val timeZoneText = safeText { cityInfo.tz }.ifEmpty { "--" }
    val utcOffsetText = safeText { cityInfo.utcOffset }.ifEmpty { "--" }
    val coordinateText = formatCoordinate(cityInfo.lat, cityInfo.lon)
    val rankText = safeText { cityInfo.rank }.ifEmpty { "--" }

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(CoreR.string.altitude_current_location),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurface.copy(alpha = 0.65f)
                )
                if (rankText != "--") {
                    GlassSurface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "CITY RANK $rankText",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = cityName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = onSurface
                )
                Text(
                    text = regionText,
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurface.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                InfoCell(
                    label = stringResource(CoreR.string.altitude_admin_area),
                    value = adminText,
                    modifier = Modifier.weight(1f)
                )
                InfoCell(
                    label = stringResource(CoreR.string.altitude_coordinates),
                    value = coordinateText,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                InfoCell(
                    label = stringResource(CoreR.string.altitude_timezone),
                    value = timeZoneText,
                    modifier = Modifier.weight(1f)
                )
                InfoCell(
                    label = stringResource(CoreR.string.altitude_utc_offset),
                    value = utcOffsetText,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = onSurface.copy(alpha = 0.4f),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun formatCoordinate(lat: Double, lon: Double): String {
    val latDirection = if (lat >= 0) "N" else "S"
    val lonDirection = if (lon >= 0) "E" else "W"
    return "%.2f° %s, %.2f° %s".format(abs(lat), latDirection, abs(lon), lonDirection)
}

private fun safeText(provider: () -> String): String {
    return runCatching(provider).getOrNull().orEmpty()
}

