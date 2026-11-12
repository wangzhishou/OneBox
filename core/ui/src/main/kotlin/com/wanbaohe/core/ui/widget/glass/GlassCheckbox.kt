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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.helper.ProvidesValue

/**
 * 毛玻璃风格 Checkbox —— 参数与 Material3 [Checkbox] 完全对齐。
 *
 * - **Glassmorphism 开启** → 复选框外圈使用玻璃描边 / 背景；选中态由 [checkedGlassColor] 着色
 * - **Glassmorphism 关闭** → 由同一玻璃管线退化为相近通透度的平涂底色
 *
 * @param style              毛玻璃浓度等级
 * @param checkedGlassColor  选中时玻璃底色
 * @param uncheckedGlassColor 未选中时玻璃底色
 * @param glassBorderWidth   描边宽度
 */
@Composable
fun GlassCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = AppTheme.colors.checkboxColors(),
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Regular,
    checkedGlassColor: Color = MaterialTheme.colorScheme.primaryContainer,
    uncheckedGlassColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    glassBorderWidth: Dp = 0.9.dp,
) {
    val glassColor = if (checked) checkedGlassColor else uncheckedGlassColor
    val transparentBoxColors = colors.copy(
        uncheckedBoxColor = Color.Transparent,
        checkedBoxColor = Color.Transparent,
        disabledUncheckedBoxColor = Color.Transparent,
        disabledCheckedBoxColor = Color.Transparent,
    )

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier.glassControlStyle(
                style = style,
                backgroundAlpha = if (checked) {
                    (style.backgroundAlpha + 0.12f).coerceAtMost(1f)
                } else {
                    (style.backgroundAlpha + 0.05f).coerceAtMost(1f)
                },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                color = glassColor,
                borderWidth = glassBorderWidth,
                enabled = enabled,
            ),
            enabled = enabled,
            colors = transparentBoxColors,
            interactionSource = interactionSource,
        )
    }
}
