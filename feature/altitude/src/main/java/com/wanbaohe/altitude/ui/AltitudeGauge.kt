package com.wanbaohe.altitude.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.altitude.component.AltitudeUiState
import com.wanbaohe.altitude.domain.AltitudeSource
import com.wanbaohe.altitude.domain.AltitudeUnit
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.ArrowUpward
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMinus
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDownward
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGpsFixed
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGpsNotFixed

/**
 * 海拔仪核心展示卡片（重构版）
 *
 * 布局分三段，互不遮挡：
 *   TOP    ─ 数据来源徽章（GPS / 气压计 / 搜索中），仅在无坐标时显示"搜索中"
 *   CENTER ─ 固定 220 dp 圆形区域：同心圆装饰 + 海拔大数字 + 趋势箭头
 *   BOTTOM ─ 精度信息、经纬度坐标、单位切换提示
 *
 * - "搜索中"文字只在 latitude == null 时显示；已有坐标则静默等待高程
 * - 点击卡片切换 m / ft 单位
 */
@Composable
internal fun AltitudeGauge(
    state: AltitudeUiState,
    onToggleUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedMeters by animateFloatAsState(
        targetValue = state.currentAltitudeMeters ?: 0f,
        animationSpec = tween(durationMillis = 800),
        label = "altitude_anim"
    )
    val displayValue = remember(animatedMeters, state.unit, state.currentAltitudeMeters) {
        if (state.currentAltitudeMeters == null) "--"
        else "%.1f".format(state.unit.fromMeters(animatedMeters))
    }

    val containerColor  = MaterialTheme.colorScheme.primaryContainer
    val containerVariant = MaterialTheme.colorScheme.secondaryContainer
    val onContainer     = MaterialTheme.colorScheme.onPrimaryContainer
    val primary         = MaterialTheme.colorScheme.primary
    val error           = MaterialTheme.colorScheme.error
    val tertiary        = MaterialTheme.colorScheme.tertiary
    val gradient = remember(containerColor, containerVariant) {
        Brush.verticalGradient(listOf(containerColor, containerVariant))
    }

    // 只有完全没有任何位置数据（lat == null）时才算"真正在搜索"
    val hasLocation = state.latitude != null
    val isReallySearching = state.isAcquiring && !hasLocation
    val isWaitingAltitude = hasLocation && state.currentAltitudeMeters == null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(gradient)
            .clickable(onClick = onToggleUnit)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 28.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ────────────────────────────────────────────────────────
            // TOP：来源徽章 / 搜索中指示 / 空白占位（高度固定，防布局跳动）
            // ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isReallySearching -> {
                        // 完全无位置：显示搜索中
                        SourceRow(
                            label = stringResource(CoreR.string.altitude_searching),
                            useFixedIcon = false,
                            accuracyLevel = 0,
                            primary = primary,
                            onContainer = onContainer
                        )
                    }
                    state.altitudeSource != null -> {
                        // 有海拔数据：显示来源
                        SourceRow(
                            label = if (state.altitudeSource == AltitudeSource.BAROMETER) {
                                stringResource(CoreR.string.altitude_source_barometer)
                            } else {
                                stringResource(CoreR.string.altitude_source_gps)
                            },
                            useFixedIcon = true,
                            accuracyLevel = state.accuracyLevel,
                            primary = primary,
                            onContainer = onContainer
                        )
                    }
                    isWaitingAltitude -> {
                        SourceRow(
                            label = stringResource(CoreR.string.altitude_waiting_altitude_short),
                            useFixedIcon = false,
                            accuracyLevel = 1,
                            primary = primary,
                            onContainer = onContainer
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ────────────────────────────────────────────────────────
            // CENTER：220 dp 固定圆形区域，装饰环在内部，不溢出
            // ────────────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                // 背景装饰同心圆（仅在真正搜索时脉冲）
                DecorationRings(isAcquiring = isReallySearching, ringColor = onContainer)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 海拔大数字 + 单位
                    Row(verticalAlignment = Alignment.Bottom) {
                        AnimatedContent(
                            targetState = displayValue,
                            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                            label = "altitude_text"
                        ) { value ->
                            Text(
                                text = value,
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.currentAltitudeMeters != null) onContainer
                                        else onContainer.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                letterSpacing = (-2).sp
                            )
                        }
                        if (state.currentAltitudeMeters != null) {
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = state.unit.suffix,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = primary,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                    }
                    // 趋势箭头
                    AltitudeDeltaRow(
                        delta = state.altitudeDeltaMeters,
                        unit = state.unit,
                        onContainer = onContainer,
                        risingColor = tertiary,
                        fallingColor = error
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ────────────────────────────────────────────────────────
            // BOTTOM：精度 → 坐标 → 单位切换（各自独立行，间距 8 dp）
            // ────────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.currentAltitudeMeters != null) {
                    AccuracyRow(
                        accuracy = state.accuracyMeters,
                        verticalAccuracy = state.verticalAccuracyMeters,
                        verticalAccuracyAvailable = state.isVerticalAccuracyAvailable,
                        onContainer = onContainer
                    )
                }
                if (isWaitingAltitude) {
                    AwaitingAltitudeHint(onContainer = onContainer)
                }
                if (state.latitude != null && state.longitude != null) {
                    CoordinatesRow(
                        lat = state.latitude,
                        lon = state.longitude,
                        onContainer = onContainer
                    )
                }
                UnitToggleHint(unit = state.unit, primary = primary, onContainer = onContainer)
            }
        }
    }
}

