package com.wanbaohe.diceroller.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import com.wanbaohe.diceroller.R
import com.wanbaohe.diceroller.component.DiceType

/**
 * 骰子类型选择行
 *
 * 横向展示 D4 / D6 / D8 / D10 / D12 / D20 六种骰子类型 Chip。
 * 选中态有颜色高亮 + 轻微放大效果。
 */
@Composable
fun DiceTypeSelector(
    selected: DiceType,
    onSelect: (DiceType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DiceType.entries.forEach { type ->
            DiceTypeChip(
                label = type.label,
                isSelected = type == selected,
                onClick = { onSelect(type) }
            )
        }
    }
}

@Composable
private fun DiceTypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "chip_color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chip_content_color"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 0.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "chip_elevation"
    )

    AnimatedVisibility(
        visible = true,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 36.dp)
                .glassThin(
                    shape = RoundedCornerShape(12.dp),
                    color = containerColor,
                    borderWidth = 0.dp
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

// ─── 骰子数量选择行 ────────────────────────────────────────────────────────────

/**
 * 1~[maxCount] 骰子数量选择器
 */
@Composable
fun DiceCountSelector(
    count: Int,
    maxCount: Int = 6,
    onCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..maxCount).forEach { n ->
            val isSelected = n == count
            val containerColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                label = "count_chip_color_$n"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.onSecondary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                label = "count_chip_content_color_$n"
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .glassThin(
                        shape = RoundedCornerShape(50),
                        color = containerColor,
                        borderWidth = 0.dp
                    )
                    .clickable(onClick = { onCountChange(n) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = n.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

