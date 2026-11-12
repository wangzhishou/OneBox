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

package com.shifenmiao.online.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.shifenmiao.base.pullrefresh.PullToRefreshLayout
import com.shifenmiao.base.pullrefresh.rememberPullToRefreshStateOnTime
import com.wanbaohe.base.ui.DeleteConfirmDialog
import com.shifenmiao.base.utils.DateUtils.convertElapsedTimeIntoText
import com.shifenmiao.common.components.LoadingNextPageItem
import com.shifenmiao.common.components.comments.commentUidForListType
import com.shifenmiao.common.handle.HandleEvent
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.sync.ManualRefreshPolicy
import com.shifenmiao.common.sync.SyncState
import com.shifenmiao.common.sync.SyncTimeoutException
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.reorderable.ReorderableType
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.online.ui.CreateChoiceCard
import com.shifenmiao.online.ui.HomeEmptyState
import com.shifenmiao.online.ui.VerticalStaggeredCard
import com.shifenmiao.online.ui.aiCreateActionFor
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.utils.helper.isPortraitOrCompactWidthLayoutAsState
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.provider.LocalWindowSizeClass
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLabel

@Composable
fun PagingDataItemScreen(
    modifier: Modifier,
    itemListComponent: ItemListComponent,
    listType: ListItemType,
    onAiCreate: () -> Unit,
    // 开合由一级 tab 统一控制；默认值让非首页调用方保持原来「默认收起」的行为。
    chipsExpanded: Boolean = false,
    onChipsExpandedChange: (Boolean) -> Unit = {},
) {
    val chips by itemListComponent.observeChips(listType).collectAsState(initial = emptyList())
    var selectedChipId by rememberSaveable(listType.id) { mutableStateOf<Int?>(null) }

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
    // 用户主动下拉刷新才需要失败提示; 与上面控制 spinner 的标记分开,
    // 因为 spinner 标记会被分页加载状态提前清掉, 不等同步结果。
    var manualRefreshFeedbackPending by remember(listType.id) { mutableStateOf(false) }
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
    val networkErrorText = stringResource(R.string.comment_network_error)
    val refreshCooldownText = stringResource(R.string.refresh_cooldown_hint)
    LaunchedEffect(refreshState) {
        val state = refreshState
        // 进页/启动时的后台同步失败一律静默: 列表读本地缓存、断网也能用,
        // 每次冷启动或进页都弹"网络异常"只会打扰用户(也容易被隐私合规检测判定为异常提示)。
        if (manualRefreshFeedbackPending) {
            if (state is SyncState.Error) {
                // 断网挂起触发的超时明确提示网络异常, 其余错误优先展示服务端/网络层文案
                val message = when (state.cause) {
                    is SyncTimeoutException -> networkErrorText
                    else -> state.cause.message
                        ?.takeIf { it.isNotBlank() }
                        ?: refreshFailedText
                }
                AppToastHost.showToast(message)
            }
            if (state is SyncState.Error || state is SyncState.Success) {
                manualRefreshFeedbackPending = false
            }
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
        onChipsExpandedChange(true)
    }

    // 与「待办事项」快捷 chip 一致：点未选中项应用筛选，再点已选中项取消该筛选。
    // 只改筛选、不收起 chip 栏；收起统一交给 × 或一级 tab 的重复点击。
    fun onSelectChip(chipId: Int?) {
        selectedChipId = if (chipId != null && selectedChipId == chipId) null else chipId
    }

    // × / 空态「清除筛选」：取消对应筛选并隐藏整条 chip 栏。
    fun onCloseChips() {
        selectedChipId = null
        onChipsExpandedChange(false)
    }

    val localNavigator = LocalUrlNavigator.current
    val onNavigator = LocalOnNavigate.current
    val aiCreateAction = aiCreateActionFor(
        listType = listType,
        onNavigator = onNavigator,
        fallback = onAiCreate,
    )
    val showAiCreate = listType != ListItemType.AGENT

    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = chipsExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            ChipFilterRow(
                chips = chips,
                selectedCategoryId = selectedChipId,
                chipsExpanded = chipsExpanded,
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
                    manualRefreshFeedbackPending = true
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
                    listType = listType,
                    isFiltered = selectedChipId != null,
                    onClearFilter = ::onCloseChips,
                    onManualCreate = { onNavigator(createScreen) },
                    onAiCreate = aiCreateAction,
                    showAiCreate = showAiCreate,
                )

                else -> ItemGrid(
                    itemListComponent = itemListComponent,
                    pagingItems = pagingItems,
                    isGrid = isGrid,
                    listType = listType,
                    chipCategoryId = selectedChipId,
                    onCategoryBadgeTap = ::onCategoryBadgeTap,
                    onManualCreate = { onNavigator(createScreen) },
                    onAiCreate = aiCreateAction,
                    showAiCreate = showAiCreate,
                )
            }
        }
    }
}

