package com.wanbaohe.measurement.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.measurement.component.MeasurementComponent
import com.wanbaohe.measurement.component.MeasurementTab
import com.wanbaohe.measurement.component.RulerUnit
import kotlin.math.abs
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStraighten
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWaterDrop

@Composable
fun MeasurementScreen(component: MeasurementComponent) {
    val state by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(CoreR.string.measurement_tools),
        onGoBack = component.onGoBack,
        background = if (state.selectedTab == MeasurementTab.RULER) {
            { FullScreenRulerBackground(ppi = state.ppi, unit = state.rulerUnit) }
        } else null,
        showNavigationBarsPadding = false
    ) {
        AnimatedContent(
            targetState = state.selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f)
        ) { tab ->
            when (tab) {
                MeasurementTab.LEVEL -> LevelContent(
                    pitch = state.pitch,
                    roll = state.roll,
                    isSensorAvailable = state.isSensorAvailable,
                    isLocked = state.isLocked,
                    onToggleLock = component::toggleLock
                )

                MeasurementTab.RULER -> RulerContent(
                    unit = state.rulerUnit,
                    onToggleUnit = component::toggleRulerUnit
                )
            }
        }

        BottomNavigationBar(
            items = listOf(
                BottomNavItem(
                    id = "level",
                    label = stringResource(CoreR.string.level),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWaterDrop,
                    selectedIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWaterDrop
                ),
                BottomNavItem(
                    id = "ruler",
                    label = stringResource(CoreR.string.ruler),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStraighten,
                    selectedIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStraighten
                )
            ),
            selectedItemId = when (state.selectedTab) {
                MeasurementTab.LEVEL -> "level"
                MeasurementTab.RULER -> "ruler"
            },
            onItemClick = { item ->
                when (item.id) {
                    "level" -> component.selectTab(MeasurementTab.LEVEL)
                    "ruler" -> component.selectTab(MeasurementTab.RULER)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─── 水平仪 ───────────────────────────────────────────────────────────────────

@Composable
private fun LevelContent(
    pitch: Float,
    roll: Float,
    isSensorAvailable: Boolean,
    isLocked: Boolean,
    onToggleLock: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isSensorAvailable) {
            SensorUnavailableCard(
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LevelBubble(
                pitch = pitch,
                roll = roll,
                isLevel = abs(pitch) < 1f && abs(roll) < 1f,
                modifier = Modifier.weight(1f)
            )

            AngleDisplayCard(
                pitch = pitch,
                roll = roll,
                isLevel = abs(pitch) < 1f && abs(roll) < 1f,
                modifier = Modifier.fillMaxWidth()
            )

            LockButton(
                isLocked = isLocked,
                onToggleLock = onToggleLock,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LevelBubble(
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isLevel) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val bubbleContentColor = if (isLevel) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val size = 240.dp
        val density = LocalDensity.current
        val sizePx = with(density) { size.toPx() }
        val radiusPx = sizePx / 2f

        val maxAngle = 20f
        val clampedPitch = pitch.coerceIn(-maxAngle, maxAngle)
        val clampedRoll = roll.coerceIn(-maxAngle, maxAngle)

        val bubbleOffsetX = (clampedRoll / maxAngle) * radiusPx * 0.75f
        val bubbleOffsetY = (clampedPitch / maxAngle) * radiusPx * 0.75f

        Canvas(modifier = Modifier.size(size)) {
            drawCircle(
                color = surfaceVariant,
                radius = radiusPx,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            val crossLen = radiusPx * 0.3f
            drawLine(
                color = outline,
                start = Offset(center.x - crossLen, center.y),
                end = Offset(center.x + crossLen, center.y),
                strokeWidth = 1.5f
            )
            drawLine(
                color = outline,
                start = Offset(center.x, center.y - crossLen),
                end = Offset(center.x, center.y + crossLen),
                strokeWidth = 1.5f
            )

            drawCircle(
                color = outlineVariant,
                radius = radiusPx * 0.5f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )

            drawCircle(
                color = bubbleColor,
                radius = radiusPx * 0.15f,
                center = Offset(center.x + bubbleOffsetX, center.y + bubbleOffsetY)
            )
            drawCircle(
                color = bubbleContentColor,
                radius = radiusPx * 0.05f,
                center = Offset(center.x + bubbleOffsetX, center.y + bubbleOffsetY)
            )
        }
    }
}

@Composable
private fun AngleDisplayCard(
    pitch: Float,
    roll: Float,
    isLevel: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isLevel) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isLevel) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AngleColumn(
                label = stringResource(CoreR.string.level_pitch),
                value = pitch,
                textColor = textColor
            )
            if (isLevel) {
                Text(
                    text = stringResource(CoreR.string.level_horizontal),
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
            }
            AngleColumn(
                label = stringResource(CoreR.string.level_roll),
                value = roll,
                textColor = textColor
            )
        }
    }
}

@Composable
private fun AngleColumn(
    label: String,
    value: Float,
    textColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%+.1f°".format(value),
            style = MaterialTheme.typography.headlineMedium,
            color = textColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun LockButton(
    isLocked: Boolean,
    onToggleLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isLocked) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isLocked) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onToggleLock
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isLocked) {
                    stringResource(CoreR.string.level_unlock)
                } else {
                    stringResource(CoreR.string.level_lock)
                },
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Composable
private fun SensorUnavailableCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWaterDrop,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(CoreR.string.level_unavailable),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── 屏幕直尺 ─────────────────────────────────────────────────────────────────

@Composable
private fun RulerContent(
    unit: RulerUnit,
    onToggleUnit: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val containerColor = MaterialTheme.colorScheme.primaryContainer
        val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(containerColor)
                .clickable(onClick = onToggleUnit),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (unit == RulerUnit.CM) "CM" else "IN",
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Composable
private fun FullScreenRulerBackground(
    ppi: Float,
    unit: RulerUnit
) {
    val textMeasurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tickColor = MaterialTheme.colorScheme.outline
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val rulerBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    val pixelsPerUnit = when (unit) {
        RulerUnit.CM -> ppi / 2.54f
        RulerUnit.INCH -> ppi
    }
    val minorInterval = when (unit) {
        RulerUnit.CM -> pixelsPerUnit / 10f
        RulerUnit.INCH -> pixelsPerUnit / 16f
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val isPortrait = size.height > size.width
        val rulerBreadth = 200f

        if (isPortrait) {
            // ── 竖屏：纵向贴右边缘 ──────────────────────────────────────
            val startY = 0f
            val endY = size.height
            val rightEdge = size.width
            val leftEdge = rightEdge - rulerBreadth

            drawRect(
                color = rulerBackground,
                topLeft = Offset(leftEdge, startY),
                size = Size(rulerBreadth, endY - startY)
            )

            drawLine(
                color = outlineVariant,
                start = Offset(leftEdge, startY),
                end = Offset(leftEdge, endY),
                strokeWidth = 1f
            )
            drawLine(
                color = outlineVariant,
                start = Offset(rightEdge, startY),
                end = Offset(rightEdge, endY),
                strokeWidth = 1f
            )

            var currentY = startY
            var count = 0
            while (currentY <= endY) {
                val isMajor = when (unit) {
                    RulerUnit.CM -> count % 10 == 0
                    RulerUnit.INCH -> count % 16 == 0
                }
                val isMedium = when (unit) {
                    RulerUnit.CM -> count % 5 == 0 && count % 10 != 0
                    RulerUnit.INCH -> count % 8 == 0 && count % 16 != 0
                }

                val tickLen = when {
                    isMajor -> rulerBreadth * 0.75f
                    isMedium -> rulerBreadth * 0.5f
                    else -> rulerBreadth * 0.3f
                }
                val strokeWidth = if (isMajor) 2f else 1f

                drawLine(
                    color = tickColor,
                    start = Offset(rightEdge - tickLen, currentY),
                    end = Offset(rightEdge, currentY),
                    strokeWidth = strokeWidth
                )

                if (isMajor) {
                    val value = when (unit) {
                        RulerUnit.CM -> count / 10
                        RulerUnit.INCH -> count / 16
                    }
                    val textLayoutResult = textMeasurer.measure(
                        text = value.toString(),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 10.sp
                        )
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            rightEdge - tickLen - textLayoutResult.size.width - 4f,
                            currentY - textLayoutResult.size.height / 2f
                        )
                    )
                }

                currentY += minorInterval
                count++
            }

            val zeroText = textMeasurer.measure(
                text = "0",
                style = TextStyle(
                    color = textColor,
                    fontSize = 12.sp
                )
            )
            drawText(
                textLayoutResult = zeroText,
                topLeft = Offset(
                    leftEdge - zeroText.size.width - 6f,
                    4f
                )
            )

            val maxValue = when (unit) {
                RulerUnit.CM -> ((endY / pixelsPerUnit * 10).toInt() / 10)
                RulerUnit.INCH -> ((endY / pixelsPerUnit * 16).toInt() / 16)
            }
            if (maxValue > 0) {
                val maxText = textMeasurer.measure(
                    text = "~${maxValue}${if (unit == RulerUnit.CM) "cm" else "\""}",
                    style = TextStyle(
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                )
                drawText(
                    textLayoutResult = maxText,
                    topLeft = Offset(
                        leftEdge - maxText.size.width - 6f,
                        endY - maxText.size.height - 4f
                    )
                )
            }
        } else {
            // ── 横屏：横向贴上边缘 ──────────────────────────────────────
            val startX = 0f
            val endX = size.width
            val rulerY = rulerBreadth / 2f

            drawRect(
                color = rulerBackground,
                topLeft = Offset(startX, 0f),
                size = Size(endX - startX, rulerBreadth)
            )

            drawLine(
                color = outlineVariant,
                start = Offset(startX, 0f),
                end = Offset(endX, 0f),
                strokeWidth = 1f
            )
            drawLine(
                color = outlineVariant,
                start = Offset(startX, rulerBreadth),
                end = Offset(endX, rulerBreadth),
                strokeWidth = 1f
            )

            var currentX = startX
            var count = 0
            while (currentX <= endX) {
                val isMajor = when (unit) {
                    RulerUnit.CM -> count % 10 == 0
                    RulerUnit.INCH -> count % 16 == 0
                }
                val isMedium = when (unit) {
                    RulerUnit.CM -> count % 5 == 0 && count % 10 != 0
                    RulerUnit.INCH -> count % 8 == 0 && count % 16 != 0
                }

                val tickLen = when {
                    isMajor -> rulerBreadth * 0.75f
                    isMedium -> rulerBreadth * 0.5f
                    else -> rulerBreadth * 0.3f
                }
                val strokeWidth = if (isMajor) 2f else 1f

                drawLine(
                    color = tickColor,
                    start = Offset(currentX, rulerBreadth - tickLen),
                    end = Offset(currentX, rulerBreadth),
                    strokeWidth = strokeWidth
                )

                if (isMajor) {
                    val value = when (unit) {
                        RulerUnit.CM -> count / 10
                        RulerUnit.INCH -> count / 16
                    }
                    val textLayoutResult = textMeasurer.measure(
                        text = value.toString(),
                        style = TextStyle(
                            color = textColor,
                            fontSize = 10.sp
                        )
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            currentX - textLayoutResult.size.width / 2f,
                            rulerBreadth - tickLen - textLayoutResult.size.height - 4f
                        )
                    )
                }

                currentX += minorInterval
                count++
            }

            val zeroText = textMeasurer.measure(
                text = "0",
                style = TextStyle(
                    color = textColor,
                    fontSize = 12.sp
                )
            )
            drawText(
                textLayoutResult = zeroText,
                topLeft = Offset(
                    4f,
                    rulerBreadth + 6f
                )
            )

            val maxValue = when (unit) {
                RulerUnit.CM -> ((endX / pixelsPerUnit * 10).toInt() / 10)
                RulerUnit.INCH -> ((endX / pixelsPerUnit * 16).toInt() / 16)
            }
            if (maxValue > 0) {
                val maxText = textMeasurer.measure(
                    text = "~${maxValue}${if (unit == RulerUnit.CM) "cm" else "\""}",
                    style = TextStyle(
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                )
                drawText(
                    textLayoutResult = maxText,
                    topLeft = Offset(
                        endX - maxText.size.width - 4f,
                        rulerBreadth + 6f
                    )
                )
            }
        }
    }
}
