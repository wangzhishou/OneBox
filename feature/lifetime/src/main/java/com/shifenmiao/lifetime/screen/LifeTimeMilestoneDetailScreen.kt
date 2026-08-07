package com.shifenmiao.lifetime.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconAvatar
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.component.LifeTimeMilestoneDetailComponent
import com.shifenmiao.lifetime.domain.PersonalMilestoneCalculator
import com.shifenmiao.lifetime.ui.NoteSection
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic

@Composable
fun LifeTimeMilestoneDetailScreen(
    component: LifeTimeMilestoneDetailComponent
) {
    val uiState by component.uiState.collectAsState()
    val milestone = uiState.milestone

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        ExitWithoutSavingDialog(
            title = stringResource(R.string.lifetime_delete_confirm_title),
            text = stringResource(R.string.lifetime_delete_confirm_message),
            onExit = {
                showDeleteConfirm = false
                component.deleteMilestone()
            },
            onDismiss = { showDeleteConfirm = false },
            visible = showDeleteConfirm
        )
    }

    BaseScreen(
        title = milestone?.name ?: stringResource(R.string.lifetime_edit_milestone),
        onGoBack = { component.onGoBack() },
        actions = {
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.lifetime_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    ) {
        if (milestone == null) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.lifetime_loading),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(
                        horizontal = OneBoxDesignSystem.screenPadding,
                        vertical = OneBoxDesignSystem.compactSpacing
                    ),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.sectionSpacing)
            ) {
                // Header card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = OneBoxDesignSystem.sectionCardShape,
                    containerAlpha = 0.22f,
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(OneBoxDesignSystem.cardPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
                    ) {
                        IconAvatar(
                            iconName = milestone.iconKey,
                            size = 64.dp,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(OneBoxDesignSystem.smallRadius),
                            iconSizeRatio = 0.6f,
                        )
                        Text(
                            text = milestone.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val daysUntil = milestone.targetDate?.let {
                            ChronoUnit.DAYS.between(LocalDate.now(), it)
                        }
                        val daysText = when {
                            daysUntil == null -> ""
                            daysUntil > 0 -> stringResource(R.string.lifetime_milestone_days_until, daysUntil)
                            daysUntil == 0L -> stringResource(R.string.lifetime_milestone_days_today)
                            else -> stringResource(R.string.lifetime_milestone_days_past, -daysUntil)
                        }
                        if (daysText.isNotBlank()) {
                            Text(
                                text = daysText,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = milestone.targetDate?.let {
                                stringResource(R.string.lifetime_date_ymd, it.year, it.monthValue, it.dayOfMonth)
                            } ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Current AI insight
                CurrentInsightCard(
                    isLoading = uiState.isLoadingInsight,
                    currentInsight = uiState.currentInsight,
                    onRefresh = { component.refreshInsight() }
                )

                // Insight history
                InsightHistoryCard(
                    items = uiState.insights,
                    onDelete = component::deleteInsight
                )

                NoteSection(
                    note = milestone.note,
                    onSave = { component.updateNote(it) },
                    onDelete = { component.updateNote("") },
                )

                Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
            }
        }
    }
}

@Composable
private fun CurrentInsightCard(
    isLoading: Boolean,
    currentInsight: String,
    onRefresh: () -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = OneBoxDesignSystem.sectionCardShape,
        containerAlpha = 0.22f,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.cardPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.lifetime_insight_current_title),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                TextButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    } else {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.lifetime_insight_refresh),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
            if (currentInsight.isBlank() && !isLoading) {
                Text(
                    text = stringResource(R.string.lifetime_insight_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                )
            } else if (isLoading && currentInsight.isBlank()) {
                Text(
                    text = stringResource(R.string.lifetime_insight_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                )
            } else {
                Text(
                    text = currentInsight,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun InsightHistoryCard(
    items: List<com.shifenmiao.database.lifetime.entity.MilestoneAiInsightEntity>,
    onDelete: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
        OneBoxSectionHeader(
            title = stringResource(R.string.lifetime_insight_history_title)
        )
        if (items.isEmpty()) {
            Text(
                text = stringResource(R.string.lifetime_insight_history_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = OneBoxDesignSystem.compactSpacing)
            )
        } else {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = OneBoxDesignSystem.sectionCardShape,
                containerAlpha = 0.22f,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    contentPadding = PaddingValues(OneBoxDesignSystem.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    items(items, key = { it.id }) { insight ->
                        InsightHistoryItem(
                            content = insight.content,
                            generatedAtMillis = insight.generatedAt,
                            onDelete = { onDelete(insight.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightHistoryItem(
    content: String,
    generatedAtMillis: Long,
    onDelete: () -> Unit,
) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    val dateText = remember(generatedAtMillis) {
        Instant.ofEpochMilli(generatedAtMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.lifetime_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
