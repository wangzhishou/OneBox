package com.shifenmiao.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDashboard
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlip
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSpeedTest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineReplay

// ── 面板主色 ─────────────────────────────────────────────────────────────
private val PanelBg = Color(0xFF1A1A1E)
private val LabelColor = Color.White.copy(alpha = 0.55f)
private val TrackInactive = Color.White.copy(alpha = 0.12f)
private val DividerColor = Color.White.copy(alpha = 0.08f)
private val OutlineColor = Color.White.copy(alpha = 0.18f)

/**
 * 通用播放控制面板状态
 */
@Stable
@Immutable
data class PlayerControlState(
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val progressDisplayText: String? = null,
    val fontSize: Float = 48f,
    val minFontSize: Float = 24f,
    val maxFontSize: Float = 96f,
    val speed: Float = 3f,
    val minSpeed: Float = 0.5f,
    val maxSpeed: Float = 10f,
    val isMirrorMode: Boolean = false,
    val showMirrorToggle: Boolean = true,
    val showFontSizeSlider: Boolean = true,
    val showSpeedSlider: Boolean = true,
)

/**
 * 通用播放控制面板 (共用组件)
 *
 * 布局严格对标设计稿：
 * 1. 顶部拖拽手柄
 * 2. PROGRESS 进度条
 * 3. FONT SIZE 滑块
 * 4. SPEED 滑块
 * 5. 大号居中播放/暂停按钮 + 状态文字
 * 6. MIRROR MODE 行（图标+文字 | Switch）
 * 7. RESET / EXIT 描边按钮行
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerControlPanel(
    state: PlayerControlState,
    onTogglePlay: () -> Unit,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onProgressChangeFinished: (() -> Unit)? = null,
    onFontSizeChange: ((Float) -> Unit)? = null,
    onSpeedChange: ((Float) -> Unit)? = null,
    onToggleMirror: (() -> Unit)? = null,
    onReset: (() -> Unit)? = null,
    onExit: (() -> Unit)? = null,
    backgroundColor: Color = PanelBg,
    cornerRadius: Dp = 24.dp,
) {
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)).navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // ── 顶部拖拽手柄 ─────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(28.dp),
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                // ═══ PROGRESS ═══════════════════════════════════════
                SliderSection(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDashboard,
                    label = stringResource(R.string.common_player_progress).uppercase(),
                    trailingText = state.progressDisplayText
                        ?: "${(state.progress * 100).toInt()}%",
                    value = state.progress,
                    onValueChange = onProgressChange,
                    onValueChangeFinished = onProgressChangeFinished,
                    accentColor = primary,
                )

                Spacer(Modifier.height(20.dp))

                // ═══ FONT SIZE ══════════════════════════════════════
                if (state.showFontSizeSlider && onFontSizeChange != null) {
                    SliderSection(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText,
                        label = stringResource(R.string.common_player_font_size).uppercase(),
                        value = state.fontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = state.minFontSize..state.maxFontSize,
                        accentColor = primary,
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // ═══ SPEED ══════════════════════════════════════════
                if (state.showSpeedSlider && onSpeedChange != null) {
                    SliderSection(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSpeedTest,
                        label = stringResource(R.string.common_player_speed).uppercase(),
                        value = state.speed,
                        onValueChange = onSpeedChange,
                        valueRange = state.minSpeed..state.maxSpeed,
                        accentColor = primary,
                        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // ═══ 播放/暂停大按钮 ════════════════════════════════
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledIconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = primary.copy(alpha = 0.85f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Filled.Pause else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                            contentDescription = if (state.isPlaying)
                                stringResource(R.string.common_player_pause)
                            else
                                stringResource(R.string.common_player_resume),
                            modifier = Modifier.size(42.dp),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (state.isPlaying) stringResource(R.string.common_player_playing).uppercase()
                        else stringResource(R.string.common_player_paused).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LabelColor,
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ═══ MIRROR MODE ════════════════════════════════════
                if (state.showMirrorToggle && onToggleMirror != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlip,
                            contentDescription = null,
                            tint = LabelColor,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.common_player_mirror).uppercase() + " MODE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f),
                        )
                        GlassSwitch(
                            checked = state.isMirrorMode,
                            onCheckedChange = { onToggleMirror() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = primary,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.12f),
                                uncheckedBorderColor = Color.White.copy(alpha = 0.2f),
                            ),
                            modifier = Modifier.scale(0.85f),
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                }

                // ═══ RESET / EXIT 描边按钮 ══════════════════════════
                if (onReset != null || onExit != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (onReset != null) {
                            OutlinedPanelButton(
                                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineReplay,
                                label = stringResource(R.string.common_player_reset).uppercase(),
                                onClick = onReset,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (onExit != null) {
                            OutlinedPanelButton(
                                icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                                label = stringResource(R.string.common_player_exit).uppercase(),
                                onClick = onExit,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 带标签的 Slider 区块 ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SliderSection(
    icon: ImageVector,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    trailingText: String? = null,
    trailingIcon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Column(modifier = modifier) {
        // 标签行
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LabelColor,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = LabelColor,
            )
            Spacer(Modifier.weight(1f))
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = accentColor,
                )
            }
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = LabelColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // 自定义细轨道 Slider
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
            thumb = {
                // 圆形 thumb（14dp）
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            },
            track = { sliderState ->
                val fraction = (sliderState.value - sliderState.valueRange.start) /
                        (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TrackInactive)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accentColor)
                    )
                }
            },
        )
    }
}

// ── 描边按钮（RESET / EXIT） ─────────────────────────────────────────────
@Composable
private fun OutlinedPanelButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(48.dp)
            .border(
                width = 1.dp,
                color = OutlineColor,
                shape = RoundedCornerShape(12.dp)
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.6f),
        )
    }
}
