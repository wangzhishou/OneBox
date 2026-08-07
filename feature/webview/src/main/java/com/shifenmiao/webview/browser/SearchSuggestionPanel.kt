package com.shifenmiao.webview.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import coil3.compose.SubcomposeAsyncImage
import com.shifenmiao.webview.R
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBookmark

@Composable
fun SearchSuggestionPanel(
    query: String,
    history: List<HistoryItem>,
    bookmarks: List<BookmarkItem>,
    onItemClick: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (historyItems, bookmarkItems) = remember(query, history, bookmarks) {
        val q = query.lowercase()
        val historyMatches = if (q.isBlank()) {
            history.take(10)
        } else {
            history.filter {
                it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
            }.take(10)
        }
        val bookmarkMatches = if (q.isBlank()) {
            emptyList()
        } else {
            bookmarks.filter {
                it.title.lowercase().contains(q) || it.url.lowercase().contains(q)
            }.take(5)
        }
        historyMatches to bookmarkMatches
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onDismiss() }
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            containerAlpha = 1.0f,
            borderWidth = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (bookmarkItems.isNotEmpty()) {
                    SectionLabel(stringResource(R.string.browser_bookmarks))
                    bookmarkItems.forEach { item ->
                        SuggestionItemRow(
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBookmark,
                            faviconUrl = item.favicon,
                            title = item.title.ifEmpty { item.url },
                            subtitle = item.url,
                            onClick = { onItemClick(item.url) }
                        )
                    }
                    if (historyItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                if (historyItems.isNotEmpty()) {
                    SectionLabel(stringResource(if (query.isBlank()) R.string.browser_history_records else R.string.browser_history))
                    historyItems.forEach { item ->
                        SuggestionItemRow(
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                            faviconUrl = item.favicon,
                            title = item.title.ifEmpty { item.url },
                            subtitle = item.url,
                            onClick = { onItemClick(item.url) }
                        )
                    }
                }

                if (bookmarkItems.isEmpty() && historyItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.browser_no_matches),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun SuggestionItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    faviconUrl: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubcomposeAsyncImage(
            model = faviconUrl.takeIf { it.isNotEmpty() },
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            loading = { FallbackSuggestionIcon(icon) },
            error = { FallbackSuggestionIcon(icon) }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = { Clipboard.copy(subtitle) },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                contentDescription = stringResource(R.string.browser_copy_link),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FallbackSuggestionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
