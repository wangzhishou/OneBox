package com.shifenmiao.ai.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shifenmiao.ai.model.MessageUiModel
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.SearchCitation
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore

/**
 * 搜索结果展示组件
 */
@Composable
fun RobotSearchResultsBlock(
    searchResults: MessageUiModel.RobotSearchResults,
    modifier: Modifier = Modifier
) {
    val searchResult = searchResults.searchResult
    var isExpanded by rememberSaveable { mutableStateOf(searchResults.isExpanded) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(
                vertical = AppTheme.dimens.paddingNormal,
                horizontal = AppTheme.dimens.spaceNormal
            ).padding(top = 0.dp),
    ) {
        // 搜索结果头部（可点击展开/折叠）
        SearchResultsHeader(
            citationCount = searchResult.citations.size,
            query = searchResult.query,
            isExpanded = isExpanded,
            onToggle = { isExpanded = !isExpanded }
        )

        // 折叠状态下显示横向滚动的来源预览
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            SearchResultsPreview(citations = searchResult.citations)
        }

        // 展开状态下显示完整的来源列表
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            SearchResultsExpanded(citations = searchResult.citations)
        }
    }
}

@Composable
private fun SearchResultsHeader(
    citationCount: Int,
    query: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(
                bottom = AppTheme.dimens.paddingTooSmall
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.ai_search_sources, citationCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (query.isNotBlank()) {
                Text(
                    text = "「$query」",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }

        Icon(
            imageVector = if (isExpanded) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SearchResultsPreview(
    citations: List<SearchCitation>
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppTheme.dimens.paddingTooSmall),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(citations.take(5)) { citation ->
            SearchCitationChip(citation = citation)
        }

        if (citations.size > 5) {
            item {
                MoreCitationsChip(count = citations.size - 5)
            }
        }
    }
}

@Composable
private fun SearchCitationChip(
    citation: SearchCitation
) {
    val navigator = LocalUrlNavigator.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable {
                if (citation.url.isNotBlank()) {
                    navigator.navigate(citation.url)
                }
            }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Favicon
        if (citation.favicon.isNotBlank()) {
            AsyncImage(
                model = citation.favicon,
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.web_traffic),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 序号
        Text(
            text = "[${citation.index}]",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )

        // 标题或域名
        Text(
            text = citation.hostname.ifBlank { citation.title }.take(15),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MoreCitationsChip(count: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "+$count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SearchResultsExpanded(
    citations: List<SearchCitation>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppTheme.dimens.paddingSmall),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        citations.forEach { citation ->
            SearchCitationCard(citation = citation)
        }
    }
}

@Composable
private fun SearchCitationCard(
    citation: SearchCitation
) {
    val navigator = LocalUrlNavigator.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (citation.url.isNotBlank()) {
                    navigator.navigate(citation.url)
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 头部：序号 + Favicon + 域名 + 打开链接
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 序号
                    Text(
                        text = "[${citation.index}]",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Favicon
                    if (citation.favicon.isNotBlank()) {
                        AsyncImage(
                            model = citation.favicon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // 域名
                    Text(
                        text = citation.hostname,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 标题
            Text(
                text = citation.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // 摘要
            if (citation.snippet.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = citation.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 发布日期
            if (citation.publishedDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = citation.publishedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

