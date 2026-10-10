package com.wanbaohe.chess.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMemory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMusicNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePlay
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRecordVoiceOver
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStop
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.chess.R
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.port.outbound.ChessAiSource
import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.presentation.displayNames
import com.wanbaohe.chess.router.screenLogic.ChessRouterComponent
import com.wanbaohe.chess.ui.ChessOpponentPicker

private enum class AiSlot { FAST, DUEL_A, DUEL_B }

/**
 * 简化设置页(一期):声音开关与试听、TTS 播报开关、走棋 AI 槽位来源。
 * 自定义音效 URL / TTS 模板编辑留到二期(沿用象棋设置的完整形态)。
 */
@Composable
fun ChessSettingsScreen(
    component: ChessRouterComponent,
    modifier: Modifier = Modifier,
) {
    val settings by component.chessSettings.collectAsState()
    val fastEngine by component.currentAIEngine.collectAsState()
    val duelA by component.duelEngineA.collectAsState()
    val duelB by component.duelEngineB.collectAsState()
    val aiConfig by component.chessAiConfig.collectAsState()
    val runningActions by component.runningSettingsActions.collectAsState()
    var pickingSlot by remember { mutableStateOf<AiSlot?>(null) }

    val pickingSlotValue = pickingSlot
    if (pickingSlotValue != null) {
        val engines by component.allAiEngines.collectAsState()
        val models by component.modelsByProvider.collectAsState()
        val slot = when (pickingSlotValue) {
            AiSlot.FAST -> EngineSlot.FAST
            AiSlot.DUEL_A -> EngineSlot.DUEL_A
            AiSlot.DUEL_B -> EngineSlot.DUEL_B
        }
        val engine = when (slot) {
            EngineSlot.FAST -> fastEngine
            EngineSlot.DUEL_A -> duelA
            EngineSlot.DUEL_B -> duelB
        }
        ChessOpponentPicker(
            config = GameAiPlayerConfig.capture(aiConfig.sourceFor(slot), engine),
            workingEngine = engine,
            allEngines = engines,
            modelsByProvider = models,
            onSourceSelected = { component.switchAiSource(slot, it) },
            onModelSelected = { provider, model ->
                when (slot) {
                    EngineSlot.FAST -> component.switchAiModel(provider, model)
                    EngineSlot.DUEL_A -> component.switchDuelEngineA(provider, model)
                    EngineSlot.DUEL_B -> component.switchDuelEngineB(provider, model)
                }
                component.switchAiSource(slot, ChessAiSource.WorkingModel)
            },
            onDismiss = { pickingSlot = null },
        )
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 声音:总开关 + 三个试听
        SettingsSection(
            title = stringResource(R.string.chess_settings_sound_title),
            subtitle = stringResource(R.string.chess_settings_sound_subtitle),
            icon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMusicNote, contentDescription = null) },
        ) {
            SettingSwitchRow(
                label = stringResource(R.string.chess_settings_sound_enabled),
                checked = settings.soundEnabled,
                onCheckedChange = component::updateSoundEnabled,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PreviewButton(
                    label = stringResource(R.string.chess_settings_preview_move),
                    running = ChessRouterComponent.SettingsAction.PreviewMoveSound in runningActions,
                    onClick = component::previewMoveSound,
                    modifier = Modifier.weight(1f),
                )
                PreviewButton(
                    label = stringResource(R.string.chess_settings_preview_check),
                    running = ChessRouterComponent.SettingsAction.PreviewCheckSound in runningActions,
                    onClick = component::previewCheckSound,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PreviewButton(
                    label = stringResource(R.string.chess_settings_preview_bgm),
                    running = ChessRouterComponent.SettingsAction.PreviewBackgroundMusic in runningActions,
                    onClick = component::previewBackgroundMusic,
                    modifier = Modifier.weight(1f),
                )
                PreviewButton(
                    label = stringResource(R.string.chess_settings_stop_bgm),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStop,
                    running = ChessRouterComponent.SettingsAction.StopBackgroundMusic in runningActions,
                    onClick = component::stopBackgroundMusicPreview,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // TTS 播报
        SettingsSection(
            title = stringResource(R.string.chess_settings_tts_title),
            subtitle = stringResource(R.string.chess_settings_tts_subtitle),
            icon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRecordVoiceOver, contentDescription = null) },
        ) {
            SettingSwitchRow(
                label = stringResource(R.string.chess_settings_tts_enabled),
                checked = settings.ttsEnabled,
                onCheckedChange = component::updateTTSEnabled,
            )
            if (settings.ttsEnabled) {
                PreviewButton(
                    label = stringResource(R.string.chess_settings_tts_preview),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlay,
                    running = ChessRouterComponent.SettingsAction.PreviewTTSConfig in runningActions,
                    onClick = component::previewTTSConfig,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // 走棋 AI 来源
        SettingsSection(
            title = stringResource(R.string.chess_settings_ai_title),
            subtitle = stringResource(R.string.chess_settings_ai_subtitle),
            icon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory, contentDescription = null) },
        ) {
            AiSourceRow(
                label = stringResource(R.string.chess_settings_ai_fast),
                value = GameAiPlayerConfig.capture(aiConfig.fastSource, fastEngine).displayLabel(),
                onClick = { pickingSlot = AiSlot.FAST },
            )
            AiSourceRow(
                label = stringResource(R.string.chess_settings_ai_duel_a),
                value = GameAiPlayerConfig.capture(aiConfig.duelASource, duelA).displayLabel(),
                onClick = { pickingSlot = AiSlot.DUEL_A },
            )
            AiSourceRow(
                label = stringResource(R.string.chess_settings_ai_duel_b),
                value = GameAiPlayerConfig.capture(aiConfig.duelBSource, duelB).displayLabel(),
                onClick = { pickingSlot = AiSlot.DUEL_B },
            )
        }
    }
}

private fun GameAiPlayerConfig.displayLabel(): String = displayNames().let { (service, model) ->
    if (model.isBlank()) service else "$service · $model"
}

@Composable
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun PreviewButton(
    label: String,
    running: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlay,
) {
    GlassTonalButton(
        onClick = onClick,
        enabled = !running,
        modifier = modifier,
    ) {
        if (running) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        Text(label, modifier = Modifier.padding(start = 6.dp), maxLines = 1)
    }
}

@Composable
private fun AiSourceRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Medium,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                icon()
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}
