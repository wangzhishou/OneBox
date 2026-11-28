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

package com.wanbaohe.core.ui.widget.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState

/** 底栏顶角。导航栏和底部操作栏共用，避免两套圆角。 */
val BottomBarContainerShape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

/**
 * 页面有没有铺在内容后面的背景层（网格渐变或自定义背景图）。
 * 没有背景层时底栏不画自己的底，和页面连成一体。
 */
@Composable
fun bottomBarHasBackdrop(): Boolean {
    val settingsState = LocalSettingsState.current
    return settingsState.isMeshGradientBackgroundEnabled ||
        settingsState.customBackgroundImageUri != null
}

/**
 * 底栏背景。打卡页导航栏和 [com.wanbaohe.core.ui.widget.system.OneBoxBottomActionBar] 都走这里。
 *
 * 有渐变或自定义背景时透出背后的颜色：玻璃开着用薄玻璃，关着留一层半透明，不铺实色。
 * 纯色页面不画底。
 */
@Composable
fun Modifier.bottomBarBackground(
    shape: Shape = BottomBarContainerShape,
    color: Color = MaterialTheme.colorScheme.surface,
): Modifier {
    if (!bottomBarHasBackdrop()) return this
    return if (LocalSettingsState.current.isGlassAlphaEnabled) {
        glassBackground(
            style = GlassStyle.Thin,
            shape = shape,
            color = color,
        )
    } else {
        background(
            color = color.copy(alpha = 0.18f),
            shape = shape,
        )
    }
}
