/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.core.ui.widget.enhanced

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBackspace
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFingerprint

/**
 * 锁屏公共基础组件状态枚举。
 */
@Immutable
enum class LockScreenStatus {
    Idle,
    Awaiting,
    Verifying,
    Success,
    Error,
}

/**
 * 锁屏密度预设，用于在父容器空间受限 / 充裕时切换排版节奏。
 */
@Immutable
enum class LockScreenDensity {
    Adaptive,
    Compact,
    Standard,
    Roomy,
}

/**
 * 锁屏公共基础组件交互回调集合。
 */
@Immutable
data class LockScreenActions(
    val onDigit: (Char) -> Unit,
    val onBackspace: () -> Unit,
    val onAction: (() -> Unit)? = null,
    val onBiometric: (() -> Unit)? = null,
    val onClear: (() -> Unit)? = null,
    val onSubmit: (() -> Unit)? = null,
    val onEmergency: (() -> Unit)? = null,
    val onRecover: (() -> Unit)? = null,
)

@Immutable
private data class LockScreenMetrics(
    val horizontalPadding: Dp,
    val sectionSpacing: Dp,
    val keySpacing: Dp,
    val keySize: Dp,
    val statusBadgeFontSize: TextUnit,
    val pinGlyphFontSize: TextUnit,
    val pinLabelFontSize: TextUnit,
    val pinGlyphSpacing: Dp,
    val footerActionFontSize: TextUnit,
)

private val DefaultLockScreenMetrics = LockScreenMetrics(
    horizontalPadding = 24.dp,
    sectionSpacing = 24.dp,
    keySpacing = 16.dp,
    keySize = 76.dp,
    statusBadgeFontSize = 12.sp,
    pinGlyphFontSize = 36.sp,
    pinLabelFontSize = 11.sp,
    pinGlyphSpacing = 26.dp,
    footerActionFontSize = 12.sp,
)

private fun LockScreenMetrics.resolve(density: LockScreenDensity): LockScreenMetrics =
    when (density) {
        LockScreenDensity.Compact -> copy(
            horizontalPadding = 16.dp,
            sectionSpacing = 16.dp,
            keySpacing = 10.dp,
            keySize = 60.dp,
            statusBadgeFontSize = 11.sp,
            pinGlyphFontSize = 28.sp,
            pinLabelFontSize = 10.sp,
            pinGlyphSpacing = 18.dp,
            footerActionFontSize = 11.sp,
        )

        LockScreenDensity.Standard -> this

        LockScreenDensity.Roomy -> copy(
            horizontalPadding = 32.dp,
            sectionSpacing = 32.dp,
            keySpacing = 20.dp,
            keySize = 88.dp,
            statusBadgeFontSize = 13.sp,
            pinGlyphFontSize = 42.sp,
            pinLabelFontSize = 12.sp,
            pinGlyphSpacing = 32.dp,
            footerActionFontSize = 13.sp,
        )

        LockScreenDensity.Adaptive -> this
    }

private fun resolveAdaptiveDensity(width: Dp, height: Dp): LockScreenDensity {
    val shortest = if (width < height) width else height
    val longest = if (width < height) height else width
    return when {
        shortest < 280.dp || longest < 480.dp -> LockScreenDensity.Compact
        longest >= 720.dp -> LockScreenDensity.Roomy
        else -> LockScreenDensity.Standard
    }
}