// ─── 子组件 ────────────────────────────────────────────────────────────────

/**
 * 数据来源/状态行
 * [useFixedIcon] = true → GpsFixed 图标（已定位）；false → GpsNotFixed（搜索中）
 */
@Composable
private fun SourceRow(
    label: String,
    useFixedIcon: Boolean,
    accuracyLevel: Int,
    primary: Color,
    onContainer: Color
) {
    val tint = if (useFixedIcon) primary else primary.copy(alpha = 0.55f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (useFixedIcon) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGpsFixed else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGpsNotFixed,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = tint,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(2.dp))
        repeat(3) { i ->
            SignalBar(
                height = (10 + i * 4).dp,
                filled = i < accuracyLevel,
                accentColor = primary,
                onContainer = onContainer
            )
        }
    }
}

/** 背景装饰同心圆（搜寻信号时有脉冲动画），圆径已适配 220 dp 容器 */
@Composable
private fun DecorationRings(isAcquiring: Boolean, ringColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "rings")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val scale = if (isAcquiring) pulse else 1f

    Box(contentAlignment = Alignment.Center) {
        Box(Modifier.size(210.dp).scale(scale).clip(CircleShape).background(ringColor.copy(alpha = 0.04f)))
        Box(Modifier.size(158.dp).clip(CircleShape).background(ringColor.copy(alpha = 0.05f)))
        Box(Modifier.size(106.dp).clip(CircleShape).background(ringColor.copy(alpha = 0.06f)))
    }
}

/** 单格信号条 */
@Composable
private fun SignalBar(
    height: Dp,
    filled: Boolean,
    accentColor: Color,
    onContainer: Color
) {
    Box(
        modifier = Modifier
            .width(5.dp)
            .height(height)
            .clip(RoundedCornerShape(2.dp))
            .background(if (filled) accentColor else onContainer.copy(alpha = 0.22f))
    )
}

/** 上升 / 下降趋势行 */
@Composable
private fun AltitudeDeltaRow(
    delta: Float?,
    unit: AltitudeUnit,
    onContainer: Color,
    risingColor: Color,
    fallingColor: Color
) {
    if (delta == null) return
    val absDelta = kotlin.math.abs(delta)
    val (icon, color, label) = when {
        absDelta < 0.5f -> Triple(
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMinus, onContainer.copy(0.4f),
            stringResource(CoreR.string.altitude_level)
        )
        delta > 0f -> Triple(
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward, risingColor,
            "+${"%.1f".format(unit.fromMeters(delta))} ${unit.suffix}"
        )
        else -> Triple(
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDownward, fallingColor,
            "${"%.1f".format(unit.fromMeters(delta))} ${unit.suffix}"
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(text = label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

/** 精度信息胶囊 */
@Composable
private fun AccuracyRow(
    accuracy: Float,
    verticalAccuracy: Float,
    verticalAccuracyAvailable: Boolean,
    onContainer: Color
) {
    val text = buildString {
        append(stringResource(CoreR.string.altitude_accuracy, "%.0f".format(accuracy)))
        append("  ")
        if (verticalAccuracyAvailable && verticalAccuracy > 0f) {
            append(
                stringResource(
                    CoreR.string.altitude_vertical_accuracy,
                    "%.0f".format(verticalAccuracy)
                )
            )
        } else {
            append(stringResource(CoreR.string.altitude_vertical_accuracy_unavailable))
        }
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(onContainer.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 11.sp, color = onContainer.copy(0.55f))
    }
}

@Composable
private fun AwaitingAltitudeHint(onContainer: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(onContainer.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(CoreR.string.altitude_waiting_altitude_hint),
            fontSize = 11.sp,
            color = onContainer.copy(0.58f),
            textAlign = TextAlign.Center
        )
    }
}

/** 经纬度坐标胶囊 */
@Composable
private fun CoordinatesRow(
    lat: Double,
    lon: Double,
    onContainer: Color
) {
    val latStr = "%.5f° %s".format(kotlin.math.abs(lat), if (lat >= 0) "N" else "S")
    val lonStr = "%.5f° %s".format(kotlin.math.abs(lon), if (lon >= 0) "E" else "W")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(onContainer.copy(alpha = 0.08f))
            .padding(horizontal = 14.dp, vertical = 5.dp)
    ) {
        Text(
            text = "$latStr   $lonStr",
            fontSize = 11.sp,
            color = onContainer.copy(0.65f),
            textAlign = TextAlign.Center,
            letterSpacing = 0.3.sp
        )
    }
}

/** 底部单位切换提示（m · ft） */
@Composable
private fun UnitToggleHint(
    unit: AltitudeUnit,
    primary: Color,
    onContainer: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        val mActive = unit == AltitudeUnit.METERS
        Text(
            text = "m",
            fontSize = 13.sp,
            fontWeight = if (mActive) FontWeight.Bold else FontWeight.Normal,
            color = if (mActive) primary else onContainer.copy(0.35f)
        )
        Text(text = "·", fontSize = 13.sp, color = onContainer.copy(0.25f))
        Text(
            text = "ft",
            fontSize = 13.sp,
            fontWeight = if (!mActive) FontWeight.Bold else FontWeight.Normal,
            color = if (!mActive) primary else onContainer.copy(0.35f)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(CoreR.string.altitude_tap_to_switch),
            fontSize = 10.sp,
            color = onContainer.copy(0.3f),
            letterSpacing = 0.5.sp
        )
    }
}
