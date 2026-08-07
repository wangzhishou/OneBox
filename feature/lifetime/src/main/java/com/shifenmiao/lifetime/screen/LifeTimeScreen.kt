package com.shifenmiao.lifetime.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.component.LifeTimeComponent
import com.shifenmiao.lifetime.domain.model.CountdownEvent
import com.shifenmiao.lifetime.domain.model.FrequencyEvent
import com.shifenmiao.lifetime.domain.model.FrequencyType
import com.shifenmiao.lifetime.domain.model.MilestoneStatus
import com.shifenmiao.lifetime.domain.model.PersonalMilestone
import com.shifenmiao.lifetime.ui.CountdownEventCard
import com.shifenmiao.lifetime.ui.FrequencyEventCard
import com.shifenmiao.lifetime.ui.HourglassHeroCard
import com.shifenmiao.lifetime.ui.LifeProgressCard
import com.shifenmiao.lifetime.ui.PersonalMilestoneCard
import com.shifenmiao.lifetime.ui.TimeDisplayMode
import com.shifenmiao.lifetime.util.localizedEventUnit
import com.shifenmiao.lifetime.util.localizedPresetEventName
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEventAvailable
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEventRepeat

@Composable
fun LifeTimeScreen(
    component: LifeTimeComponent
) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(R.string.lifetime_title),
        onGoBack = { component.onGoBack() },
        actions = {
            IconButton(
                onClick = { component.navigateToSettings() },
                colors = AppTheme.colors.iconButtonColors()
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                    contentDescription = stringResource(R.string.lifetime_settings)
                )
            }
        },
        foreground = {
            LifetimeFabMenu(
                onAddMilestone = component::navigateToAddMilestone,
                onAddEvent = component::navigateToAddEvent,
                onAddCountdown = component::navigateToAddCountdown,
            )
        }
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.lifetime_loading),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            if (uiState.isDemoMode) {
                DemoBanner(
                    onClick = { component.navigateToSettings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = OneBoxDesignSystem.screenPadding,
                            vertical = OneBoxDesignSystem.compactSpacing
                        )
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = OneBoxDesignSystem.screenPadding,
                    vertical = OneBoxDesignSystem.compactSpacing
                ),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
            ) {
                item {
                    HourglassHeroCard(
                        pastTimeData = uiState.pastTimeData,
                        remainingLifeData = uiState.remainingLifeData,
                        displayMode = uiState.timeDisplayMode,
                        onToggleMode = { component.toggleTimeDisplayMode() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    LifeProgressCard(
                        remainingLifeData = uiState.remainingLifeData,
                        targetAge = uiState.targetAge,
                        isExpanded = uiState.isLifeProgressExpanded,
                        onToggleExpand = { component.toggleLifeProgressExpanded() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    MilestonesSection(
                        milestones = uiState.personalMilestones,
                        onAddMilestone = { component.navigateToAddMilestone() },
                        onMilestoneClick = { component.navigateToMilestoneDetail(it.milestone.id) },
                        onDeleteMilestone = { component.deletePersonalMilestone(it) }
                    )
                }

                item {
                    CountdownSection(
                        countdowns = uiState.countdowns,
                        onAddCountdown = { component.navigateToAddCountdown() },
                        onCountdownClick = { component.navigateToCountdownDetail(it.event.id) },
                        onDeleteCountdown = { component.deleteCountdown(it) },
                    )
                }

                item {
                    FrequencyEventsSection(
                        eventStats = uiState.frequencyEventStats,
                        onAddEvent = { component.navigateToAddEvent() }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun DemoBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = OneBoxDesignSystem.sectionCardShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = OneBoxDesignSystem.blockSpacing,
                    vertical = OneBoxDesignSystem.compactSpacing
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.lifetime_demo_hint),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.lifetime_demo_action),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BoxScope.LifetimeFabMenu(
    onAddMilestone: () -> Unit,
    onAddEvent: () -> Unit,
    onAddCountdown: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val scrimAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "lifetime_fab_scrim"
    )
    val fabRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "lifetime_fab_rotation"
    )
    val fabScale by animateFloatAsState(
        targetValue = if (expanded) 0.92f else 1f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "lifetime_fab_scale"
    )

    BackHandler(enabled = expanded) {
        expanded = false
    }

    if (expanded || scrimAlpha > 0.01f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f * scrimAlpha))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { expanded = false }
                )
        )
    }

    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .navigationBarsPadding()
            .padding(end = 20.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(180, delayMillis = 40)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(220, delayMillis = 20, easing = FastOutSlowInEasing)
                ) +
                scaleIn(
                    initialScale = 0.84f,
                    animationSpec = tween(220, delayMillis = 20, easing = FastOutSlowInEasing)
                ),
            exit = fadeOut(animationSpec = tween(140)) +
                slideOutVertically(
                    targetOffsetY = { it / 3 },
                    animationSpec = tween(160, easing = FastOutSlowInEasing)
                ) +
                scaleOut(
                    targetScale = 0.84f,
                    animationSpec = tween(160, easing = FastOutSlowInEasing)
                ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                SpeedDialAction(
                    label = stringResource(R.string.lifetime_action_countdown),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEventAvailable,
                    onClick = {
                        expanded = false
                        onAddCountdown()
                    },
                )
                SpeedDialAction(
                    label = stringResource(R.string.lifetime_action_milestones),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                    onClick = {
                        expanded = false
                        onAddMilestone()
                    },
                )
                SpeedDialAction(
                    label = stringResource(R.string.lifetime_action_frequency_events),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEventRepeat,
                    onClick = {
                        expanded = false
                        onAddEvent()
                    },
                )
            }
        }

        FloatingActionButton(
            modifier = Modifier.graphicsLayer {
                scaleX = fabScale
                scaleY = fabScale
            },
            onClick = { expanded = !expanded },
            containerColor = AppTheme.colors.getPrimaryColor(),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                contentDescription = stringResource(if (expanded) R.string.lifetime_cancel else R.string.lifetime_add),
                modifier = Modifier.graphicsLayer { rotationZ = fabRotation },
            )
        }
    }
}

