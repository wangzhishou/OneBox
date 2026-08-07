package com.wanbaohe.file.browser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanbaohe.file.browser.model.FileItem
import com.wanbaohe.file.browser.model.FileBrowserViewMode

/**
 * List of files with lazy loading and performance optimization
 *
 * @param files The list of files to display
 * @param onItemClick Callback when a file item is clicked
 * @param contentPadding Padding around the list
 * @param modifier Modifier for the composable
 */
@Composable
fun FileList(
    files: List<FileItem>,
    onItemClick: (FileItem) -> Unit,
    onItemLongClick: ((FileItem) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
    viewMode: FileBrowserViewMode = FileBrowserViewMode.LIST
) {
    val highlightedIndex = files.indexOfFirst { it.isHighlighted }

    when (viewMode) {
        FileBrowserViewMode.LIST -> {
            val listState = rememberLazyListState()

            // Auto-scroll to highlighted item
            LaunchedEffect(files) {
                if (highlightedIndex != -1) {
                    listState.animateScrollToItem(highlightedIndex)
                }
            }

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                state = listState,
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = files,
                    key = { it.uri.toString() }
                ) { file ->
                    FileListItem(
                        item = file,
                        onClick = { onItemClick(file) },
                        onLongClick = if (onItemLongClick != null) ({ onItemLongClick(file) }) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    )
                }
            }
        }

        FileBrowserViewMode.GRID -> {
            val gridState = rememberLazyGridState()

            // Auto-scroll to highlighted item
            LaunchedEffect(files) {
                if (highlightedIndex != -1) {
                    gridState.animateScrollToItem(highlightedIndex)
                }
            }

            LazyVerticalGrid(
                modifier = modifier.fillMaxSize(),
                state = gridState,
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = files,
                    key = { it.uri.toString() }
                ) { file ->
                    FileGridItem(
                        item = file,
                        onClick = { onItemClick(file) },
                        onLongClick = if (onItemLongClick != null) ({ onItemLongClick(file) }) else null,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}
