package com.wanbaohe.app.app_screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.shifenmiao.common.components.sectionGradient
import com.shifenmiao.common.components.sectionIconColor
import com.shifenmiao.common.components.sectionIconContainerColor
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.TopActions
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.model.activity.ActivityCategory
import com.shifenmiao.model.activity.ActivityLogEntry
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassMedium
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.t8rin.imagetoolbox.core.ui.widget.text.HtmlText
import com.wanbaohe.app.component.ActivityLogComponent
import com.wanbaohe.app.component.FavoriteComponent
import com.wanbaohe.app.navigation.ActivityLogNavigator
import com.wanbaohe.com.string.TimeFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAgent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiDuelChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAudioFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBlog
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBook
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBookkeeping
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMarkdownEdit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LineOcrDocument
import com.t8rin.imagetoolbox.core.resources.icons.line.LineResizeConvert
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTeleprompter
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.t8rin.imagetoolbox.core.resources.icons.line.LineXiangqi
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
                    val previous = if (index > 0) activityLogItems.peek(index - 1) else null
                    val next = if (index < activityLogItems.itemCount - 1) {
                        activityLogItems.peek(index + 1)
                    } else null
                    val isFirstInDay = previous == null ||
                        !isSameDay(previous.createdAt, entry.createdAt)
                    val isLastInDay = next == null ||
                        !isSameDay(next.createdAt, entry.createdAt)
                    ActivityLogTimelineItem(
                        entry = entry,
                        showDateHeader = isFirstInDay,
                        isFirstInDay = isFirstInDay,
                        isLastInDay = isLastInDay,
                        onDeleteRequested = {
                            selectedLogEntry.value = entry
                            showDeleteDialog.value = true
                        }
                    )
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
//  5. 历史记录分区 — 时间线样式
// ══════════════════════════════════════════════════════════════

private val recordTimeFormat by lazy { SimpleDateFormat("HH:mm", Locale.getDefault()) }

/** 时间线左轨宽度、圆点中心坐标（相对左轨左上） */
private val railWidth = 52.dp
private val railDotSize = 8.dp
private val railLineX = 42.dp
private val railDotCenterY = 25.dp

@Composable
private fun ActivityLogTimelineItem(
    entry: ActivityLogEntry,
    showDateHeader: Boolean,
    isFirstInDay: Boolean,
    isLastInDay: Boolean,
    onDeleteRequested: () -> Unit
) {
    val theme = recordSectionTheme(entry.category)
    val accent = sectionIconColor(theme)
    Column(modifier = Modifier.fillMaxWidth()) {
        if (showDateHeader) {
            Text(
                text = recordDayHeaderText(entry.createdAt),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            TimelineRail(
                timeText = recordTimeFormat.format(entry.createdAt),
                accent = accent,
                isFirstInDay = isFirstInDay,
                isLastInDay = isLastInDay
            )
            ActivityLogCard(
                entry = entry,
                theme = theme,
                onDeleteRequested = onDeleteRequested,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 6.dp, bottom = 6.dp)
            )
        }
    }
}

/** 左轨：时间文本 + 彩色圆点 + 连接线（组内首/尾截断） */
@Composable
private fun TimelineRail(
    timeText: String,
    accent: Color,
    isFirstInDay: Boolean,
    isLastInDay: Boolean
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    Box(
        modifier = Modifier
            .width(railWidth)
            .fillMaxHeight()
            .drawBehind {
                val x = railLineX.toPx()
                val dotY = railDotCenterY.toPx()
                val stroke = 1.dp.toPx()
                if (!isFirstInDay) {
                    drawLine(lineColor, Offset(x, 0f), Offset(x, dotY), strokeWidth = stroke)
                }
                if (!isLastInDay) {
                    drawLine(lineColor, Offset(x, dotY), Offset(x, size.height), strokeWidth = stroke)
                }
            }
    ) {
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = railWidth - railLineX + railDotSize / 2 + 4.dp, top = 16.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    end = railWidth - railLineX - railDotSize / 2,
                    top = railDotCenterY - railDotSize / 2
                )
                .size(railDotSize)
                .background(accent, CircleShape)
        )
    }
}

