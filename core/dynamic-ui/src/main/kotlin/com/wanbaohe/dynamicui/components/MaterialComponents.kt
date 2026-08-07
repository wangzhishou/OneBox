package com.wanbaohe.dynamicui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.wanbaohe.dynamicui.ir.ActionSpec
import com.wanbaohe.dynamicui.ir.UiNode
import com.wanbaohe.dynamicui.ir.propBool
import com.wanbaohe.dynamicui.ir.propFloat
import com.wanbaohe.dynamicui.ir.propInt
import com.wanbaohe.dynamicui.ir.propNode
import com.wanbaohe.dynamicui.ir.propNodeList
import com.wanbaohe.dynamicui.ir.propString
import com.wanbaohe.dynamicui.action.DynamicUiInternalState
import com.wanbaohe.dynamicui.modifier.ModifierRegistry
import com.wanbaohe.dynamicui.renderer.ComponentRegistry
import com.wanbaohe.dynamicui.renderer.RenderContext
import com.wanbaohe.dynamicui.renderer.UiNodeRenderer
import com.wanbaohe.dynamicui.state.UiStateScope
import com.wanbaohe.dynamicui.state.ValueExprResolver
import com.wanbaohe.dynamicui.state.rememberResolvedBool
import com.wanbaohe.dynamicui.state.rememberResolvedString
import com.shifenmiao.base.ui.icon.IconRegistry
import com.shifenmiao.base.ui.icon.IconAvatar
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassAssistChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassBadge
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassBadgedBox
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCheckbox
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCircularProgressIndicator
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilledIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassLinearProgressIndicator
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassRadioButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStepper
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSuggestionChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextFieldVisualPreset
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTopAppBar
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomRangeSlider
import com.t8rin.imagetoolbox.core.ui.widget.glass.SingleChoiceGlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.fillMaxSize
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle

/**
 * Provides Scaffold padding to deeply nested list components (e.g. LazyColumn)
 * so they can implement edge-to-edge scrolling without being hard-clipped.
 */
val LocalScaffoldPadding = compositionLocalOf { PaddingValues(0.dp) }

/**
 * Material3 component renderers.
 * Registered in [registerMaterialComponents].
 */

// ─── Registration ────────────────────────────────────────────────────────────

fun ComponentRegistry.registerMaterialComponents() {
    register("Text") { n, s, c, i -> TextRenderer(n, s, c, i) }
    register("Button") { n, s, c, i -> ButtonRenderer(n, s, c, i) }
    register("OutlinedButton") { n, s, c, i -> OutlinedButtonRenderer(n, s, c, i) }
    register("TextButton") { n, s, c, i -> TextButtonRenderer(n, s, c, i) }
    register("ElevatedButton") { n, s, c, i -> ElevatedButtonRenderer(n, s, c, i) }
    register("FilledTonalButton") { n, s, c, i -> TonalButtonRenderer(n, s, c, i) }
    register("TextField") { n, s, c, i -> TextFieldRenderer(n, s, c, i) }
    register("OutlinedTextField") { n, s, c, i -> OutlinedTextFieldRenderer(n, s, c, i) }
    register("Card") { n, s, c, i -> CardRenderer(n, s, c, i) }
    register("Surface") { n, s, c, i -> SurfaceRenderer(n, s, c, i) }
    register("Switch") { n, s, c, i -> SwitchRenderer(n, s, c, i) }
    register("Checkbox") { n, s, c, i -> CheckboxRenderer(n, s, c, i) }
    register("Slider") { n, s, c, i -> SliderRenderer(n, s, c, i) }
    register("RangeSlider") { n, s, c, i -> RangeSliderRenderer(n, s, c, i) }
    register("SingleChoiceSegmentedButtonRow") { n, s, c, i -> SegmentedButtonRowRenderer(n, s, c, i) }
    register("SegmentedButtonRow") { n, s, c, i -> SegmentedButtonRowRenderer(n, s, c, i) }
    register("Icon") { n, s, c, i -> IconRenderer(n, s, c, i) }
    register("Chip") { n, s, c, i -> ChipRenderer(n, s, c, i) }
    register("FilterChip") { n, s, c, i -> ChipRenderer(n, s, c, i) }
    register("AssistChip") { n, s, c, i -> AssistChipRenderer(n, s, c, i) }
    register("SuggestionChip") { n, s, c, i -> SuggestionChipRenderer(n, s, c, i) }
    register("Badge") { n, s, c, i -> BadgeRenderer(n, s, c, i) }
    register("BadgedBox") { n, s, c, i -> BadgedBoxRenderer(n, s, c, i) }
    register("RadioButton") { n, s, c, i -> RadioButtonRenderer(n, s, c, i) }
    register("RadioGroup") { n, s, c, i -> RadioGroupRenderer(n, s, c, i) }
    register("Stepper") { n, s, c, i -> StepperRenderer(n, s, c, i) }
    register("BottomSheet") { n, s, c, i -> BottomSheetRenderer(n, s, c, i) }
    register("ModalBottomSheet") { n, s, c, i -> BottomSheetRenderer(n, s, c, i) }
    register("Form") { n, s, c, i -> FormRenderer(n, s, c, i) }
    register("CircularProgressIndicator") { n, s, c, i -> CircularProgressRenderer(n, s, c, i) }
    register("LinearProgressIndicator") { n, s, c, i -> LinearProgressRenderer(n, s, c, i) }
    register("Scaffold") { n, s, c, i -> ScaffoldRenderer(n, s, c, i) }
    register("AlertDialog") { n, s, c, i -> AlertDialogRenderer(n, s, c, i) }
    register("TopAppBar") { n, s, c, i -> TopAppBarRenderer(n, s, c, i) }
    register("RowSelector") { n, s, c, i -> RowSelectorRenderer(n, s, c, i) }
    register("ColumnSelector") { n, s, c, i -> ColumnSelectorRenderer(n, s, c, i) }
}

private fun RenderContext.dispatch(action: ActionSpec, scope: UiStateScope, itemContext: Map<String, Any?>) {
    actionDispatcher(action, scope, this, itemContext)
}

// ─── Text ────────────────────────────────────────────────────────────────────

