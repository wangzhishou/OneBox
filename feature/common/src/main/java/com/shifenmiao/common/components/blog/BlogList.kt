package com.shifenmiao.common.components.blog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.shifenmiao.base.components.ErrorBox
import com.shifenmiao.base.ui.loading.EmptyBox
import com.shifenmiao.common.components.LoadingItem
import com.shifenmiao.core.R
import com.shifenmiao.model.blog.BlogItem

@Composable
fun BlogList(
    pagingItems: LazyPagingItems<BlogItem>,
    onBlogClick: (BlogItem) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            count = pagingItems.itemCount,
            key = { index ->
                pagingItems[index]?.id?.let { "blog_$it" } ?: "blog_placeholder_$index"
            }
        ) { index ->
            val blog = pagingItems[index]
            if (blog != null) {
                BlogListItem(
                    blog = blog,
                    onClick = { onBlogClick(blog) }
                )
            }
        }

        pagingItems.apply {
            when (loadState.append) {
                is LoadState.Loading -> {
                    item(key = "blog_append_loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingItem()
                        }
                    }
                }

                is LoadState.Error -> {
                    item(key = "blog_append_error") {
                        ErrorBox(
                            errorMessage = stringResource(R.string.error_message),
                            onRetry = { retry() },
                            onGoBack = {}
                        )
                    }
                }

                is LoadState.NotLoading -> {
                    if (pagingItems.itemCount > 0) {
                        item(key = "blog_append_no_more") {
                            EmptyBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                text = stringResource(id = R.string.load_no_more),
                                textColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
