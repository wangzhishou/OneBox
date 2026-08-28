package com.t8rin.imagetoolbox.core.ui.widget.editor

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
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
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.emoji.Emoji
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCelebration
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEmojiFace
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilterFrames
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRobot
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTrendingUp
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton

/**
 * 贴纸素材共享弹层(自 markup-layers 上移 core/ui,参数化确认动作):
 * 顶部分类 Tab(可横滑)+ 5 列素材网格。
 * 「表情」走 core emoji 表;装饰/边框/潮流动态列出 assets/stickers/<dir> 下的
 * 内置 SVG(新增素材免改代码);「AI 生成贴纸」由宿主经 [aiGeneration] 注入
 * 生成逻辑(登录/积分预检在宿主 onGenerate 外包,本组件不引 core/base),
 * 未注入时保留 coming soon 占位。
 * 宿主在 [onStickerClick] 里落地各自的图层/元素模型。
 */
@Composable
fun StickerToolSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onStickerClick: (StickerSource) -> Unit,
    aiGeneration: AiStickerGeneration? = null,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        sheetContent = {
            StickerPanel(
                onStickerClick = { source ->
                    onStickerClick(source)
                    onDismiss()
                },
                aiGeneration = aiGeneration
            )
        }
    )
}

/** AI 生成贴纸的宿主注入:生成中态、单次积分成本与生成动作(预检由宿主外包) */
class AiStickerGeneration(
    val isGenerating: Boolean,
    val pointsCost: Int,
    val onGenerate: (prompt: String) -> Unit,
)

@Composable
private fun StickerPanel(
    onStickerClick: (StickerSource) -> Unit,
    aiGeneration: AiStickerGeneration?,
) {
    var category by rememberSaveable { mutableStateOf(StickerCategory.Emoji.name) }
    // 兜底:枚举改名/重构后恢复出的旧值不再存在时回退到默认分类,避免 valueOf 抛异常
    val currentCategory = StickerCategory.entries.firstOrNull { it.name == category }
        ?: StickerCategory.Emoji

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // 键盘弹出时弹层整体上移,AI 生成贴纸的输入框不被遮挡
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.sticker_panel_title),
            style = MaterialTheme.typography.titleMedium
        )
        CategoryTabRow(
            selected = currentCategory,
            onSelect = { category = it.name }
        )
        when {
            currentCategory.isAiPlaceholder -> if (aiGeneration != null) {
                AiStickerContent(aiGeneration)
            } else AiPlaceholderContent()
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
        text = stringResource(R.string.sticker_new_badge),
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
        EmptyContent(text = stringResource(R.string.sticker_empty))
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

/**
 * AI 生成贴纸页:描述输入 + 生成按钮 + 积分说明;生成中按钮禁用并显示转圈。
 * 空描述 toast 内部处理,不回调;预检/扣费/落地全在宿主 [AiStickerGeneration.onGenerate] 链路。
 */
@Composable
private fun AiStickerContent(
    aiGeneration: AiStickerGeneration,
) {
    var prompt by rememberSaveable { mutableStateOf("") }
    val emptyHint = stringResource(R.string.sticker_ai_empty)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(STICKER_GRID_HEIGHT)
    ) {
        GlassOutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            placeholder = { Text(stringResource(R.string.sticker_ai_prompt_hint)) },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
        OnePrimaryButton(
            text = stringResource(R.string.sticker_ai_generate_action),
            onClick = {
                val trimmed = prompt.trim()
                if (trimmed.isEmpty()) {
                    // 空描述直接提示,不进宿主预检
                    AppToastHost.showToast(
                        message = emptyHint,
                        icon = Icons.Outlined.LineInfo,
                        duration = ToastDuration.Short
                    )
                    return@OnePrimaryButton
                }
                aiGeneration.onGenerate(trimmed)
            },
            enabled = !aiGeneration.isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
        if (aiGeneration.isGenerating) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp)
            ) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.sticker_ai_generating),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = stringResource(R.string.sticker_ai_points_hint, aiGeneration.pointsCost),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )
    }
}

@Composable
private fun AiPlaceholderContent() {
    EmptyContent(
        text = stringResource(R.string.coming_soon),
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
 * [isAiPlaceholder] 为 AI 生成贴纸(内容由宿主注入,未注入时显示占位);
 * 其余(core emoji)走 [Emoji] 表。
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
        labelRes = R.string.sticker_category_emoji
    ),
    Decor(
        assetDir = "decor",
        isAiPlaceholder = false,
        icon = Icons.Outlined.LineCelebration,
        labelRes = R.string.sticker_category_decor
    ),
    Frame(
        assetDir = "frame",
        isAiPlaceholder = false,
        icon = Icons.Outlined.LineFilterFrames,
        labelRes = R.string.sticker_category_frame
    ),
    Trend(
        assetDir = "trend",
        isAiPlaceholder = false,
        icon = Icons.Outlined.LineTrendingUp,
        labelRes = R.string.sticker_category_trend
    ),
    AiGenerated(
        assetDir = null,
        isAiPlaceholder = true,
        icon = Icons.Outlined.LineRobot,
        labelRes = R.string.sticker_category_ai,
        newBadge = true
    ),
}