@Composable
private fun TextRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val text = rememberResolvedString(node.props["text"], scope, itemContext)
    val color = ModifierRegistry.parseColor(node.propString("color"))
    val fontSize = node.propFloat("fontSize")
    val fontWeight = parseFontWeight(node.propString("fontWeight"))
    val maxLines = node.propInt("maxLines", Int.MAX_VALUE)
    val style = node.propString("textStyle")
    val textAlign = parseTextAlign(node.propString("textAlign"))
    val overflow = parseOverflow(node.propString("overflow"))
    val clickAction = node.actions["onClick"]

    val baseStyle = parseTextStyle(style)
    Text(
        text = text,
        modifier = ctx.modifierPipeline.build(node.modifier).let { baseModifier ->
            if (clickAction != null) {
                baseModifier.clickable { ctx.dispatch(clickAction, scope, itemContext) }
            } else {
                baseModifier
            }
        },
        color = color ?: Color.Unspecified,
        fontSize = if (fontSize > 0f) fontSize.sp else baseStyle.fontSize,
        fontWeight = fontWeight ?: baseStyle.fontWeight,
        style = baseStyle,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

// ─── Buttons ─────────────────────────────────────────────────────────────────

@Composable
private fun ButtonRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val enabled = rememberResolvedBool(node.props["enabled"] ?: true, scope, itemContext)
    GlassButton(
        onClick = { node.actions["onClick"]?.let { ctx.dispatch(it, scope, itemContext) } },
        modifier = ctx.modifierPipeline.build(node.modifier),
        enabled = enabled,
    ) {
        if (node.children.isNotEmpty()) node.children.forEach { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
        else Text(rememberResolvedString(node.props["text"], scope, itemContext))
    }
}

@Composable
private fun OutlinedButtonRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val enabled = rememberResolvedBool(node.props["enabled"] ?: true, scope, itemContext)
    GlassOutlinedButton(
        onClick = { node.actions["onClick"]?.let { ctx.dispatch(it, scope, itemContext) } },
        modifier = ctx.modifierPipeline.build(node.modifier),
        enabled = enabled,
    ) {
        if (node.children.isNotEmpty()) node.children.forEach { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
        else Text(rememberResolvedString(node.props["text"], scope, itemContext))
    }
}

@Composable
private fun TextButtonRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    GlassTextButton(
        onClick = { node.actions["onClick"]?.let { ctx.dispatch(it, scope, itemContext) } },
        modifier = ctx.modifierPipeline.build(node.modifier),
    ) {
        if (node.children.isNotEmpty()) node.children.forEach { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
        else Text(rememberResolvedString(node.props["text"], scope, itemContext))
    }
}

@Composable
private fun ElevatedButtonRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    GlassTonalButton(
        onClick = { node.actions["onClick"]?.let { ctx.dispatch(it, scope, itemContext) } },
        modifier = ctx.modifierPipeline.build(node.modifier),
    ) {
        if (node.children.isNotEmpty()) node.children.forEach { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
        else Text(rememberResolvedString(node.props["text"], scope, itemContext))
    }
}

@Composable
private fun TonalButtonRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    GlassTonalButton(
        onClick = { node.actions["onClick"]?.let { ctx.dispatch(it, scope, itemContext) } },
        modifier = ctx.modifierPipeline.build(node.modifier),
    ) {
        if (node.children.isNotEmpty()) node.children.forEach { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
        else Text(rememberResolvedString(node.props["text"], scope, itemContext))
    }
}

// ─── TextField ───────────────────────────────────────────────────────────────

@Composable
private fun TextFieldRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val value = rememberResolvedString(node.props["value"], scope, itemContext)
    val label = node.propString("label")
    val placeholder = node.propString("placeholder")
    val labelNode = node.propNode("label")
    val placeholderNode = node.propNode("placeholder")
    val isPassword = node.propBool("password")
    val keyboardType = parseKeyboardType(node.propString("keyboardType"))
    val validationError = node.id?.let { scope.getByPath(DynamicUiInternalState.validationErrorKey(it))?.toString() }

    GlassOutlinedTextField(
        value = value,
        onValueChange = { newVal ->
            val action = node.actions["onValueChange"]
            if (action != null) {
                ctx.dispatch(action.copy(params = action.params + ("value" to newVal)), scope, itemContext)
            } else {
                resolveBindingPath(node, "value")?.let { scope.setByPath(it, newVal) }
            }
            node.id?.let { scope.setByPath(DynamicUiInternalState.validationErrorKey(it), null) }
        },
        modifier = ctx.modifierPipeline.build(node.modifier).fillMaxWidth(),
        label = composableTextSlot(labelNode, label, scope, ctx, itemContext),
        placeholder = composableTextSlot(placeholderNode, placeholder, scope, ctx, itemContext),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = node.propBool("singleLine", false),
        maxLines = node.propInt("maxLines", Int.MAX_VALUE),
        minLines = node.propInt("minLines", 1),
        style = GlassStyle.Thin,
        visualPreset = GlassTextFieldVisualPreset.Quiet,
        isError = validationError != null,
        supportingText = validationError?.let { { Text(it) } },
    )
}

@Composable
private fun OutlinedTextFieldRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val value = rememberResolvedString(node.props["value"], scope, itemContext)
    val label = node.propString("label")
    val placeholder = node.propString("placeholder")
    val labelNode = node.propNode("label")
    val placeholderNode = node.propNode("placeholder")
    val isPassword = node.propBool("password")
    val readOnly = node.propBool("readOnly", false)
    val enabled = node.propBool("enabled", true)
    val keyboardType = parseKeyboardType(node.propString("keyboardType"))
    val trailingIconSlot = node.propNode("trailingIcon")
    val leadingIconSlot = node.propNode("leadingIcon")
    val validationError = node.id?.let { scope.getByPath(DynamicUiInternalState.validationErrorKey(it))?.toString() }

    GlassOutlinedTextField(
        value = value,
        onValueChange = { newVal ->
            val action = node.actions["onValueChange"]
            if (action != null) {
                ctx.dispatch(action.copy(params = action.params + ("value" to newVal)), scope, itemContext)
            } else {
                resolveBindingPath(node, "value")?.let { scope.setByPath(it, newVal) }
            }
            node.id?.let { scope.setByPath(DynamicUiInternalState.validationErrorKey(it), null) }
        },
        modifier = ctx.modifierPipeline.build(node.modifier).fillMaxWidth(),
        label = composableTextSlot(labelNode, label, scope, ctx, itemContext),
        placeholder = composableTextSlot(placeholderNode, placeholder, scope, ctx, itemContext),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = node.propBool("singleLine", false),
        maxLines = node.propInt("maxLines", Int.MAX_VALUE),
        minLines = node.propInt("minLines", 1),
        readOnly = readOnly,
        enabled = enabled,
        trailingIcon = trailingIconSlot?.let { iconNode ->
            {
                UiNodeRenderer.Render(iconNode, scope, ctx, itemContext)
            }
        },
        leadingIcon = leadingIconSlot?.let { iconNode ->
            {
                UiNodeRenderer.Render(iconNode, scope, ctx, itemContext)
            }
        },
        isError = validationError != null,
        supportingText = validationError?.let { { Text(it) } },
    )
}

// ─── Card ────────────────────────────────────────────────────────────────────

