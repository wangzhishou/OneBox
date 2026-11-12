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

package com.wanbaohe.profile.settingItem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.glass.glassMedium
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem

/**
 * 「我的」页设置分组卡片。
 *
 * 与页面底部「备份 / 恢复」按钮、胶囊按钮共用 glassMedium + surfaceContainerLow，
 * 保证卡片与按钮是同一套玻璃灰度。
 */
@Composable
fun ProfileGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = OneBoxDesignSystem.sectionCardShape
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(shape)
            .glassMedium(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = shape,
            )
            .padding(PaddingValues(horizontal = 0.dp, vertical = 16.dp))
    ) {
        content()
    }
}
