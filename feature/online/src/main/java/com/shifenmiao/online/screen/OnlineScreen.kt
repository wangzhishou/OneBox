package com.shifenmiao.online.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.shifenmiao.common.components.comments.CommentsHost
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.model.ListItemType
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.online.component.PlaygroundComponent
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace

@Composable
fun HomeContent(
    itemListComponent: ItemListComponent,
    playgroundComponent: PlaygroundComponent,
    onAiCreate: () -> Unit = {},
    initialTab: HomeTabKey? = null,
) {
    val pagerState = rememberPagerState(
        initialPage = homeTabs.indexOfFirst { it.key == (initialTab ?: HomeTabKey.APP) }
            .coerceAtLeast(0),
        pageCount = { homeTabs.size },
    )
    val coroutineScope = rememberCoroutineScope()

    // 二级分类 chip 行的展开状态原来在每页内部 rememberSaveable。
    // 提升到 Pager 外层，是为了让一级 tab 也能开合它；各 tab 各自记忆的语义保持不变。
    // 用 List<Int> 存页码，保证 rememberSaveable 能直接持久化。
    var expandedFilterTabs by rememberSaveable { mutableStateOf(listOf<Int>()) }

    fun setFilterExpanded(page: Int, expanded: Boolean) {
        expandedFilterTabs = if (expanded) {
            (expandedFilterTabs + page).distinct()
        } else {
            expandedFilterTabs - page
        }
    }

    LaunchedEffect(Unit) {
        StartupTrace.markOnce("home_content_composed", "HomeContent.composed")
    }

    HomeTabRow(
        pagerState = pagerState,
        tabs = homeTabs,
        coroutineScope = coroutineScope,
        onToggleCategoryFilter = {
            val page = pagerState.currentPage
            setFilterExpanded(page, page !in expandedFilterTabs)
        },
        filterExpanded = pagerState.currentPage in expandedFilterTabs,
    )

    HorizontalPager(
        verticalAlignment = androidx.compose.ui.Alignment.Top,
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        // 默认 Pager 会预合成相邻页面；这里只合成当前可见的 6 个分类之一。
        beyondViewportPageCount = 0,
    ) { index ->
        when (val kind = homeTabs[index].kind) {
            HomeTabKind.Text -> PagingDataItemScreen(
                modifier = Modifier.fillMaxSize(),
                itemListComponent = itemListComponent,
                listType = ListItemType.NOTE,
                onAiCreate = onAiCreate,
                chipsExpanded = index in expandedFilterTabs,
                onChipsExpandedChange = { expanded -> setFilterExpanded(index, expanded) },
            )
            is HomeTabKind.ListByType -> PagingDataItemScreen(
                modifier = Modifier.fillMaxSize(),
                itemListComponent = itemListComponent,
                listType = kind.listType,
                onAiCreate = onAiCreate,
                chipsExpanded = index in expandedFilterTabs,
                onChipsExpandedChange = { expanded -> setFilterExpanded(index, expanded) },
            )
            is HomeTabKind.Blog -> PlaygroundScreen(
                playgroundComponent = playgroundComponent,
            )
        }
    }

    // 评论浮动层: 整个首页只挂这一个宿主.
    //
    // 之前它挂在 Pager 每一页的 ItemGrid 里, 而所有页共享同一个 ItemListComponent
    // (同一个 commentsSlot). 页面切换时 Compose 会同时合成相邻两页 (手势 / 动画期间
    // 还可能更久), 于是同一个 child 被两处渲染 → 每处都创建一个全屏 PopupLayout →
    // 一次点击弹出多层评论浮动层. 宿主体积小, 提到 Pager 外层后与页数彻底解耦.
    val commentsSlot by itemListComponent.commentsSlot.subscribeAsState()
    commentsSlot.child?.instance?.let { child ->
        CommentsHost(
            component = child.component,
            onDismissed = itemListComponent::dismissComments,
        )
    }
}
