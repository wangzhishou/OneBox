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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWaterDrop
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAir

/**
 * 天气详情行 — Wind / Humidity / Visibility
 */
@Composable
internal fun WeatherDetailsCard(
    weather: WeatherInfo,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.onPrimaryContainer

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            DetailRow(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAir,
                label = stringResource(CoreR.string.altitude_wind),
                value = "${weather.windDir} ${weather.windSpeed} km/h",
                primary = primary,
                onSurface = onSurface
            )
            DetailRow(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWaterDrop,
                label = stringResource(CoreR.string.altitude_humidity),
                value = "${weather.humidity}%",
                primary = primary,
                onSurface = onSurface
            )
            DetailRow(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                label = stringResource(CoreR.string.altitude_visibility),
                value = "${weather.vis} km",
                primary = primary,
                onSurface = onSurface
            )
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    primary: androidx.compose.ui.graphics.Color,
    onSurface: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primary.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = onSurface.copy(alpha = 0.4f),
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = onSurface
            )
        }
    }
}

