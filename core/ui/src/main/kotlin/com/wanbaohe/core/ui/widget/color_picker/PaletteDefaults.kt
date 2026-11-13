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

package com.wanbaohe.core.ui.widget.color_picker

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.domain.model.ColorPalette
import com.t8rin.imagetoolbox.core.resources.R

/**
 * 新建调色板的默认名「调色板 N」，跳过已经占用的序号。
 *
 * 色彩工具的编辑器、选色面板、加入调色板面板都用这一份，避免三处各写一份
 * 序号计算逻辑（改名后序号会撞车）。
 *
 * 注意是 @Composable：字符串要在组合期取，不能放进点击回调里现算。
 */
@Composable
fun newPaletteName(palettes: List<ColorPalette>): String {
    var index = palettes.size + 1
    while (palettes.any { it.name == stringResource(R.string.palette_default_name, index) }) {
        index++
    }
    return stringResource(R.string.palette_default_name, index)
}
