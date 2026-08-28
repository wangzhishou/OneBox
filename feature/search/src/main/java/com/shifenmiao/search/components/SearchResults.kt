package com.shifenmiao.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.shifenmiao.base.ui.DeleteConfirmDialog
import com.shifenmiao.base.ui.loading.EmptyBox
import com.shifenmiao.base.ui.utils.Animation
import com.shifenmiao.common.components.SearchMessageCard
import com.shifenmiao.common.components.comments.CommentsHost
import com.shifenmiao.common.handle.HandleEvent
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.search.SuggestionModel
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.online.ui.VerticalStaggeredCard
import com.shifenmiao.search.logic.SearchComponent
import com.shifenmiao.storage.SearchHistoryStore
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSearchOff

@Composable
fun SearchResults(
    searchComponent: SearchComponent,
    itemListComponent: ItemListComponent,
    queryValue: MutableState<String>
) {
    val searchItemList = searchComponent.searchItemList.collectAsState()
    val searchMessageEntity = searchComponent.searchMessageEntity.collectAsState()
    val hasResults = searchItemList.value.isNotEmpty()

    // 与列表页一致: 删除确认弹窗 + 评论浮动层(由 ItemListComponent 的 childSlot 驱动)
    val deleteState = rememberDeleteState()
    val commentsSlot by itemListComponent.commentsSlot.subscribeAsState()

    if (hasResults) {
        val lazyStaggeredGridState: LazyStaggeredGridState = rememberLazyStaggeredGridState()
        val onNavigate = LocalOnNavigate.current
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        Text(
            text = stringResource(R.string.search_result),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                horizontal = AppTheme.dimens.paddingNormal
            )
        )
        LazyVerticalStaggeredGrid(
            state = lazyStaggeredGridState,
            modifier = Modifier.fillMaxHeight(),
            columns = StaggeredGridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingNormal),
            verticalItemSpacing = AppTheme.dimens.paddingNormal,
            contentPadding = PaddingValues(
                start = AppTheme.dimens.paddingNormal,
                end = AppTheme.dimens.paddingNormal,
                top = AppTheme.dimens.spaceNormal,
                bottom = AppTheme.dimens.paddingNormal
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
                                highlightKeyword = queryValue.value,
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
                                        SuggestionModel(queryValue.value)
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
                            queryValue = queryValue.value,
                            tag = stringResource(R.string.message_card_tag)
                        ) {
                            SearchHistoryStore.addHistoryItem(
                                SuggestionModel(queryValue.value)
                            )
                            HandleEvent.onNavigateMessageEntity(
                                onNavigate = onNavigate,
                                messageEntity = messageEntity
                            )
                        }
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

private fun uidFor(listType: ListItemType?): String = when (listType) {
    ListItemType.BLOG -> "api::blog.blog"
    else -> "api::item-list.item-list"
}

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
