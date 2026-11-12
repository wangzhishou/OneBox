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

package com.wanbaohe.app.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.app.update.rememberUpdateAvailable
import com.wanbaohe.core.ui.widget.navigation.BottomNavCenterAction
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
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
    // 有开源新版本时在"我的"tab 上点一个红点：入口固定可见，不用滚设置页才发现
    val hasUpdateAvailable = rememberUpdateAvailable()
    val items = tabs.map { (id, screen) ->
        BottomNavItem(
            id = id,
            label = stringResource(screen.title),
            icon = screen.icon,
            selectedIcon = screen.twoToneIcon ?: screen.icon,
            contentDescription = stringResource(screen.subtitle),
            hasBadge = hasUpdateAvailable && screen is Screen.Profile,
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
