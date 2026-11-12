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

package com.wanbaohe.compass.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.vector.ImageVector
import com.wanbaohe.compass.component.CompassUiState
import com.wanbaohe.compass.ui.luopan.LuopanPage
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.Compass
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompass

/**
 * 一个仪表盘页面：底部导航 label + 图标 + 页面内容。
 *
 * 页面自含表盘与自己的底部面板；heading（高频，只重绘）与
 * uiState（低频，驱动文本）由屏幕层统一注入，各页共享同一份传感器订阅。
 */
class DialPage(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val content: @Composable (heading: State<Float>, uiState: CompassUiState) -> Unit
)

/**
 * 表盘注册表：顺序即底部导航/页顺序，第 0 项为默认页。
 * 新增仪表盘：实现页面 Composable 并在此加一行即可。
 */
val DialPages: List<DialPage> = listOf(
    DialPage(
        titleRes = CoreR.string.compass_tab_luopan,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Compass
    ) { heading, uiState ->
        LuopanPage(heading = heading, uiState = uiState)
    },
    DialPage(
        titleRes = CoreR.string.compass_tab_compass,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompass
    ) { heading, uiState ->
        ClassicCompassPage(heading = heading, uiState = uiState)
    }
)
