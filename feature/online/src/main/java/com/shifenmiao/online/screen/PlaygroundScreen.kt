package com.shifenmiao.online.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.shifenmiao.base.components.ErrorBox
import com.shifenmiao.base.pullrefresh.PullToRefreshLayout
import com.shifenmiao.base.pullrefresh.rememberPullToRefreshStateOnTime
import com.shifenmiao.base.ui.card.PlaceholderCard
import com.shifenmiao.base.utils.DateUtils.convertElapsedTimeIntoText
import com.shifenmiao.common.components.LoadingNextPageItem
import com.shifenmiao.common.components.PageLoader
import com.shifenmiao.core.R
import com.shifenmiao.model.ScreenParams
import com.shifenmiao.model.blog.BlogItem
import com.shifenmiao.online.component.PlaygroundComponent
import com.shifenmiao.online.ui.PlaygroundCard
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen

@Composable
fun PlaygroundScreen(
    playgroundComponent: PlaygroundComponent
) {
    val isRefreshing by playgroundComponent.isRefreshing.collectAsState()
    val refreshError by playgroundComponent.refreshError.collectAsState()
    val pagingItems = playgroundComponent.getBlogFlow().collectAsLazyPagingItems()
    val onNavigator = LocalOnNavigate.current
    val isGrid = LocalSettingsState.current.groupOptionsByTypes
    val gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState()

    LaunchedEffect(Unit) {
        playgroundComponent.refreshIfNeeded()
    }

    val pullRefreshLayoutState = rememberPullToRefreshStateOnTime(
        onTimeUpdated = { convertElapsedTimeIntoText(it) }
    )

    val createFeedbackScreen = rememberCreateFeedbackScreen()

    PullToRefreshLayout(
        modifier = Modifier.fillMaxSize(),
        pullRefreshLayoutState = pullRefreshLayoutState,
        onRefresh = { playgroundComponent.refresh() },
        isRefreshing = isRefreshing
    ) {
        when {
            pagingItems.itemCount == 0 && isRefreshing -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PageLoader(modifier = Modifier.fillMaxWidth())
                }
            }

            pagingItems.itemCount == 0 && refreshError != null -> {
                ErrorBox(
                    errorMessage = refreshError?.message
                        ?: stringResource(R.string.error_message),
                    onGoBack = { playgroundComponent.consumeRefreshError() },
                    onRetry = { playgroundComponent.refresh() }
                )
            }

            pagingItems.itemCount == 0 -> {
                EmptyPlaceholder(
                    onCreate = { onNavigator(createFeedbackScreen) }
                )
            }

            else -> {
                PlaygroundGrid(
                    pagingItems = pagingItems,
                    isGrid = isGrid,
                    gridState = gridState,
                    onBlogClick = { blog ->
                        onNavigator(
                            Screen.BlogDetail(
                                ScreenParams(
                                    id = blog.id,
                                    title = blog.title,
                                    blogType = PlaygroundComponent.BLOG_TYPE_PLAYGROUND
                                )
                            )
                        )
                    },
                    onCreateFeedback = { onNavigator(createFeedbackScreen) }
                )
            }
        }
    }
}

@Composable
private fun rememberCreateFeedbackScreen(): Screen = Screen.CreateFeedback(
    blogType = PlaygroundComponent.BLOG_TYPE_PLAYGROUND
)

@Composable
private fun EmptyPlaceholder(
    onCreate: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(width = 240.dp, height = 280.dp),
            contentAlignment = Alignment.Center
        ) {
            PlaceholderCard(
                onClick = onCreate,
                title = stringResource(R.string.playground_empty_title),
                description = stringResource(R.string.playground_empty_description),
            )
        }
    }
}

@Composable
private fun PlaygroundGrid(
    pagingItems: androidx.paging.compose.LazyPagingItems<BlogItem>,
    isGrid: Boolean,
    gridState: LazyStaggeredGridState,
    onBlogClick: (BlogItem) -> Unit,
    onCreateFeedback: () -> Unit,
) {
    LazyVerticalStaggeredGrid(
        state = gridState,
        modifier = Modifier.fillMaxHeight(),
        columns = playgroundGridColumns(isGrid),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp,
        contentPadding = PaddingValues(
            start = AppTheme.dimens.paddingNormal,
            end = AppTheme.dimens.paddingNormal,
            top = 4.dp,
            bottom = AppTheme.dimens.paddingNormal,
        )
    ) {
        items(
            count = pagingItems.itemCount,
            key = { index ->
                pagingItems[index]?.id?.let { "playground_blog_$it" }
                    ?: "playground_placeholder_$index"
            }
        ) { index ->
            val blog = pagingItems[index] ?: return@items
            PlaygroundCard(
                blog = blog,
                onClick = { onBlogClick(blog) },
                modifier = Modifier.animateItem()
            )
        }

        if (pagingItems.loadState.append is LoadState.Loading) {
            item(key = "playground_append_loading") {
                LoadingNextPageItem(modifier = Modifier)
            }
        }

        item(key = "playground_footer_placeholder") {
            PlaceholderCard(
                onClick = onCreateFeedback,
                title = stringResource(R.string.playground_footer_title),
                description = stringResource(R.string.playground_footer_description),
            )
        }
    }
}

private fun playgroundGridColumns(isGrid: Boolean) = if (isGrid) {
    StaggeredGridCells.Adaptive(minSize = 160.dp)
} else {
    StaggeredGridCells.Adaptive(minSize = 300.dp)
}
