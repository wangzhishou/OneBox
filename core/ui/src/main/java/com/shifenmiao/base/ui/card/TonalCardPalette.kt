package com.shifenmiao.base.ui.card

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
                        supportingContentColor = colorScheme.secondary,
                        accentColor = colorScheme.secondary,
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
                        accentColor = colorScheme.tertiary,
                    )
                }
            }
        }
    }
}
