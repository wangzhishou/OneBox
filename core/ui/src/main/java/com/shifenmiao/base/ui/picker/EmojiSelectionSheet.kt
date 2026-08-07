package com.shifenmiao.base.ui.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.utils.StringUtils
import kotlinx.coroutines.launch

@Composable
fun EmojiSelectionSheet(
    onDismissRequest: (Boolean) -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    val categories = remember { StringUtils.generateEmojiCategories() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { categories.size })

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest(false) },
        sheetState = sheetState,
        dragHandle = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
            ) {
                Text(
                    text = "选择表情",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            SecondaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(category.title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val emojis = categories[page].emojis
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(emojis) { emoji ->
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable {
                                    scope.launch {
                                        sheetState.hide()
                                    }.invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            onEmojiSelected(emoji)
                                            onDismissRequest(true)
                                        }
                                    }
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
