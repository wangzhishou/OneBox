/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.survive30s.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePauseBars
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePlay
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.wanbaohe.base.ui.button.PrimaryButton
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassIconButton
import com.wanbaohe.core.ui.widget.glass.GlassLinearProgressIndicator
import com.wanbaohe.engine.android.EngineController
import com.wanbaohe.engine.android.EngineSnapshot
import com.wanbaohe.engine.android.surface.GameSurface
import com.wanbaohe.survive30s.R
import com.wanbaohe.survive30s.component.GameState
import com.wanbaohe.survive30s.component.SURVIVE_GAME_DURATION
import com.wanbaohe.survive30s.component.Survive30sComponent
import com.wanbaohe.survive30s.component.Survive30sUiState
import com.wanbaohe.survive30s.component.SurviveThemeColors
import com.wanbaohe.survive30s.component.SurvivalPhase
import java.util.Locale

@Composable
fun Survive30sScreen(
    component: Survive30sComponent,
) {
    val state by component.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var previousGameState by remember { mutableStateOf(state.gameState) }
    var previousShieldCount by remember { mutableStateOf(state.shieldCount) }
    var isGameOverPanelVisible by remember { mutableStateOf(true) }
    val colorScheme = MaterialTheme.colorScheme
    val showTopBarIconBackground = LocalSettingsState.current.isGlassAlphaEnabled
    val topBarIconColors = IconButtonDefaults.iconButtonColors(
        containerColor = colorScheme.primaryContainer,
        contentColor = if (showTopBarIconBackground) {
            colorScheme.onPrimaryContainer
        } else {
            colorScheme.onSurface
        },
    )

    LaunchedEffect(state.gameState, state.shieldCount) {
        if (state.shieldCount > previousShieldCount) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        }
        if (state.gameState != previousGameState) {
            if (state.gameState == GameState.GAME_OVER) isGameOverPanelVisible = true
            when (state.gameState) {
                GameState.GAME_OVER -> haptic.performHapticFeedback(HapticFeedbackType.Reject)
                GameState.WIN -> {
                    haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
                    AppToastHost.showConfetti()
                }
                else -> Unit
            }
        }
        previousGameState = state.gameState
        previousShieldCount = state.shieldCount
    }
    LaunchedEffect(
        colorScheme.surface,
        colorScheme.primaryContainer,
        colorScheme.secondaryContainer,
        colorScheme.tertiaryContainer,
        colorScheme.outline,
        colorScheme.error,
    ) {
        component.updateTheme(
            SurviveThemeColors(
                surface = colorScheme.surface.toArgb(),
                primary = colorScheme.primaryContainer.toArgb(),
                secondary = colorScheme.secondaryContainer.toArgb(),
                tertiary = colorScheme.tertiaryContainer.toArgb(),
                outline = colorScheme.outline.toArgb(),
                player = colorScheme.error.toArgb(),
            )
        )
    }

    var dragTargetX by remember { mutableStateOf(Float.NaN) }
    var dragTargetY by remember { mutableStateOf(Float.NaN) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) component.pauseGame()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BaseScreen(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (state.gameState == GameState.PLAYING) {
                    Modifier.systemGestureExclusion()
                } else {
                    Modifier
                }
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragTargetX = offset.x
                        dragTargetY = offset.y
                        component.movePlayerTo(offset.x, offset.y)
                    },
                    onDragCancel = {
                        dragTargetX = Float.NaN
                        dragTargetY = Float.NaN
                    },
                    onDragEnd = {
                        dragTargetX = Float.NaN
                        dragTargetY = Float.NaN
                    },
                ) { change, dragAmount ->
                    change.consume()
                    val newX = (dragTargetX.takeIf { !it.isNaN() } ?: 0f) + dragAmount.x
                    val newY = (dragTargetY.takeIf { !it.isNaN() } ?: 0f) + dragAmount.y
                    dragTargetX = newX
                    dragTargetY = newY
                    component.movePlayerTo(newX, newY)
                }
            },
        title = stringResource(R.string.survive_30s_title),
        onGoBack = component.onGoBack,
        showNavigationBarsPadding = false,
        isBackHandler = false,
        navigationIcon = {
            GlassIconButton(
                onClick = component.onGoBack,
                modifier = Modifier.size(48.dp),
                colors = topBarIconColors,
                showBackground = showTopBarIconBackground,
                glassBorderWidth = 0.dp,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.survive_30s_back),
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        colors = topAppBarColors().copy(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = colorScheme.onSurface,
            navigationIconContentColor = colorScheme.onSurface,
            actionIconContentColor = colorScheme.onSurface,
        ),
        actions = {
            when {
                state.gameState == GameState.PLAYING -> {
                    GlassIconButton(
                        onClick = component::pauseGame,
                        modifier = Modifier.size(48.dp),
                        colors = topBarIconColors,
                        showBackground = showTopBarIconBackground,
                        glassBorderWidth = 0.dp,
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePauseBars,
                            contentDescription = stringResource(R.string.survive_30s_pause),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                state.gameState == GameState.GAME_OVER && !isGameOverPanelVisible -> {
                    GlassIconButton(
                        onClick = { isGameOverPanelVisible = true },
                        modifier = Modifier.size(48.dp),
                        colors = topBarIconColors,
                        showBackground = showTopBarIconBackground,
                        glassBorderWidth = 0.dp,
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.survive_30s_show_result),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        },
        background = {
            GameStage(
                controller = component.engineController,
                onSnapshot = component::updateSnapshot,
                onCanvasSizeChanged = component::updateCanvasSize,
                modifier = Modifier.fillMaxSize(),
            )
        },
        foreground = {
            if (state.gameState == GameState.PLAYING) {
                GameHud(
                    state = state,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(
                            horizontal = AppTheme.dimens.pageHorizontal,
                            vertical = 16.dp,
                        ),
                )
            }
            when (state.gameState) {
                GameState.IDLE -> IdleOverlay(
                    onStart = component::startGame,
                    onBack = component.onGoBack,
                )
                GameState.PAUSED -> PausedOverlay(
                    elapsed = state.elapsedSec,
                    onResume = component::resumeGame,
                    onRestart = component::startGame,
                    onBack = component.onGoBack,
                )
                GameState.GAME_OVER -> if (isGameOverPanelVisible) {
                    GameOverOverlay(
                        elapsed = state.elapsedSec,
                        bestTime = state.bestTime,
                        nearMissCount = state.nearMissCount,
                        onRestart = component::startGame,
                        onBack = component.onGoBack,
                        onShowStage = { isGameOverPanelVisible = false },
                        onWatchAds = component.onNavigateToAdWatch,
                    )
                }
                GameState.WIN -> WinOverlay(
                    nearMissCount = state.nearMissCount,
                    onRestart = component::startGame,
                    onBack = component.onGoBack,
                )
                GameState.PLAYING -> Unit
            }
        },
    )
    // 对局中边缘滑动和系统返回只暂停，不退出。球是跟手拖的，滑动退出会把这一局直接丢掉。
    // 暂停面板上的「返回」才离开。未开局、暂停、结束时不拦截，准备弹窗自己处理返回。
    BackHandler(enabled = state.gameState == GameState.PLAYING) {
        component.pauseGame()
    }
}

@Composable
private fun GameHud(
    state: Survive30sUiState,
    modifier: Modifier = Modifier,
) {
    val progress = (state.elapsedSec / SURVIVE_GAME_DURATION).coerceIn(0f, 1f)
    val remaining = (SURVIVE_GAME_DURATION - state.elapsedSec).coerceAtLeast(0f)
    val timerColor = timerColorFor(progress)
    val phaseLabel = when (state.phase) {
        SurvivalPhase.Warmup -> stringResource(R.string.survive_30s_phase_warmup)
        SurvivalPhase.Rush -> stringResource(R.string.survive_30s_phase_rush)
        SurvivalPhase.Storm -> stringResource(R.string.survive_30s_phase_storm)
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.invincibleSec > 0f || state.dangerLevel > 0.45f) {
            Text(
                text = if (state.invincibleSec > 0f) {
                    stringResource(R.string.survive_30s_invincible, state.invincibleSec)
                } else if (state.dangerLevel > 0.8f) {
                    stringResource(R.string.survive_30s_danger_high)
                } else {
                    stringResource(R.string.survive_30s_danger_medium)
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (state.invincibleSec > 0f) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    dangerColorFor(state.dangerLevel)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = String.format(Locale.getDefault(), "%.1fs", remaining),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = timerColor,
                )
                Text(
                    text = stringResource(R.string.survive_30s_remaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = phaseLabel + " · " + stringResource(
                        R.string.survive_30s_charge_value,
                        state.nearMissCharge,
                        3,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.survive_30s_shield_count, state.shieldCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.survive_30s_near_miss_count, state.nearMissCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.bestTime > 0f) {
                    Text(
                        text = stringResource(R.string.survive_30s_best_time, state.bestTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        TimerProgressBar(
            progress = progress,
            color = timerColor,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun dangerColorFor(dangerLevel: Float): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when {
        dangerLevel > 0.8f -> colorScheme.onErrorContainer
        dangerLevel > 0.45f -> colorScheme.onTertiaryContainer
        else -> colorScheme.onPrimaryContainer
    }
}

@Composable
private fun TimerProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100),
        label = "timer_progress",
    )
    GlassLinearProgressIndicator(
        progress = { 1f - animatedProgress },
        modifier = modifier.height(3.dp),
        color = color,
        trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f),
    )
}

@Composable
private fun timerColorFor(progress: Float): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when {
        progress < 0.33f -> colorScheme.onPrimaryContainer
        progress < 0.66f -> colorScheme.onTertiaryContainer
        else -> colorScheme.onErrorContainer
    }
}

@Composable
private fun GameStage(
    controller: EngineController,
    onSnapshot: (EngineSnapshot) -> Unit,
    onCanvasSizeChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.onSizeChanged { size ->
            onCanvasSizeChanged(size.width.toFloat(), size.height.toFloat())
        },
    ) {
        GameSurface(
            controller = controller,
            modifier = Modifier.fillMaxSize(),
            onSnapshot = onSnapshot,
            translucent = true,
        )
    }
}

@Composable
private fun GameDialog(
    title: String,
    primaryLabel: String,
    primaryIcon: ImageVector,
    onPrimary: () -> Unit,
    onBack: (() -> Unit)? = null,
    primaryContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    primaryContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    content: @Composable ColumnScope.() -> Unit,
) {
    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = { onBack?.invoke() },
        properties = DialogProperties(
            dismissOnBackPress = onBack != null,
            dismissOnClickOutside = false,
        ),
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        },
        confirmButton = {
            PrimaryButton(
                text = primaryLabel,
                onClick = onPrimary,
                modifier = Modifier.heightIn(min = 48.dp),
                containerColor = primaryContainerColor,
                contentColor = primaryContentColor,
                icon = {
                    Icon(
                        imageVector = primaryIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        },
        dismissButton = if (onBack == null) null else {
            {
                PrimaryButton(
                    text = stringResource(R.string.survive_30s_back),
                    onClick = onBack,
                    modifier = Modifier.heightIn(min = 48.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    icon = {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        },
    )
}

@Composable
private fun IdleOverlay(onStart: () -> Unit, onBack: () -> Unit) {
    GameDialog(
        title = stringResource(R.string.survive_30s_ready),
        primaryLabel = stringResource(R.string.survive_30s_start),
        primaryIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlay,
        onPrimary = onStart,
        onBack = onBack,
    ) {
        Text(
            text = stringResource(R.string.survive_30s_instruction),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.survive_30s_instruction_bonus),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PausedOverlay(
    elapsed: Float,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    GameDialog(
        title = stringResource(R.string.survive_30s_paused),
        primaryLabel = stringResource(R.string.survive_30s_resume),
        primaryIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlay,
        onPrimary = onResume,
        onBack = onBack,
    ) {
        Text(
            text = stringResource(R.string.survive_30s_survived_time, elapsed),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.survive_30s_pause_desc),
            textAlign = TextAlign.Center,
        )
        PrimaryButton(
            text = stringResource(R.string.survive_30s_retry),
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
        )
    }
}

@Composable
private fun GameOverOverlay(
    elapsed: Float,
    bestTime: Float,
    nearMissCount: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    onShowStage: () -> Unit,
    onWatchAds: (() -> Unit)?,
) {
    GameDialog(
        title = stringResource(R.string.survive_30s_game_over),
        primaryLabel = stringResource(R.string.survive_30s_retry),
        primaryIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
        onPrimary = onRestart,
        onBack = onBack,
        primaryContainerColor = MaterialTheme.colorScheme.errorContainer,
        primaryContentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            text = stringResource(R.string.survive_30s_survived_time, elapsed),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (elapsed >= bestTime) {
            Text(
                text = stringResource(R.string.survive_30s_new_record),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
        Text(
            text = stringResource(R.string.survive_30s_near_miss_count, nearMissCount),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PrimaryButton(
            text = stringResource(R.string.survive_30s_back_to_scene),
            onClick = onShowStage,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
        )
        onWatchAds?.let { watchAds ->
            PrimaryButton(
                text = stringResource(R.string.survive_30s_watch_ads_for_points),
                onClick = watchAds,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                icon = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlay,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun WinOverlay(
    nearMissCount: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    GameDialog(
        title = stringResource(R.string.survive_30s_win),
        primaryLabel = stringResource(R.string.survive_30s_retry),
        primaryIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
        onPrimary = onRestart,
        onBack = onBack,
    ) {
        Text(
            text = stringResource(R.string.survive_30s_win_desc),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.survive_30s_near_miss_count, nearMissCount),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
