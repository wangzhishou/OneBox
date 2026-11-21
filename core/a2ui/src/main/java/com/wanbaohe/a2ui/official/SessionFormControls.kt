package com.wanbaohe.a2ui.official

import androidx.a2ui.compose.runtime.A2uiComponentScope
import androidx.a2ui.compose.runtime.A2uiComponentState
import androidx.a2ui.compose.ui.catalog.A2uiBasicCatalogV1
import androidx.a2ui.compose.ui.catalog.A2uiBasicCatalogV1.AccessibilityAttributes
import androidx.a2ui.compose.ui.catalog.A2uiBasicCatalogV1.CheckRule
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.a2ui.catalog.MaterialA2uiBasicCatalogV1Defaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassCheckbox
import com.wanbaohe.core.ui.widget.glass.GlassCustomSlider
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedButton
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassTextButton

internal object SessionButton : A2uiBasicCatalogV1.Button {
    @Composable
    override fun A2uiComponentScope.TypedContent(
        childId: String,
        variant: A2uiBasicCatalogV1.Button.Variant,
        action: Map<String, Any?>,
        accessibility: AccessibilityAttributes?,
        checks: List<CheckRule>,
        modifier: Modifier,
    ) {
        val failure = checks.firstOrNull { !it.condition }
        val enabled = failure == null && observeA2uiComponentState(childId) is A2uiComponentState.Success
        val onClick: () -> Unit = { dispatchAction(action) }
        val semantics = Modifier.a2uiControlSemantics(accessibility, failure)
        Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            when (variant) {
                A2uiBasicCatalogV1.Button.Variant.Primary ->
                    GlassButton(onClick = onClick, enabled = enabled, modifier = semantics) { SessionChild(childId) }
                A2uiBasicCatalogV1.Button.Variant.Secondary ->
                    GlassOutlinedButton(onClick = onClick, enabled = enabled, modifier = semantics) { SessionChild(childId) }
                A2uiBasicCatalogV1.Button.Variant.Borderless ->
                    GlassTextButton(onClick = onClick, enabled = enabled, modifier = semantics) { SessionChild(childId) }
            }
            CheckMessage(failure)
        }
    }
}

internal object SessionTextField : A2uiBasicCatalogV1.TextField {
    @Suppress("DEPRECATION")
    @Composable
    override fun A2uiComponentScope.TypedContent(
        label: String,
        value: String?,
        variant: A2uiBasicCatalogV1.TextField.Variant,
        validationRegexp: String?,
        onValueChange: (String) -> Unit,
        enabled: Boolean,
        accessibility: AccessibilityAttributes?,
        checks: List<CheckRule>,
        modifier: Modifier,
    ) {
        val regex = remember(validationRegexp) { validationRegexp?.let { runCatching { Regex(it) }.getOrNull() } }
        if (validationRegexp != null && regex == null) {
            val scope = this
            with(MaterialA2uiBasicCatalogV1Defaults.textField) {
                scope.TypedContent(label, value, variant, validationRegexp, onValueChange, enabled, accessibility, checks, modifier)
            }
            return
        }
        val text = value.orEmpty()
        val failure = checks.firstOrNull { !it.condition }
        val obscured = variant == A2uiBasicCatalogV1.TextField.Variant.Obscured
        val keyboardType = when (variant) {
            A2uiBasicCatalogV1.TextField.Variant.Obscured -> KeyboardType.Password
            A2uiBasicCatalogV1.TextField.Variant.Number -> KeyboardType.Number
            else -> KeyboardType.Text
        }
        GlassOutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth().a2uiControlSemantics(accessibility, failure),
            enabled = enabled,
            label = { Text(label) },
            singleLine = variant != A2uiBasicCatalogV1.TextField.Variant.LongText,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (obscured) PasswordVisualTransformation() else VisualTransformation.None,
            isError = failure != null || (text.isNotEmpty() && regex?.matches(text) == false),
            supportingText = failure?.message?.takeIf(String::isNotBlank)?.let { message -> { Text(message) } },
        )
    }
}

