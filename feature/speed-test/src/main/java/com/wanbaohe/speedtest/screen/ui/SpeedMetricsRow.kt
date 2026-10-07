package com.wanbaohe.speedtest.screen.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
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
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        containerAlpha = 0.92f,
        borderWidth = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 图标
            Box(
                Modifier.size(32.dp).background(primaryColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = subtitleColor
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.getPrimaryTextColor(),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.size(3.dp))
                    Text(
                        text = unit,
                        fontSize = 10.sp,
                        color = subtitleColor,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }
            }
        }
    }
}
