package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.theme.blend
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSearch

// ──────────────────────────────────────────────────────────────
//  GlassOutlinedTextField — 毛玻璃风格 OutlinedTextField
// ──────────────────────────────────────────────────────────────

@Immutable
enum class GlassTextFieldVisualPreset(
    val fillMultiplier: Float,
    val tintMultiplier: Float,
    val sheenMultiplier: Float,
    val depthMultiplier: Float,
    val strokeMultiplier: Float,
    val accentStrokeMultiplier: Float,
    val innerStrokeMultiplier: Float,
) {
    Quiet(
        fillMultiplier = 0.93f,
        tintMultiplier = 0.78f,
        sheenMultiplier = 0.84f,
        depthMultiplier = 0.88f,
        strokeMultiplier = 0.88f,
        accentStrokeMultiplier = 0.76f,
        innerStrokeMultiplier = 0.86f,
    ),
    Balanced(
        fillMultiplier = 1f,
        tintMultiplier = 1f,
        sheenMultiplier = 0.9f,
        depthMultiplier = 1f,
        strokeMultiplier = 1f,
        accentStrokeMultiplier = 0.96f,
        innerStrokeMultiplier = 1f,
    ),
    Expressive(
        fillMultiplier = 1.04f,
        tintMultiplier = 1.18f,
        sheenMultiplier = 1.16f,
        depthMultiplier = 1.12f,
        strokeMultiplier = 1.12f,
        accentStrokeMultiplier = 1.22f,
        innerStrokeMultiplier = 1.08f,
    ),
}

/**
 * 毛玻璃风格输入框 —— 参数签名与 [OutlinedTextField] 完全一致，
 * 额外增加 [style]、[visualPreset]、[glassColor]、[glassBorderWidth]、[focusedAlphaBoost] 五个玻璃参数。
 *
 * ### 渲染策略
 *
 * 输入框是高频重组、高频输入组件，因此这里不复用通用的 Liquid Glass 路径，
 * 而是使用**专门的轻量级现代玻璃渲染**：
 *
 * | 全局开关状态               | 渲染方式                                                        |
 * |--------------------------|-----------------------------------------------------------------|
 * | `isGlassmorphismEnabled` | 单次缓存绘制：半透明底色 + 轻微顶部 sheen + 内侧景深 + 统一描边 |
 * | 关闭                     | 退化为标准 [OutlinedTextField]，使用原始 [colors]               |
 *
 * ### 焦点动画
 *
 * 聚焦输入时，玻璃背景透明度自动提升 [focusedAlphaBoost]（默认 +20%），
 * 对应 Tailwind CSS `focus-within:bg-surface-container-lowest/70`
 * （基础态 alpha 50% → 聚焦态 alpha 70%），300ms 平滑过渡。
 *
 * 当玻璃效果激活时，组件会自动将 [colors] 中的 **容器色** 和 **指示线** 覆盖为透明，
 * 并交由输入框专用现代玻璃容器统一渲染。其余文字、标签、图标等颜色保持不变，
 * 调用方仍可通过 [colors] 自由定制。
 *
 * ### 快速使用
 *
 * ```kotlin
 * // 直接替换 OutlinedTextField，零配置获得玻璃效果
 * GlassOutlinedTextField(
 *     value = text,
 *     onValueChange = { text = it },
 *     placeholder = { Text("搜索...") },
 *     leadingIcon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSearch, null) },
 * )
 * ```
 *
 * ### 自定义色调
 *
 * ```kotlin
 * GlassOutlinedTextField(
 *     value = text,
 *     onValueChange = { text = it },
 *     style = GlassStyle.Regular,
 *     glassColor = MaterialTheme.colorScheme.primaryContainer,
 *     label = { Text("用户名") },
 * )
 * ```
 *
 * ### 搭配自定义 TextFieldColors
 *
 * ```kotlin
 * GlassOutlinedTextField(
 *     value = text,
 *     onValueChange = { text = it },
 *     colors = OutlinedTextFieldDefaults.colors(
 *         focusedTextColor = Color.White,
 *         cursorColor = Color.White,
 *         focusedLabelColor = Color.White.copy(alpha = 0.7f),
 *     ),
 *     // 容器色和指示线会自动被覆盖为透明，
 *     // 上面设置的 textColor/cursorColor/labelColor 正常生效
 * )
 * ```
 *
 * @param value              输入文本
 * @param onValueChange      文本变更回调
 * @param modifier           外部修饰符
 * @param enabled            是否可用
 * @param readOnly           是否只读
 * @param textStyle          文本样式
 * @param label              标签
 * @param placeholder        占位文本
 * @param leadingIcon        前置图标
 * @param trailingIcon       后置图标
 * @param prefix             前缀
 * @param suffix             后缀
 * @param supportingText     辅助文本
 * @param isError            是否处于错误状态
 * @param visualTransformation 视觉转换（如密码掩码）
 * @param keyboardOptions    键盘选项
 * @param keyboardActions    键盘动作
 * @param singleLine         是否单行
 * @param maxLines           最大行数
 * @param minLines           最小行数
 * @param interactionSource  交互源
 * @param shape              输入框形状，默认跟随 [OutlinedTextFieldDefaults.shape]
 * @param colors             文本框颜色；玻璃模式下容器色和指示线自动覆盖为透明
 * @param style              毛玻璃浓度等级，见 [GlassStyle]
 * @param visualPreset       输入框专用视觉档位：安静 / 平衡 / 更显眼
 * @param glassColor         玻璃叠加色调（传入不透明原色，alpha 内部控制）；
 *                           传入 [Color.Unspecified] 时使用默认毛玻璃底色
 * @param glassBorderWidth   玻璃描边宽度，默认 0.5 dp
 * @param focusedAlphaBoost  聚焦时背景透明度增量，默认 0.20f
 */