@Composable
private fun CardRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val containerColor = rememberResolvedString(node.props["containerColor"], scope, itemContext)
        .takeIf { it.isNotBlank() }
        ?.let(ModifierRegistry::parseColor)
        ?: MaterialTheme.colorScheme.surfaceContainer
    val clickAction = node.actions["onClick"]
    if (clickAction != null) {
        GlassCard(
            onClick = { ctx.dispatch(clickAction, scope, itemContext) },
            modifier = ctx.modifierPipeline.build(node.modifier),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = containerColor,
            ),
        ) {
            node.children.forEach { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
        }
    } else {
        GlassCard(
            modifier = ctx.modifierPipeline.build(node.modifier),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = containerColor,
            ),
        ) {
            node.children.forEach { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
        }
    }
}

// ─── Surface ─────────────────────────────────────────────────────────────────

@Composable
private fun SurfaceRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    GlassSurface(modifier = ctx.modifierPipeline.build(node.modifier)) {
        node.children.forEach { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
    }
}

// ─── Switch ──────────────────────────────────────────────────────────────────

@Composable
private fun SwitchRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val checked = rememberResolvedBool(node.props["checked"], scope, itemContext)
    val validationError = node.id?.let { scope.getByPath(DynamicUiInternalState.validationErrorKey(it))?.toString() }
    GlassSwitch(
        checked = checked,
        onCheckedChange = { newVal ->
            val action = node.actions["onCheckedChange"]
            if (action != null) {
                ctx.dispatch(action.copy(params = action.params + ("value" to newVal.toString())), scope, itemContext)
            } else {
                resolveBindingPath(node, "checked")?.let { scope.setByPath(it, newVal) }
            }
            node.id?.let { scope.setByPath(DynamicUiInternalState.validationErrorKey(it), null) }
        },
        modifier = ctx.modifierPipeline.build(node.modifier),
        checkedGlassColor = if (validationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
    )
}

// ─── Checkbox ────────────────────────────────────────────────────────────────

@Composable
private fun CheckboxRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val checked = rememberResolvedBool(node.props["checked"], scope, itemContext)
    val validationError = node.id?.let { scope.getByPath(DynamicUiInternalState.validationErrorKey(it))?.toString() }
    GlassCheckbox(
        checked = checked,
        onCheckedChange = { newVal ->
            val action = node.actions["onCheckedChange"]
            if (action != null) {
                ctx.dispatch(action.copy(params = action.params + ("value" to newVal.toString())), scope, itemContext)
            } else {
                resolveBindingPath(node, "checked")?.let { scope.setByPath(it, newVal) }
            }
            node.id?.let { scope.setByPath(DynamicUiInternalState.validationErrorKey(it), null) }
        },
        modifier = ctx.modifierPipeline.build(node.modifier),
        checkedGlassColor = if (validationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
    )
}

// ─── Slider ──────────────────────────────────────────────────────────────────

/**
 * 渲染 [com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider],
 * 参数与 M3 [Slider] 一致:
 *
 * - `value: Float` (state 表达式或字面量)
 * - `valueRange: "0..100"` (字符串范围)
 * - `steps: Int`
 * - `enabled: Boolean = true`
 * - `onValueChange: Boolean` → 走 `actions.onValueChange` 事件
 * - `onValueChangeFinished: Boolean` → 走 `actions.onValueChangeFinished` 事件
 *
 * Binding 优先:有 `binding` prop 时,onValueChange 直接写回 state 路径。
 */
@Composable
private fun SliderRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val rawValue = rememberResolvedString(node.props["value"], scope, itemContext)
    val value = (rawValue.toFloatOrNull() ?: node.propFloat("value"))
    val valueRangeStr = node.propString("valueRange")
    val min = valueRangeStr.substringBefore("..").toFloatOrNull() ?: 0f
    val max = valueRangeStr.substringAfter("..").toFloatOrNull() ?: 1f
    val steps = node.propInt("steps", 0).coerceAtLeast(0)
    val enabled = node.propBool("enabled", true)
    val onValueChangeFinished = node.propBool("onValueChangeFinished", false)

    val writeBack: (Float) -> Unit = { newVal ->
        val action = node.actions["onValueChange"]
        if (action != null) {
            ctx.dispatch(action.copy(params = action.params + ("value" to newVal.toString())), scope, itemContext)
        } else {
            resolveBindingPath(node, "value")?.let { scope.setByPath(it, newVal) }
        }
    }

    GlassCustomSlider(
        value = value,
        onValueChange = writeBack,
        modifier = ctx.modifierPipeline.build(node.modifier),
        enabled = enabled,
        valueRange = min..max,
        steps = steps,
        onValueChangeFinished = if (onValueChangeFinished) {
            {
                node.actions["onValueChangeFinished"]?.let { ctx.dispatch(it, scope, itemContext) }
            }
        } else null,
    )
}

// ─── RangeSlider ─────────────────────────────────────────────────────────────

/**
 * 渲染 [com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomRangeSlider],
 * 参数与 M3 `RangeSlider` 一致。
 *
 * JSON:
 * ```json
 * {
 *   "type": "RangeSlider",
 *   "props": {
 *     "value": "${state.form.priceRange}",  // [Float, Float] 或 "${a}..${b}"
 *     "valueRange": "0..1000",
 *     "steps": 0,
 *     "binding": "state.form.priceRange"
 *   }
 * }
 * ```
 */
@Composable
private fun RangeSliderRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val (initialStart, initialEnd) = rememberResolvedRange(node.props["value"], scope, itemContext)
    val valueRangeStr = node.propString("valueRange")
    val min = valueRangeStr.substringBefore("..").toFloatOrNull() ?: 0f
    val max = valueRangeStr.substringAfter("..").toFloatOrNull() ?: 1f
    val steps = node.propInt("steps", 0).coerceAtLeast(0)
    val enabled = node.propBool("enabled", true)
    val onValueChangeFinished = node.propBool("onValueChangeFinished", false)

    var sliderValue by remember(node.id, initialStart, initialEnd) {
        mutableStateOf(initialStart..initialEnd)
    }

    val writeBack: (ClosedFloatingPointRange<Float>) -> Unit = { newVal ->
        sliderValue = newVal
        val action = node.actions["onValueChange"]
        if (action != null) {
            ctx.dispatch(
                action.copy(
                    params = action.params + mapOf(
                        "start" to newVal.start.toString(),
                        "end" to newVal.endInclusive.toString(),
                    ),
                ),
                scope,
                itemContext,
            )
        } else {
            resolveBindingPath(node, "value")?.let { path ->
                scope.setByPath(path, listOf(newVal.start, newVal.endInclusive))
            }
        }
    }

    GlassCustomRangeSlider(
        value = sliderValue,
        onValueChange = writeBack,
        modifier = ctx.modifierPipeline.build(node.modifier),
        enabled = enabled,
        valueRange = min..max,
        steps = steps,
        onValueChangeFinished = if (onValueChangeFinished) {
            {
                node.actions["onValueChangeFinished"]?.let { ctx.dispatch(it, scope, itemContext) }
            }
        } else null,
    )
}

