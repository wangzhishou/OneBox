package com.shifenmiao.online.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.shifenmiao.base.pullrefresh.PullToRefreshLayout
import com.shifenmiao.base.pullrefresh.rememberPullToRefreshStateOnTime
import com.shifenmiao.base.ui.DeleteConfirmDialog
import com.shifenmiao.base.ui.card.PlaceholderCard
import com.shifenmiao.base.utils.DateUtils.convertElapsedTimeIntoText
import com.shifenmiao.common.components.LoadingNextPageItem
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.shifenmiao.common.components.comments.CommentsHost
import com.shifenmiao.common.handle.HandleEvent
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.sync.ManualRefreshPolicy
import com.shifenmiao.common.sync.SyncState
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.reorderable.ReorderableType
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.online.ui.VerticalStaggeredCard
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilterAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLabel

@Composable
fun PagingDataItemScreen(
    modifier: Modifier,
    itemListComponent: ItemListComponent,
    listType: ListItemType
) {
    val chips by itemListComponent.observeChips(listType).collectAsState(initial = emptyList())
    var selectedChipId by rememberSaveable(listType.id) { mutableStateOf<Int?>(null) }
    var chipsExpanded by rememberSaveable(listType.id) { mutableStateOf(false) }

    LaunchedEffect(chips, selectedChipId) {
        if (selectedChipId != null && chips.none { it.categoryId == selectedChipId }) {
            selectedChipId = null
        }
    }

    // 同步过滤用分类 documentId（本地自增 id 不是服务端 id，仅用于本地分页查询）
    val selectedChipDocumentId = chips.firstOrNull { it.categoryId == selectedChipId }?.documentId

    val flow = remember(listType.id, selectedChipId) {
        itemListComponent.getItemsFlow(listType, selectedChipId)
    }
    val pagingItems = flow.collectAsLazyPagingItems()

    // 进入页面时做一次带持久化冷却的增量同步(切 chip 不触发),新发布的条目能及时出现。
    LaunchedEffect(listType.id) {
        itemListComponent.syncOnPageEnter(listType)
    }


    LaunchedEffect(pagingItems.itemCount) {
        if (pagingItems.itemCount > 0) {
            StartupTrace.markOnce(
                key = "paging_items_visible_${listType.id}",
                stage = "PagingDataItemScreen.items_visible listType=${listType.id} count=${pagingItems.itemCount}",
            )
        }
    }

    var userRefreshTriggered by remember(listType.id) { mutableStateOf(false) }
    val isLoading = pagingItems.loadState.refresh is LoadState.Loading
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            userRefreshTriggered = false
        }
    }
    val initialLoadInProgress = userRefreshTriggered && isLoading

    val refreshState by itemListComponent
        .refreshStateFlow(listType, selectedChipDocumentId)
        .collectAsState()
    val isRefreshing = initialLoadInProgress || refreshState is SyncState.Loading
    val refreshFailedText = stringResource(R.string.ai_refresh_failed)
    val refreshCooldownText = stringResource(R.string.refresh_cooldown_hint)
    LaunchedEffect(refreshState) {
        (refreshState as? SyncState.Error)?.let { state ->
            val message = state.cause.message
                ?.takeIf { it.isNotBlank() }
                ?: refreshFailedText
            AppToastHost.showToast(message)
        }
    }

    val isGrid = LocalSettingsState.current.groupOptionsByTypes
    val pullRefreshState = rememberPullToRefreshStateOnTime(
        onTimeUpdated = { convertElapsedTimeIntoText(it) }
    )
    val createScreen = createScreenForListType(listType)

    fun onCategoryBadgeTap(categoryId: Int?) {
        if (categoryId == null) return
        selectedChipId = categoryId
        chipsExpanded = true
    }

    fun onSelectChip(chipId: Int?) {
        selectedChipId = chipId
        if (chipId == null) {
            chipsExpanded = false
        }
    }

    fun onCloseChips() {
        selectedChipId = null
        chipsExpanded = false
    }

    val localNavigator = LocalUrlNavigator.current
    val onNavigator = LocalOnNavigate.current

    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = chipsExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            ChipFilterRow(
                chips = chips,
                selectedCategoryId = selectedChipId,
                onSelect = ::onSelectChip,
                onClose = ::onCloseChips,
                onManageTags = {
                    localNavigator.navigate(
                        Screen.Reorderable(type = ReorderableType.CATEGORY)
                    )
                },
            )
        }

        PullToRefreshLayout(
            modifier = Modifier.fillMaxWidth(),
            pullRefreshLayoutState = pullRefreshState,
            onRefresh = {
                if (ManualRefreshPolicy.canRefresh()) {
                    userRefreshTriggered = true
                    itemListComponent.refreshData(listType, selectedChipDocumentId)
                    ManualRefreshPolicy.markRefreshed()
                } else {
                    AppToastHost.showToast(refreshCooldownText)
                }
            },
            isRefreshing = isRefreshing,
        ) {
            when {
                pagingItems.itemCount == 0 -> EmptyOrErrorState(
                    pagingItems = pagingItems,
                    onCreate = { onNavigator(createScreen) },
                )

                else -> ItemGrid(
                    itemListComponent = itemListComponent,
                    pagingItems = pagingItems,
                    isGrid = isGrid,
                    listType = listType,
                    onCategoryBadgeTap = ::onCategoryBadgeTap,
                    createScreen = createScreen,
                )
            }
        }
    }
}

