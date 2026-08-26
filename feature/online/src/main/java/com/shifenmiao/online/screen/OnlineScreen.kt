package com.shifenmiao.online.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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

    LaunchedEffect(Unit) {
        StartupTrace.markOnce("home_content_composed", "HomeContent.composed")
    }

    HomeTabRow(
        pagerState = pagerState,
        tabs = homeTabs,
        coroutineScope = coroutineScope,
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
            )
            is HomeTabKind.ListByType -> PagingDataItemScreen(
                modifier = Modifier.fillMaxSize(),
                itemListComponent = itemListComponent,
                listType = kind.listType,
                onAiCreate = onAiCreate,
            )
            is HomeTabKind.Blog -> PlaygroundScreen(
                playgroundComponent = playgroundComponent,
            )
        }
    }
}