@Composable
private fun ChipFilterRow(
    chips: List<ChipFilter>,
    selectedCategoryId: Int?,
    chipsExpanded: Boolean,
    onSelect: (Int?) -> Unit,
    onClose: () -> Unit,
    onManageTags: () -> Unit,
) {
    val allLabel = stringResource(R.string.home_chip_all)
    val manageLabel = stringResource(R.string.home_tab_tags_manage)
    val closeLabel = stringResource(R.string.home_chip_close)
    val chipListState = rememberLazyListState()

    // 分类项横向很长，从卡片分类 badge 展开、或从别的 tab 切回来时，
    // 选中项可能在屏幕外。只在展开且尚未可见时把选中 chip 滚进视野，
    // 已经看得见就不动，避免每次展开都把整行拽回起点。
    // item 顺序：0 = 全部，1..n = 分类，n+1 = 标签管理。
    LaunchedEffect(chipsExpanded, selectedCategoryId, chips) {
        if (!chipsExpanded) return@LaunchedEffect
        val targetIndex = if (selectedCategoryId == null) {
            0
        } else {
            val matched = chips.indexOfFirst { it.categoryId == selectedCategoryId }
            if (matched < 0) 0 else matched + 1
        }
        val visibleItems = chipListState.layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty() || visibleItems.none { it.index == targetIndex }) {
            chipListState.animateScrollToItem(targetIndex)
        }
    }

    LazyRow(
        state = chipListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = AppTheme.dimens.paddingNormal,
                end = AppTheme.dimens.paddingNormal,
                top = 2.dp,
                bottom = 8.dp,
            ),
        // 容器级左右留白，与一级 tab 行、下方卡片网格的 16dp 对齐。
        // 用 contentPadding 只在内容首尾留白，横向滚动时 chip 仍会顶到屏幕边缘，
        // 看起来就像整行没有边界、比上下多出一截。
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item(key = "chip_all") {
            CategoryFilterChip(
                label = allLabel,
                selected = selectedCategoryId == null,
                onClick = { onSelect(null) },
                onClose = onClose,
                closeLabel = closeLabel,
            )
        }
        items(items = chips, key = { it.categoryId ?: 0 }) { chip ->
            CategoryFilterChip(
                label = chip.name,
                selected = selectedCategoryId == chip.categoryId,
                onClick = { onSelect(chip.categoryId) },
                onClose = onClose,
                closeLabel = closeLabel,
            )
        }
        item(key = "chip_manage_tags") {
            CategoryFilterChip(
                label = manageLabel,
                selected = false,
                onClick = onManageTags,
                leadingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLabel,
            )
        }
    }
}

/**
 * 二级分类 chip：28dp 高、labelMedium 单行文字、尾部关闭图标。
 * 玻璃浓度与文字对比都压低一档，作为一级 tab 与内容之间的辅助层，不抢注意力。
 *
 * 关闭图标有自己的点击区，命中时不会冒泡到 chip 主体，因此「取消筛选并收起整条 chip 栏」
 * 与「点主体改选分类」互不干扰；末尾不再需要额外的关闭按钮。
 */
@Composable
private fun CategoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit = {},
    closeLabel: String = "",
    leadingIcon: ImageVector? = null,
) {
    val closeInteractionSource = remember { MutableInteractionSource() }
    val neutralContainer = MaterialTheme.colorScheme.surfaceContainer
    // 一级 tab 选中走 primary 系，二级 chip 选中改用 tertiary 系：
    // 背景与文字/图标统一到 tertiaryContainer + onTertiaryContainer 同一色系，
    // 修掉 M3 默认 onSecondaryContainer 与玻璃底色不同色系的问题，也拉出层级。
    val selectedContainer = MaterialTheme.colorScheme.tertiaryContainer
    // 文字与图标压低对比：未选中项更淡，选中态仍保持可读，形成主次。
    val selectedContent = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
    val unselectedContent = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
    GlassFilterChip(
        selected = selected,
        onClick = onClick,
        // 比 Dense 淡一档，玻璃底色更透；层级用 GlassStyle 阶梯表达，不给容器色烘 alpha。
        style = GlassStyle.Thick,
        glassContainerColor = neutralContainer,
        glassSelectedContainerColor = selectedContainer,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = neutralContainer,
            selectedContainerColor = selectedContainer,
            labelColor = unselectedContent,
            selectedLabelColor = selectedContent,
            selectedLeadingIconColor = selectedContent,
        ),
        modifier = Modifier.height(28.dp),
        border = null,
        glassBorderWidth = 0.dp,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        },
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = unselectedContent,
                )
            }
        },
        trailingIcon = if (selected) {
            {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = closeInteractionSource,
                            indication = null,
                            onClick = onClose,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = closeLabel,
                        tint = selectedContent,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        } else {
            null
        },
        // 带 × 的选中 chip 把右侧内边距收窄，抵消图标槽位，
        // 让文字与 × 图形到各自边缘的视觉距离接近左侧。
        contentPadding = PaddingValues(
            start = 8.dp,
            end = if (selected) 4.dp else 8.dp,
            top = 0.dp,
            bottom = 0.dp,
        ),
    )
}

