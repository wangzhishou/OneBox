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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePlay
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassLinearProgressIndicator
import com.wanbaohe.core.ui.widget.glass.GlassStyle
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
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(state.gameState, state.shieldCount) {
        if (state.shieldCount > previousShieldCount) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        }
        if (state.gameState != previousGameState) {
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
        colorScheme.primary,
        colorScheme.secondary,
        colorScheme.tertiary,
        colorScheme.outline,
        colorScheme.onSurface,
    ) {
        component.updateTheme(
            SurviveThemeColors(
                surface = colorScheme.surface.toArgb(),
                primary = colorScheme.primary.toArgb(),
                secondary = colorScheme.secondary.toArgb(),
                tertiary = colorScheme.tertiary.toArgb(),
                outline = colorScheme.outline.toArgb(),
                onSurface = colorScheme.onSurface.toArgb(),
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
        title = {
            Text(
                text = stringResource(R.string.survive_30s_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        onGoBack = component.onGoBack,
        colors = topAppBarColors().copy(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = colorScheme.onSurface,
            navigationIconContentColor = colorScheme.onSurface,
            actionIconContentColor = colorScheme.onSurface,
        ),
        actions = {
            if (state.gameState == GameState.PLAYING) {
                GlassButton(
                    onClick = component::pauseGame,
                    color = colorScheme.primaryContainer,
                    contentColor = colorScheme.onPrimaryContainer,
                    borderWidth = 0.dp,
                    modifier = Modifier.heightIn(min = 44.dp),
                ) {
                    Text(stringResource(R.string.survive_30s_pause))
                }
            }
        },
        backgroundImage = {
            GameStage(
                controller = component.engineController,
                onSnapshot = component::updateSnapshot,
                onCanvasSizeChanged = component::updateCanvasSize,
                modifier = Modifier
                    .fillMaxSize()
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
                GameState.IDLE -> IdleOverlay(onStart = component::startGame)
                GameState.PAUSED -> PausedOverlay(
                    elapsed = state.elapsedSec,
                    onResume = component::resumeGame,
                    onRestart = component::startGame,
                    onBack = component.onGoBack,
                )
                GameState.GAME_OVER -> GameOverOverlay(
                    elapsed = state.elapsedSec,
                    bestTime = state.bestTime,
                    nearMissCount = state.nearMissCount,
                    onRestart = component::startGame,
                    onBack = component.onGoBack,
                    onWatchAds = component.onNavigateToAdWatch,
                )
                GameState.WIN -> WinOverlay(
                    nearMissCount = state.nearMissCount,
                    onRestart = component::startGame,
                    onBack = component.onGoBack,
                )
                GameState.PLAYING -> Unit
            }
        },
    )
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
        )
    }
}

@Composable
private fun OverlayCard(
    onTap: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 64.dp)
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.46f))
            .padding(AppTheme.dimens.pageHorizontal)
            .let { base ->
                if (onTap == null) base else base.pointerInput(Unit) {
                    detectTapGestures { onTap() }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        GlassCard(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            style = GlassStyle.Thick,
        ) {
            Column(
                modifier = Modifier.padding(AppTheme.dimens.cardContentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun IdleOverlay(onStart: () -> Unit) {
    OverlayCard(onTap = onStart) {
        Text(
            text = stringResource(R.string.survive_30s_ready),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = stringResource(R.string.survive_30s_instruction),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.survive_30s_instruction_bonus),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
        GlassButton(
            onClick = onStart,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            borderWidth = 0.dp,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) {
            Text(
                text = stringResource(R.string.survive_30s_start),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PausedOverlay(
    elapsed: Float,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    OverlayCard {
        Text(
            text = stringResource(R.string.survive_30s_paused),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = stringResource(R.string.survive_30s_survived_time, elapsed),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.survive_30s_pause_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        GlassButton(
            onClick = onResume,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            borderWidth = 0.dp,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) {
            Text(
                text = stringResource(R.string.survive_30s_resume),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        ResultActions(
            onBack = onBack,
            onRestart = onRestart,
            restartContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            restartContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultActions(
    onBack: () -> Unit,
    onRestart: () -> Unit,
    restartContainerColor: Color,
    restartContentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlassButton(
            onClick = onBack,
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            borderWidth = 0.dp,
            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.survive_30s_back),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
        GlassButton(
            onClick = onRestart,
            color = restartContainerColor,
            contentColor = restartContentColor,
            borderWidth = 0.dp,
            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.survive_30s_retry),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun GameOverOverlay(
    elapsed: Float,
    bestTime: Float,
    nearMissCount: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    onWatchAds: (() -> Unit)?,
) {
    OverlayCard {
        Text(
            text = stringResource(R.string.survive_30s_game_over),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ResultActions(
            onBack = onBack,
            onRestart = onRestart,
            restartContainerColor = MaterialTheme.colorScheme.errorContainer,
            restartContentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        onWatchAds?.let { watchAds ->
            GlassButton(
                onClick = watchAds,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                borderWidth = 0.dp,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlay,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.survive_30s_watch_ads_for_points),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun WinOverlay(
    nearMissCount: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    OverlayCard {
        Text(
            text = stringResource(R.string.survive_30s_win),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = stringResource(R.string.survive_30s_win_desc),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.survive_30s_near_miss_count, nearMissCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ResultActions(
            onBack = onBack,
            onRestart = onRestart,
            restartContainerColor = MaterialTheme.colorScheme.primaryContainer,
            restartContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
