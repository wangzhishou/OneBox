package com.wanbaohe.blog.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.shifenmiao.base.pullrefresh.PullToRefreshLayout
import com.shifenmiao.base.pullrefresh.rememberPullToRefreshStateOnTime
import com.shifenmiao.base.utils.DateUtils.convertElapsedTimeIntoText
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.model.blog.BlogDetailState
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.wanbaohe.blog.logic.BlogComponent
import com.shifenmiao.common.components.blog.BlogHeader
import com.wanbaohe.blog.ui.BlogContent

@Composable
fun BlogDetailScreen(
    appComponent: AppComponent,
    blogComponent: BlogComponent
) {
    val blogDetailState by blogComponent.blogDetailState.collectAsState()
    val currentTitle = remember(blogDetailState) {
        mutableStateOf(
            (blogDetailState as? BlogDetailState.Success)?.blog?.title ?: ""
        )
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    BaseScreen(
        title = {
            BlogHeader(title = currentTitle.value, isFixed = false)
        },
        onGoBack = appComponent.onGoBack,
        type = EnhancedTopAppBarType.Medium,
        supportGlassEffect = true,
        scrollBehavior = scrollBehavior,
        showNavigationBarsPadding = true
    ) {
        val pullRefreshLayoutState = rememberPullToRefreshStateOnTime(
            onTimeUpdated = { timeElapsed ->
                convertElapsedTimeIntoText(timeElapsed)
            }
        )
        PullToRefreshLayout(
            modifier = Modifier
                .fillMaxSize(),
            pullRefreshLayoutState = pullRefreshLayoutState,
            onRefresh = {
                blogComponent.refreshBlogDetail()
            },
            isRefreshing = blogDetailState is BlogDetailState.PageLoading
        ) {
            BlogContent(
                blogComponent = blogComponent,
                onGoBack = { appComponent.onGoBack },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            )
        }
    }
}