/**
 * 锁屏公共基础组件：自适应父级容器，使用系统主题色与玻璃质感样式。
 *
 * 设计为通用、可复用的高层组件，不绑定具体业务（如密码保险箱），
 * 适合任何"输入 N 位数字 + 验证"的场景。
 *
 * 组件本身是无状态的：只负责渲染 UI、发出 `onDigit` / `onBackspace` / `onBiometric`
 * 等回调。状态机（步骤推进、密码匹配、落库）由调用方（通常是 ViewModel）维护，
 * 组件不持有任何密码数据，符合"高层只依赖抽象"的设计约定。
 *
 * # 用法 1：解锁
 *
 * ```kotlin
 * LockScreenBase(
 *     pinLength = 6,
 *     filledLength = uiState.filledLength,
 *     actions = LockScreenActions(
 *         onDigit = vm::onDigit,
 *         onBackspace = vm::onBackspace,
 *         onBiometric = {
 *             biometric.authenticate(
 *                 activity = activity,
 *                 onSuccess = vm::onBiometricSuccess,
 *                 onError = vm::onBiometricError,
 *             )
 *         },
 *         onEmergency = { /* 紧急锁定 */ },
 *         onRecover = { /* 找回密码 */ },
 *     ),
 *     showBiometric = uiState.biometricEnabled,
 *     title = stringResource(R.string.lock_title),
 *     statusText = stringResource(R.string.lock_status_awaiting),
 *     pinLabel = stringResource(R.string.lock_hint),
 *     status = uiState.status,
 *     footer = { LockScreenFooterActions(actions = vm.actions) },
 * )
 * ```
 *
 * # 用法 2：设置新密码（两次确认）
 *
 * 设置流程是状态机，由 ViewModel 拥有；组件本身**不需要单独派生**，
 * 只需在 ViewModel 切换步骤时改变 `title` / `statusText` / `pinLabel` / `status`：
 *
 * ```kotlin
 * sealed interface PasswordSetupStep {
 *     data object EnterNew : PasswordSetupStep
 *     data class Confirm(val first: String) : PasswordSetupStep
 *     data object Mismatch : PasswordSetupStep
 *     data object Saving : PasswordSetupStep
 *     data object Done : PasswordSetupStep
 * }
 *
 * LockScreenBase(
 *     pinLength = 6,
 *     filledLength = uiState.filledLength,
 *     actions = LockScreenActions(
 *         onDigit = vm::onDigit,
 *         onBackspace = vm::onBackspace,
 *         // onBiometric / onEmergency / onRecover 全部置 null → footer 自动不渲染
 *     ),
 *     showBiometric = false, // 设置阶段不出现指纹键
 *     status = when (uiState.step) {
 *         is PasswordSetupStep.Mismatch -> LockScreenStatus.Error
 *         else -> LockScreenStatus.Awaiting
 *     },
 *     title = when (uiState.step) {
 *         is PasswordSetupStep.EnterNew -> stringResource(R.string.setup_title_new)
 *         is PasswordSetupStep.Confirm -> stringResource(R.string.setup_title_confirm)
 *         else -> null
 *     },
 *     statusText = when (uiState.step) {
 *         is PasswordSetupStep.EnterNew -> stringResource(R.string.setup_status_new)
 *         is PasswordSetupStep.Confirm -> stringResource(R.string.setup_status_confirm)
 *         is PasswordSetupStep.Mismatch -> stringResource(R.string.setup_status_mismatch)
 *         else -> null
 *     },
 *     pinLabel = when (uiState.step) {
 *         is PasswordSetupStep.EnterNew -> stringResource(R.string.setup_hint_new)
 *         is PasswordSetupStep.Confirm -> stringResource(R.string.setup_hint_confirm)
 *         else -> null
 *     },
 * )
 * ```
 *
 * 对应的 ViewModel 状态机骨架：
 *
 * ```kotlin
 * fun onDigit(d: Char) {
 *     when (val step = state.value.step) {
 *         is PasswordSetupStep.EnterNew -> {
 *             val next = (state.value.firstInput + d).take(6)
 *             update { copy(firstInput = next, filledLength = next.length) }
 *             if (next.length == 6) update { copy(step = PasswordSetupStep.Confirm(next)) }
 *         }
 *         is PasswordSetupStep.Confirm -> {
 *             val next = (state.value.secondInput + d).take(6)
 *             update { copy(secondInput = next, filledLength = next.length) }
 *             if (next.length == 6) {
 *                 if (next == step.first) savePassword(next)
 *                 else update { copy(step = PasswordSetupStep.Mismatch, filledLength = 0) }
 *             }
 *         }
 *         else -> Unit
 *     }
 * }
 *
 * fun onBackspace() {
 *     when (state.value.step) {
 *         is PasswordSetupStep.Mismatch -> update {
 *             copy(step = PasswordSetupStep.EnterNew, filledLength = 0, secondInput = "")
 *         }
 *         is PasswordSetupStep.EnterNew -> update {
 *             copy(filledLength = (filledLength - 1).coerceAtLeast(0),
 *                  firstInput = firstInput.dropLast(1))
 *         }
 *         is PasswordSetupStep.Confirm -> update {
 *             copy(filledLength = (filledLength - 1).coerceAtLeast(0),
 *                  secondInput = secondInput.dropLast(1))
 *         }
 *         else -> Unit
 *     }
 * }
 * ```
 *
 * 注意：
 * - 真实密码落库（哈希 / 加密盐）必须在 ViewModel 或 Service 层完成，
 *   组件不持有任何密码数据。
 * - 不匹配状态下按删除键回到 `EnterNew` 比直接清空更友好——
 *   `LockScreenStatus.Error` 会把已填字符显示为 `×`，用户能直观看到错了多少位。
 * - 如果后续要支持更复杂的引导（首次确认成功后还要二次验证码、邮箱绑定等），
 *   再抽一个 `LockScreenSetupFlow` 高阶组件包装本组件即可。
 *
 * @param pinLength 期望输入的总位数。
 * @param filledLength 当前已输入的位数，会自动 clamp 到 `[0, pinLength]`。
 * @param actions 交互回调集合。
 * @param modifier 外部修饰符。
 * @param title 主标题；为空时不渲染。
 * @param statusText 状态徽标文本（如 AWAITING_AUTHORIZATION）；为空时不渲染。
 * @param status 当前状态，决定主题色与状态点颜色。
 * @param showBiometric 是否显示生物识别按键；为 `false` 时该位置留空。
 * @param pinReveal 是否以明文显示已输入字符（默认隐藏为 `*`）。
 * @param pinLabel PIN 显示下方的辅助标签；为空时不渲染。
 * @param actionKeyIcon 数字键盘右下角动作键的图标（如 ✕/✓）；为 `null` 时该位置留空。
 * @param actionKeyTint 动作键图标的 tint；为 `null` 时使用 onSurface。
 * @param header 顶部头部 slot；为空时不渲染。
 * @param footer 底部脚部 slot；为空时不渲染。
 * @param density 排版密度，`Adaptive` 时根据父容器尺寸自动选择档位。
 * @param contentPadding 容器整体内边距。
 */
@Composable
fun LockScreenBase(
    pinLength: Int,
    filledLength: Int,
    actions: LockScreenActions,
    modifier: Modifier = Modifier,
    title: String? = null,
    statusText: String? = null,
    status: LockScreenStatus = LockScreenStatus.Awaiting,
    showBiometric: Boolean = true,
    pinReveal: Boolean = false,
    pinLabel: String? = null,
    actionKeyIcon: ImageVector? = null,
    actionKeyTint: Color? = null,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    density: LockScreenDensity = LockScreenDensity.Adaptive,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    require(pinLength >= 1) { "pinLength must be >= 1, was $pinLength" }
    val clampedFilled = filledLength.coerceIn(0, pinLength)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        val effectiveDensity = when (density) {
            LockScreenDensity.Adaptive -> resolveAdaptiveDensity(maxWidth, maxHeight)
            else -> density
        }
        val metrics = DefaultLockScreenMetrics.resolve(effectiveDensity)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = metrics.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (header != null) {
                Spacer(modifier = Modifier.height(metrics.sectionSpacing))
                header()
                Spacer(modifier = Modifier.height(metrics.sectionSpacing))
            }

            Spacer(modifier = Modifier.weight(1f))

            TitleBlock(
                title = title,
                statusText = statusText,
                status = status,
                metrics = metrics,
            )

            Spacer(modifier = Modifier.height(metrics.sectionSpacing))

            PinDisplay(
                pinLength = pinLength,
                filledLength = clampedFilled,
                status = status,
                reveal = pinReveal,
                label = pinLabel,
                metrics = metrics,
            )

            Spacer(modifier = Modifier.height(metrics.sectionSpacing))

            NumericKeypad(
                actions = actions,
                showBiometric = showBiometric,
                actionKeyIcon = actionKeyIcon,
                actionKeyTint = actionKeyTint,
                metrics = metrics,
                modifier = Modifier.fillMaxWidth(),
            )

            if (footer != null) {
                Spacer(modifier = Modifier.height(metrics.sectionSpacing))
                footer()
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TitleBlock(
    title: String?,
    statusText: String?,
    status: LockScreenStatus,
    metrics: LockScreenMetrics,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(metrics.keySpacing),
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }

        if (!statusText.isNullOrBlank()) {
            StatusBadge(
                text = statusText,
                status = status,
                fontSize = metrics.statusBadgeFontSize,
            )
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    status: LockScreenStatus,
    fontSize: TextUnit,
) {
    val accent = status.accentColor()
    GlassSurface(
        modifier = Modifier.wrapContentHeight(),
        style = GlassStyle.Thin,
        shape = RoundedCornerShape(50),
        color = accent.copy(alpha = 0.18f),
        borderWidth = 0.9.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accent, CircleShape),
            )
            Text(
                text = text.uppercase(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun PinDisplay(
    pinLength: Int,
    filledLength: Int,
    status: LockScreenStatus,
    reveal: Boolean,
    label: String?,
    metrics: LockScreenMetrics,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(metrics.keySpacing),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 320.dp),
            horizontalArrangement = Arrangement.spacedBy(
                metrics.pinGlyphSpacing,
                Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pinLength) { index ->
                PinGlyph(
                    state = pinGlyphState(index, filledLength, status),
                    reveal = reveal && index < filledLength,
                    fontSize = metrics.pinGlyphFontSize,
                )
            }
        }

        if (!label.isNullOrBlank()) {
            Text(
                text = label.uppercase(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = metrics.pinLabelFontSize,
                    letterSpacing = 3.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PinGlyph(
    state: PinGlyphState,
    reveal: Boolean,
    fontSize: TextUnit,
) {
    val color = state.color()
    when (state) {
        PinGlyphState.Empty -> Text(
            text = "_",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize,
                fontWeight = FontWeight.Light,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        )

        PinGlyphState.Filled -> Text(
            text = if (reveal) "•" else "*",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
            ),
            color = color,
        )

        PinGlyphState.Error -> Text(
            text = "×",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
            ),
            color = color,
        )
    }
}

private enum class PinGlyphState { Empty, Filled, Error }

private fun pinGlyphState(
    index: Int,
    filledLength: Int,
    status: LockScreenStatus,
): PinGlyphState = when {
    index < filledLength -> when (status) {
        LockScreenStatus.Error -> PinGlyphState.Error
        else -> PinGlyphState.Filled
    }

    else -> PinGlyphState.Empty
}

@Composable
private fun PinGlyphState.color(): Color = when (this) {
    PinGlyphState.Empty -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    PinGlyphState.Filled -> MaterialTheme.colorScheme.primary
    PinGlyphState.Error -> MaterialTheme.colorScheme.error
}

@Composable
private fun LockScreenStatus.accentColor(): Color = when (this) {
    LockScreenStatus.Error -> MaterialTheme.colorScheme.error
    LockScreenStatus.Success -> MaterialTheme.colorScheme.primary
    LockScreenStatus.Verifying -> MaterialTheme.colorScheme.tertiary
    LockScreenStatus.Idle, LockScreenStatus.Awaiting -> MaterialTheme.colorScheme.primary
}

private val KeypadRows: List<List<Char>> = listOf(
    listOf('1', '2', '3'),
    listOf('4', '5', '6'),
    listOf('7', '8', '9'),
)

@Composable
private fun NumericKeypad(
    actions: LockScreenActions,
    showBiometric: Boolean,
    metrics: LockScreenMetrics,
    actionKeyIcon: ImageVector? = null,
    actionKeyTint: Color? = null,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val rows = remember { KeypadRows }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(metrics.keySpacing),
    ) {
        rows.forEach { row ->
            KeypadRow(
                modifier = Modifier.fillMaxWidth(),
                spacing = metrics.keySpacing,
            ) {
                row.forEach { digit ->
                    KeypadKey(
                        modifier = Modifier.size(metrics.keySize),
                        shape = CircleShape,
                        style = GlassStyle.Thin,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            actions.onDigit(digit)
                        },
                    ) {
                        KeypadDigitLabel(digit = digit, fontSize = metrics.pinGlyphFontSize)
                    }
                }
            }
        }

        KeypadRow(
            modifier = Modifier.fillMaxWidth(),
            spacing = metrics.keySpacing,
        ) {
            KeypadKey(
                modifier = Modifier.size(metrics.keySize),
                shape = CircleShape,
                style = GlassStyle.Thin,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    actions.onBackspace()
                },
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBackspace,
                    contentDescription = null,
                    modifier = Modifier.size(metrics.keySize * 0.36f),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            KeypadKey(
                modifier = Modifier.size(metrics.keySize),
                shape = CircleShape,
                style = GlassStyle.Thin,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    actions.onDigit('0')
                },
            ) {
                KeypadDigitLabel(digit = '0', fontSize = metrics.pinGlyphFontSize)
            }

            if (actionKeyIcon != null && actions.onAction != null) {
                KeypadKey(
                    modifier = Modifier.size(metrics.keySize),
                    shape = CircleShape,
                    style = GlassStyle.Thin,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        actions.onAction()
                    },
                ) {
                    Icon(
                        imageVector = actionKeyIcon,
                        contentDescription = null,
                        modifier = Modifier.size(metrics.keySize * 0.40f),
                        tint = actionKeyTint ?: MaterialTheme.colorScheme.onSurface,
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(metrics.keySize))
            }
        }

        if (showBiometric && actions.onBiometric != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KeypadKey(
                    modifier = Modifier.size(metrics.keySize),
                    shape = CircleShape,
                    style = GlassStyle.Thin,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        actions.onBiometric()
                    },
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(metrics.keySize * 0.42f),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadRow(
    modifier: Modifier,
    spacing: Dp,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun KeypadKey(
    modifier: Modifier,
    shape: Shape,
    style: GlassStyle,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        GlassSurface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            style = style,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            borderWidth = 0.9.dp,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}

@Composable
private fun KeypadDigitLabel(digit: Char, fontSize: TextUnit) {
    Text(
        text = digit.toString(),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = fontSize * 0.78f,
            fontWeight = FontWeight.Medium,
        ),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
    )
}

/**
 * 标准底部脚部 slot：左侧 emergency、右侧 recover。
 *
 * 适用于 `LockScreenBase(footer = { LockScreenFooterActions(...) })`。
 */
@Composable
fun LockScreenFooterActions(
    actions: LockScreenActions,
    modifier: Modifier = Modifier,
    emergencyLabel: String? = null,
    recoverLabel: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.onEmergency?.let { onEmergency ->
            FooterAction(
                label = emergencyLabel,
                onClick = onEmergency,
            )
        }

        actions.onRecover?.let { onRecover ->
            FooterAction(
                label = recoverLabel,
                onClick = onRecover,
            )
        }
    }
}

@Composable
private fun FooterAction(
    label: String?,
    onClick: () -> Unit,
) {
    GlassSurface(
        onClick = onClick,
        style = GlassStyle.Thin,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        borderWidth = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
            if (!label.isNullOrBlank()) {
                Text(
                    text = label.uppercase(),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}