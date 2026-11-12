/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.blog.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.wanbaohe.blog.logic.BlogComponent
import com.shifenmiao.common.components.blog.BlogHeader
import com.wanbaohe.blog.ui.BlogContent
import com.wanbaohe.core.ui.utils.helper.scrollToContentStart
import kotlinx.coroutines.launch

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
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    BaseScreen(
        title = {
            BlogHeader(title = currentTitle.value, isFixed = false)
        },
        onGoBack = appComponent.onGoBack,
        type = EnhancedTopAppBarType.Medium,
        supportGlassEffect = true,
        scrollBehavior = scrollBehavior,
        // 标题栏点击只回到正文首条，不改动 MarkdownLazyContent 自身的滚动行为
        onTitleClick = {
            coroutineScope.launch { lazyListState.scrollToContentStart() }
        },
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
                appComponent = appComponent,
                onGoBack = { appComponent.onGoBack },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                lazyListState = lazyListState
            )
        }
    }
}