@Composable
private fun rememberResolvedRange(
    expr: Any?,
    scope: UiStateScope,
    itemContext: Map<String, Any?>,
): Pair<Float, Float> {
    val resolved by remember(expr, itemContext) {
        derivedStateOf {
            when (val raw = ValueExprResolver.resolve(expr?.toString().orEmpty(), scope, itemContext)) {
                is List<*> -> {
                    val a = (raw.getOrNull(0) as? Number)?.toFloat() ?: 0f
                    val b = (raw.getOrNull(1) as? Number)?.toFloat() ?: 1f
                    a to b
                }
                is String -> {
                    val parts = raw.split("..")
                    val a = parts.getOrNull(0)?.toFloatOrNull() ?: 0f
                    val b = parts.getOrNull(1)?.toFloatOrNull() ?: 1f
                    a to b
                }
                is ClosedFloatingPointRange<*> -> {
                    val a = (raw.start as? Number)?.toFloat() ?: 0f
                    val b = (raw.endInclusive as? Number)?.toFloat() ?: 1f
                    a to b
                }
                else -> 0f to 1f
            }
        }
    }
    return resolved
}

// ─── SingleChoiceSegmentedButtonRow ──────────────────────────────────────────

/**
 * 渲染 [SingleChoiceGlassSegmentedButtonRow]。
 *
 * JSON:
 * ```json
 * {
 *   "type": "SingleChoiceSegmentedButtonRow",
 *   "props": {
 *     "options": [
 *       { "label": "口语", "value": "casual" },
 *       { "label": "正式", "value": "formal" }
 *     ],
 *     "value": "${state.form.tone}",
 *     "binding": "state.form.tone"
 *   }
 * }
 * ```
 *
 * 选中时把 `value` 写回 binding 路径。`actions.onValueChange` 也会派发。
 */
@Composable
private fun SegmentedButtonRowRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val optionsRaw = node.props["options"] ?: node.props["data"]
    val items = remember(optionsRaw, itemContext) {
        parseRowSelectorItems(optionsRaw, scope, itemContext)
    }
    if (items.isEmpty()) return

    val currentRaw = rememberResolvedString(node.props["value"], scope, itemContext)
    val current = items.firstOrNull { it.value == currentRaw } ?: items.first()
    val showCheckIcon = node.propBool("showCheckIcon", true)
    val maxLines = node.propInt("maxLines", 1)
    val overflow = parseOverflow(node.propString("overflow"))

    val onSelect: (RowSelectorItem) -> Unit = { item ->
        val action = node.actions["onValueChange"]
        if (action != null) {
            ctx.dispatch(action.copy(params = action.params + ("value" to item.value)), scope, itemContext)
        } else {
            resolveBindingPath(node, "value")?.let { scope.setByPath(it, item.value) }
        }
        node.id?.let { scope.setByPath(DynamicUiInternalState.validationErrorKey(it), null) }
    }

    SingleChoiceGlassSegmentedButtonRow(
        options = items,
        selectedOption = current,
        onOptionSelected = onSelect,
        modifier = ctx.modifierPipeline.build(node.modifier),
        label = {
            Text(
                text = it.label,
                maxLines = maxLines,
                overflow = overflow,
            )
        },
        selectedIcon = if (showCheckIcon) {
            {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = null,
                )
            }
        } else null,
    )
}

// ─── Icon ─────────────────────────────────────────────────────────────────────

@Composable
private fun IconRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val iconName = node.propString("name").ifEmpty { node.propString("imageVector") }
    val imageVector = remember(iconName) { IconRegistry.resolve(iconName) }
    val clickAction = node.actions["onClick"]
    val iconModifier = ctx.modifierPipeline.build(node.modifier)
    val variant = node.propString("variant").lowercase()
    val enabled = node.propBool("enabled", true)
    if (imageVector != null) {
        val iconContent: @Composable () -> Unit = {
            Icon(
                imageVector = imageVector,
                contentDescription = node.propString("contentDescription"),
                tint = ModifierRegistry.parseColor(node.propString("tint")) ?: Color.Unspecified,
            )
        }
        if (clickAction != null) {
            when (variant) {
                "icon", "standard" -> GlassIconButton(
                    onClick = { ctx.dispatch(clickAction, scope, itemContext) },
                    modifier = iconModifier,
                    enabled = enabled,
                    content = iconContent,
                )
                "filled" -> GlassFilledIconButton(
                    onClick = { ctx.dispatch(clickAction, scope, itemContext) },
                    modifier = iconModifier,
                    enabled = enabled,
                    content = iconContent,
                )
                "outlined" -> GlassOutlinedIconButton(
                    onClick = { ctx.dispatch(clickAction, scope, itemContext) },
                    modifier = iconModifier,
                    enabled = enabled,
                    content = iconContent,
                )
                else -> GlassTonalIconButton(
                    onClick = { ctx.dispatch(clickAction, scope, itemContext) },
                    modifier = iconModifier,
                    enabled = enabled,
                    content = iconContent,
                )
            }
        } else {
            Icon(
                imageVector = imageVector,
                contentDescription = node.propString("contentDescription"),
                modifier = iconModifier,
                tint = ModifierRegistry.parseColor(node.propString("tint")) ?: Color.Unspecified,
            )
        }
    } else if (iconName.isNotBlank()) {
        val avatarContent: @Composable () -> Unit = {
            IconAvatar(
                iconName = iconName,
                size = 20.dp,
                iconSizeRatio = 0.6f,
            )
        }
        if (clickAction != null) {
            when (variant) {
                "icon", "standard" -> GlassIconButton(
                    onClick = { ctx.dispatch(clickAction, scope, itemContext) },
                    modifier = iconModifier,
                    enabled = enabled,
                    content = avatarContent,
                )
                "filled" -> GlassFilledIconButton(
                    onClick = { ctx.dispatch(clickAction, scope, itemContext) },
                    modifier = iconModifier,
                    enabled = enabled,
                    content = avatarContent,
                )
                "outlined" -> GlassOutlinedIconButton(
                    onClick = { ctx.dispatch(clickAction, scope, itemContext) },
                    modifier = iconModifier,
                    enabled = enabled,
                    content = avatarContent,
                )
                else -> GlassTonalIconButton(
                    onClick = { ctx.dispatch(clickAction, scope, itemContext) },
                    modifier = iconModifier,
                    enabled = enabled,
                    content = avatarContent,
                )
            }
        } else {
            IconAvatar(
                modifier = iconModifier,
                iconName = iconName,
                size = 40.dp,
                iconSizeRatio = 0.6f,
            )
        }
    }
}

// ─── Chip ────────────────────────────────────────────────────────────────────

@Composable
private fun ChipRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val selected = rememberResolvedBool(node.props["selected"], scope, itemContext)
    GlassFilterChip(
        selected = selected,
        onClick = { node.actions["onClick"]?.let { ctx.dispatch(it, scope, itemContext) } },
        label = { Text(rememberResolvedString(node.props["text"] ?: node.props["label"], scope, itemContext)) },
        modifier = ctx.modifierPipeline.build(node.modifier),
    )
}

