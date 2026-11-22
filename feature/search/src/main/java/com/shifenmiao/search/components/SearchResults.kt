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

package com.shifenmiao.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.wanbaohe.base.ui.DeleteConfirmDialog
import com.wanbaohe.base.ui.loading.EmptyBox
import com.wanbaohe.base.ui.utils.Animation
import com.shifenmiao.common.components.SearchMessageCard
import com.shifenmiao.common.components.comments.CommentsHost
import com.shifenmiao.common.components.comments.commentUidForListType
import com.shifenmiao.common.handle.HandleEvent
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.search.SuggestionModel
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.online.ui.VerticalStaggeredCard
import com.shifenmiao.search.logic.SearchComponent
import com.shifenmiao.storage.SearchHistoryStore
import com.shifenmiao.theme.AppFontMode
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSearchOff

@Composable
fun SearchResults(
    searchComponent: SearchComponent,
    itemListComponent: ItemListComponent,
    queryValue: String
) {
    val searchItemList = searchComponent.searchItemList.collectAsState()
    val searchMessageEntity = searchComponent.searchMessageEntity.collectAsState()
    val globalSearchResults = searchComponent.globalSearchResults.collectAsState()
    val isGlobalSearching = searchComponent.isGlobalSearching.collectAsState()
    val isGlobalSearchTruncated = searchComponent.isGlobalSearchTruncated.collectAsState()
    val hasResults = searchItemList.value.isNotEmpty() ||
        searchMessageEntity.value.isNotEmpty() || globalSearchResults.value.isNotEmpty()

    // 与列表页一致: 删除确认弹窗 + 评论浮动层(由 ItemListComponent 的 childSlot 驱动)
    val deleteState = rememberDeleteState()
    val commentsSlot by itemListComponent.commentsSlot.subscribeAsState()

    if (hasResults || isGlobalSearching.value) {
        val lazyStaggeredGridState: LazyStaggeredGridState = rememberLazyStaggeredGridState()
        val onNavigate = LocalOnNavigate.current
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        Text(
            text = stringResource(R.string.search_result),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                horizontal = AppTheme.dimens.pageHorizontal
            )
        )
        if (isGlobalSearching.value) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.dimens.pageHorizontal)
            )
        }
        // 横屏有导航轨和助手侧栏时，结果区的真实宽度远小于窗口宽度；
        // 列数只按这一层的 maxWidth 决定，大字下主动减列，不缩用户字号。
        BoxWithConstraints(modifier = Modifier.fillMaxHeight()) {
            LazyVerticalStaggeredGrid(
                state = lazyStaggeredGridState,
                modifier = Modifier.fillMaxSize(),
                columns = searchGridColumns(maxWidth),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.contentGap),
                verticalItemSpacing = AppTheme.dimens.contentGap,
                contentPadding = PaddingValues(
                    start = AppTheme.dimens.pageHorizontal,
                    end = AppTheme.dimens.pageHorizontal,
                    top = 4.dp,
                    bottom = AppTheme.dimens.pageHorizontal
                )
            ) {
                searchItemList.value.forEachIndexed { index, itemWithRelation ->
                    item {
                        if (BaseUtils.isShowByIdString(itemWithRelation.item.miniProgramId)
                            && !BaseUtils.isHiddenId(itemWithRelation.item.id)
                        ) {
                            Animation.StaggeredAnimatedItem(index, content = {
                                VerticalStaggeredCard(
                                    index = index,
                                    itemListComponent = itemListComponent,
                                    itemWithStats = itemWithRelation,
                                    highlightKeyword = queryValue,
                                    commentCount = itemWithRelation.item.commentCount,
                                    onCommentClick = itemWithRelation.item.documentId
                                        ?.takeIf { it.isNotBlank() }
                                        ?.let { documentId ->
                                            {
                                                itemListComponent.showComments(
                                                    documentId = documentId,
                                                    itemTitle = itemWithRelation.item.title,
                                                    uid = uidFor(ListItemType.fromId(itemWithRelation.item.listType)),
                                                    localItemId = itemWithRelation.item.id,
                                                )
                                            }
                                        },
                                    onDeleteRequest = deleteState::request,
                                    onClick = {
                                        SearchHistoryStore.addHistoryItem(
                                            SuggestionModel(queryValue)
                                        )
                                        searchComponent.recordClick(itemWithRelation.item.id)
                                        scope.launch {
                                            val resource = com.shifenmiao.common.handle.ItemResourceResolver.resolve(
                                                appDatabase = searchComponent.appDatabase,
                                                itemId = itemWithRelation.item.id,
                                                listType = itemWithRelation.item.listType,
                                            )
                                            HandleEvent.handleCardClick(
                                                context = context,
                                                onNavigate = onNavigate,
                                                itemWithRelation = itemWithRelation.toItemWithCategories(),
                                                resource = resource,
                                            )
                                        }
                                    }
                                )
                            })
                        }
                    }
                }
                searchMessageEntity.value.forEachIndexed { index, messageEntity ->
                    item {
                        Animation.StaggeredAnimatedItem(index) {
                            SearchMessageCard(
                                id = messageEntity.id,
                                title = messageEntity.title,
                                description = messageEntity.answer + messageEntity.question,
                                queryValue = queryValue,
                                tag = stringResource(R.string.message_card_tag)
                            ) {
                                SearchHistoryStore.addHistoryItem(
                                    SuggestionModel(queryValue)
                                )
                                HandleEvent.onNavigateMessageEntity(
                                    onNavigate = onNavigate,
                                    messageEntity = messageEntity
                                )
                            }
                        }
                    }
                }
                globalSearchResults.value.forEachIndexed { index, result ->
                    item(key = "${result.sourceId}:${result.id}") {
                        Animation.StaggeredAnimatedItem(index) {
                            SearchMessageCard(
                                id = result.id.hashCode(),
                                title = result.title,
                                description = result.description,
                                queryValue = queryValue,
                                tag = stringResource(result.labelRes),
                                iconName = result.iconName,
                            ) {
                                SearchHistoryStore.addHistoryItem(
                                    SuggestionModel(queryValue)
                                )
                                searchComponent.openGlobalSearchResult(
                                    result = result,
                                    context = context,
                                    onNavigate = onNavigate,
                                )
                            }
                        }
                    }
                }
                if (isGlobalSearchTruncated.value) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(
                            text = stringResource(R.string.search_results_truncated),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSearchOff,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            EmptyBox(
                modifier = Modifier.padding(16.dp),
                text = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.nothing_found_by_search)
            )
        }
    }

    DeleteConfirmDialog(
        onDelete = {
            itemListComponent.deleteItem(deleteState.itemId)
            deleteState.dismiss()
        },
        showDeleteDialogState = deleteState.showDialog,
        message = stringResource(R.string.delete_item_confirm_message, deleteState.title),
    )

    commentsSlot.child?.instance?.let { child ->
        CommentsHost(
            component = child.component,
            onDismissed = itemListComponent::dismissComments,
        )
    }
}

