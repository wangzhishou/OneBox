package com.wanbaohe.speedtest.screen.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import com.wanbaohe.speedtest.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTimer

/**
 * 顶部双列玻璃指标卡片：网络延迟 | 下载速度
 * 空闲时显示 "--"，测速完成后显示实际值。
 */
@Composable
fun SpeedMetricsRow(
    latencyDisplay: String,
    downloadDisplay: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTimer,
            label = stringResource(R.string.speed_test_latency_label),
            value = latencyDisplay,
            unit = stringResource(R.string.speed_test_unit_ms),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload,
            label = stringResource(R.string.speed_test_download_label),
            value = downloadDisplay,
            unit = stringResource(R.string.speed_test_unit_mbps),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = AppTheme.colors.getPrimaryColor()
    val subtitleColor = AppTheme.colors.getOnInactiveContainerColor()

    Row(
        modifier = modifier
            .glassThin(
                color = AppTheme.colors.getContainerSurfaceColor(),
                shape = RoundedCornerShape(16.dp),
                borderWidth = 0.dp
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 图标
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryColor.copy(alpha = 0.75f),
            modifier = Modifier.size(20.dp)
        )

        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = subtitleColor
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.getPrimaryTextColor()
                )
                Spacer(Modifier.size(3.dp))
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = subtitleColor,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}