@Composable
private fun AssistChipRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val leadingIcon = node.propNode("leadingIcon")
    val label = rememberResolvedString(node.props["text"] ?: node.props["label"], scope, itemContext)
    GlassAssistChip(
        onClick = { node.actions["onClick"]?.let { ctx.dispatch(it, scope, itemContext) } },
        label = { Text(label) },
        leadingIcon = leadingIcon?.let { iconNode ->
            { UiNodeRenderer.Render(iconNode, scope, ctx, itemContext) }
        },
        modifier = ctx.modifierPipeline.build(node.modifier),
    )
}

@Composable
private fun SuggestionChipRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val icon = node.propNode("leadingIcon") ?: node.propNode("icon")
    val label = rememberResolvedString(node.props["text"] ?: node.props["label"], scope, itemContext)
    GlassSuggestionChip(
        onClick = { node.actions["onClick"]?.let { ctx.dispatch(it, scope, itemContext) } },
        label = { Text(label) },
        icon = icon?.let { iconNode ->
            { UiNodeRenderer.Render(iconNode, scope, ctx, itemContext) }
        },
        modifier = ctx.modifierPipeline.build(node.modifier),
    )
}

// ─── Badge / BadgedBox ────────────────────────────────────────────────────────

@Composable
private fun BadgeRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val text = rememberResolvedString(node.props["text"], scope, itemContext)
    GlassBadge(
        modifier = ctx.modifierPipeline.build(node.modifier),
        containerColor = ModifierRegistry.parseColor(node.propString("containerColor"))
            ?: MaterialTheme.colorScheme.error,
    ) {
        if (text.isNotEmpty()) Text(text)
    }
}

@Composable
private fun BadgedBoxRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val badgeNode = node.propNode("badge")
    val contentNode = node.children.firstOrNull()
        ?: node.propNode("content")
    if (contentNode == null) return
    GlassBadgedBox(
        modifier = ctx.modifierPipeline.build(node.modifier),
        badge = {
            badgeNode?.let { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
        },
    ) {
        UiNodeRenderer.Render(contentNode, scope, ctx, itemContext)
    }
}

// ─── RadioButton / RadioGroup ─────────────────────────────────────────────────

@Composable
private fun RadioButtonRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val selected = rememberResolvedBool(node.props["selected"], scope, itemContext)
    val enabled = node.propBool("enabled", true)
    GlassRadioButton(
        selected = selected,
        onClick = { node.actions["onClick"]?.let { ctx.dispatch(it, scope, itemContext) } },
        modifier = ctx.modifierPipeline.build(node.modifier),
        enabled = enabled,
    )
}

/**
 * 渲染 [androidx.compose.foundation.layout.Column] 包裹的互斥单选组。
 *
 * JSON 用法（两种等价）：
 * ```json
 * {
 *   "type": "RadioGroup",
 *   "props": {
 *     "value": "${state.form.gender}",
 *     "binding": "state.form.gender",
 *     "options": [
 *       { "label": "男", "value": "male" },
 *       { "label": "女", "value": "female" }
 *     ]
 *   }
 * }
 * ```
 *
 * 选中变更后:
 * - 通过 `actions.onValueChange` 派发
 * - 否则写回 `binding` 指定的 state 路径
 */
@Composable
private fun RadioGroupRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val optionsRaw = node.props["options"] ?: node.props["data"]
    val items = remember(optionsRaw, itemContext) {
        parseRowSelectorItems(optionsRaw, scope, itemContext)
    }
    if (items.isEmpty()) return

    val currentRaw = rememberResolvedString(node.props["value"], scope, itemContext)
    val enabled = node.propBool("enabled", true)

    androidx.compose.foundation.layout.Column(
        modifier = ctx.modifierPipeline.build(node.modifier),
    ) {
        items.forEach { item ->
            val isSelected = item.value == currentRaw
            val onPick = {
                val action = node.actions["onValueChange"]
                if (action != null) {
                    ctx.dispatch(action.copy(params = action.params + ("value" to item.value)), scope, itemContext)
                } else {
                    resolveBindingPath(node, "value")?.let { scope.setByPath(it, item.value) }
                }
            }
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (node.propBool("clickableRow", true)) {
                            Modifier.clickable(enabled = enabled) { onPick() }
                        } else Modifier
                    )
                    .then(
                        PaddingValues(vertical = 4.dp).let { _ ->
                            Modifier.padding(vertical = 4.dp)
                        }
                    ),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                GlassRadioButton(
                    selected = isSelected,
                    onClick = { onPick() },
                    enabled = enabled,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = item.label)
            }
        }
    }
}

// ─── Stepper ──────────────────────────────────────────────────────────────────

@Composable
private fun StepperRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val rawValue = rememberResolvedString(node.props["value"], scope, itemContext)
    val value = (rawValue.toIntOrNull() ?: node.propInt("value"))
    val min = node.propInt("min", 0)
    val max = node.propInt("max", 100)
    val step = node.propInt("step", 1).coerceAtLeast(1)
    val enabled = node.propBool("enabled", true)

    GlassStepper(
        value = value,
        onValueChange = { newVal ->
            val action = node.actions["onValueChange"]
            if (action != null) {
                ctx.dispatch(action.copy(params = action.params + ("value" to newVal.toString())), scope, itemContext)
            } else {
                resolveBindingPath(node, "value")?.let { scope.setByPath(it, newVal) }
            }
        },
        valueRange = min..max,
        step = step,
        enabled = enabled,
        modifier = ctx.modifierPipeline.build(node.modifier),
    )
}

// ─── BottomSheet / ModalBottomSheet ───────────────────────────────────────────

@Composable
private fun BottomSheetRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val visibleRaw = rememberResolvedBool(node.props["visible"] ?: true, scope, itemContext)
    val skipPartiallyExpanded = node.propBool("skipPartiallyExpanded", false)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
    )
    val onDismiss: () -> Unit = {
        node.actions["onDismissRequest"]?.let { ctx.dispatch(it, scope, itemContext) }
    }
    if (visibleRaw) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = ctx.modifierPipeline.build(node.modifier),
        ) {
            node.children.forEach { child ->
                UiNodeRenderer.Render(child, scope, ctx, itemContext)
            }
        }
    }
}

// ─── Form ─────────────────────────────────────────────────────────────────────

/**
 * 表单容器 —— 统一管理 `required` 字段校验。
 *
 * 工作机制:
 * 1. 渲染时递归收集子树中所有 `props.required = true` 节点的 id
 * 2. 持续观察这些字段的值变化,任意一个为空就把 `state.__validation.<id>` 设为 `"必填"`,
 *    否则清空。`__validation.<id>` 已经被 OutlinedTextField / Slider / Switch 等 renderer 读取
 *    并自动显示为 `isError` 红框
 * 3. 实时把 `state.__forms.<formId>.valid` 写为 `true` / `false`,
 *    宿主 / 提交按钮可以读这个状态判断是否可以提交
 *
 * 这样调用方只需在提交按钮中读 `${state.__forms.myForm.valid}`,
 * 不用手动维护每个字段的错误状态。
 */
