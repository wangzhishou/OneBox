package com.wanbaohe.markuplayers.presentation.tools.sticker

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.emoji.Emoji
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCelebration
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEmojiFace
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilterFrames
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRobot
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTrendingUp
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.StickerSource
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent

/**
 * 贴纸素材面板(设计稿「贴纸贴图素材面板」):
 * 顶部分类 Tab(可横滑)+ 5 列素材网格。
 * 「表情」走 core emoji 表;装饰/边框/潮流动态列出 assets/stickers/<dir> 下的
 * 内置 SVG(新增素材免改代码);「AI 生成贴纸」为带 NEW 角标的占位。
 * 点击素材即创建居中贴纸图层(addLayer 默认选中)并关闭面板。
 */
@Composable
fun StickerToolSheet(
    visible: Boolean,
    component: MarkupLayersComponent,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        sheetContent = {
            StickerPanel(
                onStickerClick = { source ->
                    component.addLayer(MarkupLayer(type = LayerType.Sticker(source)))
                    onDismiss()
                }
            )
        }
    )
}

@Composable
private fun StickerPanel(
    onStickerClick: (StickerSource) -> Unit,
) {
    var category by rememberSaveable { mutableStateOf(StickerCategory.Emoji.name) }
    val currentCategory = StickerCategory.valueOf(category)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.markup_sticker_title),
            style = MaterialTheme.typography.titleMedium
        )
        CategoryTabRow(
            selected = currentCategory,
            onSelect = { category = it.name }
        )
        when {
            currentCategory.isAiPlaceholder -> AiPlaceholderContent()
            currentCategory.assetDir != null -> AssetStickerGrid(
                assetDir = currentCategory.assetDir,
                onStickerClick = onStickerClick
            )
            else -> EmojiStickerGrid(onStickerClick = onStickerClick)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CategoryTabRow(
    selected: StickerCategory,
    onSelect: (StickerCategory) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        StickerCategory.entries.forEach { category ->
            EnhancedChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                selectedColor = MaterialTheme.colorScheme.secondary,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = stringResource(category.labelRes))
                    if (category.newBadge) {
                        Spacer(Modifier.width(4.dp))
                        NewBadge()
                    }
                }
            }
        }
    }
}

@Composable
private fun NewBadge() {
    Text(
        text = stringResource(R.string.markup_sticker_new_badge),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onTertiary,
        modifier = Modifier
            .clip(ShapeDefaults.small)
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}

/** core emoji 表:下标即 [StickerSource.Emoji.emojiIndex] */
@Composable
private fun EmojiStickerGrid(
    onStickerClick: (StickerSource) -> Unit,
) {
    val emojis = Emoji.allIcons()
    LazyVerticalGrid(
        columns = GridCells.Fixed(STICKER_GRID_COLUMNS),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(STICKER_GRID_HEIGHT)
    ) {
        itemsIndexed(emojis) { index, uri ->
            StickerCell(
                model = uri,
                onClick = { onStickerClick(StickerSource.Emoji(index)) }
            )
        }
    }
}

/** 内置素材包:动态列出 assets/stickers/[assetDir] 下的 SVG,新增素材免改代码 */
@Composable
private fun AssetStickerGrid(
    assetDir: String,
    onStickerClick: (StickerSource) -> Unit,
) {
    val context = LocalContext.current
    val paths = remember(assetDir) {
        context.assets.list("stickers/$assetDir")
            ?.filter { it.endsWith(".svg") }
            ?.sortedWith(String.CASE_INSENSITIVE_ORDER)
            ?.map { "stickers/$assetDir/$it" }
            ?: emptyList()
    }
    if (paths.isEmpty()) {
        EmptyContent(text = stringResource(R.string.markup_sticker_empty))
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(STICKER_GRID_COLUMNS),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(STICKER_GRID_HEIGHT)
    ) {
        items(paths) { path ->
            StickerCell(
                model = "file:///android_asset/$path",
                onClick = { onStickerClick(StickerSource.Asset(path)) }
            )
        }
    }
}

@Composable
private fun StickerCell(
    model: Any,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .clip(ShapeDefaults.default)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
    ) {
        Picture(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            showTransparencyChecker = false,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }
}

@Composable
private fun AiPlaceholderContent() {
    EmptyContent(
        text = stringResource(R.string.markup_coming_soon),
        icon = Icons.Outlined.LineRobot
    )
}

@Composable
private fun EmptyContent(
    text: String,
    icon: ImageVector? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(STICKER_GRID_HEIGHT)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private const val STICKER_GRID_COLUMNS = 5
private val STICKER_GRID_HEIGHT = 340.dp

/**
 * 贴纸分类。assetDir 非空 = assets/stickers 下的内置素材目录;
 * [isAiPlaceholder] 为 AI 生成贴纸占位;其余(core emoji)走 [Emoji] 表。
 */
private enum class StickerCategory(
    val assetDir: String?,
    val isAiPlaceholder: Boolean,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
    val newBadge: Boolean = false,
) {
    Emoji(
        assetDir = null,
        isAiPlaceholder = false,
        icon = Icons.Outlined.LineEmojiFace,
        labelRes = R.string.markup_sticker_category_emoji
    ),
    Decor(
        assetDir = "decor",
        isAiPlaceholder = false,
        icon = Icons.Outlined.LineCelebration,
        labelRes = R.string.markup_sticker_category_decor
    ),
    Frame(
        assetDir = "frame",
        isAiPlaceholder = false,
        icon = Icons.Outlined.LineFilterFrames,
        labelRes = R.string.markup_sticker_category_frame
    ),
    Trend(
        assetDir = "trend",
        isAiPlaceholder = false,
        icon = Icons.Outlined.LineTrendingUp,
        labelRes = R.string.markup_sticker_category_trend
    ),
    AiGenerated(
        assetDir = null,
        isAiPlaceholder = true,
        icon = Icons.Outlined.LineRobot,
        labelRes = R.string.markup_sticker_category_ai,
        newBadge = true
    ),
}
