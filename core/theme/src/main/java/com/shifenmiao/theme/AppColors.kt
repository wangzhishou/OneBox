package com.shifenmiao.theme

import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

class AppColors {

    @Composable
    fun assistChipColors() = AssistChipDefaults.assistChipColors().copy(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.15f),
        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    @Composable
    fun filledTonalButtonColors() = ButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
    )

    @Composable
    fun switchColors() = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
        checkedIconColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
        checkedBorderColor = Color.Transparent,
        uncheckedThumbColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
        uncheckedBorderColor = Color.Transparent,
        uncheckedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    @Composable
    fun getSuggestionChipColors() = SuggestionChipDefaults.suggestionChipColors().copy(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun getPrimaryColor(): Color {
        return MaterialTheme.colorScheme.onPrimaryContainer
    }

    @Composable
    fun getOnPrimaryColor(): Color {
        return MaterialTheme.colorScheme.onPrimaryContainer
    }

    @Composable
    fun getInactiveContainerColor(): Color {
        return MaterialTheme.colorScheme.surfaceContainerLow
    }

    @Composable
    fun getOnInactiveContainerColor(): Color {
        return MaterialTheme.colorScheme.onSurfaceVariant
    }

    @Composable
    fun getPrimaryButtonColors(): ButtonColors {
        return ButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }

    @Composable
    fun getSurfaceContainerButtonColors(): ButtonColors {
        return ButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }

    @Composable
    fun getSecondaryContainerButtonColors(): ButtonColors {
        return ButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }

    @Composable
    fun getOutlinedTextFieldColors(): TextFieldColors {
        return OutlinedTextFieldDefaults.colors().copy(
            focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f),
            cursorColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    @Composable
    fun getPrimaryTextColor(): Color {
        return MaterialTheme.colorScheme.onSurfaceVariant
    }

    @Composable
    fun buttonColors(): ButtonColors {
        return ButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }

    @Composable
    fun iconButtonColors(): IconButtonColors {
        return IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }

    @Composable
    fun getContainerSurfaceColor(): Color {
        return MaterialTheme.colorScheme.surfaceContainerLowest
    }

    @Composable
    fun getGrayColor(): Color {
        return MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
    }

    @Composable
    fun getFilterChipColors(): SelectableChipColors {
        return FilterChipDefaults.filterChipColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    @Composable
    fun filledIconButtonColors(): IconButtonColors {
        return IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

val LocalAppColors = staticCompositionLocalOf {
    AppColors()
}