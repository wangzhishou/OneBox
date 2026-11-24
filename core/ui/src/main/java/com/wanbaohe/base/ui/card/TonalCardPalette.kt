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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground

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

private const val LIGHT_CARD_SATURATION = 0.60f
private const val LIGHT_CARD_LUMINANCE = 0.56f
private const val DARK_CARD_SATURATION = 0.36f
private const val DARK_CARD_LIGHTNESS = 0.30f
private const val DARK_CARD_CONTENT_SATURATION = 0.32f
private const val DARK_CARD_CONTENT_LUMINANCE = 0.72f

@Composable
private fun uniformCardTint(color: Color): Color {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    return remember(color, isLight) {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color.toArgb(), hsl)
        hsl[1] = when {
            hsl[1] < 0.08f -> 0f
            isLight -> LIGHT_CARD_SATURATION
            else -> DARK_CARD_SATURATION
        }
        hsl[2] = if (isLight) {
            lightnessForLuminance(hsl, LIGHT_CARD_LUMINANCE)
        } else {
            DARK_CARD_LIGHTNESS
        }
        Color(ColorUtils.HSLToColor(hsl))
    }
}

@Composable
private fun uniformCardContentColor(color: Color, isNeutral: Boolean): Color {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    return remember(color, isNeutral, isLight) {
        if (isLight) return@remember color
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color.toArgb(), hsl)
        hsl[1] = if (isNeutral || hsl[1] < 0.08f) 0f else DARK_CARD_CONTENT_SATURATION
        hsl[2] = lightnessForLuminance(hsl, DARK_CARD_CONTENT_LUMINANCE)
        Color(ColorUtils.HSLToColor(hsl))
    }
}

private fun lightnessForLuminance(hsl: FloatArray, targetLuminance: Float): Float {
    var low = 0f
    var high = 1f
    repeat(12) {
        val middle = (low + high) / 2f
        hsl[2] = middle
        val luminance = Color(ColorUtils.HSLToColor(hsl)).luminance()
        if (luminance < targetLuminance) low = middle else high = middle
    }
    return (low + high) / 2f
}

object TonalCardPaletteDefaults {
    val containerStyle = GlassStyle.Thin
    val accentStyle = GlassStyle.Medium
    val contentSpacing = 8.dp

    val shape: Shape
        @Composable get() = MaterialTheme.shapes.large

    val contentPadding: Dp
        @Composable get() = AppTheme.dimens.cardContentPadding

    /** 横排和网格共用同一个四色顺序，只保留主题色相，浓淡由统一色阶和 GlassStyle 控制。 */
    @Composable
    fun forIndex(index: Int): TonalCardPalette {
        val colors = MaterialTheme.colorScheme
        val colorIndex = Math.floorMod(index, 4)
        val (container, content) = when (colorIndex) {
            0 -> colors.secondaryContainer to colors.onSecondaryContainer
            1 -> colors.tertiaryContainer to colors.onTertiaryContainer
            2 -> colors.primaryContainer to colors.onPrimaryContainer
            else -> colors.surfaceContainer to colors.onSurfaceVariant
        }
        val containerColor = if (colorIndex == 3) container else uniformCardTint(container)
        val contentColor = uniformCardContentColor(content, isNeutral = colorIndex == 3)
        return remember(colors, container, content, containerColor, contentColor) {
            TonalCardPalette(
                containerColor = containerColor,
                iconContentColor = contentColor,
                iconContainerColor = containerColor,
                actionContainerColor = colors.surfaceContainer,
                actionContentColor = contentColor,
                tagBackgroundColor = containerColor,
                tagTextColor = contentColor,
                titleColor = contentColor,
                descriptionColor = contentColor,
                supportingContentColor = contentColor,
                accentColor = contentColor,
            )
        }
    }

    /**
     * 调色板颜色只负责色调, 不携带透明度 —— 这些颜色全部经玻璃管线
     * (glassBackground/GlassCard)渲染, 颜色自身 alpha 会被覆盖率公式丢弃,
     * 层次由 GlassStyle 阶梯表达。常规内容卡沿用小工具的次级主题色。
     */
    @Composable
    fun palette(
        variant: TonalCardVisualVariant,
        defaultContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        defaultIconContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        defaultIconContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        defaultTagBackgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        defaultTagTextColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
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

/** 内容卡共用的图标底座；插槽支持图标注册表和页面直接传入的 ImageVector。 */
@Composable
fun TonalCardIcon(
    palette: TonalCardPalette,
    content: @Composable (Modifier, Color) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .glassBackground(
                style = TonalCardPaletteDefaults.accentStyle,
                shape = TonalCardPaletteDefaults.shape,
                color = palette.iconContainerColor,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content(Modifier.size(24.dp), palette.iconContentColor)
    }
}
