package com.wanbaohe.householditems.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendarAlert
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCatBulky
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocationOn
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePieChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSearchTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.householditems.R
import com.wanbaohe.householditems.component.HouseholdItemsComponent
import com.wanbaohe.householditems.model.ExpiryStatus
import com.wanbaohe.householditems.model.HouseholdDisplayMode
import com.wanbaohe.householditems.model.HouseholdItemUi
import com.wanbaohe.householditems.model.HouseholdItemsUiState
import com.wanbaohe.householditems.model.HouseholdLocationUi
import com.wanbaohe.householditems.model.HouseholdTab
import com.wanbaohe.householditems.model.categoryIconOrNull
import com.wanbaohe.householditems.model.locationIcon
import com.wanbaohe.householditems.model.locationPathOf
import com.wanbaohe.householditems.model.subtreeLocationIds
import java.io.File
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.filled.MoreVert

// ─────────────────────────────────────────────────────────────────────────────
// 家庭物品主页:底部三 tab(物品 / 位置 / 统计),列表 tab = 搜索 + 位置面包屑浏览
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HouseholdItemsScreen(
    component: HouseholdItemsComponent,
    onGoBack: () -> Unit,
) {
    val uiState by component.uiState.collectAsState()
    // 显示模式:用户手动切换优先,否则跟随系统设置(true=宫格)
    val displayMode = uiState.displayModeOverride
        ?: if (LocalSettingsState.current.groupOptionsByTypes) {
            HouseholdDisplayMode.GRID
        } else {
            HouseholdDisplayMode.LIST
        }

    // 轻量内部导航:编辑态整页替换主页,保存/返回回到列表
    if (uiState.showItemEditor) {
        ItemEditScreen(
            component = component,
            onGoBack = component::hideItemEditor,
        )
        return
    }

    BaseScreen(
        title = stringResource(R.string.household_title),
        onGoBack = onGoBack,
        actions = {
            IconButton(onClick = component::startAddItem) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.household_add_item),
                )
            }
        },
        supportGlassEffect = true,
        content = {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when (uiState.selectedTab) {
                        HouseholdTab.LIST -> ListTab(
                            uiState = uiState,
                            displayMode = displayMode,
                            component = component,
                            modifier = Modifier.fillMaxSize(),
                        )

                        HouseholdTab.LOCATIONS -> LocationManageTab(
                            component = component,
                            modifier = Modifier.fillMaxSize(),
                        )

                        HouseholdTab.STATS -> HouseholdStatsTab(
                            component = component,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                HouseholdBottomBar(
                    current = uiState.selectedTab,
                    onSelect = component::switchTab,
                )
            }
        },
        showNavigationBarsPadding = false,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 底部导航栏
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HouseholdBottomBar(
    current: HouseholdTab,
    onSelect: (HouseholdTab) -> Unit,
) {
    val tabs = listOf(
        HouseholdTab.LIST to BottomNavItem(
            id = HouseholdTab.LIST.name,
            label = stringResource(R.string.household_tab_list),
            icon = Icons.Outlined.LineViewList,
            contentDescription = stringResource(R.string.household_tab_list),
        ),
        HouseholdTab.STATS to BottomNavItem(
            id = HouseholdTab.STATS.name,
            label = stringResource(R.string.household_tab_stats),
            icon = Icons.Outlined.LinePieChart,
            contentDescription = stringResource(R.string.household_tab_stats),
        ),
        HouseholdTab.LOCATIONS to BottomNavItem(
            id = HouseholdTab.LOCATIONS.name,
            label = stringResource(R.string.household_tab_locations),
            icon = Icons.Outlined.LineLocationOn,
            contentDescription = stringResource(R.string.household_tab_locations),
        ),
    )

    BottomNavigationBar(
        items = tabs.map { it.second },
        selectedItemId = current.name,
        onItemClick = { item ->
            tabs.firstOrNull { it.second.id == item.id }?.first?.let(onSelect)
        },
        modifier = Modifier.fillMaxWidth(),
        showBar = true,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 列表 tab:搜索 + 位置浏览(List/Grid);空态给手动/AI 两个入口
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ListTab(
    uiState: HouseholdItemsUiState,
    displayMode: HouseholdDisplayMode,
    component: HouseholdItemsComponent,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        GlassSearchTextField(
            value = uiState.searchQuery,
            onValueChange = component::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = stringResource(R.string.household_search_hint),
        )
        when {
            uiState.searchQuery.isNotBlank() -> SearchResults(
                items = uiState.items,
                onItemClick = component::startEditItem,
                onItemDelete = component::deleteItem,
                onLocationTagClick = component::browseToLocation,
            )

            uiState.items.isEmpty() -> EmptyGuide(
                onAddManually = component::startAddItem,
                onAddWithAi = component::navigateToAiAssist,
            )

            else -> BrowseContent(
                uiState = uiState,
                displayMode = displayMode,
                onLocationClick = component::enterLocation,
                onBreadcrumbClick = component::navigateToBreadcrumb,
                onDisplayModeToggle = {
                    component.setDisplayMode(
                        if (displayMode == HouseholdDisplayMode.LIST) {
                            HouseholdDisplayMode.GRID
                        } else {
                            HouseholdDisplayMode.LIST
                        }
                    )
                },
                onItemClick = component::startEditItem,
                onItemDelete = component::deleteItem,
                onLocationTagClick = component::browseToLocation,
            )
        }
    }
}

@Composable
private fun EmptyGuide(
    onAddManually: () -> Unit,
    onAddWithAi: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.LineCatBulky,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.household_empty_guide_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.household_empty_guide_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        GlassTonalButton(
            onClick = onAddManually,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.household_add_manually))
        }
        GlassOutlinedButton(
            onClick = onAddWithAi,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            Text(stringResource(R.string.household_add_with_ai))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 搜索结果(始终列表形态)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchResults(
    items: List<HouseholdItemUi>,
    onItemClick: (HouseholdItemUi) -> Unit,
    onItemDelete: (String) -> Unit,
    onLocationTagClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (items.isEmpty()) {
            item { EmptyHint(stringResource(R.string.household_empty_search)) }
        }
        items(items, key = { it.id }) { item ->
            ItemRow(
                item = item,
                onClick = { onItemClick(item) },
                onDelete = { onItemDelete(item.id) },
                onLocationTagClick = onLocationTagClick,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 按位置浏览:面包屑(+视图切换)+ 子位置 chips + 当前层级物品(List/Grid)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BrowseContent(
    uiState: HouseholdItemsUiState,
    displayMode: HouseholdDisplayMode,
    onLocationClick: (String) -> Unit,
    onBreadcrumbClick: (String?) -> Unit,
    onDisplayModeToggle: () -> Unit,
    onItemClick: (HouseholdItemUi) -> Unit,
    onItemDelete: (String) -> Unit,
    onLocationTagClick: (String) -> Unit,
) {
    val children = uiState.locations.filter { it.parentId == uiState.currentLocationId }
    // 子树过滤:根 = 全部物品;选中位置 = 该位置及所有后代位置的物品
    val subtreeIds = subtreeLocationIds(uiState.locations, uiState.currentLocationId)
    val currentItems = uiState.items.filter { subtreeIds == null || it.locationId in subtreeIds }
    val breadcrumb = locationPathOf(uiState.locations, uiState.currentLocationId)
    val isEmpty = children.isEmpty() && currentItems.isEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        // ── 面包屑 + 视图切换 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BreadcrumbRow(
                breadcrumb = breadcrumb,
                onBreadcrumbClick = onBreadcrumbClick,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDisplayModeToggle) {
                Icon(
                    imageVector = if (displayMode == HouseholdDisplayMode.LIST) {
                        Icons.Outlined.LineQuickTiles
                    } else {
                        Icons.Outlined.LineViewList
                    },
                    contentDescription = stringResource(
                        if (displayMode == HouseholdDisplayMode.LIST) {
                            R.string.household_view_grid
                        } else {
                            R.string.household_view_list
                        }
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── 子位置 chips(点击进入,面包屑推进) ──
        if (children.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(children, key = { it.id }) { location ->
                    GlassFilterChip(
                        selected = false,
                        onClick = { onLocationClick(location.id) },
                        label = { Text(location.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = locationIcon(location.iconKey, location.id),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }
        }

        // ── 物品区 ──
        when (displayMode) {
            HouseholdDisplayMode.LIST -> ItemListContent(
                currentItems = currentItems,
                isEmpty = isEmpty,
                onItemClick = onItemClick,
                onItemDelete = onItemDelete,
                onLocationTagClick = onLocationTagClick,
            )

            HouseholdDisplayMode.GRID -> ItemGridContent(
                currentItems = currentItems,
                isEmpty = isEmpty,
                onItemClick = onItemClick,
                onItemDelete = onItemDelete,
                onLocationTagClick = onLocationTagClick,
            )
        }
    }
}

@Composable
private fun ItemListContent(
    currentItems: List<HouseholdItemUi>,
    isEmpty: Boolean,
    onItemClick: (HouseholdItemUi) -> Unit,
    onItemDelete: (String) -> Unit,
    onLocationTagClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(currentItems, key = { it.id }) { item ->
            ItemRow(
                item = item,
                onClick = { onItemClick(item) },
                onDelete = { onItemDelete(item.id) },
                onLocationTagClick = onLocationTagClick,
            )
        }
        if (isEmpty) {
            item(key = "empty") {
                EmptyHint(stringResource(R.string.household_empty_items))
            }
        }
    }
}

@Composable
private fun ItemGridContent(
    currentItems: List<HouseholdItemUi>,
    isEmpty: Boolean,
    onItemClick: (HouseholdItemUi) -> Unit,
    onItemDelete: (String) -> Unit,
    onLocationTagClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        gridItems(currentItems, key = { it.id }) { item ->
            ItemCard(
                item = item,
                onClick = { onItemClick(item) },
                onDelete = { onItemDelete(item.id) },
                onLocationTagClick = onLocationTagClick,
            )
        }
        if (isEmpty) {
            item(key = "empty", span = { GridItemSpan(2) }) {
                EmptyHint(stringResource(R.string.household_empty_items))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 面包屑
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BreadcrumbRow(
    breadcrumb: List<HouseholdLocationUi>,
    onBreadcrumbClick: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item {
            BreadcrumbSegment(
                text = stringResource(R.string.household_breadcrumb_home),
                isLast = breadcrumb.isEmpty(),
                onClick = { onBreadcrumbClick(null) },
            )
        }
        items(breadcrumb, key = { it.id }) { location ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LineChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BreadcrumbSegment(
                    text = location.name,
                    isLast = location.id == breadcrumb.lastOrNull()?.id,
                    onClick = { onBreadcrumbClick(location.id) },
                )
            }
        }
    }
}

@Composable
private fun BreadcrumbSegment(
    text: String,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (isLast) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 物品行(List)/ 物品卡片(Grid)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun ItemRow(
    item: HouseholdItemUi,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onLocationTagClick: (String) -> Unit,
) {
    val isExpired = item.expiryStatus == ExpiryStatus.EXPIRED
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        style = GlassStyle.Medium,
        // 过期整卡高亮:errorContainer 玻璃底 + error 描边
        colors = if (isExpired) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            null
        },
        border = if (isExpired) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f))
        } else {
            null
        },
    ) {
        // 右侧只留 4dp,让 ⋯ 按钮贴近卡片右缘
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ItemThumbnail(item = item)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                LocationTag(item = item, onClick = onLocationTagClick)
                if (item.note.isNotBlank()) {
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            ExpiryBadge(item = item)
            ItemOverflowMenu(onEdit = onClick, onDelete = onDelete)
        }
    }
}

/** 卡片/行右下 ⋯ 菜单:编辑 / 删除。自定义小尺寸(热区 32dp),不用 IconButton(强制 48dp) */
@Composable
private fun ItemOverflowMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable { expanded = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MaterialIcons.Filled.MoreVert,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                // 浅淡色,只做入口不做视觉焦点
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        EnhancedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.household_edit_item)) },
                onClick = {
                    expanded = false
                    onEdit()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.household_delete_item)) },
                onClick = {
                    expanded = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }
    }
}

