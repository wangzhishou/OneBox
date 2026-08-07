package com.wanbaohe.altitude.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.altitude.component.AltitudeUiState
import com.wanbaohe.altitude.domain.AltitudeSource
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import kotlin.math.ln
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWaterDrop

/**
 * 海拔仪表盘区域 — 大型圆弧仪表 + 海拔数字 + 趋势徽章
 */
@Composable
internal fun ElevationGaugeSection(
    state: AltitudeUiState,
    modifier: Modifier = Modifier
) {
    val animatedMeters by animateFloatAsState(
        targetValue = state.currentAltitudeMeters ?: 0f,
        animationSpec = tween(durationMillis = 800),
        label = "altitude_anim"
    )
    val displayValue = remember(animatedMeters, state.unit, state.currentAltitudeMeters) {
        if (state.currentAltitudeMeters == null) "--"
        else "%,.0f".format(state.unit.fromMeters(animatedMeters))
    }

    val colorScheme = MaterialTheme.colorScheme
    val gaugeStart = lerp(colorScheme.primary, colorScheme.tertiary, 0.18f)
    val gaugeMid = lerp(colorScheme.primaryContainer, colorScheme.secondaryContainer, 0.46f)
    val gaugeEnd = lerp(colorScheme.secondary, colorScheme.surface, 0.4f)
    val ringTrack = lerp(colorScheme.primaryContainer, colorScheme.surface, 0.55f).copy(alpha = 0.24f)
    val onSurface = colorScheme.onSurface
    val numberFontSize = remember(displayValue) {
        when {
            displayValue.length >= 7 -> 48.sp
            displayValue.length >= 6 -> 54.sp
            displayValue.length >= 5 -> 60.sp
            else -> 66.sp
        }
    }
    val unitFontSize = remember(displayValue) {
        if (displayValue.length >= 6) 16.sp else 20.sp
    }

    // 仪表弧度进度（0~1），使用对数映射让中低海拔视觉更接近设计稿。
    val maxAlt = 9000f
    val progress by animateFloatAsState(
        targetValue = ((ln((state.currentAltitudeMeters ?: 0f) + 1f) / ln(maxAlt + 1f))).coerceIn(0f, 1f),
        animationSpec = tween(1000),
        label = "gauge_progress"
    )

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(CoreR.string.altitude_elevation),
                style = MaterialTheme.typography.labelSmall,
                color = onSurface.copy(alpha = 0.55f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(4.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                Canvas(modifier = Modifier.size(190.dp)) {
                    val strokeWidth = 11.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val gapAngle = 120f
                    // Center gap at 6 o'clock (90deg in Canvas polar coordinates).
                    val startAngle = 90f + gapAngle / 2f
                    val sweepAngle = 360f - gapAngle
                    val radius = (size.minDimension - strokeWidth) / 2f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(gaugeStart.copy(alpha = 0.08f), colorScheme.surface.copy(alpha = 0f)),
                            center = center,
                            radius = size.minDimension * 0.5f
                        ),
                        radius = size.minDimension * 0.48f,
                        center = center
                    )

                    drawArc(
                        color = ringTrack,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    withTransform({ rotate(degrees = startAngle, pivot = center) }) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colorStops = arrayOf(
                                    0f to gaugeStart.copy(alpha = 0.95f),
                                    (sweepAngle * 0.5f / 360f) to gaugeMid.copy(alpha = 0.72f),
                                    (sweepAngle / 360f) to gaugeEnd.copy(alpha = 0.5f),
                                    1f to gaugeEnd.copy(alpha = 0.5f)
                                ),
                                center = center
                            ),
                            startAngle = 0f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    val markerAngle = (startAngle + sweepAngle * progress) * (PI / 180f).toFloat()
                    val marker = Offset(
                        x = center.x + radius * cos(markerAngle),
                        y = center.y + radius * sin(markerAngle)
                    )
                    drawCircle(
                        color = gaugeStart.copy(alpha = 0.2f),
                        radius = 8.dp.toPx(),
                        center = marker
                    )
                    drawCircle(
                        color = gaugeStart.copy(alpha = 0.95f),
                        radius = 3.5.dp.toPx(),
                        center = marker
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = displayValue,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                        label = "altitude_num"
                    ) { value ->
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = value,
                                fontSize = numberFontSize,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (state.currentAltitudeMeters != null) onSurface
                                else onSurface.copy(alpha = 0.3f),
                                textAlign = TextAlign.Center,
                                letterSpacing = (-1.6).sp
                            )
                            if (state.currentAltitudeMeters != null) {
                                Text(
                                    text = state.unit.suffix.uppercase(),
                                    fontSize = unitFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = gaugeStart,
                                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            val badge = state.trendBadge
            val badgeText = when (badge) {
                "ASCENDING" -> stringResource(CoreR.string.altitude_ascending)
                "DESCENDING" -> stringResource(CoreR.string.altitude_descending)
                else -> stringResource(CoreR.string.altitude_level)
            }
            val badgeColor = when (badge) {
                "ASCENDING" -> gaugeStart
                "DESCENDING" -> MaterialTheme.colorScheme.error
                else -> onSurface.copy(alpha = 0.4f)
            }
            GlassSurface(
                shape = RoundedCornerShape(50),
                color = badgeColor.copy(alpha = 0.12f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWaterDrop,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        letterSpacing = 1.sp
                    )
                }
            }

            // ── 数据来源 + 精度 ──────────────────────────────────────
            if (state.altitudeSource != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sourceText = when (state.altitudeSource) {
                        AltitudeSource.GPS -> stringResource(CoreR.string.altitude_source_gps)
                        AltitudeSource.BAROMETER -> stringResource(CoreR.string.altitude_source_barometer)
                    }
                    Text(
                        text = "${stringResource(CoreR.string.altitude_detail_source)}: $sourceText",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.45f)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.25f)
                    )
                    Text(
                        text = "${stringResource(CoreR.string.altitude_detail_accuracy)}: ±${"%.1f".format(state.accuracyMeters)} m",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}

