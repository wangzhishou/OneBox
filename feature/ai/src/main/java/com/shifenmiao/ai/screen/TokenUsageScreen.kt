package com.shifenmiao.ai.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.component.TokenUsageComponent
import com.shifenmiao.ai.component.TokenUsageUiState
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.handle.AIConversationNavigation
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.dao.ModelUsageStat
import com.shifenmiao.database.ai.dao.TopQueryStat
import com.shifenmiao.database.ai.dao.TokenUsageSummary
import com.shifenmiao.model.ai.AIConversationEntryType
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxGroupDivider
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxListItem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInsights
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTrendingUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQueryStats

@Composable
fun TokenUsageScreen(
    component: TokenUsageComponent
) {
    val uiState by component.uiState.collectAsState()
    val onNavigate = LocalOnNavigate.current

    BaseScreen(
        title = stringResource(R.string.profile_item_ai_usage),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
    ) {
        when (val state = uiState) {
            is TokenUsageUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is TokenUsageUiState.Empty -> {
                TokenUsageEmptyState()
            }

            is TokenUsageUiState.Error -> {
                TokenUsageErrorState(
                    message = state.message,
                    onRetry = { component.loadData() }
                )
            }

            is TokenUsageUiState.Content -> {
                TokenUsageContent(
                    summary = state.summary,
                    modelStats = state.modelStats,
                    topQueries = state.topQueries,
                    onOpenQuery = { query ->
                        if (query.conversationId.isBlank()) return@TokenUsageContent
                        onNavigate(
                            AIConversationNavigation.buildHistoryDetailScreen(
                                conversationId = query.conversationId,
                                title = query.title,
                                entryType = query.resolveEntryType(),
                                entryRefId = query.entryRefId,
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TokenUsageContent(
    summary: TokenUsageSummary,
    modelStats: List<ModelUsageStat>,
    topQueries: List<TopQueryStat>,
    onOpenQuery: (TopQueryStat) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OneBoxDesignSystem.screenPadding),
    ) {
        Spacer(modifier = Modifier.height(OneBoxDesignSystem.screenTopSpacing))

        // Hero Summary
        HeroSummaryCard(summary = summary)

        Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))

        // Model Distribution
        if (modelStats.isNotEmpty()) {
            ModelDistributionSection(
                modelStats = modelStats,
                totalTokens = summary.totalTokens
            )
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))
        }

        // Top Queries
        if (topQueries.isNotEmpty()) {
            TopQueriesSection(
                topQueries = topQueries,
                onOpenQuery = onOpenQuery,
            )
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))
        }
    }
}