@Composable
private fun SpeedDialAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun MilestonesSection(
    milestones: List<MilestoneStatus>,
    onAddMilestone: () -> Unit,
    onMilestoneClick: (MilestoneStatus) -> Unit,
    onDeleteMilestone: (PersonalMilestone) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
        OneBoxSectionHeader(
            title = stringResource(R.string.lifetime_milestone_section),
            modifier = Modifier.fillMaxWidth()
        )

        if (milestones.isEmpty()) {
            EmptyMilestoneCard(onAddMilestone)
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
                contentPadding = PaddingValues(horizontal = OneBoxDesignSystem.microSpacing)
            ) {
                itemsIndexed(milestones, key = { _, it -> it.milestone.id }) { index, status ->
                    PersonalMilestoneCard(
                        status = status,
                        onClick = { onMilestoneClick(status) },
                        themeIndex = index,
                        modifier = Modifier.width(140.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CountdownSection(
    countdowns: List<com.shifenmiao.lifetime.domain.model.CountdownStatus>,
    onAddCountdown: () -> Unit,
    onCountdownClick: (com.shifenmiao.lifetime.domain.model.CountdownStatus) -> Unit,
    onDeleteCountdown: (CountdownEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
        OneBoxSectionHeader(
            title = stringResource(R.string.lifetime_countdown_section),
            modifier = Modifier.fillMaxWidth()
        )

        if (countdowns.isEmpty()) {
            EmptyCountdownCard(onAddCountdown)
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
                contentPadding = PaddingValues(horizontal = OneBoxDesignSystem.microSpacing)
            ) {
                itemsIndexed(countdowns, key = { _, it -> it.event.id }) { index, status ->
                    CountdownEventCard(
                        status = status,
                        onClick = { onCountdownClick(status) },
                        themeIndex = index,
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMilestoneCard(onClick: () -> Unit) {
    OneBoxSectionCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = OneBoxDesignSystem.blockSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.NoteAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.lifetime_empty_milestones_title),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.lifetime_empty_milestones_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyCountdownCard(onClick: () -> Unit) {
    OneBoxSectionCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = OneBoxDesignSystem.blockSpacing)
        ) {
            Text(
                text = stringResource(R.string.lifetime_empty_countdown_title),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))
            Text(
                text = stringResource(R.string.lifetime_empty_countdown_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FrequencyEventsSection(
    eventStats: List<com.shifenmiao.lifetime.domain.model.FrequencyEventStats>,
    onAddEvent: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
        OneBoxSectionHeader(
            title = stringResource(R.string.lifetime_frequency_section),
            modifier = Modifier.fillMaxWidth()
        )

        if (eventStats.isEmpty()) {
            EmptyEventsCard(onAddEvent)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)) {
                eventStats.chunked(2).forEach { rowStats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
                    ) {
                        rowStats.forEach { stats ->
                            val themeIndex = eventStats.indexOf(stats)
                            val totalCount = stats.completedCount + stats.remainingCount
                            val progress = if (totalCount > 0) {
                                stats.completedCount.toFloat() / totalCount.toFloat()
                            } else null

                            FrequencyEventCard(
                                iconKey = stats.event.iconKey,
                                eventName = localizedPresetEventName(stats.event.name, stats.event.isPreset),
                                completedCount = formatCount(stats.completedCount),
                                remainingCount = formatCount(stats.remainingCount),
                                unit = localizedEventUnit(stats.event.unit),
                                frequencyLabel = buildFrequencyLabel(stats.event),
                                progress = progress,
                                themeIndex = themeIndex,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowStats.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyEventsCard(onClick: () -> Unit) {
    OneBoxSectionCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = OneBoxDesignSystem.blockSpacing)
        ) {
            Text(
                text = stringResource(R.string.lifetime_empty_events_title),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))
            Text(
                text = stringResource(R.string.lifetime_empty_events_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun formatCount(count: Long): String {
    // 中文习惯用 万/亿 缩写大数，其他语言直接用千分位
    val isChinese = LocalConfiguration.current.locales[0]?.language == Locale.CHINESE.language
    return when {
        isChinese && count >= 100000000 -> "${String.format("%.1f", count / 100000000.0)}亿"
        isChinese && count >= 10000 -> "${String.format("%.1f", count / 10000.0)}万"
        count >= 1000 -> String.format("%,d", count)
        else -> count.toString()
    }
}

@Composable
private fun buildFrequencyLabel(event: FrequencyEvent): String {
    val period = when (event.frequencyType) {
        FrequencyType.ONE_TIME -> stringResource(R.string.lifetime_frequency_one_time)
        FrequencyType.DAILY -> stringResource(R.string.lifetime_frequency_daily)
        FrequencyType.WEEKLY -> stringResource(R.string.lifetime_frequency_weekly)
        FrequencyType.MONTHLY -> stringResource(R.string.lifetime_frequency_monthly)
        FrequencyType.YEARLY -> stringResource(R.string.lifetime_frequency_yearly)
    }
    return stringResource(
        R.string.lifetime_frequency_value_label,
        period,
        event.timesPerPeriod,
        localizedEventUnit(event.unit)
    )
}
