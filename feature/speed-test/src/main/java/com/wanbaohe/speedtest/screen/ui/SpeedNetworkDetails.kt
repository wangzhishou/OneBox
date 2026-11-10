package com.wanbaohe.speedtest.screen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocationOn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNetworkCell
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePublic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTimer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUpload
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.speedtest.R
import com.wanbaohe.speedtest.data.SpeedTestConfig
import com.wanbaohe.speedtest.domain.SpeedTestRecord

@Composable
internal fun SpeedNetworkDetails(
    record: SpeedTestRecord?,
    config: SpeedTestConfig?,
    modifier: Modifier = Modifier
) {
    val host = record?.serverHost?.takeIf { it.isNotBlank() }
        ?: config?.serverHost.orEmpty()
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        style = GlassStyle.Thin,
        borderWidth = 0.dp
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.speed_test_node_details_title),
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.getPrimaryTextColor()
                )
                Text(
                    text = host.ifEmpty { "--" },
                    modifier = Modifier.widthIn(max = 156.dp),
                    fontSize = 11.sp,
                    color = AppTheme.colors.getOnInactiveContainerColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.size(10.dp))
            NetworkInfoRow(
                icon = Icons.Outlined.LinePublic,
                label = stringResource(R.string.speed_test_client_ip),
                value = record?.clientIp
            )
            NetworkInfoRow(
                icon = Icons.Outlined.LineLocationOn,
                label = stringResource(R.string.speed_test_client_location),
                value = record?.clientLocation
            )
            NetworkInfoRow(
                icon = Icons.Outlined.LineNetworkCell,
                label = stringResource(R.string.speed_test_client_carrier),
                value = record?.carrier
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = AppTheme.colors.getInactiveContainerColor()
            )
            NetworkInfoRow(
                icon = Icons.Outlined.LineTimer,
                label = stringResource(R.string.speed_test_latency_node),
                value = record?.latencyNode
            )
            NetworkInfoRow(
                icon = Icons.Outlined.LineDownload,
                label = stringResource(R.string.speed_test_download_node),
                value = record?.downloadNode
            )
            NetworkInfoRow(
                icon = Icons.Outlined.LineUpload,
                label = stringResource(R.string.speed_test_upload_node),
                value = record?.uploadNode
            )
        }
    }
}

@Composable
private fun NetworkInfoRow(
    icon: ImageVector,
    label: String,
    value: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 38.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            modifier = Modifier.width(68.dp),
            fontSize = 12.sp,
            color = AppTheme.colors.getOnInactiveContainerColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "--",
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = AppTheme.colors.getPrimaryTextColor(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
