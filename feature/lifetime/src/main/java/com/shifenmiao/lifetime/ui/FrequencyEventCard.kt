package com.shifenmiao.lifetime.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.icon.IconAvatar
import com.shifenmiao.common.components.sectionGradient
import com.shifenmiao.common.components.sectionIconColor
import com.shifenmiao.common.components.sectionIconContainerColor
import com.shifenmiao.common.components.sectionOnColor
import com.shifenmiao.common.components.sectionThemeForIndex
import com.shifenmiao.lifetime.R
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem

/**
 * 频率事件卡片（点击翻转显示「已完成 / 待完成」）—— 竖屏比例 (2:3 高宽比)。
 *
 * 主题色：按 [themeIndex] 在 PRIMARY / SECONDARY / TERTIARY / SURFACE 中轮转。
 * 容器使用 [sectionGradient]（低 alpha），文字使用 [sectionOnColor]，图标使用 [sectionIconColor]。
 *
 * 底部进度条：根据 [progress]（0~1）显示"已过人生比例"。
 */
@Composable
fun FrequencyEventCard(
    iconKey: String,
    eventName: String,
    completedCount: String,
    remainingCount: String,
    unit: String,
    frequencyLabel: String,
    progress: Float? = null,
    modifier: Modifier = Modifier,
    themeIndex: Int = 0,
) {
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    val theme = sectionThemeForIndex(themeIndex)
    val containerColor = sectionGradient(theme)
    val onContainerColor = sectionOnColor(theme)
    val iconTint = sectionIconColor(theme)
    val iconContainer = sectionIconContainerColor(theme)

    Box(
        modifier = modifier
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(OneBoxDesignSystem.sectionCardShape)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { isFlipped = !isFlipped }
    ) {
        if (rotation <= 90f) {
            FrequencyCardFace(
                iconKey = iconKey,
                eventName = eventName,
                count = completedCount,
                unit = unit,
                frequencyLabel = frequencyLabel,
                containerColor = containerColor,
                onContainerColor = onContainerColor,
                iconTint = iconTint,
                iconContainer = iconContainer,
                progress = progress,
                labelText = stringResource(R.string.lifetime_completed_label)
            )
        } else {
            FrequencyCardFace(
                iconKey = iconKey,
                eventName = eventName,
                count = remainingCount,
                unit = unit,
                frequencyLabel = frequencyLabel,
                containerColor = containerColor,
                onContainerColor = onContainerColor,
                iconTint = iconTint,
                iconContainer = iconContainer,
                progress = progress,
                labelText = stringResource(R.string.lifetime_remaining_label),
                modifier = Modifier.graphicsLayer { rotationY = 180f }
            )
        }
    }
}

@Composable
private fun FrequencyCardFace(
    iconKey: String,
    eventName: String,
    count: String,
    unit: String,
    frequencyLabel: String,
    containerColor: Color,
    onContainerColor: Color,
    iconTint: Color,
    iconContainer: Color,
    progress: Float?,
    labelText: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxSize(),
        shape = OneBoxDesignSystem.sectionCardShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(OneBoxDesignSystem.cardPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top: icon + frequency label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconAvatar(
                    iconName = iconKey,
                    fallbackName = eventName,
                    size = 40.dp,
                    containerColor = iconContainer,
                    tint = iconTint,
                    shape = RoundedCornerShape(OneBoxDesignSystem.smallRadius),
                    iconSizeRatio = 0.6f,
                )

                Text(
                    text = frequencyLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    ),
                    color = onContainerColor.copy(alpha = 0.7f)
                )
            }

            // Middle: name + count + label
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = eventName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = onContainerColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$count $unit",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = onContainerColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelSmall,
                    color = onContainerColor.copy(alpha = 0.6f)
                )
            }

            // Bottom: progress bar
            if (progress != null) {
                val safeProgress = progress.coerceIn(0f, 1f)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(onContainerColor.copy(alpha = 0.18f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(safeProgress)
                                .background(onContainerColor.copy(alpha = 0.85f))
                        )
                    }
                    Text(
                        text = stringResource(R.string.lifetime_frequency_progress, (safeProgress * 100).toInt()),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = onContainerColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private const val CARD_ASPECT_RATIO: Float = 3f / 4f