@Composable
private fun FormRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val requiredIds = remember(node.id, node) { collectRequiredIds(node) }
    val formId = node.id ?: "__anonymousForm"
    val validPath = "__forms.$formId.valid"
    val errorsPath = "__forms.$formId.errors"

    // Snapshot each required id's value so changes trigger a recomposition
    val requiredValues = requiredIds.map { id -> id to scope.getByPath(id) }
    val requiredValuesMap = remember(requiredIds) { mutableStateMapOf<String, Any?>() }
    LaunchedEffect(requiredValues) {
        requiredValues.forEach { (id, v) -> requiredValuesMap[id] = v }
    }

    val errors by remember(requiredIds) {
        derivedStateOf {
            val map = mutableMapOf<String, String>()
            requiredValuesMap.forEach { (id, v) ->
                val isEmpty = v == null ||
                    (v is String && v.isBlank()) ||
                    (v is Collection<*> && v.isEmpty())
                if (isEmpty) map[id] = "必填"
            }
            map
        }
    }

    LaunchedEffect(errors) {
        requiredIds.forEach { id ->
            scope.setByPath(DynamicUiInternalState.validationErrorKey(id), errors[id])
        }
        scope.setByPath(validPath, errors.isEmpty())
        scope.setByPath(errorsPath, errors)
    }

    Column(modifier = ctx.modifierPipeline.build(node.modifier)) {
        node.children.forEach { child ->
            UiNodeRenderer.Render(child, scope, ctx, itemContext)
        }
    }
}

/** 递归收集子树中所有 `required: true` 节点的 id。 */
private fun collectRequiredIds(node: UiNode): List<String> {
    val result = mutableListOf<String>()
    fun visit(n: UiNode) {
        if (n.id != null) {
            val required = n.props["required"]
            if (required == true || (required as? String)?.toBooleanStrictOrNull() == true) {
                result.add(n.id)
            }
        }
        n.children.forEach(::visit)
    }
    visit(node)
    return result
}

// ─── Progress indicators ──────────────────────────────────────────────────────

@Composable
private fun CircularProgressRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val progressProp = node.props["progress"]
    if (progressProp != null) {
        val progress = rememberResolvedString(progressProp, scope, itemContext).toFloatOrNull()
            ?: node.propFloat("progress")
        GlassCircularProgressIndicator(
            progress = { progress },
            modifier = ctx.modifierPipeline.build(node.modifier),
        )
    } else {
        GlassCircularProgressIndicator(modifier = ctx.modifierPipeline.build(node.modifier))
    }
}

@Composable
private fun LinearProgressRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val progressProp = node.props["progress"]
    if (progressProp != null) {
        val progress = rememberResolvedString(progressProp, scope, itemContext).toFloatOrNull()
            ?: node.propFloat("progress")
        GlassLinearProgressIndicator(
            progress = { progress },
            modifier = ctx.modifierPipeline.build(node.modifier),
        )
    } else {
        GlassLinearProgressIndicator(modifier = ctx.modifierPipeline.build(node.modifier))
    }
}

// ─── Scaffold ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScaffoldRenderer(
    node: UiNode,
    scope: UiStateScope,
    ctx: RenderContext,
    itemContext: Map<String, Any?> = emptyMap(),
) {
    val topBarNode = node.propNode("topBar")
    val bottomBarNode = node.propNode("bottomBar")
    val fabNode = node.propNode("floatingActionButton")
    val bodyNode = node.propNode("body") ?: node.propNode("content")

    Scaffold(
        modifier = ctx.modifierPipeline.build(node.modifier),
        topBar = {
            topBarNode?.let { UiNodeRenderer.Render(it, scope, ctx, itemContext) } ?: run {
                val title = node.propString("title")
                if (title.isNotEmpty()) GlassTopAppBar(title = { Text(title) })
            }
        },
        bottomBar = { bottomBarNode?.let { UiNodeRenderer.Render(it, scope, ctx, itemContext) } },
        floatingActionButton = { fabNode?.let { UiNodeRenderer.Render(it, scope, ctx, itemContext) } },
    ) { paddingValues ->
        CompositionLocalProvider(LocalScaffoldPadding provides paddingValues) {
            Box(modifier = Modifier.fillMaxSize().padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding()
            )) {
                bodyNode?.let {
                    UiNodeRenderer.Render(it, scope, ctx, itemContext)
                }
                node.children.forEach { child ->
                    UiNodeRenderer.Render(child, scope, ctx, itemContext)
                }
            }
        }
    }
}

// ─── AlertDialog ─────────────────────────────────────────────────────────────

@Composable
private fun AlertDialogRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap(),) {
    val visible = rememberResolvedBool(
        node.props["visible"] ?: "\${state.${DynamicUiInternalState.DIALOG_VISIBLE}}",
        scope,
        itemContext,
    )
    val title = rememberResolvedString(
        node.props["title"] ?: "\${state.${DynamicUiInternalState.DIALOG_TITLE}}",
        scope,
        itemContext,
    )
    val message = rememberResolvedString(
        node.props["message"] ?: "\${state.${DynamicUiInternalState.DIALOG_MESSAGE}}",
        scope,
        itemContext,
    )
    val confirm = rememberResolvedString(
        node.props["confirm"] ?: "\${state.${DynamicUiInternalState.DIALOG_CONFIRM}}",
        scope,
        itemContext,
    )
    val dismiss = rememberResolvedString(
        node.props["dismiss"] ?: "\${state.${DynamicUiInternalState.DIALOG_DISMISS}}",
        scope,
        itemContext,
    )

    val onConfirmAction = node.actions["onConfirm"] ?: run {
        val onConfirmType = scope.getByPath(DynamicUiInternalState.DIALOG_ON_CONFIRM_TYPE) as? String
            ?: scope.getByPath(DynamicUiInternalState.LEGACY_DIALOG_ON_CONFIRM_TYPE) as? String
        if (onConfirmType != null) ActionSpec(type = onConfirmType) else null
    }

    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = {
            scope.setByPath(DynamicUiInternalState.DIALOG_VISIBLE, false)
            scope.setByPath(DynamicUiInternalState.LEGACY_DIALOG_VISIBLE, false)
        },
        title = if (title.isNotEmpty()) { { Text(title) } } else null,
        text = if (message.isNotEmpty()) { { Text(message) } } else null,
        confirmButton = {
            GlassTextButton(onClick = {
                scope.setByPath(DynamicUiInternalState.DIALOG_VISIBLE, false)
                scope.setByPath(DynamicUiInternalState.LEGACY_DIALOG_VISIBLE, false)
                onConfirmAction?.let { ctx.dispatch(it, scope, itemContext) }
            }) { Text(confirm.ifEmpty { "OK" }) }
        },
        dismissButton = if (dismiss.isNotEmpty()) {
            {
                GlassTextButton(onClick = {
                    scope.setByPath(DynamicUiInternalState.DIALOG_VISIBLE, false)
                    scope.setByPath(DynamicUiInternalState.LEGACY_DIALOG_VISIBLE, false)
                }) {
                    Text(dismiss)
                }
            }
        } else null,
    )
}

