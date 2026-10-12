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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.theme.ThemeDefaults
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.theme.blend

// ──────────────────────────────────────────────────────────────
//  GlassOutlinedTextField — 毛玻璃风格 OutlinedTextField
// ──────────────────────────────────────────────────────────────

/**
 * 输入框描边可见度的下限系数。
 *
 * 输入框的描边是"这里可以输入"的**功能性提示**, 和卡片/按钮的装饰描边不是一回事,
 * 所以不能直接乘全局"玻璃边界透明度"(默认 20%):
 * 未聚焦时描边基础 alpha 只有 0.35, 再乘 0.20 只剩 0.06 可见度 —— 实测边框只比背景暗
 * 7.8/255, 看上去就是一块没有边界的高亮文字, 认不出是输入框。
 *
 * 这里把设置值映射到 [0.62, 1] 区间: 只要设置不是 0, 输入框就保留可辨认的边界, 且仍随
 * 设置线性变化; 设置拉到 0 依旧按"隐藏描边"处理(保留这个明确的手动出口)。
 * 卡片/按钮等装饰描边不受影响, 仍然直接乘设置值。
 */
private const val TEXT_FIELD_MIN_STROKE_VISIBILITY = 0.62f

/** 与列表行同一档圆角, 单行和多行共用, 避免搜索框胶囊、表单框小圆角各写各的。 */
internal val GlassTextFieldShape: Shape = RoundedCornerShape(16.dp)

private val GlassTextFieldHorizontalPadding = 16.dp
private val GlassTextFieldSingleLineVerticalPadding = 14.dp
private val GlassTextFieldMultiLineVerticalPadding = 12.dp

/**
 * 没传 [contentPadding] 时的留白。
 * 有浮动标签走 Material 自己的内边距, 避免标签和文字抢同一块空间;
 * 单行上下 14, 配合 56dp 最小高度, 文字垂直居中且点按区域够大;
 * 多行上下 12, 首行末行离开 16dp 圆角, 又不会把文本区撑得过空。
 */
private fun glassTextFieldContentPadding(
    hasLabel: Boolean,
    singleLine: Boolean,
    maxLines: Int,
): PaddingValues {
    if (hasLabel) return OutlinedTextFieldDefaults.contentPadding()
    val vertical = if (singleLine || maxLines <= 1) {
        GlassTextFieldSingleLineVerticalPadding
    } else {
        GlassTextFieldMultiLineVerticalPadding
    }
    return PaddingValues(
        horizontal = GlassTextFieldHorizontalPadding,
        vertical = vertical,
    )
}