@Composable
fun GlassOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = MaterialTheme.shapes.large,
    colors: TextFieldColors = AppTheme.colors.getOutlinedTextFieldColors(),
    style: GlassStyle = GlassStyle.Medium,
    visualPreset: GlassTextFieldVisualPreset = GlassTextFieldVisualPreset.Balanced,
    glassColor: Color = Color.Unspecified,
    glassBorderWidth: Dp = 0.5.dp,
    focusedAlphaBoost: Float = 0.20f,
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
) {
    val settingsState = LocalSettingsState.current
    val isGlassActive = settingsState.isGlassAlphaEnabled

    if (!isGlassActive) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            shape = shape,
            colors = colors,
        )
        return
    }

    // ── 创建/复用交互源，收集焦点状态 ──
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // ── 焦点动画：聚焦时提升底色透明度（50% → 70%）；readOnly 时不提升 ──
    val animatedBgAlpha by animateFloatAsState(
        targetValue = if (isFocused && isGlassActive && !readOnly) {
            (style.backgroundAlpha + focusedAlphaBoost).coerceAtMost(1f)
        } else {
            style.backgroundAlpha
        },
        animationSpec = tween(durationMillis = 300),
        label = "GlassTextFieldFocusAlpha",
    )

    val containerColor = glassColor.takeUnless { it == Color.Unspecified }
        ?: colors.containerColor(enabled, isError, isFocused)
    val cursorColor = if (isError) colors.errorIndicatorColor else colors.focusedIndicatorColor
    val resolvedColors = resolveGlassDecorationColors(colors = colors, isGlassActive = isGlassActive)

    GlassOutlinedTextFieldImpl(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = resolvedColors,
        style = style,
        visualPreset = visualPreset,
        animatedBgAlpha = animatedBgAlpha,
        glassColor = containerColor,
        glassBorderWidth = glassBorderWidth,
        cursorColor = cursorColor,
        isFocused = isFocused,
        contentPadding = contentPadding,
    )
}

// ──────────────────────────────────────────────────────────────
//  TextFieldValue 重载
// ──────────────────────────────────────────────────────────────

