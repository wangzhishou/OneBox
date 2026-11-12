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

package com.shifenmiao.search.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.components.ClickInfoType
import com.shifenmiao.common.components.FeaturedGrid
import com.shifenmiao.common.components.TopSearchBar
import com.shifenmiao.common.handle.HandleEvent
import com.shifenmiao.common.handle.ItemResourceResolver
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.search.components.SearchResults
import com.shifenmiao.search.components.SearchSuggest
import com.shifenmiao.search.logic.SearchComponent
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    searchComponent: SearchComponent,
    itemListComponent: ItemListComponent,
    appComponent: AppComponent
) {
    /**
     * 搜索相关
     */
    val queryValue = remember { mutableStateOf("") }
    val hasQuery by remember { derivedStateOf { queryValue.value.isNotEmpty() } }

    // 缓存回调以避免不必要的重组
    val onQueryChange = remember<(String) -> Unit>(searchComponent) {
        { newQuery ->
            queryValue.value = newQuery
            searchComponent.onSearchQueryChange(newQuery)
        }
    }

    val onSuggestionClick = remember<(com.shifenmiao.model.search.SuggestionModel) -> Unit>(searchComponent) {
        { suggestion ->
            queryValue.value = suggestion.tag
            searchComponent.onSearchQueryChange(suggestion.tag)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        TopSearchBar(
            isShowBack = true,
            appComponent = appComponent,
            queryValue = queryValue,
            onQueryChange = onQueryChange,
            modifier = Modifier,
            onSearchFocusChange = { _ ->

            }
        )

        if (hasQuery) {
            SearchResults(
                searchComponent = searchComponent,
                itemListComponent = itemListComponent,
                queryValue = queryValue
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = AppTheme.dimens.paddingNormal
                    )
                    .verticalScroll(rememberScrollState())
            ) {
                SearchSuggest(
                    onSuggestionClick = onSuggestionClick,
                )

                RecentVisitsSection(
                    searchComponent = searchComponent,
                )

                FeaturedSection(
                    searchComponent = searchComponent,
                )
            }
        }
    }

    BackHandler {
        appComponent.onGoBack()
    }
}

@Composable
private fun FeaturedSection(
    searchComponent: SearchComponent,
) {
    val recommended by searchComponent.recommendedFlow.collectAsState()
    val onNavigate = LocalOnNavigate.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (recommended.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = AppTheme.dimens.spaceNormal)) {
            Text(
                text = stringResource(R.string.favorite_featured_title),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = AppTheme.dimens.paddingSmall)
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))
            FeaturedGrid(
                items = recommended,
                clickInfo = ClickInfoType.COUNT,
                onItemClick = { item ->
                    scope.launch {
                        val resource = com.shifenmiao.common.handle.ItemResourceResolver.resolve(
                            appDatabase = searchComponent.appDatabase,
                            itemId = item.item.id,
                            listType = item.item.listType,
                        )
                        HandleEvent.handleCardClick(
                            context = context,
                            onNavigate = onNavigate,
                            itemWithRelation = item.toItemWithCategories(),
                            resource = resource,
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun RecentVisitsSection(
    searchComponent: SearchComponent,
) {
    val recentItems by searchComponent.recentClickedFlow.collectAsState()
    val onNavigate = LocalOnNavigate.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    if (recentItems.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = AppTheme.dimens.spaceNormal)) {
            Text(
                text = stringResource(R.string.recent_visits),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = AppTheme.dimens.paddingSmall)
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))
            FeaturedGrid(
                items = recentItems,
                clickInfo = ClickInfoType.TIME,
                reverseTheme = true,
                onItemClick = { item ->
                    scope.launch {
                        val resource = ItemResourceResolver.resolve(
                            appDatabase = searchComponent.appDatabase,
                            itemId = item.item.id,
                            listType = item.item.listType,
                        )
                        HandleEvent.handleCardClick(
                            context = context,
                            onNavigate = onNavigate,
                            itemWithRelation = item.toItemWithCategories(),
                            resource = resource,
                        )
                    }
                }
            )
        }
    }
}