/** 把全局玻璃描边可见度映射成输入框描边的可见度(带下限, 保证输入框边界始终可辨认)。 */
private fun textFieldStrokeVisibility(glassBorderAlpha: Float): Float =
    TEXT_FIELD_MIN_STROKE_VISIBILITY +
        (1f - TEXT_FIELD_MIN_STROKE_VISIBILITY) * glassBorderAlpha.coerceIn(0f, 1f)

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
 * ### 配色与留白
 *
 * 静止态用 [androidx.compose.material3.ColorScheme.surfaceContainerLow] 做一层薄玻璃,
 * 覆盖率和卡片同一档, 页面渐变能透过来。描边用 outlineVariant。聚焦时底色略实一点,
 * 并往 primaryContainer 轻染, 描边和光标改用 onPrimaryContainer。
 * 单行、多行共用 16dp 圆角; 未传入 [contentPadding] 时按有无标签、单行或多行取统一留白。
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
 * @param focusedAlphaBoost  聚焦时底色加深幅度，默认 0.12f
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
    shape: Shape = GlassTextFieldShape,
    colors: TextFieldColors = AppTheme.colors.getOutlinedTextFieldColors(),
    style: GlassStyle = GlassStyle.Medium,
    visualPreset: GlassTextFieldVisualPreset = GlassTextFieldVisualPreset.Balanced,
    glassColor: Color = Color.Unspecified,
    glassBorderWidth: Dp = 0.5.dp,
    focusedAlphaBoost: Float = 0.06f,
    contentPadding: PaddingValues? = null,
) {
    val resolvedContentPadding = contentPadding ?: glassTextFieldContentPadding(
        hasLabel = label != null,
        singleLine = singleLine,
        maxLines = maxLines,
    )
    // 关掉玻璃也走同一条描边。系统 OutlinedTextField 聚焦会从 1dp 加到 2dp。
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val containerColor = glassColor.takeUnless { it == Color.Unspecified }
        ?: MaterialTheme.colorScheme.surfaceContainerLow
    val cursorColor = if (isError) colors.errorCursorColor else colors.cursorColor
    val resolvedColors = resolveGlassDecorationColors(colors = colors, isGlassActive = true)

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
        focusedAlphaBoost = focusedAlphaBoost,
        glassColor = containerColor,
        glassBorderWidth = glassBorderWidth,
        cursorColor = cursorColor,
        isFocused = isFocused,
        contentPadding = resolvedContentPadding,
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
    shape: Shape = GlassTextFieldShape,
    colors: TextFieldColors = AppTheme.colors.getOutlinedTextFieldColors(),
    style: GlassStyle = GlassStyle.Medium,
    visualPreset: GlassTextFieldVisualPreset = GlassTextFieldVisualPreset.Balanced,
    glassColor: Color = Color.Unspecified,
    glassBorderWidth: Dp = 0.5.dp,
    focusedAlphaBoost: Float = 0.06f,
    contentPadding: PaddingValues? = null,
) {
    val resolvedContentPadding = contentPadding ?: glassTextFieldContentPadding(
        hasLabel = label != null,
        singleLine = singleLine,
        maxLines = maxLines,
    )
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val containerColor = glassColor.takeUnless { it == Color.Unspecified }
        ?: MaterialTheme.colorScheme.surfaceContainerLow
    val cursorColor = if (isError) colors.errorCursorColor else colors.cursorColor
    val resolvedColors = resolveGlassDecorationColors(colors = colors, isGlassActive = true)

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
        focusedAlphaBoost = focusedAlphaBoost,
        glassColor = containerColor,
        glassBorderWidth = glassBorderWidth,
        cursorColor = cursorColor,
        isFocused = isFocused,
        contentPadding = resolvedContentPadding,
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
    focusedAlphaBoost: Float,
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
                        focusedAlphaBoost = focusedAlphaBoost,
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
    focusedAlphaBoost: Float,
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
                        focusedAlphaBoost = focusedAlphaBoost,
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
    focusedAlphaBoost: Float,
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
                focusedAlphaBoost = focusedAlphaBoost,
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
    // 底色和描边由输入框自己画。文字、图标、占位符沿用调用方传入的角色色,
    // 不再往别的颜色上混, 避免聚焦时 onPrimaryContainer 被洗回灰色。
    return colors.copy(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
    )
}

/**
 * 输入框容器: 薄玻璃底 + 一条细描边。
 * 覆盖率贴近卡片, 不铺实色, 页面上的浅色渐变可以透过来。
 * 聚焦时只略微加实, 并收到 onPrimaryContainer 描边。
 */