/**
 * [GlassOutlinedTextField] 的 [TextFieldValue][androidx.compose.ui.text.input.TextFieldValue] 重载版本。
 *
 * 参数和行为与 [String] 版本完全一致，仅 [value] / [onValueChange] 类型不同，
 * 适用于需要精确控制光标位置和选区的场景。
 *
 * @see GlassOutlinedTextField
 */
@Composable
fun GlassOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    style: GlassStyle = GlassStyle.Medium,
    visualPreset: GlassTextFieldVisualPreset = GlassTextFieldVisualPreset.Balanced,
    glassColor: Color = Color.Unspecified,
    glassBorderWidth: Dp = 0.5.dp,
    focusedAlphaBoost: Float = 0.20f,
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
) {
    val settingsState = LocalSettingsState.current
    val isGlassActive = settingsState.isGlassAlphaEnabled

    if (!isGlassActive) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            shape = shape,
            colors = colors,
        )
        return
    }

    // ── 创建/复用交互源，收集焦点状态 ──
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // ── 焦点动画：聚焦时提升底色透明度 ──
    val animatedBgAlpha by animateFloatAsState(
        targetValue = if (isFocused && isGlassActive) {
            (style.backgroundAlpha + focusedAlphaBoost).coerceAtMost(1f)
        } else {
            style.backgroundAlpha
        },
        animationSpec = tween(durationMillis = 300),
        label = "GlassTextFieldFocusAlpha",
    )

    val containerColor = glassColor.takeUnless { it == Color.Unspecified }
        ?: colors.containerColor(enabled, isError, isFocused)
    val cursorColor = if (isError) colors.errorIndicatorColor else colors.focusedIndicatorColor
    val resolvedColors = resolveGlassDecorationColors(colors = colors, isGlassActive = isGlassActive)

    GlassOutlinedTextFieldImpl(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = resolvedColors,
        style = style,
        visualPreset = visualPreset,
        animatedBgAlpha = animatedBgAlpha,
        glassColor = containerColor,
        glassBorderWidth = glassBorderWidth,
        cursorColor = cursorColor,
        isFocused = isFocused,
        contentPadding = contentPadding,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassOutlinedTextFieldImpl(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    readOnly: Boolean,
    textStyle: TextStyle,
    label: @Composable (() -> Unit)?,
    placeholder: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    prefix: @Composable (() -> Unit)?,
    suffix: @Composable (() -> Unit)?,
    supportingText: @Composable (() -> Unit)?,
    isError: Boolean,
    visualTransformation: VisualTransformation,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    singleLine: Boolean,
    maxLines: Int,
    minLines: Int,
    interactionSource: MutableInteractionSource,
    shape: Shape,
    colors: TextFieldColors,
    style: GlassStyle,
    visualPreset: GlassTextFieldVisualPreset,
    animatedBgAlpha: Float,
    glassColor: Color,
    glassBorderWidth: Dp,
    cursorColor: Color,
    isFocused: Boolean,
    contentPadding: PaddingValues,
) {
    val visualEnabled = enabled && !readOnly
    val mergedTextStyle = textStyle.merge(
        TextStyle(
            color = textStyle.color.takeOrElse {
                colors.textColor(enabled = visualEnabled, isError = isError, focused = isFocused)
            }
        )
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.glassDecorationModifier(label = label),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(if (readOnly) Color.Transparent else cursorColor),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = visualEnabled,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                isError = isError,
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                supportingText = supportingText,
                colors = colors,
                contentPadding = contentPadding,
                container = {
                    GlassOutlinedContainer(
                        style = style,
                        visualPreset = visualPreset,
                        animatedBgAlpha = animatedBgAlpha,
                        shape = shape,
                        color = glassColor,
                        borderWidth = glassBorderWidth,
                        enabled = enabled,
                        readOnly = readOnly,
                        isError = isError,
                        isFocused = isFocused,
                    )
                },
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassOutlinedTextFieldImpl(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    readOnly: Boolean,
    textStyle: TextStyle,
    label: @Composable (() -> Unit)?,
    placeholder: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    prefix: @Composable (() -> Unit)?,
    suffix: @Composable (() -> Unit)?,
    supportingText: @Composable (() -> Unit)?,
    isError: Boolean,
    visualTransformation: VisualTransformation,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    singleLine: Boolean,
    maxLines: Int,
    minLines: Int,
    interactionSource: MutableInteractionSource,
    shape: Shape,
    colors: TextFieldColors,
    style: GlassStyle,
    visualPreset: GlassTextFieldVisualPreset,
    animatedBgAlpha: Float,
    glassColor: Color,
    glassBorderWidth: Dp,
    cursorColor: Color,
    isFocused: Boolean,
    contentPadding: PaddingValues,
) {
    val visualEnabled = enabled && !readOnly
    val mergedTextStyle = textStyle.merge(
        TextStyle(
            color = textStyle.color.takeOrElse {
                colors.textColor(enabled = visualEnabled, isError = isError, focused = isFocused)
            }
        )
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.glassDecorationModifier(label = label),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(if (readOnly) Color.Transparent else cursorColor),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value.text,
                innerTextField = innerTextField,
                enabled = visualEnabled,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                isError = isError,
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                supportingText = supportingText,
                colors = colors,
                contentPadding = contentPadding,
                container = {
                    GlassOutlinedContainer(
                        style = style,
                        visualPreset = visualPreset,
                        animatedBgAlpha = animatedBgAlpha,
                        shape = shape,
                        color = glassColor,
                        borderWidth = glassBorderWidth,
                        enabled = enabled,
                        readOnly = readOnly,
                        isError = isError,
                        isFocused = isFocused,
                    )
                },
            )
        },
    )
}

@Composable
private fun Modifier.glassDecorationModifier(
    label: (@Composable (() -> Unit))?,
): Modifier {
    val topPadding = if (label != null) OutlinedTextFieldTopPadding else 0.dp
    return this
        .semantics(mergeDescendants = true) {}
        .defaultMinSize(
            minWidth = TextFieldDefaults.MinWidth,
            minHeight = TextFieldDefaults.MinHeight,
        )
        .let {
            if (topPadding > 0.dp) it.then(Modifier.padding(top = topPadding)) else it
        }
}

@Composable
private fun GlassOutlinedContainer(
    style: GlassStyle,
    visualPreset: GlassTextFieldVisualPreset,
    animatedBgAlpha: Float,
    shape: Shape,
    color: Color,
    borderWidth: Dp,
    enabled: Boolean,
    readOnly: Boolean,
    isError: Boolean,
    isFocused: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .modernGlassTextFieldContainer(
                style = style,
                visualPreset = visualPreset,
                backgroundAlpha = animatedBgAlpha,
                shape = shape,
                color = color,
                borderWidth = borderWidth,
                enabled = enabled,
                readOnly = readOnly,
                isError = isError,
                isFocused = isFocused,
            )
    )
}

