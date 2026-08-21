package com.shifenmiao.online.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.GenericScrollableTabRow
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.model.ListItemType
import com.shifenmiao.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import com.shifenmiao.core.R as CoreR

sealed interface HomeTabKind {
    data object Text : HomeTabKind
    data class ListByType(val listType: ListItemType) : HomeTabKind
    data class Blog(val blogType: Int) : HomeTabKind
}

data class HomeTab(
    val key: HomeTabKey,
    @StringRes val titleRes: Int,
    val kind: HomeTabKind,
)

data class ChipFilter(
    val categoryId: Int?,
    val name: String,
    /** 分类的 Strapi v5 documentId；同步过滤传它（本地自增 id 不是服务端 id）。 */
    val documentId: String? = null,
)

internal val homeTabs: List<HomeTab> = listOf(
    HomeTab(
        key = HomeTabKey.TEXT,
        titleRes = CoreR.string.record_tab_title,
        kind = HomeTabKind.Text,
    ),
    HomeTab(
        key = HomeTabKey.APP,
        titleRes = CoreR.string.type_default,
        kind = HomeTabKind.ListByType(ListItemType.NORMAL),
    ),
    HomeTab(
        key = HomeTabKey.AGENT,
        titleRes = CoreR.string.type_agent,
        kind = HomeTabKind.ListByType(ListItemType.AGENT),
    ),
    HomeTab(
        key = HomeTabKey.PROMPT,
        titleRes = CoreR.string.type_prompt,
        kind = HomeTabKind.ListByType(ListItemType.PROMPT),
    ),
    HomeTab(
        key = HomeTabKey.WEB,
        titleRes = CoreR.string.home_tab_web_title,
        kind = HomeTabKind.ListByType(ListItemType.HTML),
    ),
    HomeTab(
        key = HomeTabKey.BLOG,
        titleRes = CoreR.string.playground,
        kind = HomeTabKind.Blog(blogType = 2),
    ),
)

@Composable
fun HomeTabRow(
    pagerState: PagerState,
    tabs: List<HomeTab>,
    coroutineScope: CoroutineScope,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    GenericScrollableTabRow(
        pagerState = pagerState,
        items = tabs,
        coroutineScope = coroutineScope,
        indicatorHeight = 34.dp,
        trailingContent = trailingContent,
        modifier = Modifier.padding(end = AppTheme.dimens.paddingNormal),
        edgePadding = AppTheme.dimens.paddingNormal,
        indicatorShape = MaterialTheme.shapes.large,
        getTitle = { tab -> stringResource(id = tab.titleRes) },
    )
}