@Composable
private fun Modifier.modernGlassTextFieldContainer(
    style: GlassStyle,
    visualPreset: GlassTextFieldVisualPreset,
    focusedAlphaBoost: Float,
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
    val settings = LocalSettingsState.current
    val isGlassActive = settings.isGlassAlphaEnabled
    val glassBaseAlpha = settings.glassBaseAlpha.coerceIn(0f, 1f)
    val glassBorderAlpha = settings.glassBorderAlpha.takeIf { it.isFinite() }
        ?.coerceIn(0f, 1f) ?: ThemeDefaults.DEFAULT_GLASS_BORDER_ALPHA
    val strokeVisibility = textFieldStrokeVisibility(glassBorderAlpha)
    val visualFocused = isFocused && enabled && !readOnly
    val focusProgress by animateFloatAsState(
        targetValue = if (visualFocused) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "GlassTextFieldFocus",
    )

    val neutralBase = if (color != Color.Unspecified) color else colorScheme.surfaceContainerLow
    // 不往 scrim 压。压深再叠高覆盖率会变成一块实灰, 和页面上的透明玻璃脱开。
    val restingHue = neutralBase
    val focusedHue = restingHue.blend(colorScheme.primaryContainer, if (isLight) 0.08f else 0.12f)
    val errorHue = restingHue.blend(colorScheme.error, 0.10f)
    val presetScale = visualPreset.fillMultiplier.coerceIn(0.96f, 1.04f)
    // 开着玻璃: Medium(0.15) 大约 0.20, 和卡片同一档。
    // 关掉玻璃: 固定一层更浅的底, 不跟玻璃透明度走。
    val restingFillAlpha = if (isGlassActive) {
        ((style.backgroundAlpha * 0.55f) + 0.12f)
            .coerceIn(0.14f, 0.28f) * presetScale * glassBaseAlpha
    } else {
        0.26f
    }
    val fillAlpha = when {
        !enabled -> if (isGlassActive) restingFillAlpha * 0.6f else 0.16f
        isError -> if (isGlassActive) (restingFillAlpha + 0.04f).coerceAtMost(0.34f) else 0.30f
        isGlassActive -> (restingFillAlpha + focusProgress * focusedAlphaBoost).coerceAtMost(0.36f)
        else -> restingFillAlpha + focusProgress * 0.04f
    }
    val fillHue = when {
        !enabled -> restingHue.blend(colorScheme.outlineVariant, 0.25f)
        isError -> lerp(restingHue, errorHue, 0.85f)
        else -> lerp(restingHue, focusedHue, focusProgress)
    }
    val fillColor = fillHue.copy(alpha = fillAlpha)

    val restingStroke = if (isLight) colorScheme.outlineVariant else colorScheme.outline
    val strokeHue = when {
        !enabled -> colorScheme.outline
        isError -> colorScheme.error
        else -> lerp(restingStroke, colorScheme.onPrimaryContainer, focusProgress)
    }
    // 线宽始终是 borderWidth, 聚焦不加粗。关掉玻璃时颜色再淡一档, 聚焦只换色。
    val strokeAlpha = if (isGlassActive) {
        when {
            !enabled -> 0.22f
            isError -> 0.62f
            else -> 0.78f + focusProgress * 0.08f
        } * glassBaseAlpha * visualPreset.strokeMultiplier.coerceIn(0.94f, 1.06f)
    } else {
        when {
            !enabled -> 0.14f
            isError -> 0.50f
            // 未聚焦从 0.22 收回到 0.32。聚焦仍停在 0.28, 只动默认边框。
            else -> 0.49f + focusProgress * (0.28f - 0.32f)
        }
    }
    val strokeColor = strokeHue.copy(alpha = strokeAlpha.coerceIn(0f, 1f))
    val strokeDrawAlpha = if (isGlassActive) strokeVisibility else 1f
    val drawStroke = if (isGlassActive) glassBorderAlpha > 0f else true

    return drawWithCache {
        val outline: Outline = shape.createOutline(
            size = size,
            layoutDirection = layoutDirection,
            density = this,
        )
        val stroke = Stroke(width = borderWidth.toPx().coerceAtLeast(0.75f))
        onDrawBehind {
            drawOutline(outline = outline, color = fillColor)
            if (drawStroke) {
                drawOutline(
                    outline = outline,
                    color = strokeColor,
                    style = stroke,
                    alpha = strokeDrawAlpha,
                )
            }
        }
    }
}
