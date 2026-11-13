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

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * 浅色 / 半透明色块在浅色玻璃底上会「消失」，看不出一块颜色的边界。
 *
 * 阈值取 0.72 而不是 0.9：#F4F0F4 这类近白纸巾色亮度只有 0.88，
 * 放在浅色卡片上仍然糊成一片。只给这种色块补一圈极淡的描边，
 * 正常饱和色不描边，保持大色块的干净。
 */
@Composable
fun Modifier.colorSwatchOutline(
    color: Color,
    shape: Shape
): Modifier = if (color.alpha < 1f || color.luminance() > 0.72f) {
    this.border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        shape = shape
    )
} else {
    this
}
