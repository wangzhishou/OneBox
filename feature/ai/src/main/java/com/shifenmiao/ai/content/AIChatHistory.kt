package com.shifenmiao.ai.content


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.shifenmiao.ai.component.AIChatBaseComponent
import com.shifenmiao.base.components.ErrorItem
import com.shifenmiao.base.manager.DeleteConfirmationManager
import com.shifenmiao.base.ui.AdvancedDeleteConfirmDialog
import com.shifenmiao.base.ui.loading.EmptyBox
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.MessageEntity
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxListItem
import java.text.SimpleDateFormat
import java.util.Locale

private val historyDateFormat by lazy { SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA) }

@Composable
@Suppress("DEPRECATION")
fun AIChatHistory(
    aiChatBaseComponent: AIChatBaseComponent
) {
    val showDeleteDialog = remember { mutableStateOf(false) }
    val selectMessageEntity = remember {
        mutableStateOf<MessageEntity?>(null)
    }
    val shape = MaterialTheme.shapes.medium
    val historyMessageEntityList =
        aiChatBaseComponent.historyMessageEntityListFlow.collectAsLazyPagingItems()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 16.dp
        ),
    ) {
        // 数据渲染
        items(historyMessageEntityList.itemCount, key = { index ->
            historyMessageEntityList[index]?.id ?: 0
        }, itemContent = { index ->
            val dataItem = historyMessageEntityList[index]
            if (dataItem != null) {
                SwipeToDismissBox(
                    state = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart
                                || dismissValue == SwipeToDismissBoxValue.StartToEnd
                            ) {
                                selectMessageEntity.value = dataItem
                                showDeleteDialog.value = true
                                false
                            } else false
                        }
                    ),
                    backgroundContent = {
                    },
                    content = {
                        HistoryItemContent(
                            dataItem = dataItem,
                            shape = shape,
                            onClick = {
                                aiChatBaseComponent.loadHistoryById(dataItem.conversationId)
                            }
                        )
                    }
                )
            }
        })

        // 处理加载状态
        when (historyMessageEntityList.loadState.refresh) {
            is LoadState.Error -> {
                item {
                    ErrorItem(
                        message = stringResource(R.string.ai_error_unknown),
                        onRetry = { historyMessageEntityList.retry() }
                    )
                }
            }
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is LoadState.NotLoading -> {
                if (historyMessageEntityList.itemCount == 0) {
                    item {
                        EmptyBox(
                            modifier = Modifier
                                .padding(16.dp)
                                .heightIn(200.dp)
                        )
                    }
                }
            }
        }

        // 处理底部加载更多状态
        when (historyMessageEntityList.loadState.append) {
            is LoadState.Error -> {
                item {
                    ErrorItem(
                        message = stringResource(R.string.ai_error_unknown),
                        onRetry = { historyMessageEntityList.retry() }
                    )
                }
            }
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            }
            else -> {}
        }
    }

    // 替换为高级删除确认对话框
    if (showDeleteDialog.value) {
        AdvancedDeleteConfirmDialog(
            operationType = DeleteConfirmationManager.OperationType.CHAT_MESSAGE,
            showDialog = showDeleteDialog,
            onConfirm = {
                selectMessageEntity.value?.let {
                    aiChatBaseComponent.deleteHistoryMessageEntity(it)
                }
            },
            title = stringResource(R.string.ai_chat_delete_title),
            message = stringResource(R.string.ai_chat_delete_message)
        )
    }
}

@Composable
private fun HistoryItemContent(
    dataItem: MessageEntity,
    shape: androidx.compose.ui.graphics.Shape,
    onClick: () -> Unit
) {
    OneBoxListItem(
        modifier = Modifier
            .clip(shape)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = shape
            )
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        headlineContent = {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = dataItem.question,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            val formattedDate = historyDateFormat.format(dataItem.createdAt)
            Text(
                text = formattedDate,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
