package com.wanbaohe.speedtest.screen.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import com.shifenmiao.theme.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import com.wanbaohe.speedtest.R
import com.wanbaohe.speedtest.domain.SpeedTestRecord
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTimer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNetworkCell

/** 根据下载速度估算"打败全国X%用户"百分比 */
private fun beatPercent(mbps: Float): Float = when {
    mbps < 1f -> 2f
    mbps < 5f -> 8f
    mbps < 10f -> 18f
    mbps < 25f -> 35f
    mbps < 50f -> 52f
    mbps < 100f -> 70f
    mbps < 200f -> 85f
    else -> 95f
}

/**
 * 测速完成后的结果区域（视觉增强版）
 *
 * - 大字速度 + scaleIn 出场动效
 * - 速度徽章 "打败X%用户"
 * - 渐变色速度条（"慢" → "快" 标签）
 * - 带图标的延迟/网络类型卡片 + 竖向分割线
 */
@Composable
fun SpeedResultDisplay(
    record: SpeedTestRecord,
    modifier: Modifier = Modifier
) {
    // 触发渐入动画
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── 标题 ──────────────────────────────────────────────────────────
        Text(
            text = stringResource(R.string.speed_test_result_download_title),
            fontSize = 14.sp,
            color = AppTheme.colors.getOnInactiveContainerColor()
        )
        Spacer(Modifier.height(4.dp))

        // ── 大字速度（scaleIn + fadeIn 动效）──────────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                animationSpec = tween(500),
                initialScale = 0.7f
            ) + fadeIn(animationSpec = tween(500)),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%.1f".format(record.downloadMbps),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppTheme.colors.getPrimaryColor(),
                    lineHeight = 60.sp
                )
                Text(
                    text = " ${stringResource(R.string.speed_test_unit_mbps)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.getOnInactiveContainerColor(),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }

        // ── 换算速度 ─────────────────────────────────────────────────────
        val mbPerSec = record.downloadMbPerSec
        Text(
            text = stringResource(R.string.speed_test_result_mb_per_sec, mbPerSec),
            fontSize = 13.sp,
            color = AppTheme.colors.getOnInactiveContainerColor()
        )

        Spacer(Modifier.height(10.dp))

        // ── "打败X%用户" 徽章 ────────────────────────────────────────────
        val beat = beatPercent(record.downloadMbps)
        Box(
            modifier = Modifier
                .glassThin(
                    color = AppTheme.colors.getPrimaryColor().copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    borderWidth = 0.dp
                )
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.speed_test_beat_percent_badge, beat.roundToInt()),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.getPrimaryColor()
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── 渐变速度条 ────────────────────────────────────────────────────
        SpeedGradientBar(
            speedMbps = record.downloadMbps,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(20.dp))

        // ── 延迟 & 网络类型卡片（带图标 + 竖向分割线）────────────────────
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.getContainerSurfaceColor()
            ),
            borderWidth = 0.dp,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricCardItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTimer,
                    label = stringResource(R.string.speed_test_latency_result_label),
                    value = record.latencyDisplay,
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    thickness = 1.dp,
                    color = AppTheme.colors.getInactiveContainerColor()
                )
                MetricCardItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNetworkCell,
                    label = stringResource(R.string.speed_test_network_type_label),
                    value = record.networkType,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricCardItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.getPrimaryColor().copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppTheme.colors.getOnInactiveContainerColor()
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.getPrimaryTextColor()
        )
    }
}

/**
 * 渐变速度条（"慢" ── 进度条 ── "快"）
 * 指针位置根据速度相对最大值 (200 Mbps) 计算。
 */
@Composable
private fun SpeedGradientBar(
    speedMbps: Float,
    modifier: Modifier = Modifier
) {
    val fraction = (speedMbps / 200f).coerceIn(0f, 1f)
    val primaryColor = AppTheme.colors.getPrimaryColor()
    val subtitleColor = AppTheme.colors.getOnInactiveContainerColor()
    val gradientColors = listOf(
        Color(0xFFE53935), // 红（慢）
        Color(0xFFFF7043),
        Color(0xFFFFB300),
        Color(0xFF66BB6A),
        Color(0xFF2E7D32)  // 深绿（快）
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.speed_test_bar_slow),
                fontSize = 12.sp,
                color = subtitleColor
            )

            // 渐变条 + 指针
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
            ) {
                val barH = size.height
                val radius = barH / 2f

                // 渐变条背景
                drawRoundRect(
                    brush = Brush.horizontalGradient(gradientColors),
                    cornerRadius = CornerRadius(radius)
                )

                // 指针（圆点 + 竖线）
                val pointerX = fraction * size.width
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = Offset(pointerX, barH / 2f)
                )
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(pointerX, barH / 2f)
                )
            }

            Text(
                text = stringResource(R.string.speed_test_bar_fast),
                fontSize = 12.sp,
                color = subtitleColor
            )
        }
    }
}