@Composable
@Suppress("DEPRECATION")
private fun ActivityLogCard(
    entry: ActivityLogEntry,
    theme: SectionTheme,
    onDeleteRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onNavigate = LocalOnNavigate.current
    val dataDraftHelper = LocalDataDraftHelper.current
    val logScope = rememberCoroutineScope()
    val shape = RoundedCornerShape(20.dp)

    SwipeToDismissBox(
        modifier = modifier,
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
            ActivityLogCardContent(
                entry = entry,
                theme = theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .glassRegular(shape = shape, color = sectionGradient(theme))
                    .clickable {
                        logScope.launch {
                            ActivityLogNavigator.resolve(entry, dataDraftHelper)?.let { screen ->
                                onNavigate(screen)
                            }
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }
    )
}

@Composable
private fun ActivityLogCardContent(
    entry: ActivityLogEntry,
    theme: SectionTheme,
    modifier: Modifier = Modifier
) {
    val localUrlNavigator = LocalUrlNavigator.current
    val accent = sectionIconColor(theme)
    val topLabel = entry.appTitle.trim()
    var rowTitle = entry.title.trim()
    if (rowTitle.isEmpty() && entry.description.isBlank()) {
        rowTitle = topLabel.ifEmpty { stringResource(R.string.record_tab_title) }
    }

    Column(modifier = modifier) {
        if (topLabel.isNotEmpty() && topLabel != rowTitle) {
            Text(
                text = topLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sectionIconContainerColor(theme).copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = recordCategoryIcon(entry.category),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = accent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                if (rowTitle.isNotEmpty()) {
                    Text(
                        text = rowTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                if (entry.description.isNotBlank()) {
                    HtmlText(
                        html = entry.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        hyperlinkStyle = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        onHyperlinkClick = { localUrlNavigator.navigate(it) },
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        enableAutoLinkify = false
                    )
                }
            }
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                contentDescription = null,
                modifier = Modifier.size(9.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * 各活动分类的卡片主题色 — 复用精选卡片的 SectionTheme 体系，
 * 圆点 / 标签 / 图标用 sectionIconColor，卡片底色用 sectionGradient。
 */
private fun recordSectionTheme(category: ActivityCategory): SectionTheme = when (category) {
    ActivityCategory.AI_CHAT -> SectionTheme.PRIMARY
    ActivityCategory.AI_DUEL -> SectionTheme.SECONDARY
    ActivityCategory.AI_AGENT -> SectionTheme.TERTIARY
    ActivityCategory.IMAGE_EDIT -> SectionTheme.SECONDARY
    ActivityCategory.FILE_CONVERT -> SectionTheme.TERTIARY
    ActivityCategory.NOTE_EDIT -> SectionTheme.PRIMARY
    ActivityCategory.HTML_EDIT -> SectionTheme.SECONDARY
    ActivityCategory.AUDIO_EDIT -> SectionTheme.TERTIARY
    ActivityCategory.BLOG_POST -> SectionTheme.PRIMARY
    ActivityCategory.OCR_DOCUMENT -> SectionTheme.SECONDARY
    ActivityCategory.BOOKKEEPING -> SectionTheme.TERTIARY
    ActivityCategory.TODO -> SectionTheme.PRIMARY
    ActivityCategory.XIANGQI -> SectionTheme.SECONDARY
    ActivityCategory.TELEPROMPTER -> SectionTheme.TERTIARY
    ActivityCategory.HABIT -> SectionTheme.PRIMARY
    ActivityCategory.POEM -> SectionTheme.SECONDARY
    ActivityCategory.OTHER -> SectionTheme.SURFACE
}

private fun recordCategoryIcon(category: ActivityCategory): ImageVector = when (category) {
    ActivityCategory.AI_CHAT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat
    ActivityCategory.AI_DUEL -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiDuelChat
    ActivityCategory.AI_AGENT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAgent
    ActivityCategory.IMAGE_EDIT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage
    ActivityCategory.FILE_CONVERT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineResizeConvert
    ActivityCategory.NOTE_EDIT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote
    ActivityCategory.HTML_EDIT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkdownEdit
    ActivityCategory.AUDIO_EDIT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAudioFile
    ActivityCategory.BLOG_POST -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlog
    ActivityCategory.OCR_DOCUMENT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineOcrDocument
    ActivityCategory.BOOKKEEPING -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBookkeeping
    ActivityCategory.TODO -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList
    ActivityCategory.XIANGQI -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineXiangqi
    ActivityCategory.TELEPROMPTER -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTeleprompter
    ActivityCategory.HABIT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckCircleOutline
    ActivityCategory.POEM -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook
    ActivityCategory.OTHER -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory
}

private fun isSameDay(a: Date, b: Date): Boolean {
    val ca = Calendar.getInstance().apply { time = a }
    val cb = Calendar.getInstance().apply { time = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
        ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
}

/** 分组标题，如「今天 · 8月15日」「昨天 · 8月14日」「8月13日」 */
@Composable
private fun recordDayHeaderText(date: Date): String {
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
    val dayLabel = when {
        TimeFormatter.isToday(date.time) -> stringResource(R.string.activity_log_today)
        isSameDay(date, yesterday) -> stringResource(R.string.activity_log_yesterday)
        else -> null
    }
    val sameYear = Calendar.getInstance().get(Calendar.YEAR) ==
        Calendar.getInstance().apply { time = date }.get(Calendar.YEAR)
    val isZh = Locale.getDefault().language == Locale.CHINESE.language
    val pattern = when {
        isZh && sameYear -> "M月d日"
        isZh -> "yyyy年M月d日"
        sameYear -> "MMM d"
        else -> "MMM d, yyyy"
    }
    val dateText = SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    return listOfNotNull(dayLabel, dateText).joinToString(" · ")
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

