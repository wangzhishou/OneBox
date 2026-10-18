package com.shifenmiao.theme

import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CheckboxDefaults
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
    fun checkboxColors() = CheckboxDefaults.colors().copy(
        checkedBoxColor = MaterialTheme.colorScheme.primaryContainer,
        checkedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
        checkedCheckmarkColor = MaterialTheme.colorScheme.onPrimaryContainer,
        uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
        uncheckedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        // 关闭和禁用都是实心轨道，不画 Material 默认描边。
        disabledCheckedThumbColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f),
        disabledCheckedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
        disabledCheckedBorderColor = Color.Transparent,
        disabledCheckedIconColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.7f),
        disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        disabledUncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f),
        disabledUncheckedBorderColor = Color.Transparent,
        disabledUncheckedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
        val scheme = MaterialTheme.colorScheme
        return OutlinedTextFieldDefaults.colors().copy(
            focusedTextColor = scheme.onSurface,
            unfocusedTextColor = scheme.onSurface,
            disabledTextColor = scheme.onSurface.copy(alpha = 0.38f),
            errorTextColor = scheme.onSurface,
            // 底和描边由 GlassOutlinedTextField 自己画, 聚焦和未聚焦同一条线宽。
            // 这里的指示色只留给还没接上组件的调用方, 比上一档再淡。
            focusedContainerColor = scheme.surfaceContainerLow.copy(alpha = 0.30f),
            unfocusedContainerColor = scheme.surfaceContainerLow.copy(alpha = 0.26f),
            disabledContainerColor = scheme.surfaceContainerLow.copy(alpha = 0.16f),
            errorContainerColor = scheme.surfaceContainerLow.copy(alpha = 0.30f),
            cursorColor = scheme.onPrimaryContainer,
            errorCursorColor = scheme.error,
            focusedIndicatorColor = scheme.onPrimaryContainer.copy(alpha = 0.32f),
            unfocusedIndicatorColor = scheme.outline.copy(alpha = 0.24f),
            disabledIndicatorColor = scheme.outline.copy(alpha = 0.16f),
            errorIndicatorColor = scheme.error.copy(alpha = 0.55f),
            focusedPlaceholderColor = scheme.onSurfaceVariant.copy(alpha = 0.50f),
            unfocusedPlaceholderColor = scheme.onSurfaceVariant.copy(alpha = 0.62f),
            disabledPlaceholderColor = scheme.onSurfaceVariant.copy(alpha = 0.32f),
            focusedLabelColor = scheme.onPrimaryContainer,
            unfocusedLabelColor = scheme.onSurfaceVariant,
            disabledLabelColor = scheme.onSurfaceVariant.copy(alpha = 0.38f),
            errorLabelColor = scheme.error,
            focusedLeadingIconColor = scheme.onPrimaryContainer,
            unfocusedLeadingIconColor = scheme.onSurfaceVariant,
            disabledLeadingIconColor = scheme.onSurfaceVariant.copy(alpha = 0.38f),
            errorLeadingIconColor = scheme.error,
            focusedTrailingIconColor = scheme.onPrimaryContainer,
            unfocusedTrailingIconColor = scheme.onSurfaceVariant,
            disabledTrailingIconColor = scheme.onSurfaceVariant.copy(alpha = 0.38f),
            errorTrailingIconColor = scheme.error,
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

val LocalAppColors = staticCompositionLocalOf {
    AppColors()
}