@Composable
private fun HeroSummaryCard(summary: TokenUsageSummary) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = OneBoxDesignSystem.sectionCardShape,
        containerAlpha = 0.28f,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
    ) {
        Column(
            modifier = Modifier.padding(OneBoxDesignSystem.cardPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInsights,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.token_usage_total_consumption),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = StringUtils.formatNumber(summary.totalTokens),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
            ) {
                SummaryStatItem(
                    modifier = Modifier.weight(1f),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTrendingUp,
                    label = stringResource(R.string.token_usage_prompt_tokens),
                    value = StringUtils.formatNumber(summary.promptTokens)
                )
                SummaryStatItem(
                    modifier = Modifier.weight(1f),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBarChart,
                    label = stringResource(R.string.token_usage_completion_tokens),
                    value = StringUtils.formatNumber(summary.completionTokens)
                )
                SummaryStatItem(
                    modifier = Modifier.weight(1f),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQueryStats,
                    label = stringResource(R.string.token_usage_request_count),
                    value = StringUtils.formatNumber(summary.requestCount)
                )
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ModelDistributionSection(
    modelStats: List<ModelUsageStat>,
    totalTokens: Long
) {
    OneBoxSectionCard {
        Text(
            text = stringResource(R.string.token_usage_model_distribution),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = OneBoxDesignSystem.compactSpacing)
        )

        val maxTokens = remember(modelStats) {
            modelStats.maxOfOrNull { it.totalTokens } ?: 1L
        }

        modelStats.forEachIndexed { index, stat ->
            ModelDistributionItem(
                stat = stat,
                totalTokens = totalTokens,
                progress = if (maxTokens > 0) stat.totalTokens.toFloat() / maxTokens.toFloat() else 0f
            )
            if (index < modelStats.lastIndex) {
                OneBoxGroupDivider()
            }
        }
    }
}

@Composable
private fun ModelDistributionItem(
    stat: ModelUsageStat,
    totalTokens: Long,
    progress: Float
) {
    val displayName = remember(stat.model, stat.engine) {
        when {
            stat.model.isNotBlank() && stat.engine.isNotBlank() -> "${stat.engine} · ${stat.model}"
            stat.model.isNotBlank() -> stat.model
            stat.engine.isNotBlank() -> stat.engine
            else -> "Default Model"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = OneBoxDesignSystem.compactSpacing),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(OneBoxDesignSystem.compactSpacing))
            Text(
                text = StringUtils.formatNumber(stat.totalTokens),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${stat.requestCount} ${stringResource(R.string.token_usage_request_count)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            if (totalTokens > 0) {
                val percent = (stat.totalTokens * 100 / totalTokens).toInt()
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun TopQueriesSection(
    topQueries: List<TopQueryStat>,
    onOpenQuery: (TopQueryStat) -> Unit,
) {
    OneBoxSectionCard {
        Text(
            text = stringResource(R.string.token_usage_top_queries),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = OneBoxDesignSystem.compactSpacing)
        )

        topQueries.forEachIndexed { index, query ->
            TopQueryItem(
                rank = index + 1,
                query = query,
                onClick = { onOpenQuery(query) },
            )
            if (index < topQueries.lastIndex) {
                OneBoxGroupDivider()
            }
        }
    }
}

@Composable
private fun TopQueryItem(
    rank: Int,
    query: TopQueryStat,
    onClick: () -> Unit,
) {
    val defaultModelLabel = stringResource(R.string.token_usage_default_model)
    val displayModel = remember(query.model, query.engine, defaultModelLabel) {
        when {
            query.model.isNotBlank() -> query.model
            query.engine.isNotBlank() -> query.engine
            else -> defaultModelLabel
        }
    }

    OneBoxListItem(
        onClick = if (query.conversationId.isNotBlank()) onClick else null,
        headlineContent = {
            Text(
                text = query.question,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        subtitle = {
            Text(
                text = displayModel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        leadingContent = {
            RankBadge(rank = rank)
        },
        trailingContent = {
            Text(
                text = "${StringUtils.formatNumber(query.totalTokens)} ${stringResource(R.string.token_usage_tokens_label)}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    )
}

private fun TopQueryStat.resolveEntryType(): AIConversationEntryType {
    return runCatching { AIConversationEntryType.valueOf(entryType) }
        .getOrDefault(AIConversationEntryType.CHAT)
}

@Composable
private fun RankBadge(rank: Int) {
    val contentColor = when (rank) {
        1 -> MaterialTheme.colorScheme.onPrimaryContainer
        2 -> MaterialTheme.colorScheme.onSecondaryContainer
        3 -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TokenUsageEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInsights,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Text(
                text = stringResource(R.string.token_usage_empty_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.token_usage_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = OneBoxDesignSystem.screenPadding)
            )
        }
    }
}

@Composable
private fun TokenUsageErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )
            Text(
                text = stringResource(R.string.token_usage_error_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = message.takeIf { it.isNotBlank() } ?: stringResource(R.string.token_usage_error_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = OneBoxDesignSystem.screenPadding)
            )
            OneSecondaryButton(
                text = stringResource(R.string.button_retry),
                onClick = onRetry,
                leadingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh
            )
        }
    }
}
