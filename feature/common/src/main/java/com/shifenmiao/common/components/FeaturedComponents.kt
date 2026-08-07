package com.shifenmiao.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconAvatar
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.wanbaohe.com.string.TimeFormatter
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Delete

// ══════════════════════════════════════════════════════════════
//  精选 — 横向大卡片
// ══════════════════════════════════════════════════════════════

@Composable
fun FeaturedGrid(
    items: List<ItemWithCategoriesAndStats>,
    onItemClick: (ItemWithCategoriesAndStats) -> Unit,
    modifier: Modifier = Modifier,
    clickInfo: ClickInfoType = ClickInfoType.NONE,
    reverseTheme: Boolean = false
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(count = items.size, key = { index -> "featured_row_$index" }) { rowIndex ->
            FeaturedCard(
                item = items[rowIndex],
                themeIndex = rowIndex,
                clickInfo = clickInfo,
                reverseTheme = reverseTheme,
                onClick = { onItemClick(items[rowIndex]) }
            )
        }
    }
}

@Composable
fun FeaturedCard(
    item: ItemWithCategoriesAndStats,
    themeIndex: Int,
    modifier: Modifier = Modifier,
    clickInfo: ClickInfoType = ClickInfoType.NONE,
    reverseTheme: Boolean = false,
    onClick: () -> Unit
) {
    val effectiveIndex = if (reverseTheme) 3 - (themeIndex % 4) else themeIndex
    val theme = sectionThemeForIndex(effectiveIndex)
    val containerColor = sectionGradient(theme)
    val iconTint = sectionIconColor(theme)
    val contentColor = sectionOnColor(theme)
    val iconBgColor = sectionIconContainerColor(theme)

    GlassCard(
        modifier = modifier
            .width(160.dp)
            .wrapContentHeight(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            IconAvatar(
                iconName = item.item.iconName?.ifEmpty { item.item.title },
                containerColor = iconBgColor,
                tint = iconTint,
                glassStyle = GlassStyle.Thick,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = item.item.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (clickInfo != ClickInfoType.NONE) {
                    Text(
                        text = item.item.description.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 2,
                        minLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val infoText: String? = when (clickInfo) {
                        ClickInfoType.TIME -> {
                            val t = item.clickTime
                            if (t.time > 0) TimeFormatter.formatRelativeTime(t) else BaseUtils.getNameByType(
                                item.item.listType ?: 0
                            )
                        }

                        ClickInfoType.COUNT -> {
                            val c = item.clickCount
                            if (c > 0) stringResource(
                                com.shifenmiao.core.R.string.favorite_usage_count, c
                            ) else BaseUtils.getNameByType(item.item.listType ?: 0)
                        }

                        ClickInfoType.NONE -> null
                    }
                    if (infoText != null) {
                        Text(
                            modifier = Modifier
                                .glassBackground(
                                    style = GlassStyle.Thick,
                                    color = iconBgColor,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                ),
                            text = infoText,
                            style = MaterialTheme.typography.labelSmall,
                            color = iconTint
                        )
                    }
                } else {
                    Text(
                        text = item.item.description.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  最近访问 — 圆形图标横向滚动行
// ══════════════════════════════════════════════════════════════

@Composable
fun CircleIconRow(
    items: List<ItemWithCategoriesAndStats>,
    onItemClick: (ItemWithCategoriesAndStats) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.Start),
        contentPadding = PaddingValues(end = 8.dp)
    ) {
        itemsIndexed(
            items = items,
            key = { index, item -> "circle_item_${item.item.id}_$index" }
        ) { index, item ->
            CircleIconItem(
                item = item,
                themeIndex = index,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
fun CircleIconItem(
    item: ItemWithCategoriesAndStats,
    themeIndex: Int,
    onClick: () -> Unit
) {
    val theme = sectionThemeForIndex(themeIndex)
    val circleBg = sectionGradient(theme)
    val iconTint = sectionIconColor(theme)

    Column(
        modifier = Modifier
            .width(68.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconAvatar(
            iconName = item.item.iconName?.ifEmpty { item.item.title },
            size = 64.dp,
            shape = CircleShape,
            containerColor = circleBg,
            tint = iconTint,
            iconSizeRatio = 28f / 64f,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.item.title.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  我的文章 — 纵向卡片列表
// ══════════════════════════════════════════════════════════════

@Composable
fun LocalArticleList(
    items: List<ItemWithCategoriesAndStats>,
    onItemClick: (ItemWithCategoriesAndStats) -> Unit,
    onEditClick: (ItemWithCategoriesAndStats) -> Unit,
    onDeleteClick: (ItemWithCategoriesAndStats) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEachIndexed { index, item ->
            LocalArticleCard(
                item = item,
                themeIndex = index,
                onClick = { onItemClick(item) },
                onEditClick = { onEditClick(item) },
                onDeleteClick = { onDeleteClick(item) },
            )
        }
    }
}

@Composable
fun LocalArticleCard(
    item: ItemWithCategoriesAndStats,
    themeIndex: Int,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = sectionThemeForIndex(themeIndex)
    val contentColor = MaterialTheme.colorScheme.onSurface
    val editButtonTint = sectionIconColor(theme)
    val deleteTheme = sectionThemeForIndex(themeIndex + 1)
    val deleteButtonTint = sectionIconColor(deleteTheme)
    val description = item.item.description
        ?.takeIf { it.isNotBlank() }
        ?: BaseUtils.getNameByType(item.item.listType ?: 0)
    val categoryTags = item.categories
        .mapNotNull { category -> category.name.takeIf { it.isNotBlank() } }
        .distinct()
    val hasCategoryTag = categoryTags.isNotEmpty()

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = TimeFormatter.formatRelativeTime(java.util.Date(item.item.updatedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = item.item.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (hasCategoryTag) {
                    FlowRow(
                        modifier = Modifier.weight(1f, fill = false),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        categoryTags.forEachIndexed { index, name ->
                            val categoryTheme = sectionThemeForIndex(index)
                            Text(
                                modifier = Modifier
                                    .glassBackground(
                                        style = GlassStyle.Thick,
                                        color = sectionIconContainerColor(categoryTheme),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = sectionIconColor(categoryTheme),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(28.dp),
                        onClick = onEditClick
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            modifier = Modifier.size(14.dp),
                            contentDescription = stringResource(com.shifenmiao.core.R.string.edit),
                            tint = editButtonTint,
                        )
                    }
                    IconButton(
                        modifier = Modifier
                            .size(28.dp),
                        onClick = onDeleteClick
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                            modifier = Modifier.size(14.dp),
                            contentDescription = stringResource(com.shifenmiao.core.R.string.delete),
                            tint = deleteButtonTint,
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  主题辅助方法
// ══════════════════════════════════════════════════════════════

enum class ClickInfoType { NONE, COUNT, TIME }

enum class SectionTheme { PRIMARY, SECONDARY, TERTIARY, SURFACE }

fun sectionThemeForIndex(index: Int): SectionTheme = when (index % 4) {
    0 -> SectionTheme.PRIMARY
    1 -> SectionTheme.SECONDARY
    2 -> SectionTheme.TERTIARY
    else -> SectionTheme.SURFACE
}

@Composable
fun sectionGradient(theme: SectionTheme): Color = when (theme) {
    SectionTheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = AppTheme.dimens.containerAlpha)
    SectionTheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = AppTheme.dimens.containerAlpha)
    SectionTheme.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = AppTheme.dimens.containerAlpha)
    SectionTheme.SURFACE -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = AppTheme.dimens.containerAlpha)
}

@Composable
fun sectionOnColor(theme: SectionTheme): Color = when (theme) {
    SectionTheme.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    SectionTheme.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    SectionTheme.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
    SectionTheme.SURFACE -> MaterialTheme.colorScheme.onSurface
}

@Composable
fun sectionIconColor(theme: SectionTheme): Color = when (theme) {
    SectionTheme.PRIMARY -> MaterialTheme.colorScheme.primary
    SectionTheme.SECONDARY -> MaterialTheme.colorScheme.secondary
    SectionTheme.TERTIARY -> MaterialTheme.colorScheme.tertiary
    SectionTheme.SURFACE -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun sectionIconContainerColor(theme: SectionTheme): Color = when (theme) {
    SectionTheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    SectionTheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
    SectionTheme.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
    SectionTheme.SURFACE -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f)
}