private val OutlinedTextFieldTopPadding = 8.dp

@Composable
private fun resolveGlassDecorationColors(
    colors: TextFieldColors,
    isGlassActive: Boolean,
): TextFieldColors {
    if (!isGlassActive) return colors

    val colorScheme = MaterialTheme.colorScheme

    return colors.copy(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        focusedTextColor = colors.focusedTextColor
            .liftToward(colorScheme.onSurface, blendRatio = 0.10f)
            .ensureMinAlpha(0.96f),
        unfocusedTextColor = colors.unfocusedTextColor
            .liftToward(colorScheme.onSurface, blendRatio = 0.16f)
            .ensureMinAlpha(0.92f),
        disabledTextColor = colors.disabledTextColor
            .liftToward(colorScheme.onSurfaceVariant, blendRatio = 0.08f)
            .ensureMinAlpha(0.42f),
        errorTextColor = colors.errorTextColor
            .liftToward(colorScheme.onSurface, blendRatio = 0.08f)
            .ensureMinAlpha(0.96f),
        focusedLabelColor = colors.focusedLabelColor
            .liftToward(colorScheme.primary, blendRatio = 0.08f)
            .ensureMinAlpha(0.84f),
        unfocusedLabelColor = colors.unfocusedLabelColor
            .liftToward(colorScheme.onSurfaceVariant, blendRatio = 0.12f)
            .ensureMinAlpha(0.72f),
        errorLabelColor = colors.errorLabelColor
            .ensureMinAlpha(0.86f),
        focusedPlaceholderColor = colors.focusedPlaceholderColor
            .liftToward(colorScheme.onSurfaceVariant, blendRatio = 0.08f)
            .ensureMinAlpha(0.42f),
        unfocusedPlaceholderColor = colors.unfocusedPlaceholderColor
            .liftToward(colorScheme.onSurfaceVariant, blendRatio = 0.08f)
            .ensureMinAlpha(0.50f),
        disabledPlaceholderColor = colors.disabledPlaceholderColor
            .ensureMinAlpha(0.34f),
        errorPlaceholderColor = colors.errorPlaceholderColor
            .ensureMinAlpha(0.50f),
        focusedLeadingIconColor = colors.focusedLeadingIconColor
            .liftToward(colorScheme.onSurfaceVariant, blendRatio = 0.10f)
            .ensureMinAlpha(0.78f),
        unfocusedLeadingIconColor = colors.unfocusedLeadingIconColor
            .liftToward(colorScheme.onSurfaceVariant, blendRatio = 0.10f)
            .ensureMinAlpha(0.68f),
        disabledLeadingIconColor = colors.disabledLeadingIconColor
            .ensureMinAlpha(0.42f),
        errorLeadingIconColor = colors.errorLeadingIconColor
            .ensureMinAlpha(0.82f),
        focusedTrailingIconColor = colors.focusedTrailingIconColor
            .liftToward(colorScheme.onSurfaceVariant, blendRatio = 0.10f)
            .ensureMinAlpha(0.78f),
        unfocusedTrailingIconColor = colors.unfocusedTrailingIconColor
            .liftToward(colorScheme.onSurfaceVariant, blendRatio = 0.10f)
            .ensureMinAlpha(0.68f),
        disabledTrailingIconColor = colors.disabledTrailingIconColor
            .ensureMinAlpha(0.42f),
        errorTrailingIconColor = colors.errorTrailingIconColor
            .ensureMinAlpha(0.82f),
    )
}

