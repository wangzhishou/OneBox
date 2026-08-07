package com.shifenmiao.base.ui.icon

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton

/**
 * 通用图标选择器组件
 *
 * 从 IconRegistry 动态获取可用图标列表
 * 支持单行滚动和网格布局两种显示模式
 *
 * @param selectedIconKey 当前选中的图标 key
 * @param onIconSelected 图标选择回调
 * @param iconKeys 可选的图标 key 列表，如果不提供则使用默认的分类图标
 * @param displayMode 显示模式：row 为单行滚动，grid 为网格布局
 * @param modifier 修饰符
 */
@Composable
fun IconSelector(
    selectedIconKey: String,
    onIconSelected: (String) -> Unit,
    iconKeys: List<String>? = null,
    displayMode: IconSelectorDisplayMode = IconSelectorDisplayMode.ROW,
    modifier: Modifier = Modifier
) {
    // 默认的分类图标列表
    val defaultIconKeys = remember {
        listOf(
            "Category",
            "Folder",
            "Work",
            "Home",
            "Star",
            "Favorite",
            "Label",
            "Bookmark",
            "Event",
            "Schedule",
            "CalendarToday",
            "Lightbulb",
            "Build",
            "Code",
            "Email",
            "Phone",
            "ShoppingCart",
            "CreditCard",
            "Palette",
            "Photo",
            "Camera",
            "Music",
            "PlayArrow",
            "Settings",
            "Lock",
            "Shield",
            "Cloud",
            "Storage",
            "Extension",
            "Widgets"
        )
    }

    val availableIconKeys = iconKeys ?: defaultIconKeys

    // 过滤出在 IconRegistry 中存在的图标
    val validIcons = remember(availableIconKeys) {
        availableIconKeys.mapNotNull { key ->
            IconRegistry.resolve(key)?.let { icon ->
                key to icon
            }
        }
    }

    when (displayMode) {
        IconSelectorDisplayMode.ROW -> {
            IconSelectorRow(
                icons = validIcons,
                selectedIconKey = selectedIconKey,
                onIconSelected = onIconSelected,
                modifier = modifier
            )
        }
        IconSelectorDisplayMode.GRID -> {
            IconSelectorGrid(
                icons = validIcons,
                selectedIconKey = selectedIconKey,
                onIconSelected = onIconSelected,
                modifier = modifier
            )
        }
    }
}

/**
 * 单行滚动的图标选择器
 */
@Composable
private fun IconSelectorRow(
    icons: List<Pair<String, ImageVector>>,
    selectedIconKey: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icons.forEach { (key, icon) ->
            IconOption(
                iconKey = key,
                icon = icon,
                isSelected = selectedIconKey == key,
                onIconSelected = onIconSelected
            )
        }
    }
}

/**
 * 网格布局的图标选择器
 */
@Composable
private fun IconSelectorGrid(
    icons: List<Pair<String, ImageVector>>,
    selectedIconKey: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        modifier = modifier.heightIn(max = 300.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(icons) { (key, icon) ->
            IconOption(
                iconKey = key,
                icon = icon,
                isSelected = selectedIconKey == key,
                onIconSelected = onIconSelected
            )
        }
    }
}

/**
 * 单个图标选项
 */
@Composable
private fun IconOption(
    iconKey: String,
    icon: ImageVector,
    isSelected: Boolean,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassTonalIconButton(
        onClick = { onIconSelected(iconKey) },
        modifier = modifier,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconKey,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * 显示模式枚举
 */
enum class IconSelectorDisplayMode {
    ROW,  // 单行滚动
    GRID  // 网格布局
}

/**
 * 从 key 获取图标的辅助函数
 */
fun getIconFromKey(key: String): ImageVector? {
    return IconRegistry.resolve(key)
}

/**
 * 从图标获取 key 的辅助函数（反向查找）
 */
fun getKeyFromIcon(icon: ImageVector): String? {
    return IconRegistry.allKeys.firstOrNull { IconRegistry.resolve(it) == icon }
}