/**
 * 位置 tag:主题色淡底小圆角,点击按该位置过滤(面包屑定位);
 * 无位置的物品不显示。嵌在卡片 onClick 内,子 clickable 优先消费点击,不冲突。
 */
@Composable
private fun LocationTag(
    item: HouseholdItemUi,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locationId = item.locationId ?: return
    if (item.locationPath.isBlank()) return
    Text(
        text = item.locationPath,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick(locationId) }
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/** 第三行信息:主题色圆点 + "分类 | 备注"(浅淡小字);都为空时不占位 */
@Composable
private fun CategoryNoteLine(item: HouseholdItemUi) {
    if (item.category.isBlank() && item.note.isBlank()) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        )
        Text(
            text = listOf(item.category, item.note)
                .filter { it.isNotBlank() }
                .joinToString("  |  "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

/** 宫格卡片:缩略图上、名称/位置 tag/保质期徽章下 */
@Composable
private fun ItemCard(
    item: HouseholdItemUi,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onLocationTagClick: (String) -> Unit,
) {
    val isExpired = item.expiryStatus == ExpiryStatus.EXPIRED
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        style = GlassStyle.Medium,
        // 过期整卡高亮:errorContainer 玻璃底 + error 描边
        colors = if (isExpired) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            null
        },
        border = if (isExpired) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f))
        } else {
            null
        },
    ) {
        Column {
            ItemCardCover(item = item)
            // 排版对齐特写稿:大封面 + 大粗名称 + 中灰位置行 + 浅淡"分类 | 备注"行;
            // 右侧只留 4dp,让 ⋯ 按钮贴近卡片右缘
            Column(
                modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                LocationTag(
                    item = item,
                    onClick = onLocationTagClick,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                ) {
                    // 第三行:有保质期显示倒计时徽章,否则显示"分类 · 备注"(原型稿样式)
                    Box(modifier = Modifier.weight(1f)) {
                        if (item.daysToExpire != null) {
                            ExpiryBadge(item = item)
                        } else {
                            CategoryNoteLine(item = item)
                        }
                    }
                    ItemOverflowMenu(onEdit = onClick, onDelete = onDelete)
                }
            }
        }
    }
}

