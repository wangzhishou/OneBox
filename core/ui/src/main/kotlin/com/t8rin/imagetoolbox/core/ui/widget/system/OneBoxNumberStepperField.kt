package com.t8rin.imagetoolbox.core.ui.widget.system

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextFieldVisualPreset
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMinus

@Composable
fun OneBoxNumberStepperField(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: IntRange,
    step: Int = 1,
    enabled: Boolean = true,
    decrementContentDescription: String,
    incrementContentDescription: String,
) {
    val focusManager = LocalFocusManager.current
    var draftValue by rememberSaveable(value) { mutableStateOf(value.toString()) }

    LaunchedEffect(value) {
        val resolvedValue = value.coerceIn(valueRange.first, valueRange.last)
        val resolvedText = resolvedValue.toString()
        if (draftValue != resolvedText) {
            draftValue = resolvedText
        }
    }

    val maxDigits = remember(valueRange) { valueRange.last.toString().length }

    fun commitDraftValue() {
        val resolvedValue = draftValue.toIntOrNull()
            ?.coerceIn(valueRange.first, valueRange.last)
            ?: value.coerceIn(valueRange.first, valueRange.last)
        draftValue = resolvedValue.toString()
        if (resolvedValue != value) {
            onValueChange(resolvedValue)
        }
    }

    fun updateByStep(delta: Int) {
        val nextValue = (value + delta).coerceIn(valueRange.first, valueRange.last)
        draftValue = nextValue.toString()
        if (nextValue != value) {
            onValueChange(nextValue)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EnhancedIconButton(
            onClick = {
                focusManager.clearFocus(force = true)
                updateByStep(-step)
            },
            enabled = enabled && value > valueRange.first,
            modifier = Modifier.size(30.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            forceMinimumInteractiveComponentSize = false,
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMinus,
                contentDescription = decrementContentDescription,
                modifier = Modifier.size(16.dp),
            )
        }

        GlassOutlinedTextField(
            value = draftValue,
            onValueChange = { input ->
                val filteredValue = input.filter(Char::isDigit).take(maxDigits)
                draftValue = filteredValue
            },
            modifier = Modifier
                .widthIn(min = 48.dp, max = 52.dp)
                .height(38.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        commitDraftValue()
                    }
                },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    commitDraftValue()
                    focusManager.clearFocus(force = true)
                }
            ),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
            colors = AppTheme.colors.getOutlinedTextFieldColors(),
            visualPreset = GlassTextFieldVisualPreset.Quiet,
            shape = OneBoxDesignSystem.listRowShape,
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
        )

        EnhancedIconButton(
            onClick = {
                focusManager.clearFocus(force = true)
                updateByStep(step)
            },
            enabled = enabled && value < valueRange.last,
            modifier = Modifier.size(30.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            forceMinimumInteractiveComponentSize = false,
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                contentDescription = incrementContentDescription,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

