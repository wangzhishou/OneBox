package com.wanbaohe.app.app_screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.shifenmiao.base.manager.DeleteConfirmationManager
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.base.ui.AdvancedDeleteConfirmDialog
import com.shifenmiao.base.ui.button.SmallFixedHeightButton
import com.shifenmiao.base.ui.icon.BuildCustomIcon
import com.shifenmiao.common.components.ClickInfoType
import com.shifenmiao.common.components.FeaturedGrid
import com.shifenmiao.common.components.SectionTheme
import com.shifenmiao.common.components.sectionIconColor
import com.shifenmiao.common.components.sectionIconContainerColor
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.TopActions
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.model.activity.ActivityLogEntry
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassMedium
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.t8rin.imagetoolbox.core.ui.widget.text.HtmlText
import com.wanbaohe.app.component.ActivityLogComponent
import com.wanbaohe.app.component.FavoriteComponent
import com.wanbaohe.app.navigation.ActivityLogNavigator
import com.wanbaohe.com.string.TimeFormatter
import kotlinx.coroutines.launch
import java.text.DateFormat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore

// ══════════════════════════════════════════════════════════════
//  分区 Key 前缀（用于顶部标题跟随）
// ══════════════════════════════════════════════════════════════
private const val KEY_FEATURED = "section_featured"
private const val KEY_MINE = "section_mine"
private const val KEY_FAVORITE = "section_favorite"
private const val KEY_RECENT = "section_recent"
private const val KEY_HISTORY = "section_history"