private fun createScreenForListType(listType: ListItemType): Screen = when (listType) {
    ListItemType.AGENT -> Screen.CreateAIAgent()
    ListItemType.PROMPT -> Screen.CreateAIChatPrompt()
    ListItemType.HTML -> Screen.CreateHtml()
    else -> Screen.CreateNote()
}

@Composable
private fun EmptyOrErrorState(
    pagingItems: LazyPagingItems<ItemWithCategoriesAndStats>,
    listType: ListItemType,
    isFiltered: Boolean,
    onClearFilter: () -> Unit,
    onManualCreate: () -> Unit,
    onAiCreate: () -> Unit,
    showAiCreate: Boolean = true,
) {
    when {
        // 同步/加载失败时用本地数据兜底，不弹错误提示，让用户无感知。
        // 仍保留“新增”引导卡片，方便创建本地内容。
        pagingItems.loadState.refresh is LoadState.Error ||
                pagingItems.loadState.refresh is LoadState.NotLoading &&
                pagingItems.itemCount == 0 &&
                pagingItems.loadState.mediator?.refresh !is LoadState.Loading -> HomeEmptyState(
            listType = listType,
            isFiltered = isFiltered,
            onClearFilter = onClearFilter,
            onManualCreate = onManualCreate,
            onAiCreate = onAiCreate,
            showAiCreate = showAiCreate,
        )

        else -> SkeletonItemGrid(isGrid = LocalSettingsState.current.groupOptionsByTypes)
    }
}

@Composable
private fun SkeletonItemGrid(isGrid: Boolean) {
    LazyVerticalStaggeredGrid(
        modifier = Modifier
            .fillMaxWidth(),
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
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f))
        )
    }
}

@Composable
private fun ItemGrid(
    itemListComponent: ItemListComponent,
    pagingItems: LazyPagingItems<ItemWithCategoriesAndStats>,
    isGrid: Boolean,
    listType: ListItemType,
    chipCategoryId: Int?,
    onCategoryBadgeTap: (Int?) -> Unit,
    onManualCreate: () -> Unit,
    onAiCreate: () -> Unit,
    showAiCreate: Boolean = true,
) {
    val onNavigator = LocalOnNavigate.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState()
    val deleteState = rememberDeleteState()
    val density = LocalDensity.current
    // 最近一张普通卡片的实测高度,用于"创建新xx"引导卡与旁边卡片对齐(双列瀑布流本身不保证行高一致)
    var siblingCardHeight by remember { mutableStateOf<Dp?>(null) }

    val isNoteTab = listType == ListItemType.NOTE
    val cardMaxTitleLines = if (isNoteTab) 2 else 1

    val headPin = remember(listType.id, chipCategoryId) { HeadPin() }
    val headFollow = remember(listType.id, chipCategoryId) {
        HeadFollowConnection(
            atHead = { gridState.isAtListHead() },
            onUserSettled = { atHead ->
                itemListComponent.setFollowsHead(listType, chipCategoryId, atHead)
            },
        )
    }
    PinListToNewestHead(headPin, gridState, pagingItems, itemListComponent, listType, chipCategoryId)

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
        modifier = Modifier
            .fillMaxHeight()
            .nestedScroll(headFollow),
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
                modifier = Modifier.onSizeChanged { size ->
                    if (size.height > 0) {
                        siblingCardHeight = with(density) { size.height.toDp() }
                    }
                },
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
        // 应用(tab)列表不需要"创建新应用"卡片，其它 tab 保留。
        if (listType != ListItemType.NORMAL) {
            item(key = "append_create_choice_card") {
                CreateChoiceCard(
                    onManualCreate = onManualCreate,
                    onAiCreate = onAiCreate,
                    siblingHeight = siblingCardHeight,
                    showAiCreate = showAiCreate,
                )
            }
        }
    }
}

