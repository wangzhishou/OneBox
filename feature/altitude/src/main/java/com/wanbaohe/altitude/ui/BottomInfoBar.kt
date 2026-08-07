package com.wanbaohe.altitude.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave

/**
 * 观测时间 + 更新时间信息（参与截图）
 */
@Composable
internal fun ObservationTimeBar(
    weather: WeatherInfo?,
    modifier: Modifier = Modifier
) {
    if (weather == null) return
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(6.dp)) {
            drawCircle(color = Color(0xFF4CAF50))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${stringResource(CoreR.string.altitude_observation)}: ${weather.obsTime.takeLast(5)}",
            style = MaterialTheme.typography.labelSmall,
            color = onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "|",
            style = MaterialTheme.typography.labelSmall,
            color = onSurface.copy(alpha = 0.2f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${stringResource(CoreR.string.altitude_updated)}: ${weather.updateTime.takeLast(5)}",
            style = MaterialTheme.typography.labelSmall,
            color = onSurface.copy(alpha = 0.5f)
        )
    }
}

/**
 * 分享 + 保存按钮栏（不参与截图）
 */
@Composable
internal fun ActionButtonsBar(
    onSaveRecord: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        OutlinedButton(
            onClick = onShare,
            shape = RoundedCornerShape(50)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(CoreR.string.altitude_share),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        FilledTonalButton(
            onClick = onSaveRecord,
            shape = RoundedCornerShape(50),
            colors = AppTheme.colors.filledTonalButtonColors()
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(CoreR.string.altitude_save_record),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * 底部信息栏 — 观测时间 + 更新时间 + 保存记录 + 分享按钮
 * (保留兼容，内部组合两个子组件)
 */
@Composable
internal fun BottomInfoBar(
    weather: WeatherInfo?,
    onSaveRecord: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ObservationTimeBar(weather = weather)
        ActionButtonsBar(onSaveRecord = onSaveRecord, onShare = onShare)
    }
}