@Composable
fun FavoriteScreen(
    activityLogComponent: ActivityLogComponent,
    appComponent: AppComponent,
    favoriteComponent: FavoriteComponent
) {
    val lazyListState = rememberLazyListState()

    // 动态获取当前顶部可见的 Section 标题
    val currentTitleRes by remember {
        derivedStateOf {
            val visibleKeys = lazyListState.layoutInfo.visibleItemsInfo.map { it.key }
            when {
                visibleKeys.any { it.toString().startsWith(KEY_RECENT) } -> R.string.recent_visits
                visibleKeys.any {
                    it.toString().startsWith(KEY_FEATURED)
                } -> R.string.favorite_featured_title

                visibleKeys.any {
                    it.toString().startsWith(KEY_MINE)
                } -> R.string.favorite_mine_title

                visibleKeys.any { it.toString().startsWith(KEY_FAVORITE) } -> R.string.favorite
                visibleKeys.any { it.toString().startsWith(KEY_HISTORY) } -> R.string.ai_history
                else -> R.string.recent_visits
            }
        }
    }

    BaseScreen(
        title = {
            AnimatedContent(targetState = currentTitleRes, label = "TitleAnimation") { titleRes ->
                Text(text = stringResource(id = titleRes))
            }
        },
        type = EnhancedTopAppBarType.Normal,
        onGoBack = { appComponent.onGoBack() },
        actions = {
            TopActions(appComponent)
        },
        navigationIcon = {},
        isShowDefaultActions = false,
        supportGlassEffect = true,
        showNavigationBarsPadding = false
    ) {
        ContentListSection(
            lazyListState = lazyListState,
            activityLogComponent = activityLogComponent,
            favoriteComponent = favoriteComponent
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  主内容区（LazyColumn，共 5 个分区）
// ══════════════════════════════════════════════════════════════

@Composable
private fun ContentListSection(
    modifier: Modifier = Modifier,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    activityLogComponent: ActivityLogComponent,
    favoriteComponent: FavoriteComponent
) {
    val recommended by favoriteComponent.recommendedFlow.collectAsState()
    val editableItems by favoriteComponent.editableFlow.collectAsState()
    val favoritedItems by favoriteComponent.favoritedFlow.collectAsState()
    val recentItems by favoriteComponent.recentClickedFlow.collectAsState()
    val activityLogItems = activityLogComponent.activityLogFlow.collectAsLazyPagingItems()
    val showDeleteDialog = remember { mutableStateOf(false) }
    val selectedLogEntry = remember { mutableStateOf<ActivityLogEntry?>(null) }
    val showDeleteMineDialog = remember { mutableStateOf(false) }
    val selectedMineItem = remember { mutableStateOf<ItemWithCategoriesAndStats?>(null) }
    val onNavigate = LocalOnNavigate.current

    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top)
    ) {
        // ── 1. 最近 Section ──
        if (recentItems.isNotEmpty()) {
            item(key = "${KEY_RECENT}_row") {
                FeaturedGrid(
                    items = recentItems,
                    clickInfo = ClickInfoType.TIME,
                    reverseTheme = true,
                    onItemClick = { item ->
                        favoriteComponent.handleItemClick(item, onNavigate)
                    }
                )
            }
            item(key = "${KEY_RECENT}_spacer") {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            }
        }

        // ── 2. 精选 Section ──
        if (recommended.isNotEmpty()) {
            item(key = KEY_FEATURED) {
                SectionHeader(title = stringResource(R.string.favorite_featured_title))
            }
            item(key = "${KEY_FEATURED}_grid") {
                FeaturedGrid(
                    items = recommended,
                    clickInfo = ClickInfoType.COUNT,
                    onItemClick = { item ->
                        favoriteComponent.handleItemClick(item, onNavigate)
                    }
                )
            }
            item(key = "${KEY_FEATURED}_spacer") {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            }
        }

        // ── 3. 我的 Section ──
        if (editableItems.isNotEmpty()) {
            item(key = KEY_MINE) {
                SectionHeader(title = stringResource(R.string.favorite_mine_title))
            }
            itemsIndexed(
                items = editableItems,
                key = { index, item -> "${KEY_MINE}_item_${item.item.id}_$index" }
            ) { index, item ->
                MineListItemCard(
                    itemWithCategories = item,
                    themeIndex = index,
                    onClick = {
                        favoriteComponent.handleItemClick(item, onNavigate)
                    },
                    showMenu = true,
                    onEdit = {
                        favoriteComponent.handleItemClick(item, onNavigate)
                    },
                    onDelete = {
                        selectedMineItem.value = item
                        showDeleteMineDialog.value = true
                    }
                )
                Spacer(modifier = Modifier.height(AppTheme.dimens.spaceExtraSmall))
            }
            item(key = "${KEY_MINE}_spacer") {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            }
        }

        // ── 4. 收藏 Section ──
        if (favoritedItems.isNotEmpty()) {
            item(key = KEY_FAVORITE) {
                SectionHeader(title = stringResource(R.string.favorite))
            }
            item(key = "${KEY_FAVORITE}_grid") {
                CompactGrid(
                    items = favoritedItems,
                    onItemClick = { item ->
                        favoriteComponent.handleItemClick(item, onNavigate)
                    }
                )
            }
            item(key = "${KEY_FAVORITE}_spacer") {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            }
        }

        // ── 5. 历史记录 Section ──
        item(key = KEY_HISTORY) {
            SectionHeader(
                stringResource(id = R.string.ai_history)
            ) {
                if (activityLogItems.itemCount > 0) {
                    SmallFixedHeightButton(
                        text = stringResource(id = R.string.clear_history),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.DeleteSweep,
                        onClick = {
                            activityLogComponent.clearAll()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
        if (activityLogItems.itemCount > 0) {
            items(activityLogItems.itemCount, key = { index ->
                activityLogItems[index]?.let { "${KEY_HISTORY}_${it.id}" }
                    ?: "${KEY_HISTORY}_placeholder_$index"
            }) { index ->
                val entry = activityLogItems[index]
                if (entry != null) {
                    ActivityLogItem(
                        entry = entry,
                        onDeleteRequested = {
                            selectedLogEntry.value = entry
                            showDeleteDialog.value = true
                        }
                    )
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))
                }
            }
        } else {
            item(key = "${KEY_HISTORY}_empty") {
                EmptyHistoryPlaceholder()
            }
        }

        // 底部间距
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 删除确认对话框
    if (showDeleteDialog.value) {
        AdvancedDeleteConfirmDialog(
            operationType = DeleteConfirmationManager.OperationType.HISTORY_ITEM,
            showDialog = showDeleteDialog,
            onConfirm = {
                selectedLogEntry.value?.let { activityLogComponent.deleteEntry(it) }
            },
            title = stringResource(R.string.ai_chat_delete_title),
            message = stringResource(R.string.ai_chat_delete_message)
        )
    }

    // "我的"删除确认对话框
    if (showDeleteMineDialog.value) {
        AdvancedDeleteConfirmDialog(
            operationType = DeleteConfirmationManager.OperationType.FAVORITE_ITEM,
            showDialog = showDeleteMineDialog,
            onConfirm = {
                selectedMineItem.value?.let { favoriteComponent.deleteItem(it.item.id) }
            },
            title = stringResource(R.string.ai_chat_delete_title),
            message = stringResource(
                R.string.delete_item_confirm_message,
                selectedMineItem.value?.item?.title.orEmpty()
            )
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  分区标题
// ══════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        trailing?.invoke()
    }
}

// ══════════════════════════════════════════════════════════════
//  4. 收藏 — 紧凑双列卡片
// ══════════════════════════════════════════════════════════════

@Composable
private fun CompactGrid(
    items: List<ItemWithCategoriesAndStats>,
    onItemClick: (ItemWithCategoriesAndStats) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (i in items.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactGridItemCard(
                    item = items[i],
                    themeIndex = i,
                    modifier = Modifier.weight(1f),
                    onClick = { onItemClick(items[i]) }
                )
                if (i + 1 < items.size) {
                    CompactGridItemCard(
                        item = items[i + 1],
                        themeIndex = i + 1,
                        modifier = Modifier.weight(1f),
                        onClick = { onItemClick(items[i + 1]) }
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CompactGridItemCard(
    item: ItemWithCategoriesAndStats,
    themeIndex: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = when (themeIndex % 4) {
        0 -> SectionTheme.TERTIARY
        1 -> SectionTheme.SECONDARY
        2 -> SectionTheme.PRIMARY
        else -> SectionTheme.SURFACE
    }
    val iconBgColor = sectionIconContainerColor(theme).copy(alpha = 0.9f)
    val iconTint = sectionIconColor(theme)

    GlassCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .glassMedium(
                        shape = RoundedCornerShape(12.dp),
                        color = iconBgColor,
                    ),
                contentAlignment = Alignment.Center
            ) {
                val iconName = item.item.iconName?.ifEmpty { item.item.title }
                if (!iconName.isNullOrEmpty()) {
                    BuildCustomIcon(
                        iconName = iconName,
                        modifier = Modifier.size(20.dp),
                        tint = iconTint
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.item.title.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.clickCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.favorite_usage_count, item.clickCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = TimeFormatter.formatRelativeTime(java.util.Date(item.item.updatedAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  3. 我的 — 纵向列表卡片
// ══════════════════════════════════════════════════════════════

@Composable
private fun MineListItemCard(
    itemWithCategories: ItemWithCategoriesAndStats,
    themeIndex: Int,
    onClick: () -> Unit,
    showMenu: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val entity = itemWithCategories.item
    val theme = when (themeIndex % 4) {
        0 -> SectionTheme.TERTIARY
        1 -> SectionTheme.SECONDARY
        2 -> SectionTheme.PRIMARY
        else -> SectionTheme.SURFACE
    }
    val iconBgColor = sectionIconContainerColor(theme).copy(alpha = 0.9f)
    val iconTint = sectionIconColor(theme)
    var expanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .glassMedium(
                        shape = RoundedCornerShape(12.dp),
                        color = iconBgColor,
                    ),
                contentAlignment = Alignment.Center
            ) {
                val iconName = entity.iconName?.ifEmpty { entity.title }
                BuildCustomIcon(
                    iconName = iconName,
                    modifier = Modifier.size(20.dp),
                    tint = iconTint
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.title.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = TimeFormatter.formatRelativeTime(itemWithCategories.clickTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (itemWithCategories.clickCount > 0) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = stringResource(R.string.favorite_usage_count, itemWithCategories.clickCount),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            if (showMenu) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore,
                            contentDescription = "More",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit)) },
                            onClick = {
                                expanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            onClick = {
                                expanded = false
                                onDelete()
                            }
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  5. 历史记录分区
// ══════════════════════════════════════════════════════════════
@Composable
@Suppress("DEPRECATION")
private fun ActivityLogItem(
    entry: ActivityLogEntry,
    onDeleteRequested: () -> Unit
) {
    val onNavigate = LocalOnNavigate.current
    val dataDraftHelper = LocalDataDraftHelper.current
    val logScope = rememberCoroutineScope()
    val shape = RoundedCornerShape(16.dp)

    SwipeToDismissBox(
        state = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                if (dismissValue == SwipeToDismissBoxValue.EndToStart
                    || dismissValue == SwipeToDismissBoxValue.StartToEnd
                ) {
                    onDeleteRequested()
                    false
                } else false
            }
        ),
        backgroundContent = {},
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassRegular(shape = shape)
                    .clickable {
                        logScope.launch {
                            ActivityLogNavigator.resolve(entry, dataDraftHelper)?.let { screen ->
                                onNavigate(screen)
                            }
                        }
                    }
                    .padding(horizontal = 16.dp)
            ) {
                ActivityLogItemContent(entry = entry)
            }
        }
    )
}

@Composable
private fun ActivityLogItemContent(entry: ActivityLogEntry) {
    val localUrlNavigator = LocalUrlNavigator.current
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        HtmlText(
            html = entry.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            hyperlinkStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary
            ),
            onHyperlinkClick = { localUrlNavigator.navigate(it) },
            overflow = TextOverflow.Ellipsis,
            maxLines = 3,
            enableAutoLinkify = false
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (entry.appTitle.isNotEmpty()) {
                HistoryTitleTag(title = entry.appTitle)
            }
            Spacer(modifier = Modifier.weight(1f))
            val dateFormat = DateFormat.getDateInstance(DateFormat.LONG)
            val formattedDate = dateFormat.format(entry.createdAt)
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun HistoryTitleTag(title: String) {
    Text(
        modifier = Modifier
            .glassBackground(
                style = GlassStyle.Thick,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
private fun EmptyHistoryPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .glassRegular(shape = RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.ai_history_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            )
        }
    }
}

