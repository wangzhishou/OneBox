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

package com.shifenmiao.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.base.ui.card.TonalCardPaletteDefaults
import com.wanbaohe.base.ui.card.TonalCardIcon
import com.wanbaohe.base.ui.icon.BuildCustomIcon
import com.wanbaohe.base.ui.icon.IconAvatar
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.theme.AppFontMode
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.com.string.TimeFormatter
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Delete

private val FeaturedCardMaxViewportWidth = 280.dp
private const val FeaturedCardViewportFraction = 0.36f

// ══════════════════════════════════════════════════════════════
//  精选 — 横向大卡片
// ══════════════════════════════════════════════════════════════

/** 横滑卡片的布局独立于配色，页面可以选择预设或用 copy 微调。 */
@Immutable
data class FeaturedCardLayout(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val minWidth: Dp = 172.dp,
    val iconTitleGap: Dp = 8.dp,
    val titleDescriptionGap: Dp = 8.dp,
    val descriptionLabelGap: Dp = 8.dp,
    val titleLineHeight: TextUnit? = null,
    val labelVerticalPadding: Dp = 4.dp,
) {
    companion object {
        @Composable
        fun standard(): FeaturedCardLayout {
            val padding = TonalCardPaletteDefaults.contentPadding
            val spacing = TonalCardPaletteDefaults.contentSpacing
            return FeaturedCardLayout(
                horizontalPadding = padding,
                verticalPadding = padding,
                iconTitleGap = spacing,
                titleDescriptionGap = spacing,
                descriptionLabelGap = spacing,
            )
        }

        @Composable
        fun spacious(): FeaturedCardLayout = FeaturedCardLayout(
            horizontalPadding = minOf(AppTheme.dimens.cardContentPadding, 16.dp),
            verticalPadding = 16.dp,
            iconTitleGap = 20.dp,
            titleDescriptionGap = 20.dp,
            descriptionLabelGap = 16.dp,
            titleLineHeight = 24.sp,
        )
    }
}

@Composable
fun FeaturedGrid(
    items: List<ItemWithCategoriesAndStats>,
    onItemClick: (ItemWithCategoriesAndStats) -> Unit,
    modifier: Modifier = Modifier,
    clickInfo: ClickInfoType = ClickInfoType.NONE,
    cardLayout: FeaturedCardLayout = FeaturedCardLayout.standard(),
    cardWidth: Dp? = null,
) {
    val titleLines = if (AppTheme.uiProfile.fontMode == AppFontMode.Regular) 1 else 2
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val readableWidth = cardLayout.minWidth * fontScale
        val viewportWidth = (maxWidth * FeaturedCardViewportFraction)
            .coerceAtMost(FeaturedCardMaxViewportWidth)
        val resolvedCardWidth = cardWidth
            ?: maxOf(readableWidth, viewportWidth).coerceAtMost(maxWidth)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.contentGap),
        ) {
            items(count = items.size, key = { index -> "featured_row_$index" }) { rowIndex ->
                FeaturedCard(
                    item = items[rowIndex],
                    themeIndex = rowIndex,
                    modifier = Modifier.width(resolvedCardWidth),
                    clickInfo = clickInfo,
                    layout = cardLayout,
                    titleLines = titleLines,
                    onClick = { onItemClick(items[rowIndex]) }
                )
            }
        }
    }
}

@Composable
private fun FeaturedCard(
    item: ItemWithCategoriesAndStats,
    themeIndex: Int,
    modifier: Modifier,
    clickInfo: ClickInfoType,
    layout: FeaturedCardLayout,
    titleLines: Int,
    onClick: () -> Unit
) {
    val palette = TonalCardPaletteDefaults.forIndex(themeIndex)
    val titleStyle = MaterialTheme.typography.titleMedium
    val infoText = when (clickInfo) {
        ClickInfoType.TIME -> {
            val time = item.clickTime
            if (time.time > 0) TimeFormatter.formatRelativeTime(time)
            else BaseUtils.getNameByType(item.item.listType ?: 0)
        }

        ClickInfoType.COUNT -> {
            val count = item.clickCount
            if (count > 0) pluralStringResource(
                com.shifenmiao.core.R.plurals.favorite_usage_count, count, count
            ) else BaseUtils.getNameByType(item.item.listType ?: 0)
        }

        ClickInfoType.NONE -> null
    }

    GlassCard(
        modifier = modifier,
        onClick = onClick,
        shape = TonalCardPaletteDefaults.shape,
        style = TonalCardPaletteDefaults.containerStyle,
        colors = CardDefaults.cardColors(
            containerColor = palette.containerColor,
            contentColor = palette.titleColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = layout.horizontalPadding, vertical = layout.verticalPadding)
        ) {
            TonalCardIcon(palette) { iconModifier, tint ->
                BuildCustomIcon(
                    iconName = item.item.iconName?.takeIf { it.isNotEmpty() } ?: item.item.title,
                    modifier = iconModifier,
                    tint = tint,
                )
            }
            Spacer(modifier = Modifier.height(layout.iconTitleGap))
            Text(
                text = item.item.title.orEmpty(),
                style = layout.titleLineHeight?.let { titleStyle.copy(lineHeight = it) } ?: titleStyle,
                color = palette.titleColor,
                minLines = titleLines,
                maxLines = titleLines,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(layout.titleDescriptionGap))
            Text(
                text = item.item.description.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = palette.descriptionColor,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (infoText != null) {
                Spacer(modifier = Modifier.height(layout.descriptionLabelGap))
                Text(
                    modifier = Modifier
                        .glassBackground(
                            style = TonalCardPaletteDefaults.accentStyle,
                            color = palette.tagBackgroundColor,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 8.dp, vertical = layout.labelVerticalPadding),
                    text = infoText,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.tagTextColor
                )
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
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                    FlexBox(
                        modifier = Modifier.weight(1f, fill = false),
                        config = {
                            wrap(FlexWrap.Wrap)
                            columnGap(6.dp)
                            rowGap(6.dp)
                        },
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

/**
 * 轮换配色只负责色调, 不携带透明度 —— 玻璃管线(createGlassDecorationColors)会丢弃
 * 颜色自身的 alpha, 底色浓淡一律由 GlassStyle 阶梯决定, 不要再 copy(alpha = …)。
 */
@Composable
fun sectionGradient(theme: SectionTheme): Color = when (theme) {
    SectionTheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    SectionTheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
    SectionTheme.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
    SectionTheme.SURFACE -> MaterialTheme.colorScheme.surfaceContainerLow
}

@Composable
fun sectionOnColor(theme: SectionTheme): Color = when (theme) {
    SectionTheme.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    SectionTheme.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    SectionTheme.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
    SectionTheme.SURFACE -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun sectionIconColor(theme: SectionTheme): Color = when (theme) {
    SectionTheme.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    SectionTheme.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    SectionTheme.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
    SectionTheme.SURFACE -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun sectionIconContainerColor(theme: SectionTheme): Color = when (theme) {
    SectionTheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    SectionTheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
    SectionTheme.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
    SectionTheme.SURFACE -> MaterialTheme.colorScheme.surfaceContainerLow
}