@Composable
private fun ItemCardCover(item: HouseholdItemUi) {
    if (!item.photoPath.isNullOrBlank()) {
        AsyncImage(
            model = File(item.photoPath),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        // 无图:透明封面区 + 居中 Glass 圆角图标容器(分类图标,回退首字)
        val categoryIcon = categoryIconOrNull(item.category)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center,
        ) {
            GlassSurface(
                shape = RoundedCornerShape(16.dp),
                style = GlassStyle.Medium,
            ) {
                Box(
                    modifier = Modifier.padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (categoryIcon != null) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = item.category,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        Text(
                            text = (item.category.ifBlank { item.name }).take(1),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemThumbnail(item: HouseholdItemUi) {
    if (!item.photoPath.isNullOrBlank()) {
        AsyncImage(
            model = File(item.photoPath),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        // 无图:预设分类显示分类图标,否则回退分类(或名称)首字占位
        val categoryIcon = categoryIconOrNull(item.category)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxSize(),
            ) {}
            if (categoryIcon != null) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = item.category,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Text(
                    text = (item.category.ifBlank { item.name }).take(1),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ExpiryBadge(item: HouseholdItemUi) {
    val days = item.daysToExpire ?: return
    val (textRes, textArg, color) = when (item.expiryStatus) {
        ExpiryStatus.EXPIRED -> Triple(
            R.string.household_expired_days,
            -days,
            MaterialTheme.colorScheme.onErrorContainer,
        )

        ExpiryStatus.EXPIRING_SOON -> {
            if (days == 0L) {
                Triple(R.string.household_expires_today, 0L, MaterialTheme.colorScheme.onTertiaryContainer)
            } else {
                Triple(R.string.household_days_left, days, MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }

        else -> return
    }
    val text = if (textRes == R.string.household_expires_today) {
        stringResource(textRes)
    } else {
        stringResource(textRes, textArg)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.LineCalendarAlert,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = color,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
