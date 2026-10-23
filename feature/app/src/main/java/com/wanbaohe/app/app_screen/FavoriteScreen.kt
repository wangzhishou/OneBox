package com.wanbaohe.app.app_screen

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.shifenmiao.base.manager.DeleteConfirmationManager
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.wanbaohe.base.ui.AdvancedDeleteConfirmDialog
import com.wanbaohe.base.ui.button.SmallFixedHeightButton
import com.wanbaohe.base.ui.icon.BuildCustomIcon
import com.shifenmiao.common.components.ClickInfoType
import com.shifenmiao.common.components.FeaturedGrid
import com.shifenmiao.common.components.SectionTheme
import com.shifenmiao.common.components.sectionGradient
import com.shifenmiao.common.components.sectionIconColor
import com.shifenmiao.common.components.sectionIconContainerColor
import com.shifenmiao.common.components.sectionThemeForIndex
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.TopActions
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.model.activity.ActivityCategory
import com.shifenmiao.model.activity.ActivityLogEntry
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassMedium
import com.wanbaohe.core.ui.widget.glass.glassRegular
import com.wanbaohe.core.ui.widget.glass.tintedGlassContentColor
import com.wanbaohe.core.ui.widget.text.HtmlText
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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArchive
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAudioFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBlog
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBook
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBookkeeping
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
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
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.t8rin.imagetoolbox.core.resources.Icons
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.filled.MoreVert

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
        verticalArrangement = Arrangement.spacedBy(timelineItemGap, Alignment.Top)
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
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                        themeIndex = index,
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
    val iconBgColor = sectionIconContainerColor(theme)
    val iconTint = sectionIconColor(theme)

    GlassCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
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
                        text = pluralStringResource(R.plurals.favorite_usage_count, item.clickCount, item.clickCount),
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
    val iconBgColor = sectionIconContainerColor(theme)
    val iconTint = sectionIconColor(theme)
    var expanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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
                            text = pluralStringResource(R.plurals.favorite_usage_count, itemWithCategories.clickCount, itemWithCategories.clickCount),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            if (showMenu) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = MaterialIcons.Filled.MoreVert,
                            contentDescription = "More",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    EnhancedDropdownMenu(
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

/**
 * 时间线条目之间的间隔 —— 由上方 LazyColumn 的 Arrangement 统一留出。
 * 左轨连线只能画在自己条目内，间隔处会断开，所以同一天内要按这个值把线往下补一个间隔（见 [TimelineRail]）。
 */
private val timelineItemGap = 6.dp

@Composable
private fun ActivityLogTimelineItem(
    entry: ActivityLogEntry,
    themeIndex: Int,
    showDateHeader: Boolean,
    isFirstInDay: Boolean,
    isLastInDay: Boolean,
    onDeleteRequested: () -> Unit
) {
    // 与「精选」同一套轮换配色：每条记录一个色调, 只负责区分, 浓淡交给 GlassStyle 阶梯
    val theme = sectionThemeForIndex(themeIndex)
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

/** 左轨：时间文本 + 圆点 + 连接线（组内首/尾截断），整条轴用 outlineVariant 加一点透明度，不带记录色调 */
@Composable
private fun TimelineRail(
    timeText: String,
    isFirstInDay: Boolean,
    isLastInDay: Boolean
) {
    // 时间文本单独留 onSurfaceVariant：outlineVariant 那种明度压在 11sp 上会读不出来
    val railColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val dotColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)
    Box(
        modifier = Modifier
            .width(railWidth)
            .fillMaxHeight()
            .drawBehind {
                val x = railLineX.toPx()
                val dotY = railDotCenterY.toPx()
                val stroke = 1.dp.toPx()
                // 条目之间的 timelineItemGap 是列表留的空档, 只画自己的高度会在空档里断成两截。
                // 用和列表同一个 roundToPx 把线段往下多画一个空档, 正好接上下一段; 只往一边补,
                // 两段既不重叠(重叠处半透明线会叠深一道), 也不留缝。
                val bridge = timelineItemGap.roundToPx().toFloat()
                if (!isFirstInDay) {
                    drawLine(lineColor, Offset(x, 0f), Offset(x, dotY), strokeWidth = stroke)
                }
                if (!isLastInDay) {
                    drawLine(lineColor, Offset(x, dotY), Offset(x, size.height + bridge), strokeWidth = stroke)
                }
            }
    ) {
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelSmall,
            color = railColor,
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
                .background(dotColor, CircleShape)
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
    val shape = RoundedCornerShape(16.dp)
    val containerColor = sectionGradient(theme)

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
            GlassCard(
                onClick = {
                    logScope.launch {
                        ActivityLogNavigator.resolve(entry, dataDraftHelper)?.let { screen ->
                            onNavigate(screen)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                // 比「我的」的 Regular 再淡一档：每条记录透出自己的色调，但只是浅浅一层
                style = GlassStyle.Thin,
                colors = CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = tintedGlassContentColor(containerColor)
                ),
            ) {
                ActivityLogCardContent(
                    entry = entry,
                    theme = theme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
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
    val topLabel = entry.appTitle.trim()
    var rowTitle = entry.title.trim()
    if (rowTitle.isEmpty() && entry.description.isBlank()) {
        rowTitle = topLabel.ifEmpty { stringResource(R.string.record_tab_title) }
    }
    // 有色调的玻璃底上不要用成对的 onColor 猜对比度，按混合后的有效底色取文字色
    val contentColor = tintedGlassContentColor(sectionGradient(theme))
    val iconTint = sectionIconColor(theme)

    Column(modifier = modifier) {
        if (topLabel.isNotEmpty() && topLabel != rowTitle) {
            Text(
                text = topLabel,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .glassRegular(
                        // 卡片是 Thin，图标井深一档到 Regular，色跟卡片同属一个色调
                        shape = RoundedCornerShape(12.dp),
                        color = sectionIconContainerColor(theme),
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = recordCategoryIcon(entry.category),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconTint
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                if (rowTitle.isNotEmpty()) {
                    Text(
                        text = rowTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                if (entry.description.isNotBlank()) {
                    HtmlText(
                        html = entry.description,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        hyperlinkStyle = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onHyperlinkClick = { localUrlNavigator.navigate(it) },
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 4,
                        enableAutoLinkify = false
                    )
                }
            }
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * 各活动分类的图标。
 *
 * 操作记录每条记录按「精选」的轮换配色拿一个色调（PRIMARY/SECONDARY/TERTIARY/SURFACE），
 * 卡片用该色调的 container 走 GlassStyle.Thin，图标井深一档，图标用 onXContainer 上色；
 * 底色浓淡仍由 GlassStyle 阶梯决定，不要往 container 色里 copy(alpha = …)。
 */
private fun recordCategoryIcon(category: ActivityCategory): ImageVector = when (category) {
    ActivityCategory.AI_CHAT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat
    ActivityCategory.AI_DUEL -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiDuelChat
    ActivityCategory.AI_AGENT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAgent
    ActivityCategory.IMAGE_EDIT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage
    ActivityCategory.FILE_CONVERT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineResizeConvert
    ActivityCategory.ARCHIVE -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArchive
    ActivityCategory.NOTE_EDIT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote
    ActivityCategory.HTML_EDIT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkdownEdit
    ActivityCategory.AUDIO_EDIT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAudioFile
    ActivityCategory.BLOG_POST -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlog
    ActivityCategory.OCR_DOCUMENT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineOcrDocument
    ActivityCategory.BOOKKEEPING -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBookkeeping
    ActivityCategory.RECORD_CENTER -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite
    ActivityCategory.TODO -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList
    ActivityCategory.XIANGQI -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineXiangqi
    ActivityCategory.TELEPROMPTER -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTeleprompter
    ActivityCategory.HABIT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckCircleOutline
    ActivityCategory.POEM -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook
    ActivityCategory.HOUSEHOLD -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStorage
    ActivityCategory.PERIOD -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite
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
    val locale = LocalConfiguration.current.locales[0]
    val isZh = locale.language == Locale.CHINESE.language
    val pattern = when {
        isZh && sameYear -> "M月d日"
        isZh -> "yyyy年M月d日"
        else -> DateFormat.getBestDateTimePattern(
            locale,
            if (sameYear) "MMMd" else "yMMMd",
        )
    }
    val dateText = SimpleDateFormat(pattern, locale).format(date)
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

