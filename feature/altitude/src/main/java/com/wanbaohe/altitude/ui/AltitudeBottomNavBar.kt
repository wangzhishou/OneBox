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

package com.wanbaohe.altitude.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R as CoreR
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSpeedTest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInsights

/**
 * 底部导航栏 — INSTRUMENT / HISTORY
 */
@Composable
internal fun AltitudeBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    data class NavItem(val label: Int, val icon: ImageVector)

    val items = listOf(
        NavItem(CoreR.string.altitude_tab_instrument, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSpeedTest),
        NavItem(CoreR.string.altitude_tab_history, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInsights)
    )

    val tabs = items.mapIndexed { index, item ->
        BottomNavItem(
            id = index.toString(),
            label = stringResource(item.label),
            icon = item.icon,
            contentDescription = stringResource(item.label),
        )
    }

    BottomNavigationBar(
        items = tabs,
        selectedItemId = selectedTab.toString(),
        onItemClick = { clicked ->
            clicked.id.toIntOrNull()?.let(onTabSelected)
        },
        // 不要在这里再套一层 navigationBarsPadding():BottomNavigationBar 内部默认
        // navigationBarsPadding = true,已把系统导航条高度算进玻璃容器高度(72dp + inset),
        // 并作为容器内边距。外面再加一层会让玻璃容器整体上移 inset,底边露出一条外部空隙。
        modifier = modifier.fillMaxWidth(),
    )
}
