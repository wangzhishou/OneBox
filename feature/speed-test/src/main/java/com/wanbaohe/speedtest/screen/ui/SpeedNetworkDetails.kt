package com.wanbaohe.speedtest.screen.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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

/** 一行网络信息；只保留真正有值的条目，空闲态不再渲染一整屏 "--"。 */
private data class NetworkInfoItem(
    val icon: ImageVector,
    val label: String,
    val value: String
)

/**
 * 网络与节点详情。
 *
 * 只有拿到测速结果且至少一个字段有值时才出现：进场用淡入 + 纵向展开，
 * 各行按 40ms 间隔轻微上浮淡入；切换历史记录时按时间戳重新播一次。
 */
@Composable
internal fun SpeedNetworkDetails(
    record: SpeedTestRecord?,
    config: SpeedTestConfig?,
    modifier: Modifier = Modifier
) {
    val host = record?.serverHost?.takeIf { it.isNotBlank() }
        ?: config?.serverHost.orEmpty()
    val clientItems = listOf(
        NetworkInfoItem(
            Icons.Outlined.LinePublic,
            stringResource(R.string.speed_test_client_ip),
            record?.clientIp.orEmpty()
        ),
        NetworkInfoItem(
            Icons.Outlined.LineLocationOn,
            stringResource(R.string.speed_test_client_location),
            record?.clientLocation.orEmpty()
        ),
        NetworkInfoItem(
            Icons.Outlined.LineNetworkCell,
            stringResource(R.string.speed_test_client_carrier),
            record?.carrier.orEmpty()
        )
    ).filter { it.value.isNotBlank() }
    val nodeItems = listOf(
        NetworkInfoItem(
            Icons.Outlined.LineTimer,
            stringResource(R.string.speed_test_latency_node),
            record?.latencyNode.orEmpty()
        ),
        NetworkInfoItem(
            Icons.Outlined.LineDownload,
            stringResource(R.string.speed_test_download_node),
            record?.downloadNode.orEmpty()
        ),
        NetworkInfoItem(
            Icons.Outlined.LineUpload,
            stringResource(R.string.speed_test_upload_node),
            record?.uploadNode.orEmpty()
        )
    ).filter { it.value.isNotBlank() }

    val hasData = record != null && (clientItems.isNotEmpty() || nodeItems.isNotEmpty())
    val revealKey = record?.recordedAt ?: 0L

    AnimatedVisibility(
        visible = hasData,
        modifier = Modifier.fillMaxWidth(),
        enter = fadeIn(tween(260, delayMillis = 60)) + expandVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            expandFrom = Alignment.Top
        ),
        exit = fadeOut(tween(160)) + shrinkVertically(
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            shrinkTowards = Alignment.Top
        )
    ) {
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
                clientItems.forEachIndexed { index, item ->
                    NetworkInfoRow(item.icon, item.label, item.value, revealKey, index)
                }
                if (clientItems.isNotEmpty() && nodeItems.isNotEmpty()) {
                    NetworkInfoDivider(revealKey, clientItems.size)
                }
                nodeItems.forEachIndexed { index, item ->
                    NetworkInfoRow(
                        item.icon, item.label, item.value,
                        revealKey, clientItems.size + index
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkInfoDivider(revealKey: Long, order: Int) {
    val reveal = remember(revealKey, order) { Animatable(0f) }
    LaunchedEffect(revealKey, order) {
        reveal.animateTo(1f, tween(220, delayMillis = 80 + order * 40))
    }
    HorizontalDivider(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .graphicsLayer { alpha = reveal.value },
        color = AppTheme.colors.getInactiveContainerColor()
    )
}

@Composable
private fun NetworkInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    revealKey: Long,
    order: Int,
    modifier: Modifier = Modifier
) {
    val reveal = remember(revealKey, order) { Animatable(0f) }
    LaunchedEffect(revealKey, order) {
        reveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 240,
                delayMillis = 80 + order * 40,
                easing = FastOutSlowInEasing
            )
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 38.dp)
            .padding(vertical = 4.dp)
            .graphicsLayer {
                alpha = reveal.value
                translationY = (1f - reveal.value) * 8.dp.toPx()
            },
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
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = AppTheme.colors.getPrimaryTextColor(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
