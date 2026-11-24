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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import com.shifenmiao.base.utils.RateLimiter
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.robot.RobotController
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDrawerClose
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDrawerOpen


@Composable
fun QuickOperations(
    appComponent: AppComponent
) {
    val uiState by appComponent.uiState.collectAsState()

    DisposableEffect(Unit) {
        onDispose { RobotController.clearSearchAnchor() }
    }

    IconButton(
        onClick = {
            appComponent.onNavigate(Screen.Search())
        },
        modifier = Modifier.onGloballyPositioned { coordinates ->
            if (!coordinates.isAttached || coordinates.size.width == 0) return@onGloballyPositioned
            val left = coordinates.positionInRoot().x
            RobotController.updateSearchAnchor(
                leftPx = left,
                rightPx = left + coordinates.size.width,
            )
        },
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
            contentDescription = stringResource(R.string.search),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
    // 抽屉开/关走同一套图标：关闭态显示「打开抽屉」(LineDrawerOpen),打开态显示
    // 「关闭抽屉」(LineDrawerClose)。抽屉打开后会被 scrim 盖住,所以抽屉内部
    // 右上角另有一个关闭按钮(DrawerContentWrapper),这里的状态切换是为了
    // 无遮罩/平板布局下仍然对得上。
    IconButton(onClick = {
        if (!RateLimiter.isFastClick()) {
            appComponent.toggleDrawer()
        }
    }) {
        Icon(
            imageVector = if (uiState.showDrawer) {
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDrawerClose
            } else {
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDrawerOpen
            },
            contentDescription = stringResource(R.string.open_menu),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

