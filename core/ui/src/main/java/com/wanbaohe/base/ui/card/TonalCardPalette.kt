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

package com.wanbaohe.base.ui.card

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Immutable
enum class TonalCardVisualVariant {
    Default,
    Recommend,
    Highlighted,
}

@Immutable
data class TonalCardPalette(
    val containerColor: Color,
    val iconContentColor: Color,
    val iconContainerColor: Color,
    val actionContainerColor: Color,
    val actionContentColor: Color,
    val tagBackgroundColor: Color,
    val tagTextColor: Color,
    val titleColor: Color,
    val descriptionColor: Color,
    val supportingContentColor: Color,
    val accentColor: Color,
)

object TonalCardPaletteDefaults {

    /**
     * 调色板颜色只负责色调, 不携带透明度 —— 这些颜色全部经玻璃管线
     * (glassBackground/GlassCard)渲染, 颜色自身 alpha 会被覆盖率公式丢弃,
     * 层次由 GlassStyle 阶梯表达。
     */
    @Composable
    fun palette(
        variant: TonalCardVisualVariant,
        defaultContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
        defaultIconContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        defaultIconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
        defaultTagBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
        defaultTagTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        defaultActionContainerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
        defaultActionContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ): TonalCardPalette {
        val colorScheme = MaterialTheme.colorScheme

        return remember(
            variant,
            colorScheme,
            defaultContainerColor,
            defaultIconContentColor,
            defaultIconContainerColor,
            defaultTagBackgroundColor,
            defaultTagTextColor,
            defaultActionContainerColor,
            defaultActionContentColor,
        ) {
            when (variant) {
                TonalCardVisualVariant.Default -> {
                    TonalCardPalette(
                        containerColor = defaultContainerColor,
                        iconContentColor = defaultIconContentColor,
                        iconContainerColor = defaultIconContainerColor,
                        actionContainerColor = defaultActionContainerColor,
                        actionContentColor = defaultActionContentColor,
                        tagBackgroundColor = defaultTagBackgroundColor,
                        tagTextColor = defaultTagTextColor,
                        titleColor = colorScheme.onSurface,
                        descriptionColor = colorScheme.onSurfaceVariant,
                        supportingContentColor = colorScheme.outline,
                        accentColor = colorScheme.onPrimaryContainer,
                    )
                }

                TonalCardVisualVariant.Recommend -> {
                    TonalCardPalette(
                        containerColor = colorScheme.secondaryContainer,
                        iconContentColor = colorScheme.onSecondaryContainer,
                        iconContainerColor = colorScheme.secondaryContainer,
                        actionContainerColor = colorScheme.surfaceContainer,
                        actionContentColor = colorScheme.onSurfaceVariant,
                        tagBackgroundColor = colorScheme.secondaryContainer,
                        tagTextColor = colorScheme.onSecondaryContainer,
                        titleColor = colorScheme.onSurface,
                        descriptionColor = colorScheme.onSurfaceVariant,
                        supportingContentColor = colorScheme.onSecondaryContainer,
                        accentColor = colorScheme.onSecondaryContainer,
                    )
                }

                TonalCardVisualVariant.Highlighted -> {
                    TonalCardPalette(
                        containerColor = colorScheme.tertiaryContainer,
                        iconContentColor = colorScheme.onTertiaryContainer,
                        iconContainerColor = colorScheme.tertiaryContainer,
                        actionContainerColor = colorScheme.surfaceContainer,
                        actionContentColor = colorScheme.onSurface,
                        tagBackgroundColor = colorScheme.tertiaryContainer,
                        tagTextColor = colorScheme.onTertiaryContainer,
                        titleColor = colorScheme.onSurface,
                        descriptionColor = colorScheme.onSurfaceVariant,
                        supportingContentColor = colorScheme.onSurface,
                        accentColor = colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
    }
}
