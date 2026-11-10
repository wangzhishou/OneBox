package com.wanbaohe.speedtest.screen.ui

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTimer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUpload
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.speedtest.R

@Composable
fun SpeedMetricsRow(
    latencyDisplay: String,
    downloadDisplay: String,
    uploadDisplay: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricCard(
            icon = Icons.Outlined.LineTimer,
            label = stringResource(R.string.speed_test_latency_label),
            value = latencyDisplay,
            unit = stringResource(R.string.speed_test_unit_ms),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            icon = Icons.Outlined.LineDownload,
            label = stringResource(R.string.speed_test_download_label),
            value = downloadDisplay,
            unit = stringResource(R.string.speed_test_unit_mbps),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            icon = Icons.Outlined.LineUpload,
            label = stringResource(R.string.speed_test_upload_label),
            value = uploadDisplay,
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
    GlassCard(
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        style = GlassStyle.Thin,
        borderWidth = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(9.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = AppTheme.colors.getOnInactiveContainerColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.getPrimaryTextColor(),
                    maxLines = 1
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = unit,
                    fontSize = 9.sp,
                    color = AppTheme.colors.getOnInactiveContainerColor(),
                    modifier = Modifier.padding(bottom = 2.dp),
                    maxLines = 1
                )
            }
        }
    }
}
