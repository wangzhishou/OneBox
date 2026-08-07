package com.shifenmiao.base.ui.card

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.shifenmiao.theme.AppTheme

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

    @Composable
    fun palette(
        variant: TonalCardVisualVariant,
        defaultContainerColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = AppTheme.dimens.containerAlpha),
        defaultIconContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        defaultIconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
        defaultTagBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = AppTheme.dimens.containerAlpha),
        defaultTagTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        defaultActionContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = AppTheme.dimens.containerAlpha),
        defaultActionContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ): TonalCardPalette {
        val colorScheme = MaterialTheme.colorScheme
        val elevatedAlpha = (AppTheme.dimens.containerAlpha + 0.08f).coerceAtMost(0.92f)
        val highlightAlpha = (AppTheme.dimens.containerAlpha + 0.14f).coerceAtMost(0.96f)

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
                        accentColor = colorScheme.primary,
                    )
                }

                TonalCardVisualVariant.Recommend -> {
                    TonalCardPalette(
                        containerColor = colorScheme.secondaryContainer.copy(alpha = elevatedAlpha),
                        iconContentColor = colorScheme.onSecondaryContainer,
                        iconContainerColor = colorScheme.secondaryContainer,
                        actionContainerColor = colorScheme.surfaceContainerHighest.copy(alpha = elevatedAlpha),
                        actionContentColor = colorScheme.onSurfaceVariant,
                        tagBackgroundColor = colorScheme.secondaryContainer.copy(alpha = 0.92f),
                        tagTextColor = colorScheme.onSecondaryContainer,
                        titleColor = colorScheme.onSurface,
                        descriptionColor = colorScheme.onSurfaceVariant,
                        supportingContentColor = colorScheme.secondary,
                        accentColor = colorScheme.secondary,
                    )
                }

                TonalCardVisualVariant.Highlighted -> {
                    TonalCardPalette(
                        containerColor = colorScheme.tertiaryContainer.copy(alpha = highlightAlpha),
                        iconContentColor = colorScheme.onTertiaryContainer,
                        iconContainerColor = colorScheme.tertiaryContainer,
                        actionContainerColor = colorScheme.surfaceContainerHighest,
                        actionContentColor = colorScheme.onSurface,
                        tagBackgroundColor = colorScheme.surfaceContainerHighest,
                        tagTextColor = colorScheme.onSurfaceVariant,
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