// ─── TopAppBar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val titleNode = node.propNode("title")
    val navigationIconNode = node.propNode("navigationIcon")
    val actionsContentNodes = node.propNodeList("actionsContent")
    val titleText = rememberResolvedString(
        node.props["title"] ?: titleNode?.props?.get("text"),
        scope,
        itemContext,
    )
    GlassTopAppBar(
        title = {
            titleNode?.let {
                UiNodeRenderer.Render(it, scope, ctx, itemContext)
            } ?: Text(titleText)
        },
        navigationIcon = {
            navigationIconNode?.let { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
        },
        actions = {
            actionsContentNodes.forEach { actionNode ->
                UiNodeRenderer.Render(actionNode, scope, ctx, itemContext)
            }
        },
        modifier = ctx.modifierPipeline.build(node.modifier),
    )
}

// ─── RowSelector ─────────────────────────────────────────────────────────────

private data class RowSelectorItem(
    val value: String,
    val label: String,
    val kind: String = "text",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RowSelectorRenderer(
    node: UiNode,
    scope: UiStateScope,
    ctx: RenderContext,
    itemContext: Map<String, Any?> = emptyMap(),
) {
    val items by remember(node.props["data"], node.props["options"], itemContext) {
        derivedStateOf {
            parseRowSelectorItems(
                raw = node.props["data"] ?: node.props["options"],
                scope = scope,
                itemContext = itemContext,
            )
        }
    }
    if (items.isEmpty()) return

    val maxSelected = node.propInt("maxSelected", 0).coerceAtLeast(0)
    val selectIndex = node.propInt("selectIndex", -1)
    val maxLines = node.propInt("maxLines", Int.MAX_VALUE)
    val maxItemsInEachRow = node.propInt("maxItemsInEachRow", Int.MAX_VALUE)
    val selectedMap = remember(node.id) { mutableStateMapOf<Int, String>() }
    val showCustomInput = remember(node.id) { mutableStateOf(false) }

    val textColor = ModifierRegistry.parseColor(node.propString("textColor")) ?: MaterialTheme.colorScheme.onSurface
    val selectedTextColor = ModifierRegistry.parseColor(node.propString("selectedTextColor")) ?: MaterialTheme.colorScheme.onPrimaryContainer
    val containerColor = ModifierRegistry.parseColor(node.propString("containerColor")) ?: MaterialTheme.colorScheme.surfaceContainer
    val selectedContainerColor = ModifierRegistry.parseColor(node.propString("selectedContainerColor")) ?: MaterialTheme.colorScheme.primaryContainer
    val iconColor = ModifierRegistry.parseColor(node.propString("iconColor")) ?: MaterialTheme.colorScheme.primary
    val textStyle = parseTextStyle(node.propString("textStyle"))

    val updateSelection: (Int, RowSelectorItem) -> Unit = { index, item ->
        if (selectedMap.containsKey(index)) {
            selectedMap.remove(index)
        } else {
            if (maxSelected == 1) {
                selectedMap.clear()
            } else if (maxSelected > 1 && selectedMap.size >= maxSelected) {
                selectedMap.keys.firstOrNull()?.let(selectedMap::remove)
            }
            selectedMap[index] = if (item.kind == "custom") "" else item.value
        }

        val selectedValues = selectedMap.values.filter { it.isNotBlank() }.joinToString("、")
        val binding = resolveBindingPath(node, "binding")
        when {
            !binding.isNullOrBlank() -> scope.setByPath(binding, selectedValues)
            !node.id.isNullOrBlank() -> scope.setByPath(node.id, selectedValues)
        }
        node.id?.let { scope.setByPath(DynamicUiInternalState.validationErrorKey(it), null) }
        node.actions["onSelectionChange"]?.let { action ->
            ctx.dispatch(
                action.copy(
                    params = action.params + mapOf(
                        "value" to selectedValues,
                        "index" to index.toString(),
                    ),
                ),
                scope,
                itemContext,
            )
        }
    }

    LaunchedEffect(selectIndex, items) {
        if (selectIndex in items.indices && selectedMap.isEmpty()) {
            updateSelection(selectIndex, items[selectIndex])
        }
    }

    val rowValidationError = node.id?.let { scope.getByPath(DynamicUiInternalState.validationErrorKey(it))?.toString() }
    val effectiveSelectedContainerColor = if (rowValidationError != null) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        selectedContainerColor
    }

    FlowRow(
        modifier = ctx.modifierPipeline.build(node.modifier),
        maxLines = maxLines,
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedMap.containsKey(index)
            GlassFilterChip(
                selected = isSelected,
                onClick = {
                    updateSelection(index, item)
                    showCustomInput.value = item.kind == "custom" && selectedMap.containsKey(index)
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected) selectedTextColor else textColor,
                        style = textStyle,
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                } else null,
                glassContainerColor = containerColor,
                glassSelectedContainerColor = effectiveSelectedContainerColor,
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
            )
        }
    }

    if (showCustomInput.value) {
        node.children.forEach { child ->
            UiNodeRenderer.Render(child, scope, ctx, itemContext)
        }
    }
}