/**
 * 打开后同步写回时，列表还停在头部就把下一次测量钉在第 0 项。
 *
 * 卡片有稳定 key，数据一变，瀑布流会把视口留在原来那张卡片上。
 * 刷新窗口如果以那张卡片为中心，最新的就在它上面。这里在测量前声明
 * 「这次从第 0 项排」，用的是条目下标，不是像素坐标。
 * 完全贴顶才算头部：第 0 项还露着但已经滑出一段，不算。
 * 人离开头部后（[ItemListComponent.followsHead] 为 false）就不再钉。
 */
@Composable
private fun PinListToNewestHead(
    gate: HeadPin,
    gridState: LazyStaggeredGridState,
    pagingItems: LazyPagingItems<ItemWithCategoriesAndStats>,
    itemListComponent: ItemListComponent,
    listType: ListItemType,
    chipCategoryId: Int?,
) {
    val refreshLoad = pagingItems.loadState.refresh
    val itemCount = pagingItems.itemCount
    // itemCount == 0 时 peek(0) 会走到 ItemSnapshotList 的越界分支。
    // 网格目前只在有条目时组合，这里仍挡住空列表。
    val headId = if (itemCount > 0) pagingItems.peek(0)?.item?.id else null
    // 在组合阶段读取，滑动结束会重组并再跑一次 SideEffect。
    // 只在 SideEffect 里读的话，这次刷新赶上滑动时 armed 会悬着，落点出来也不补。
    val scrolling = gridState.isScrollInProgress
    SideEffect {
        if (refreshLoad is LoadState.Loading) gate.sawLoading = true
        if (refreshLoad is LoadState.NotLoading && gate.sawLoading) {
            gate.sawLoading = false
            gate.armed = true
        }
        val atHead = gridState.isAtListHead()
        // 静止时按真实视口同步标记：进程恢复到中部、轻微下滑、读屏滚动都写得上。
        // 滑动过程中先不动，等落点再定；落点不在头部就放弃这次 pin。
        if (!scrolling) {
            itemListComponent.setFollowsHead(listType, chipCategoryId, atHead)
        }
        if (!atHead || scrolling || itemCount == 0 || headId == null) {
            if (!atHead && !scrolling) {
                gate.armed = false
                if (headId != null) gate.lastHeadId = headId
            }
            return@SideEffect
        }
        // 初次出现只记下头部，不打断第一次布局。
        // 贴顶时 index 和 offset 都是 0，这一次请求会丢掉上次布局记住的 key。
        // 第 0 项还露着但有偏移时不会走到这里（已经不算头部）。
        val headChanged = headId != gate.lastHeadId
        if (gate.armed || (headChanged && gate.lastHeadId != null)) {
            gridState.requestScrollToItem(0)
        }
        gate.armed = false
        gate.lastHeadId = headId
    }
}

/**
 * 第 0 项贴住容器起点。只看 index 会把「第一张还露着、但已经往上滑了一截」当成还在头部。
 * 偏移不为 0 就不跟。若以后要留几 dp 容差，单次 requestScrollToItem 清不掉记住的 key，需要连调两次。
 */
private fun LazyStaggeredGridState.isAtListHead(): Boolean =
    firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0

private class HeadPin {
    var sawLoading: Boolean = false
    var armed: Boolean = false
    var lastHeadId: Int? = null
}

/** 手指滑离贴顶位置就停止跟随；滑回贴顶或惯性落在贴顶上再恢复。 */
private class HeadFollowConnection(
    private val atHead: () -> Boolean,
    private val onUserSettled: (Boolean) -> Unit,
) : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (source == NestedScrollSource.UserInput && consumed != Offset.Zero) {
            onUserSettled(atHead())
        }
        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        onUserSettled(atHead())
        return Velocity.Zero
    }
}

private fun uidFor(listType: ListItemType): String = commentUidForListType(listType.id)

@Composable
private fun gridColumns(isGrid: Boolean): StaggeredGridCells {
    val isPortraitOrCompact by isPortraitOrCompactWidthLayoutAsState()
    val isCompact = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact

    return when {
        !isGrid -> StaggeredGridCells.Adaptive(minSize = 280.dp)
        isCompact -> StaggeredGridCells.Fixed(count = 2)
        isPortraitOrCompact -> StaggeredGridCells.Adaptive(minSize = 280.dp)
        else -> StaggeredGridCells.Adaptive(minSize = 240.dp)
    }
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
