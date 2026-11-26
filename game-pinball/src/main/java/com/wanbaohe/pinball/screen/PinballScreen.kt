package com.wanbaohe.pinball.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassLinearProgressIndicator
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.engine.android.surface.GameSurface
import com.wanbaohe.pinball.R
import com.wanbaohe.pinball.component.PinballComponent
import com.wanbaohe.pinball.component.PinballPhase
import com.wanbaohe.pinball.component.PinballThemeColors
import com.wanbaohe.pinball.component.PinballUiState
import kotlin.math.ceil

@Composable
fun PinballScreen(component: PinballComponent) {
    val state by component.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val colorScheme = MaterialTheme.colorScheme
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) component.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
            PinballThemeColors(
                surface = colorScheme.surface.toArgb(),
                primary = colorScheme.primary.toArgb(),
                secondary = colorScheme.secondary.toArgb(),
                tertiary = colorScheme.tertiary.toArgb(),
                outline = colorScheme.outline.toArgb(),
                onSurface = colorScheme.onSurface.toArgb(),
            )
        )
    }

    BaseScreen(
        title = {
            Text(
                text = stringResource(R.string.pinball_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
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
            if (state.phase == PinballPhase.Playing) {
                GlassButton(
                    onClick = component::pause,
                    color = colorScheme.primaryContainer,
                    contentColor = colorScheme.onPrimaryContainer,
                    borderWidth = 0.dp,
                    modifier = Modifier.heightIn(min = 44.dp),
                ) {
                    Text(stringResource(R.string.pinball_pause))
                }
            }
        },
        backgroundImage = {
            GameSurface(
                controller = component.engineController,
                modifier = Modifier.fillMaxSize(),
                onSnapshot = component::updateSnapshot,
            )
        },
        foreground = {
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
            if (state.phase != PinballPhase.Playing) {
                GameOverlay(
                    state = state,
                    onLaunch = component::launch,
                    onResume = component::resume,
                    onRestart = component::restart,
                    onBack = component.onGoBack,
                )
            }
        },
    )
}

@Composable
private fun GameHud(state: PinballUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.ballCount > 1 || state.enlargedBallCount > 0 || state.combo > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.ballCount > 1) {
                    Text(
                        text = stringResource(R.string.pinball_split_active, state.ballCount),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (state.enlargedBallCount > 0) {
                    Text(
                        text = stringResource(
                            R.string.pinball_enlarge_active,
                            ceil(state.enlargeSeconds).toInt(),
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
                if (state.combo > 1) {
                    Text(
                        text = stringResource(R.string.pinball_combo, state.combo),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = state.score.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.pinball_score),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.pinball_level_lives, state.level, state.lives),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.pinball_bricks_remaining, state.remainingBricks),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (state.bestScore > 0) {
                    Text(
                        text = stringResource(R.string.pinball_best_value, state.bestScore),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (state.combo > 1) {
            GlassLinearProgressIndicator(
                progress = { state.comboProgress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun PowerUpLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PowerUpLegendRow(
            color = MaterialTheme.colorScheme.secondary,
            text = stringResource(R.string.pinball_split_description),
        )
        PowerUpLegendRow(
            color = MaterialTheme.colorScheme.tertiary,
            text = stringResource(R.string.pinball_enlarge_description),
        )
    }
}

@Composable
private fun PowerUpLegendRow(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun GameOverlay(
    state: PinballUiState,
    onLaunch: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 64.dp)
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.46f))
            .padding(AppTheme.dimens.pageHorizontal),
        contentAlignment = Alignment.Center,
    ) {
        val title = when (state.phase) {
            PinballPhase.Ready -> stringResource(
                if (state.score == 0 && state.lives == 3) R.string.pinball_ready else R.string.pinball_ball_ready
            )
            PinballPhase.Paused -> stringResource(R.string.pinball_paused)
            PinballPhase.GameOver -> stringResource(R.string.pinball_game_over)
            PinballPhase.LevelComplete -> stringResource(R.string.pinball_level_complete)
            PinballPhase.Playing -> ""
        }
        val description = when (state.phase) {
            PinballPhase.Ready -> stringResource(
                if (state.score == 0 && state.lives == 3) {
                    R.string.pinball_ready_description
                } else {
                    R.string.pinball_ball_ready_description
                }
            )
            PinballPhase.Paused -> stringResource(R.string.pinball_paused_description)
            PinballPhase.LevelComplete -> stringResource(R.string.pinball_level_complete_description)
            else -> null
        }
        val primaryLabel = when (state.phase) {
            PinballPhase.Ready -> stringResource(R.string.pinball_launch)
            PinballPhase.Paused -> stringResource(R.string.pinball_resume)
            PinballPhase.GameOver -> stringResource(R.string.pinball_restart)
            PinballPhase.LevelComplete -> stringResource(R.string.pinball_next_level)
            PinballPhase.Playing -> ""
        }
        val primaryAction = when (state.phase) {
            PinballPhase.Paused -> onResume
            PinballPhase.GameOver -> onRestart
            else -> onLaunch
        }
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
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (state.phase == PinballPhase.GameOver) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    textAlign = TextAlign.Center,
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                if (state.phase == PinballPhase.Ready) PowerUpLegend()
                if (state.phase == PinballPhase.GameOver || state.phase == PinballPhase.LevelComplete) {
                    Text(
                        text = stringResource(R.string.pinball_score_value, state.score),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.pinball_best_value, state.bestScore),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                GlassButton(
                    onClick = primaryAction,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    borderWidth = 0.dp,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) {
                    Text(
                        text = primaryLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (state.phase == PinballPhase.Paused) {
                    GlassButton(
                        onClick = onRestart,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        borderWidth = 0.dp,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.pinball_restart))
                    }
                }
                if (state.phase != PinballPhase.Ready) {
                    GlassButton(
                        onClick = onBack,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        borderWidth = 0.dp,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.pinball_back))
                    }
                }
            }
        }
    }
}