@Composable
private fun ColumnSelectorRenderer(
    node: UiNode,
    scope: UiStateScope,
    ctx: RenderContext,
    itemContext: Map<String, Any?> = emptyMap(),
) {
    val items by remember(node.props["data"], node.props["options"], itemContext) {
        derivedStateOf {
            parseRowSelectorItems(
                raw = node.props["data"] ?: node.props["options"],
                scope = scope,
                itemContext = itemContext,
            )
        }
    }
    if (items.isEmpty()) return

    val maxSelected = node.propInt("maxSelected", 1).coerceAtLeast(0)
    val selectIndex = node.propInt("selectIndex", -1)
    val selectedMap = remember(node.id) { mutableStateMapOf<Int, String>() }
    val showCustomInput = remember(node.id) { mutableStateOf(false) }

    val textColor = ModifierRegistry.parseColor(node.propString("textColor")) ?: MaterialTheme.colorScheme.onSurface
    val selectedTextColor = ModifierRegistry.parseColor(node.propString("selectedTextColor")) ?: MaterialTheme.colorScheme.onPrimaryContainer
    val containerColor = ModifierRegistry.parseColor(node.propString("containerColor")) ?: MaterialTheme.colorScheme.surfaceContainer
    val selectedContainerColor = ModifierRegistry.parseColor(node.propString("selectedContainerColor")) ?: MaterialTheme.colorScheme.primaryContainer
    val iconColor = ModifierRegistry.parseColor(node.propString("iconColor")) ?: MaterialTheme.colorScheme.primary
    val textStyle = parseTextStyle(node.propString("textStyle"))

    val updateSelection: (Int, RowSelectorItem) -> Unit = { index, item ->
        if (selectedMap.containsKey(index)) {
            selectedMap.remove(index)
        } else {
            if (maxSelected == 1) {
                selectedMap.clear()
            } else if (maxSelected > 1 && selectedMap.size >= maxSelected) {
                selectedMap.keys.firstOrNull()?.let(selectedMap::remove)
            }
            selectedMap[index] = if (item.kind == "custom") "" else item.value
        }

        val selectedValues = selectedMap.values.filter { it.isNotBlank() }.joinToString("、")
        val binding = resolveBindingPath(node, "binding")
        when {
            !binding.isNullOrBlank() -> scope.setByPath(binding, selectedValues)
            !node.id.isNullOrBlank() -> scope.setByPath(node.id, selectedValues)
        }
        node.id?.let { scope.setByPath(DynamicUiInternalState.validationErrorKey(it), null) }
        node.actions["onSelectionChange"]?.let { action ->
            ctx.dispatch(
                action.copy(
                    params = action.params + mapOf(
                        "value" to selectedValues,
                        "index" to index.toString(),
                    ),
                ),
                scope,
                itemContext,
            )
        }
    }

    LaunchedEffect(selectIndex, items) {
        if (selectIndex in items.indices && selectedMap.isEmpty()) {
            updateSelection(selectIndex, items[selectIndex])
        }
    }

    val colValidationError = node.id?.let { scope.getByPath(DynamicUiInternalState.validationErrorKey(it))?.toString() }
    val effectiveSelectedContainerColor = if (colValidationError != null) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        selectedContainerColor
    }

    Column(modifier = ctx.modifierPipeline.build(node.modifier)) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedMap.containsKey(index)
            GlassFilterChip(
                selected = isSelected,
                onClick = {
                    updateSelection(index, item)
                    showCustomInput.value = item.kind == "custom" && selectedMap.containsKey(index)
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected) selectedTextColor else textColor,
                        style = textStyle,
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                } else null,
                glassContainerColor = containerColor,
                glassSelectedContainerColor = effectiveSelectedContainerColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
        }
    }

    if (showCustomInput.value) {
        node.children.forEach { child ->
            UiNodeRenderer.Render(child, scope, ctx, itemContext)
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun parseTextStyle(style: String?) = when (style) {
    "displayLarge" -> MaterialTheme.typography.displayLarge
    "displayMedium" -> MaterialTheme.typography.displayMedium
    "displaySmall" -> MaterialTheme.typography.displaySmall
    "headlineLarge" -> MaterialTheme.typography.headlineLarge
    "headlineMedium" -> MaterialTheme.typography.headlineMedium
    "headlineSmall" -> MaterialTheme.typography.headlineSmall
    "titleLarge" -> MaterialTheme.typography.titleLarge
    "titleMedium" -> MaterialTheme.typography.titleMedium
    "titleSmall" -> MaterialTheme.typography.titleSmall
    "bodyLarge" -> MaterialTheme.typography.bodyLarge
    "bodyMedium" -> MaterialTheme.typography.bodyMedium
    "bodySmall" -> MaterialTheme.typography.bodySmall
    "labelLarge" -> MaterialTheme.typography.labelLarge
    "labelMedium" -> MaterialTheme.typography.labelMedium
    "labelSmall" -> MaterialTheme.typography.labelSmall
    else -> LocalTextStyle.current
}

private fun parseFontWeight(raw: String?): FontWeight? = when (raw) {
    "Bold", "bold" -> FontWeight.Bold
    "SemiBold", "semiBold" -> FontWeight.SemiBold
    "Medium", "medium" -> FontWeight.Medium
    "Light", "light" -> FontWeight.Light
    "Thin", "thin" -> FontWeight.Thin
    "ExtraBold" -> FontWeight.ExtraBold
    "Black" -> FontWeight.Black
    null -> null
    else -> raw.toIntOrNull()?.let { FontWeight(it) }
}

private fun parseTextAlign(raw: String?): TextAlign? = when (raw) {
    "Center" -> TextAlign.Center
    "End", "Right" -> TextAlign.End
    "Start", "Left" -> TextAlign.Start
    "Justify" -> TextAlign.Justify
    else -> null
}

private fun parseOverflow(raw: String?): TextOverflow = when (raw) {
    "Ellipsis" -> TextOverflow.Ellipsis
    "Clip" -> TextOverflow.Clip
    "Visible" -> TextOverflow.Visible
    else -> TextOverflow.Clip
}

private fun parseKeyboardType(raw: String?): KeyboardType = when (raw) {
    "Number" -> KeyboardType.Number
    "Email" -> KeyboardType.Email
    "Phone" -> KeyboardType.Phone
    "Password" -> KeyboardType.Password
    "Uri" -> KeyboardType.Uri
    "Decimal" -> KeyboardType.Decimal
    else -> KeyboardType.Text
}

private fun resolveBindingPath(node: UiNode, fallbackProp: String): String? {
    val binding = node.propString("binding").ifEmpty { node.propString(fallbackProp) }
    return when {
        binding.startsWith("\${state.") && binding.endsWith("}") ->
            binding.removePrefix("\${state.").removeSuffix("}")
        binding.startsWith("state.") -> binding.removePrefix("state.")
        else -> null
    }
}

@Composable
private fun composableTextSlot(
    node: UiNode?,
    text: String,
    scope: UiStateScope,
    ctx: RenderContext,
    itemContext: Map<String, Any?>,
): (@Composable () -> Unit)? = when {
    node != null -> {
        {
            UiNodeRenderer.Render(node, scope, ctx, itemContext)
        }
    }
    text.isNotEmpty() -> {
        {
            Text(text)
        }
    }
    else -> null
}

private fun parseRowSelectorItems(
    raw: Any?,
    scope: UiStateScope,
    itemContext: Map<String, Any?>,
): List<RowSelectorItem> {
    val source = when (raw) {
        is String -> ValueExprResolver.resolve(raw, scope, itemContext)
        else -> raw
    }
    val list = source as? List<*> ?: return emptyList()
    return list.mapNotNull { item ->
        when (item) {
            is String -> RowSelectorItem(value = item, label = item)
            is Number, is Boolean -> {
                val value = item.toString()
                RowSelectorItem(value = value, label = value)
            }
            is Map<*, *> -> {
                val value = item["value"]?.toString()
                    ?: item["label"]?.toString()
                    ?: return@mapNotNull null
                val label = item["label"]?.toString() ?: value
                val kind = item["kind"]?.toString() ?: "text"
                RowSelectorItem(value = value, label = label, kind = kind)
            }
            else -> null
        }
    }
}
