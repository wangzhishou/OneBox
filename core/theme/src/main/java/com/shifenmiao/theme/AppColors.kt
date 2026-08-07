package com.shifenmiao.theme

import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

class AppColors {

    @Composable
    fun assistChipColors() = AssistChipDefaults.assistChipColors().copy(
        containerColor = MaterialTheme.colorScheme.primary.copy(0.15f),
        labelColor = MaterialTheme.colorScheme.primary
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
        checkedThumbColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.68f),
        checkedIconColor = MaterialTheme.colorScheme.primaryContainer,
        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f),
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainer,
        uncheckedBorderColor = Color.Transparent,
        uncheckedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    @Composable
    fun getSuggestionChipColors() = SuggestionChipDefaults.suggestionChipColors().copy(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        labelColor = MaterialTheme.colorScheme.onSurface,
    )

    @Composable
    fun sliderColors() = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primaryContainer,
        activeTickColor = MaterialTheme.colorScheme.primaryContainer,
        activeTrackColor = MaterialTheme.colorScheme.primaryContainer,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        inactiveTickColor = MaterialTheme.colorScheme.surfaceContainerHighest
    )

    @Composable
    fun getPrimaryColor(): Color {
        return MaterialTheme.colorScheme.primary
    }

    @Composable
    fun getAppLinearGradientColors(): List<Color> {
        return listOf(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
        )
    }

    @Composable
    fun getOnPrimaryColor(): Color {
        return MaterialTheme.colorScheme.onPrimaryContainer
    }

    @Composable
    fun getInactiveContainerColor(): Color {
        return MaterialTheme.colorScheme.surfaceContainer
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
    fun getRandomIconColor(): Color {
        val colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.tertiaryContainer
        )
        val randomIndex = Random.nextInt(colors.size)
        return colors[randomIndex]
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
    fun getDatePickerColors(): DatePickerColors {
        return DatePickerDefaults.colors().copy(
            selectedDayContentColor = AppTheme.colors.getOnPrimaryColor(),
            selectedDayContainerColor = AppTheme.colors.getPrimaryColor(),
        )
    }

    @Composable
    fun getTimePickerColors(): TimePickerColors {
        return TimePickerDefaults.colors().copy(
            periodSelectorSelectedContainerColor = AppTheme.colors.getPrimaryColor(),
            timeSelectorSelectedContainerColor = AppTheme.colors.getPrimaryColor(),
            timeSelectorSelectedContentColor = AppTheme.colors.getOnPrimaryColor(),
            selectorColor = AppTheme.colors.getPrimaryColor(),
        )

    }

    @Composable
    fun getGrayColor(): Color {
        return MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
    }

    @Composable
    fun getFilterChipColors(): SelectableChipColors {
        return FilterChipDefaults.filterChipColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            labelColor = MaterialTheme.colorScheme.onSurface,
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