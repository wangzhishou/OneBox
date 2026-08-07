package com.shifenmiao.lifetime.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.domain.LifeTimeData
import com.shifenmiao.lifetime.domain.RemainingLifeData
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem

enum class TimeDisplayMode {
    PAST, REMAINING
}

@Composable
fun MainTimeCard(
    pastTimeData: LifeTimeData,
    remainingLifeData: RemainingLifeData,
    displayMode: TimeDisplayMode,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val yearsLabel = stringResource(R.string.lifetime_unit_years)
    val daysLabel = stringResource(R.string.lifetime_unit_days)
    val hoursLabel = stringResource(R.string.lifetime_unit_hours)
    val minutesLabel = stringResource(R.string.lifetime_unit_minutes)
    val secondsLabel = stringResource(R.string.lifetime_unit_seconds)

    GlassCard(
        modifier = modifier.clickable(onClick = onToggleMode),
        shape = OneBoxDesignSystem.sectionCardShape,
        containerAlpha = 0.22f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = OneBoxDesignSystem.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing, Alignment.CenterVertically)
        ) {
            AnimatedContent(
                targetState = displayMode,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) +
                        slideInVertically { it / 2 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(200)) +
                                slideOutVertically { -it / 2 }
                        )
                },
                label = "time_display_mode"
            ) { mode ->
                val labelRes = when (mode) {
                    TimeDisplayMode.PAST -> R.string.lifetime_time_card_label
                    TimeDisplayMode.REMAINING -> R.string.lifetime_remaining_time_label
                }

                val years: Long
                val days: Long
                val hours: Long
                val minutes: Long
                val seconds: Long

                when (mode) {
                    TimeDisplayMode.PAST -> {
                        years = pastTimeData.years
                        days = pastTimeData.totalDays
                        hours = pastTimeData.hours % 24
                        minutes = pastTimeData.minutes % 60
                        seconds = pastTimeData.seconds % 60
                    }
                    TimeDisplayMode.REMAINING -> {
                        years = remainingLifeData.years
                        days = remainingLifeData.days
                        hours = remainingLifeData.hours % 24
                        minutes = remainingLifeData.minutes % 60
                        seconds = remainingLifeData.seconds % 60
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)
                ) {
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))

                    TimeDimensionRow(
                        value = years,
                        unit = yearsLabel,
                        fontSize = 44.sp
                    )
                    TimeDimensionRow(
                        value = days,
                        unit = daysLabel,
                        fontSize = 32.sp
                    )
                    TimeDimensionRow(
                        value = hours,
                        unit = hoursLabel,
                        fontSize = 24.sp
                    )
                    TimeDimensionRow(
                        value = minutes,
                        unit = minutesLabel,
                        fontSize = 18.sp
                    )
                    TimeDimensionRow(
                        value = seconds,
                        unit = secondsLabel,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeDimensionRow(
    value: Long,
    unit: String,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = fontSize * 0.5f,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}