/** 与首页工具网格同口径：先看内容容器真实宽度，再看字号，大字主动减列。 */
@Composable
private fun searchGridColumns(availableWidth: Dp): StaggeredGridCells {
    val fontMode = AppTheme.uiProfile.fontMode
    return when {
        availableWidth < 360.dp -> StaggeredGridCells.Fixed(1)
        fontMode == AppFontMode.ExtraLarge && availableWidth < 480.dp ->
            StaggeredGridCells.Fixed(1)

        availableWidth < 600.dp -> StaggeredGridCells.Fixed(2)
        fontMode == AppFontMode.ExtraLarge && availableWidth < 720.dp ->
            StaggeredGridCells.Fixed(2)

        else -> StaggeredGridCells.Adaptive(
            minSize = if (fontMode == AppFontMode.ExtraLarge) 280.dp else 240.dp
        )
    }
}

private fun uidFor(listType: ListItemType?): String =
    commentUidForListType(listType?.id)

private class DeleteState {
    val showDialog = mutableStateOf(false)
    var itemId: Int = 0
    var title: String = ""

    fun request(itemId: Int, title: String) {
        this.itemId = itemId
        this.title = title
        showDialog.value = true
    }

    fun dismiss() {
        showDialog.value = false
    }
}

@Composable
private fun rememberDeleteState(): DeleteState = remember { DeleteState() }