private fun Color.liftToward(target: Color, blendRatio: Float): Color {
    return blend(target, blendRatio)
}

private fun Color.ensureMinAlpha(minAlpha: Float): Color {
    return copy(alpha = alpha.coerceAtLeast(minAlpha))
}

// ──────────────────────────────────────────────────────────────
//  Modern Glass TextField Container
// ──────────────────────────────────────────────────────────────

/**
 * 输入框专用现代玻璃容器。
 *
 * 目标：
 * 1. **边框上下风格一致**：统一使用单色系描边，不再使用上亮下暗的边框渐变；
 * 2. **保留玻璃层次**：把顶部 sheen / 底部景深放到容器内部，而不是放到边框颜色上；
 * 3. **极致性能**：仅使用 `drawWithCache` 缓存 outline / stroke / brush，不走模糊图层。
 */
@Composable
private fun Modifier.modernGlassTextFieldContainer(
    style: GlassStyle,
    visualPreset: GlassTextFieldVisualPreset,
    backgroundAlpha: Float,
    shape: Shape,
    color: Color,
    borderWidth: Dp,
    enabled: Boolean,
    readOnly: Boolean,
    isError: Boolean,
    isFocused: Boolean,
): Modifier {
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.surface.luminance() > 0.5f
    val glassBaseAlpha = LocalSettingsState.current.glassBaseAlpha.coerceIn(0f, 1f)
    val scaledBackgroundAlpha = backgroundAlpha * glassBaseAlpha
    val baseColor = if (color != Color.Unspecified) color else colorScheme.surfaceContainerHighest
    val visualFocused = isFocused && !readOnly
    // 容器渲染：readOnly 时保持可见（不灰化），但去掉聚焦高亮；文字颜色在 GlassOutlinedTextFieldImpl 中单独处理为灰
    val resolvedFillAlpha = remember(scaledBackgroundAlpha, enabled, readOnly, visualFocused, isError) {
        when {
            !enabled -> scaledBackgroundAlpha * 0.48f
            isError -> scaledBackgroundAlpha * 0.74f
            visualFocused -> scaledBackgroundAlpha * 0.84f
            readOnly -> scaledBackgroundAlpha * 0.78f
            else -> scaledBackgroundAlpha * 0.84f
        }
    } * visualPreset.fillMultiplier
    val fillColor = remember(baseColor, resolvedFillAlpha) {
        baseColor.copy(alpha = resolvedFillAlpha)
    }
    val neutralTint = remember(colorScheme, baseColor, isLight) {
        colorScheme.surfaceTint
            .blend(colorScheme.outlineVariant, if (isLight) 0.62f else 0.54f)
            .blend(baseColor, if (isLight) 0.18f else 0.14f)
    }
    val accentTint = remember(colorScheme, neutralTint, enabled, readOnly, isError, visualFocused, isLight) {
        when {
            !enabled -> neutralTint.blend(colorScheme.outlineVariant, 0.48f)
            isError -> colorScheme.error.blend(neutralTint, if (isLight) 0.52f else 0.46f)
            visualFocused -> colorScheme.tertiary.blend(neutralTint, if (isLight) 0.60f else 0.50f)
            readOnly -> colorScheme.primary.blend(neutralTint, if (isLight) 0.52f else 0.44f)
            else -> colorScheme.primary.blend(neutralTint, if (isLight) 0.55f else 0.46f)
        }
    }
    val tintOverlayColor = remember(accentTint, scaledBackgroundAlpha, enabled, readOnly, visualFocused, isError) {
        accentTint.copy(
            alpha = scaledBackgroundAlpha * when {
                !enabled -> 0f
                isError -> 0.16f
                visualFocused -> 0.29f
                readOnly -> 0.20f
                else -> 0.24f
            } * visualPreset.tintMultiplier
        )
    }
    val topSheenColor = remember(baseColor, accentTint, style, glassBaseAlpha, isLight, enabled, readOnly, visualFocused, isError) {
        accentTint.blend(Color.White, if (isLight) 0.82f else 0.66f).copy(
            alpha = (style.surfaceOverlayAlpha * when {
                !enabled -> 0.62f
                isError -> 1.8f
                visualFocused -> 2.22f
                readOnly -> 1.70f
                else -> 1.92f
            } * visualPreset.sheenMultiplier * glassBaseAlpha).coerceIn(0f, 1f)
        )
    }
    val bottomDepthColor = remember(baseColor, accentTint, style, glassBaseAlpha, isLight, enabled, readOnly, visualFocused) {
        baseColor.blend(accentTint.blend(Color.Black, 0.70f), if (isLight) 0.28f else 0.34f).copy(
            alpha = (style.innerShadowAlpha * when {
                !enabled -> 0.50f
                visualFocused -> 1.24f
                readOnly -> 1.00f
                else -> 1.10f
            } * visualPreset.depthMultiplier * glassBaseAlpha).coerceIn(0f, 1f)
        )
    }
    val strokeTint = remember(colorScheme, enabled, readOnly, isError, visualFocused) {
        when {
            !enabled -> colorScheme.outline
            isError -> colorScheme.error
            visualFocused -> colorScheme.tertiary
            readOnly -> colorScheme.primary
            else -> colorScheme.primary
        }
    }
    val strokeColor = remember(strokeTint, style, glassBaseAlpha, enabled, readOnly, isError, visualFocused, isLight) {
        strokeTint.copy(
            alpha = (when {
                !enabled -> 0.10f
                isError -> 0.30f + style.borderAlpha * 0.22f
                visualFocused -> 0.34f + style.borderAlpha * 0.30f
                readOnly -> 0.20f + style.borderAlpha * 0.20f
                isLight -> 0.24f + style.borderAlpha * 0.22f
                else -> 0.26f + style.borderAlpha * 0.24f
            }.times(visualPreset.strokeMultiplier).coerceAtMost(0.68f) * glassBaseAlpha).coerceIn(0f, 1f)
        )
    }
    val accentStrokeColor = remember(accentTint, glassBaseAlpha, enabled, readOnly, isError, visualFocused, isLight) {
        accentTint.copy(
            alpha = (when {
                !enabled -> 0.018f
                isError -> 0.09f
                visualFocused -> if (isLight) 0.20f else 0.24f
                readOnly -> if (isLight) 0.12f else 0.15f
                else -> if (isLight) 0.15f else 0.18f
            } * visualPreset.accentStrokeMultiplier * glassBaseAlpha).coerceIn(0f, 1f)
        )
    }
    val innerStrokeColor = remember(glassBaseAlpha, enabled, readOnly, visualFocused, isError, isLight) {
        Color.White.copy(
            alpha = (when {
                !enabled -> 0.028f
                isError -> 0.07f
                visualFocused -> if (isLight) 0.14f else 0.17f
                readOnly -> if (isLight) 0.10f else 0.13f
                else -> if (isLight) 0.11f else 0.14f
            } * visualPreset.innerStrokeMultiplier * glassBaseAlpha).coerceIn(0f, 1f)
        )
    }

    return drawWithCache {
        val outline: Outline = shape.createOutline(
            size = size,
            layoutDirection = layoutDirection,
            density = this,
        )
        val strokeWidthPx = borderWidth.toPx().coerceAtLeast(0.95f)
        val mainStroke = Stroke(width = strokeWidthPx)
        val accentStroke = Stroke(width = strokeWidthPx * 1.02f)
        val innerStroke = Stroke(width = (strokeWidthPx * 0.70f).coerceAtLeast(0.52f))

        val tintBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
            0.0f to tintOverlayColor,
            0.60f to tintOverlayColor.copy(alpha = tintOverlayColor.alpha * 0.60f),
            1.0f to tintOverlayColor.copy(alpha = tintOverlayColor.alpha * 0.22f),
        )
        val sheenBrush = androidx.compose.ui.graphics.Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to topSheenColor,
                0.22f to topSheenColor.copy(alpha = topSheenColor.alpha * 0.62f),
                0.52f to Color.Transparent,
                1.0f to Color.Transparent,
            ),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.78f, size.height * 0.32f),
        )
        val depthBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
            0.0f to Color.Transparent,
            0.78f to Color.Transparent,
            0.90f to bottomDepthColor.copy(alpha = bottomDepthColor.alpha * 0.30f),
            1.0f to bottomDepthColor,
        )
        val accentStrokeBrush = androidx.compose.ui.graphics.Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to topSheenColor.copy(alpha = topSheenColor.alpha * 0.44f),
                0.24f to accentStrokeColor,
                0.72f to accentStrokeColor.copy(alpha = accentStrokeColor.alpha * 0.64f),
                1.0f to Color.White.copy(alpha = innerStrokeColor.alpha * 0.74f),
            ),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
        )

        onDrawBehind {
            drawOutline(outline = outline, color = fillColor)
            drawOutline(outline = outline, brush = tintBrush)
            drawOutline(outline = outline, brush = sheenBrush)
            drawOutline(outline = outline, brush = depthBrush)

            drawOutline(outline = outline, color = strokeColor, style = mainStroke)
            drawOutline(outline = outline, brush = accentStrokeBrush, style = accentStroke)
            drawOutline(outline = outline, color = innerStrokeColor, style = innerStroke)
        }
    }
}
