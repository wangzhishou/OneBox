package com.shifenmiao.lifetime.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.modifier.clickableWithoutRipple
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.domain.RemainingLifeData
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore

@Composable
fun LifeProgressCard(
    remainingLifeData: RemainingLifeData,
    targetAge: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = remainingLifeData.progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "life_progress"
    )
    val progressPercent = (animatedProgress * 100).toInt()
    val remainingYears = remainingLifeData.years

    GlassCard(
        modifier = modifier,
        shape = OneBoxDesignSystem.sectionCardShape,
        containerAlpha = 0.22f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(OneBoxDesignSystem.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
                ) {
                    Text(
                        text = stringResource(R.string.lifetime_life_journey),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    modifier = Modifier.clickableWithoutRipple { onToggleExpand() },
                    imageVector = if (isExpanded) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.itemSpacing))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(R.string.lifetime_progress_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )

                Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))

                Text(
                    text = stringResource(
                        R.string.lifetime_remaining_estimate,
                        remainingYears,
                        targetAge
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