@Composable
private fun ChipFilterRow(
    chips: List<ChipFilter>,
    selectedCategoryId: Int?,
    onSelect: (Int?) -> Unit,
    onClose: () -> Unit,
    onManageTags: () -> Unit,
) {
    val allLabel = stringResource(R.string.home_chip_all)
    val manageLabel = stringResource(R.string.home_tab_tags_manage)
    val closeLabel = stringResource(R.string.home_chip_close)
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        contentPadding = PaddingValues(horizontal = AppTheme.dimens.paddingNormal),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item(key = "chip_all") {
            GlassFilterChip(
                selected = selectedCategoryId == null,
                onClick = { onSelect(null) },
                label = { Text(allLabel) },
                shape = chipShape,
                border = null,
                glassBorderWidth = 0.dp,
            )
        }
        items(items = chips, key = { it.categoryId ?: 0 }) { chip ->
            val selected = selectedCategoryId == chip.categoryId
            GlassFilterChip(
                selected = selected,
                onClick = { onSelect(chip.categoryId) },
                label = { Text(chip.name) },
                shape = chipShape,
                border = null,
                glassBorderWidth = 0.dp,
                leadingIcon = {
                    if (selected) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFilterAlt,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        }
        item(key = "chip_manage_tags") {
            GlassFilterChip(
                selected = false,
                onClick = onManageTags,
                label = { Text(manageLabel) },
                leadingIcon = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLabel,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                shape = chipShape,
                border = null,
                glassBorderWidth = 0.dp,
                glassContainerColor = MaterialTheme.colorScheme.surface,
            )
        }
        item(key = "chip_close") {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    contentDescription = closeLabel,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val chipShape = RoundedCornerShape(16.dp)

private fun createScreenForListType(listType: ListItemType): Screen = when (listType) {
    ListItemType.AGENT -> Screen.CreateAIAgent()
    ListItemType.PROMPT -> Screen.CreateAIChatPrompt()
    ListItemType.HTML -> Screen.CreateHtml()
    else -> Screen.CreateNote()
}

@Composable
private fun EmptyOrErrorState(
    pagingItems: LazyPagingItems<ItemWithCategoriesAndStats>,
    onCreate: () -> Unit,
) {
    when {
        // 同步/加载失败时用本地数据兜底，不弹错误提示，让用户无感知。
        // 仍保留“新增”引导卡片，方便创建本地内容。
        pagingItems.loadState.refresh is LoadState.Error ||
                pagingItems.loadState.refresh is LoadState.NotLoading &&
                pagingItems.itemCount == 0 &&
                pagingItems.loadState.mediator?.refresh !is LoadState.Loading -> Box(
            modifier = Modifier
                .fillMaxSize()
                // 空态没有可滚子布局,嵌套滚动传不到 PullToRefreshLayout,
                // 导致下拉刷新手势失效;挂上 verticalScroll 让手势可达
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.size(width = 200.dp, height = 240.dp),
                contentAlignment = Alignment.Center,
            ) {
                PlaceholderCard(
                    onClick = onCreate,
                    title = stringResource(R.string.placeholder_empty_title),
                    description = stringResource(R.string.placeholder_empty_action),
                )
            }
        }

        else -> SkeletonItemGrid(isGrid = LocalSettingsState.current.groupOptionsByTypes)
    }
}

@Composable
private fun SkeletonItemGrid(isGrid: Boolean) {
    LazyVerticalStaggeredGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = gridColumns(isGrid),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp,
        contentPadding = gridContentPadding(),
        userScrollEnabled = false,
    ) {
        items(count = 6, key = { "skeleton_$it" }) { index ->
            SkeletonCard(seed = index)
        }
    }
}

@Composable
private fun SkeletonCard(seed: Int) {
    val heights = listOf(140.dp, 200.dp, 170.dp, 220.dp, 150.dp, 190.dp)
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heights[seed % heights.size])
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
        )
    }
}

