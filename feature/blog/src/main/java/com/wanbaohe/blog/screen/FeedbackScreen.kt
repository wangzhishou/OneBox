package com.wanbaohe.blog.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.shifenmiao.base.components.ErrorBox
import com.shifenmiao.base.pullrefresh.PullToRefreshLayout
import com.shifenmiao.base.pullrefresh.rememberPullToRefreshStateOnTime
import com.shifenmiao.base.ui.button.ExtendedFloatingActionVerticalTextButton
import com.shifenmiao.base.ui.button.SecondarySmallButton
import com.shifenmiao.base.ui.loading.EmptyBox
import com.shifenmiao.base.ui.text.VerticalText
import com.shifenmiao.base.utils.DateUtils.convertElapsedTimeIntoText
import com.shifenmiao.common.components.LoadingItem
import com.shifenmiao.common.components.PageLoader
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.model.ScreenParams
import com.shifenmiao.model.blog.BlogItem
import com.wanbaohe.blog.logic.BlogComponent
import com.shifenmiao.common.components.blog.BlogListItem
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.shifenmiao.webview.mermaid.ProvideMermaidRenderer
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd

@Composable
fun FeedbackScreen(
    blogComponent: BlogComponent,
    appComponent: AppComponent
) {
    val isRefreshing by blogComponent.isRefreshing.collectAsState()
    val lazyPagingItems = blogComponent.getBlogFlow().collectAsLazyPagingItems()
    val onNavigator = LocalOnNavigate.current
    BaseScreen(
        title = stringResource(id = R.string.feedback_title),
        onGoBack = appComponent.onGoBack,
        supportGlassEffect = true,
        actions = {
            SecondarySmallButton(
                modifier = Modifier.padding(end = 16.dp),
                icon = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.NoteAdd,
                        contentDescription = stringResource(id = R.string.feedback_create),
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                text = stringResource(id = R.string.new_add)
            ) {
                onNavigator(
                    Screen.CreateFeedback()
                )
            }
        }
    ) {
        val pullRefreshLayoutState = rememberPullToRefreshStateOnTime(
            onTimeUpdated = { timeElapsed ->
                convertElapsedTimeIntoText(timeElapsed)
            }
        )

        PullToRefreshLayout(
            modifier = Modifier.fillMaxSize(),
            pullRefreshLayoutState = pullRefreshLayoutState,
            onRefresh = {
                blogComponent.refreshBlogs()
            },
            isRefreshing = isRefreshing
        ) {
            when {
                lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh is LoadState.Loading -> {
                    PageLoader(modifier = Modifier.fillMaxWidth())
                }

                lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh is LoadState.Error -> {
                    ErrorBox(
                        errorMessage = "Failed to load blogs",
                        onGoBack = {
                            appComponent.onGoBack()
                        },
                        onRetry = {
                            lazyPagingItems.refresh()
                        }
                    )
                }

                lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh is LoadState.NotLoading -> {
                    EmptyBox(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    ProvideMermaidRenderer {
                        BlogList(lazyPagingItems = lazyPagingItems)
                    }
                }
            }
        }
    }
}

@Composable
fun BlogList(
    lazyPagingItems: LazyPagingItems<BlogItem>
) {
    val lazyListState: LazyListState = rememberLazyListState()
    val onNavigator = LocalOnNavigate.current
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(lazyPagingItems.itemCount) { index ->
                val blog = lazyPagingItems[index]
                if (blog != null) {
                    BlogListItem(blog = blog) {
                        onNavigator(
                            Screen.BlogDetail(
                                ScreenParams(
                                    id = blog.id,
                                    title = blog.title,
                                        blogType = 1
                                )
                            )
                        )
                    }
                }
            }

            lazyPagingItems.apply {
                when (loadState.append) {
                    is LoadState.Loading -> {
                        item {
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
                        item {
                            ErrorBox(
                                errorMessage = stringResource(R.string.error_message),
                                onRetry = { retry() },
                                onGoBack = {}
                            )
                        }
                    }

                    is LoadState.NotLoading -> {
                        item {
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

        ExtendedFloatingActionVerticalTextButton(
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(50),
            text = {
                VerticalText(
                    text = stringResource(id = R.string.feedback_create),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )
            },
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.NoteAdd,
                    contentDescription = stringResource(id = R.string.feedback_create),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            onClick = {
                onNavigator(
                    Screen.CreateFeedback()
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    bottom = 64.dp,
                    end = 16.dp
                ),
            containerColor = MaterialTheme.colorScheme.primary.copy(0.2f),
            contentColor = MaterialTheme.colorScheme.primary,
            expanded = lazyListState.lastScrolledBackward ||
                    !lazyListState.canScrollBackward
        )
    }
}