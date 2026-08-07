package com.shifenmiao.marquee.screen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.core.R
import com.shifenmiao.model.marquee.MarqueePresentationMode
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHourglass
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoStories
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboard
import com.t8rin.imagetoolbox.core.resources.icons.line.LineOpenWith

/**
 * 展示模式信息
 */
data class PresentationModeInfo(
    val mode: MarqueePresentationMode,
    val label: String,
    val icon: ImageVector,
    val description: String
)

/**
 * 获取所有展示模式信息
 */
@Composable
fun rememberPresentationModes(): List<PresentationModeInfo> {
    val classicLabel = stringResource(id = R.string.marquee_mode_classic)
    val classicDesc = stringResource(id = R.string.marquee_mode_classic_desc)
    val typewriterLabel = stringResource(id = R.string.marquee_mode_typewriter)
    val typewriterDesc = stringResource(id = R.string.marquee_mode_typewriter_desc)
    val oneCharLabel = stringResource(id = R.string.marquee_mode_one_char)
    val oneCharDesc = stringResource(id = R.string.marquee_mode_one_char_desc)
    val pagedLabel = stringResource(id = R.string.marquee_mode_paged)
    val pagedDesc = stringResource(id = R.string.marquee_mode_paged_desc)
    val clockLabel = stringResource(id = R.string.marquee_mode_clock)
    val clockDesc = stringResource(id = R.string.marquee_mode_clock_desc)
    val countdownLabel = stringResource(id = R.string.marquee_mode_countdown)
    val countdownDesc = stringResource(id = R.string.marquee_mode_countdown_desc)
    val bouncingLabel = stringResource(id = R.string.marquee_mode_bouncing)
    val bouncingDesc = stringResource(id = R.string.marquee_mode_bouncing_desc)

    return remember(
        classicLabel, classicDesc,
        typewriterLabel, typewriterDesc,
        oneCharLabel, oneCharDesc,
        pagedLabel, pagedDesc,
        clockLabel, clockDesc,
        countdownLabel, countdownDesc,
        bouncingLabel, bouncingDesc
    ) {
        listOf(
            PresentationModeInfo(
                mode = MarqueePresentationMode.ClassicMarquee,
                label = classicLabel,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                description = classicDesc
            ),
            PresentationModeInfo(
                mode = MarqueePresentationMode.Typewriter,
                label = typewriterLabel,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboard,
                description = typewriterDesc
            ),
            PresentationModeInfo(
                mode = MarqueePresentationMode.OneCharPerScreen,
                label = oneCharLabel,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText,
                description = oneCharDesc
            ),
            PresentationModeInfo(
                mode = MarqueePresentationMode.Paged,
                label = pagedLabel,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoStories,
                description = pagedDesc
            ),
            PresentationModeInfo(
                mode = MarqueePresentationMode.Clock,
                label = clockLabel,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime,
                description = clockDesc
            ),
            PresentationModeInfo(
                mode = MarqueePresentationMode.Countdown,
                label = countdownLabel,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHourglass,
                description = countdownDesc
            ),
            PresentationModeInfo(
                mode = MarqueePresentationMode.Bouncing,
                label = bouncingLabel,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineOpenWith,
                description = bouncingDesc
            )
        )
    }
}

/**
 * 带视觉冲击力的模式选择器
 */
@Composable
fun PresentationModeSelector(
    selectedMode: MarqueePresentationMode,
    onModeSelected: (MarqueePresentationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = rememberPresentationModes()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        itemsIndexed(modes) { _, modeInfo ->
            PresentationModeCard(
                modeInfo = modeInfo,
                isSelected = modeInfo.mode == selectedMode,
                onClick = { onModeSelected(modeInfo.mode) }
            )
        }
    }
}

@Composable
private fun PresentationModeCard(
    modeInfo: PresentationModeInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "elevation"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "backgroundColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "contentColor"
    )

    val iconBackgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "iconBg"
    )

    val shape = MaterialTheme.shapes.medium

    GlassSurface(
        modifier = modifier
            .width(90.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        style = if (isSelected) GlassStyle.Medium else GlassStyle.Regular,
        color = backgroundColor,
        shape = shape,
        borderWidth = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 图标容器
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .glassBackground(
                        style = GlassStyle.Thin,
                        color = iconBackgroundColor,
                        shape = CircleShape,
                        borderWidth = 0.dp,
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = modeInfo.icon,
                    contentDescription = modeInfo.label,
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 标题
            Text(
                text = modeInfo.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // 描述
            Text(
                text = modeInfo.description,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