internal object SessionCheckBox : A2uiBasicCatalogV1.CheckBox {
    @Composable
    override fun A2uiComponentScope.TypedContent(
        label: String,
        value: Boolean,
        onValueChange: (Boolean) -> Unit,
        enabled: Boolean,
        accessibility: AccessibilityAttributes?,
        checks: List<CheckRule>,
        modifier: Modifier,
    ) {
        val failure = checks.firstOrNull { !it.condition }
        Column(modifier) {
            Row(
                modifier = Modifier.a2uiControlSemantics(accessibility, failure)
                    .toggleable(value = value, enabled = enabled, role = Role.Checkbox, onValueChange = onValueChange),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassCheckbox(checked = value, onCheckedChange = null, enabled = enabled)
                Text(label)
            }
            CheckMessage(failure)
        }
    }
}

internal object SessionSlider : A2uiBasicCatalogV1.Slider {
    @Composable
    override fun A2uiComponentScope.TypedContent(
        label: String?,
        min: Float,
        max: Float,
        value: Float,
        onValueChange: (Float) -> Unit,
        enabled: Boolean,
        accessibility: AccessibilityAttributes?,
        checks: List<CheckRule>,
        modifier: Modifier,
    ) {
        if (!min.isFinite() || !max.isFinite() || min >= max || !value.isFinite()) {
            val scope = this
            with(MaterialA2uiBasicCatalogV1Defaults.slider) {
                scope.TypedContent(label, min, max, value, onValueChange, enabled, accessibility, checks, modifier)
            }
            return
        }
        val failure = checks.firstOrNull { !it.condition }
        Column(modifier) {
            if (!label.isNullOrBlank()) Text(label, style = MaterialTheme.typography.labelLarge)
            GlassCustomSlider(
                value = value.coerceIn(min, max),
                onValueChange = onValueChange,
                enabled = enabled,
                valueRange = min..max,
                modifier = Modifier.fillMaxWidth().a2uiControlSemantics(accessibility, failure),
            )
            CheckMessage(failure)
        }
    }
}

internal object SessionChoicePicker : A2uiBasicCatalogV1.ChoicePicker {
    @Composable
    override fun A2uiComponentScope.TypedContent(
        label: String?,
        options: List<A2uiBasicCatalogV1.ChoicePicker.Option>,
        value: List<String>,
        variant: A2uiBasicCatalogV1.ChoicePicker.Variant,
        displayStyle: A2uiBasicCatalogV1.ChoicePicker.DisplayStyle,
        filterable: Boolean,
        onValueChange: (List<String>) -> Unit,
        enabled: Boolean,
        accessibility: AccessibilityAttributes?,
        checks: List<CheckRule>,
        modifier: Modifier,
    ) {
        if (displayStyle != A2uiBasicCatalogV1.ChoicePicker.DisplayStyle.Chips || filterable) {
            val scope = this
            with(MaterialA2uiBasicCatalogV1Defaults.choicePicker) {
                scope.TypedContent(label, options, value, variant, displayStyle, filterable, onValueChange,
                    enabled, accessibility, checks, modifier)
            }
            return
        }
        val failure = checks.firstOrNull { !it.condition }
        Column(modifier.a2uiControlSemantics(accessibility, failure)) {
            if (!label.isNullOrBlank()) Text(label, style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val selected = option.value in value
                    GlassFilterChip(
                        selected = selected,
                        onClick = {
                            val next = when {
                                variant == A2uiBasicCatalogV1.ChoicePicker.Variant.MutuallyExclusive -> listOf(option.value)
                                selected -> value.filterNot { it == option.value }
                                else -> value + option.value
                            }
                            onValueChange(next)
                        },
                        label = { Text(option.label) },
                        enabled = enabled,
                    )
                }
            }
            CheckMessage(failure)
        }
    }
}

@Composable
private fun CheckMessage(failure: CheckRule?) {
    failure?.message?.takeIf(String::isNotBlank)?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

private fun Modifier.a2uiControlSemantics(accessibility: AccessibilityAttributes?, failure: CheckRule?): Modifier {
    val base = a2uiHostSemantics(accessibility)
    val message = failure?.message?.takeIf(String::isNotBlank) ?: return base
    return base.semantics { error(message) }
}
