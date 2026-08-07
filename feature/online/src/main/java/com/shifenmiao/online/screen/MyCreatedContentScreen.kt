package com.shifenmiao.online.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.shifenmiao.base.ui.DeleteConfirmDialog
import com.shifenmiao.base.ui.card.PlaceholderCard
import com.shifenmiao.common.components.LocalArticleList
import com.shifenmiao.common.handle.HandleEvent
import com.shifenmiao.common.handle.ItemResourceResolver
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.online.component.ItemListComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MyCreatedContentScreen(
    itemListComponent: ItemListComponent,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(itemListComponent) {
        itemListComponent.ensureMyCreatedItemsObserved()
    }

    val articleItems by itemListComponent.myArticleItems.collectAsState()
    val showDeleteDialog = remember { mutableStateOf(false) }
    val deleteTarget = remember { mutableStateOf<ItemWithCategoriesAndStats?>(null) }
    val onNavigate = LocalOnNavigate.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        item(key = "my_content_top_spacer") {
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (articleItems.isNotEmpty()) {
            item(key = "my_articles_list") {
                LocalArticleList(
                    items = articleItems,
                    onItemClick = { item -> openItem(scope, itemListComponent, context, onNavigate, item) },
                    onEditClick = { item -> openItem(scope, itemListComponent, context, onNavigate, item) },
                    onDeleteClick = { item ->
                        deleteTarget.value = item
                        showDeleteDialog.value = true
                    },
                )
            }
        }

        if (articleItems.isEmpty()) {
            item(key = "my_content_empty") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.size(width = 200.dp, height = 240.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        PlaceholderCard(
                            onClick = { onNavigate(Screen.CreateNote()) },
                            title = stringResource(R.string.my_content_empty),
                            description = stringResource(R.string.my_content_empty_action_note),
                        )
                    }
                }
            }
        }

        item(key = "my_content_bottom_spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    DeleteConfirmDialog(
        onDelete = {
            deleteTarget.value?.let { itemListComponent.deleteItem(it.item.id) }
            showDeleteDialog.value = false
            deleteTarget.value = null
        },
        showDeleteDialogState = showDeleteDialog,
        message = stringResource(
            R.string.delete_item_confirm_message,
            deleteTarget.value?.item?.title.orEmpty()
        )
    )
}

private fun openItem(
    scope: CoroutineScope,
    itemListComponent: ItemListComponent,
    context: android.content.Context,
    onNavigate: (com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen) -> Unit,
    item: ItemWithCategoriesAndStats,
) {
    itemListComponent.recordItemClick(item.item.id)
    scope.launch {
        val resource = ItemResourceResolver.resolve(
            appDatabase = itemListComponent.appDatabase,
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
