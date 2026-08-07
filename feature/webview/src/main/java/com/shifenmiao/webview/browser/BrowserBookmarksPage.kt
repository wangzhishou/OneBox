package com.shifenmiao.webview.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.category.CategoryManagementDialog
import com.shifenmiao.common.components.category.ManageableItem
import com.shifenmiao.model.Source
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import androidx.compose.ui.res.stringResource
import com.shifenmiao.webview.R
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBookmark
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePublic

private data class BookmarkFolderItem(
    val folder: BookmarkFolder
) : ManageableItem {
    override val id: Int get() = folder.id.hashCode()
    override val name: String get() = folder.name
    override val order: Int get() = folder.order
    override val canEdit: Boolean? get() = true
    override val source: Source get() = Source.LOCAL
}

@Composable
fun BrowserBookmarksPage(component: BrowserComponent) {
    val state by component.state.collectAsState()
    var selectedSegment by remember { mutableStateOf(0) }
    var showFolderManager by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.browser_bookmarks_history),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (selectedSegment == 0) {
                IconButton(onClick = { showFolderManager = true }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
                        contentDescription = stringResource(R.string.browser_manage_categories),
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SegmentedToggle(
            options = listOf(stringResource(R.string.browser_bookmarks), stringResource(R.string.browser_history)),
            selectedIndex = selectedSegment,
            onSelect = { selectedSegment = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedSegment) {
            0 -> BookmarksList(
                state = state,
                onClick = { url ->
                    component.loadUrl(url)
                    component.selectPage(BrowserState.BrowserPage.Home)
                },
                onDelete = { component.removeBookmark(it) },
                onManageFolders = { showFolderManager = true }
            )
            1 -> HistoryList(
                history = state.history,
                onClick = { url ->
                    component.loadUrl(url)
                    component.selectPage(BrowserState.BrowserPage.Home)
                },
                onDelete = { component.removeHistoryItem(it) },
                onClearAll = { component.clearHistory() }
            )
        }
    }

    if (showFolderManager) {
        CategoryManagementDialog(
            items = state.bookmarkFolders.map { BookmarkFolderItem(it) },
            title = stringResource(R.string.browser_manage_bookmark_folders),
            onDismiss = { showFolderManager = false },
            onAdd = { name -> component.addBookmarkFolder(name) },
            onDelete = { item -> component.deleteBookmarkFolder(item.folder.id) },
            onRename = { item, newName -> component.renameBookmarkFolder(item.folder.id, newName) },
            onReorder = { items ->
                component.reorderBookmarkFolders(items.map { it.folder })
            }
        )
    }
}

@Composable
private fun BookmarksList(
    state: BrowserState,
    onClick: (String) -> Unit,
    onDelete: (String) -> Unit,
    onManageFolders: () -> Unit
) {
    if (state.bookmarks.isEmpty()) {
        EmptyStateView(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBookmark,
            title = stringResource(R.string.browser_no_bookmarks),
            subtitle = stringResource(R.string.browser_no_bookmarks_hint)
        )
    } else {
        val grouped = state.bookmarks.groupBy { item ->
            val folderName = state.folderName(item.folderId)
            folderName.ifEmpty { stringResource(R.string.browser_uncategorized) }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            grouped.forEach { (folder, items) ->
                item {
                    Text(
                        text = folder,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
                items(items, key = { it.id }) { bookmark ->
                    BookmarkItemRow(
                        item = bookmark,
                        onClick = { onClick(bookmark.url) },
                        onDelete = { onDelete(bookmark.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryList(
    history: List<HistoryItem>,
    onClick: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClearAll: () -> Unit
) {
    if (history.isEmpty()) {
        EmptyStateView(icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory, title = stringResource(R.string.browser_no_history))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(history, key = { it.id }) { item ->
                HistoryItemRow(
                    item = item,
                    onClick = { onClick(item.url) },
                    onDelete = { onDelete(item.id) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GlassTonalButton(
                    onClick = onClearAll,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.browser_clear_history))
                }
            }
        }
    }
}

@Composable
private fun BookmarkItemRow(
    item: BookmarkItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        containerAlpha = 0.2f,
        borderWidth = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePublic,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title.ifEmpty { item.url },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.browser_delete),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HistoryItemRow(
    item: HistoryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title.ifEmpty { item.url },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                contentDescription = stringResource(R.string.browser_delete),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        containerAlpha = 0.15f,
        borderWidth = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .then(
                            if (isSelected) Modifier.background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50)
                            ) else Modifier
                        )
                        .clickable { onSelect(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