@Composable
private fun ItemGrid(
    itemListComponent: ItemListComponent,
    pagingItems: LazyPagingItems<ItemWithCategoriesAndStats>,
    isGrid: Boolean,
    listType: ListItemType,
    onCategoryBadgeTap: (Int?) -> Unit,
    createScreen: Screen,
) {
    val onNavigator = LocalOnNavigate.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState()
    val deleteState = rememberDeleteState()

    // 评论浮动层: 由 ItemListComponent 的 childSlot 驱动生命周期.
    val commentsSlot by itemListComponent.commentsSlot.subscribeAsState()

    val isNoteTab = listType == ListItemType.NOTE
    val cardMaxTitleLines = if (isNoteTab) 2 else 1

    LaunchedEffect(itemListComponent) {
        itemListComponent.scrollToTopEvent.collect {
            gridState.animateScrollToItem(0)
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

    LazyVerticalStaggeredGrid(
        state = gridState,
        modifier = Modifier.fillMaxHeight(),
        columns = gridColumns(isGrid),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp,
        contentPadding = gridContentPadding(),
    ) {
        items(
            count = pagingItems.itemCount,
            key = { index -> pagingItemKey(pagingItems, index) },
        ) { index ->
            val item = pagingItems[index] ?: return@items
            VerticalStaggeredCard(
                index = index,
                itemListComponent = itemListComponent,
                itemWithStats = item,
                maxTitleLines = cardMaxTitleLines,
                commentCount = item.item.commentCount,
                onCommentClick = item.item.documentId?.takeIf { it.isNotBlank() }?.let { documentId ->
                    {
                        itemListComponent.showComments(
                            documentId = documentId,
                            itemTitle = item.item.title,
                            uid = uidFor(listType),
                            localItemId = item.item.id,
                        )
                    }
                },
                onClick = {
                    itemListComponent.recordItemClick(item.item.id)
                    scope.launch {
                        val resource = com.shifenmiao.common.handle.ItemResourceResolver.resolve(
                            appDatabase = itemListComponent.appDatabase,
                            itemId = item.item.id,
                            listType = item.item.listType,
                        )
                        HandleEvent.handleCardClick(
                            context = context,
                            onNavigate = onNavigator,
                            itemWithStats = item,
                            resource = resource,
                        )
                    }
                },
                onDeleteRequest = deleteState::request,
                onCategoryClick = onCategoryBadgeTap
            )
        }
        // 加载/错误状态都不提示用户：有本地数据兜底且更新不频繁。
        // 保留 Loading 指示器，仅用于告知还有下一页在加载。
        if (pagingItems.loadState.append is LoadState.Loading) {
            item(key = "append_loading") {
                LoadingNextPageItem(modifier = Modifier)
            }
        }
        item(key = "append_placeholder_card") {
            PlaceholderCard(onClick = { onNavigator(createScreen) })
        }
    }

    commentsSlot.child?.instance?.let { child ->
        CommentsHost(
            component = child.component,
            onDismissed = itemListComponent::dismissComments,
        )
    }
}

private fun uidFor(listType: ListItemType): String = when (listType) {
    ListItemType.BLOG -> "api::blog.blog"
    else -> "api::item-list.item-list"
}

private fun gridColumns(isGrid: Boolean) = if (isGrid) {
    StaggeredGridCells.Adaptive(minSize = 150.dp)
} else {
    StaggeredGridCells.Adaptive(minSize = 280.dp)
}

@Composable
private fun gridContentPadding() = PaddingValues(
    start = AppTheme.dimens.paddingNormal,
    end = AppTheme.dimens.paddingNormal,
    top = 4.dp,
    bottom = AppTheme.dimens.paddingNormal,
)

private fun pagingItemKey(
    pagingItems: LazyPagingItems<ItemWithCategoriesAndStats>,
    index: Int,
): String = pagingItems[index]?.item?.id?.let { "paging_item_$it" }
    ?: "paging_placeholder_$index"

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
