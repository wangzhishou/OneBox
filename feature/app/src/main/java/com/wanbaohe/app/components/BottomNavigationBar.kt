package com.wanbaohe.app.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavCenterAction
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.Add

/**
 * 底部导航栏 — 整体容器使用玻璃透明，中间放置突出的 ADD 按钮。
 *
 * 布局结构：左侧 2 个 tab → 中间 ADD 按钮 → 右侧 2 个 tab
 *
 * - **导航栏容器**：毛玻璃背景
 * - **选中态胶囊**：毛玻璃 + 对应色系 tint
 * - **ADD 按钮**：突出的圆形按钮，点击展开浮动面板
 */
@Composable
fun AppBottomNavigationBar(
    height: Dp = AppTheme.dimens.navigationHeight,
    screenList: List<Screen>,
    showBottomBar: Boolean,
    currentTabPageIndex: Int,
    addMenuExpanded: Boolean = false,
    onToggleAddMenu: () -> Unit = {},
    onTabClick: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = screenList.mapIndexed { index, screen ->
        val id = "${index}_${screen.simpleName}"
        id to screen
    }
    val items = tabs.map { (id, screen) ->
        BottomNavItem(
            id = id,
            label = stringResource(screen.title),
            icon = screen.icon,
            selectedIcon = screen.twoToneIcon ?: screen.icon,
            contentDescription = stringResource(screen.subtitle),
        )
    }
    val selectedItemId = if (addMenuExpanded) {
        null
    } else {
        tabs.getOrNull(currentTabPageIndex)?.first
    }

    BottomNavigationBar(
        items = items,
        selectedItemId = selectedItemId,
        onItemClick = { clicked ->
            tabs.firstOrNull { it.first == clicked.id }?.second?.let(onTabClick)
        },
        modifier = modifier,
        height = height,
        showBar = showBottomBar,
        centerAction = BottomNavCenterAction(
            label = "",
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
            contentDescription = stringResource(CoreR.string.nav_add),
            expanded = addMenuExpanded,
        ),
        onCenterActionClick = onToggleAddMenu,
        imePadding = true
    )
}
