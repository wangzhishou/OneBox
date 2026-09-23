package com.wanbaohe.gomoku.screen

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
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMemory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMusicNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePlay
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRecordVoiceOver
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStop
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.boardgame.model.AiPickerItem
import com.wanbaohe.boardgame.model.AiSourceTag
import com.wanbaohe.boardgame.ui.AiPickerBottomSheet
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.application.port.outbound.EngineSlot
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource
import com.wanbaohe.gomoku.router.screenLogic.GomokuRouterComponent

private enum class AiSlot { FAST, DUEL_A, DUEL_B }

/**
 * 简化设置页(一期):声音开关与试听、TTS 播报开关、走棋 AI 槽位来源。
 * 自定义音效 URL / TTS 模板编辑留到二期(沿用象棋设置的完整形态)。
 */
@Composable
fun GomokuSettingsScreen(
    component: GomokuRouterComponent,
    modifier: Modifier = Modifier,
) {
    val settings by component.gomokuSettings.collectAsState()
    val fastEngine by component.currentAIEngine.collectAsState()
    val aiConfig by component.gomokuAiConfig.collectAsState()
    val runningActions by component.runningSettingsActions.collectAsState()
    var pickingSlot by remember { mutableStateOf<AiSlot?>(null) }

    val pickingSlotValue = pickingSlot
    if (pickingSlotValue != null) {
        val slotTitleRes = when (pickingSlotValue) {
            AiSlot.FAST -> R.string.gomoku_settings_ai_picker_title
            AiSlot.DUEL_A -> R.string.gomoku_settings_ai_duel_a_picker_title
            AiSlot.DUEL_B -> R.string.gomoku_settings_ai_duel_b_picker_title
        }
        val sources = GomokuAiSource.presets
        val items = sources.map { source ->
            when (source) {
                GomokuAiSource.WorkingModel -> AiPickerItem(
                    title = stringResource(R.string.gomoku_ai_source_working_model),
                    subtitle = fastEngine.title.ifBlank { fastEngine.name },
                    tags = listOf(
                        AiSourceTag(stringResource(R.string.gomoku_ai_tag_login), androidx.compose.ui.graphics.Color(0xFFF08A5D)),
                        AiSourceTag(stringResource(R.string.gomoku_ai_tag_points), androidx.compose.ui.graphics.Color(0xFF4F46E5)),
                    ),
                )
                is GomokuAiSource.RemoteEngine -> AiPickerItem(
                    title = stringResource(R.string.gomoku_ai_source_engine_name),
                    subtitle = stringResource(R.string.gomoku_ai_source_engine_desc),
                    tags = listOf(
                        AiSourceTag(stringResource(R.string.gomoku_ai_tag_free), androidx.compose.ui.graphics.Color(0xFF3D8B7A)),
                    ),
                    trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory,
                )
            }
        }
        val currentSource = when (pickingSlotValue) {
            AiSlot.FAST -> aiConfig.fastSource
            AiSlot.DUEL_A -> aiConfig.duelASource
            AiSlot.DUEL_B -> aiConfig.duelBSource
        }
        AiPickerBottomSheet(
            visible = true,
            title = stringResource(slotTitleRes),
            description = stringResource(R.string.gomoku_ai_picker_desc),
            items = items,
            selectedItem = items.getOrNull(sources.indexOf(currentSource)),
            onSelected = {
                val slot = when (pickingSlotValue) {
                    AiSlot.FAST -> EngineSlot.FAST
                    AiSlot.DUEL_A -> EngineSlot.DUEL_A
                    AiSlot.DUEL_B -> EngineSlot.DUEL_B
                }
                val index = items.indexOf(it)
                if (index >= 0) {
                    component.switchAiSource(slot, sources[index])
                }
                pickingSlot = null
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
            title = stringResource(R.string.gomoku_settings_sound_title),
            subtitle = stringResource(R.string.gomoku_settings_sound_subtitle),
            icon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMusicNote, contentDescription = null) },
        ) {
            SettingSwitchRow(
                label = stringResource(R.string.gomoku_settings_sound_enabled),
                checked = settings.soundEnabled,
                onCheckedChange = component::updateSoundEnabled,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PreviewButton(
                    label = stringResource(R.string.gomoku_settings_preview_move),
                    running = GomokuRouterComponent.SettingsAction.PreviewMoveSound in runningActions,
                    onClick = component::previewMoveSound,
                    modifier = Modifier.weight(1f),
                )
                PreviewButton(
                    label = stringResource(R.string.gomoku_settings_preview_win),
                    running = GomokuRouterComponent.SettingsAction.PreviewCheckSound in runningActions,
                    onClick = component::previewWinSound,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PreviewButton(
                    label = stringResource(R.string.gomoku_settings_preview_bgm),
                    running = GomokuRouterComponent.SettingsAction.PreviewBackgroundMusic in runningActions,
                    onClick = component::previewBackgroundMusic,
                    modifier = Modifier.weight(1f),
                )
                PreviewButton(
                    label = stringResource(R.string.gomoku_settings_stop_bgm),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStop,
                    running = GomokuRouterComponent.SettingsAction.StopBackgroundMusic in runningActions,
                    onClick = component::stopBackgroundMusicPreview,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // TTS 播报
        SettingsSection(
            title = stringResource(R.string.gomoku_settings_tts_title),
            subtitle = stringResource(R.string.gomoku_settings_tts_subtitle),
            icon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRecordVoiceOver, contentDescription = null) },
        ) {
            SettingSwitchRow(
                label = stringResource(R.string.gomoku_settings_tts_enabled),
                checked = settings.ttsEnabled,
                onCheckedChange = component::updateTTSEnabled,
            )
            if (settings.ttsEnabled) {
                PreviewButton(
                    label = stringResource(R.string.gomoku_settings_tts_preview),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlay,
                    running = GomokuRouterComponent.SettingsAction.PreviewTTSConfig in runningActions,
                    onClick = component::previewTTSConfig,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // 走棋 AI 来源
        SettingsSection(
            title = stringResource(R.string.gomoku_settings_ai_title),
            subtitle = stringResource(R.string.gomoku_settings_ai_subtitle),
            icon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory, contentDescription = null) },
        ) {
            AiSourceRow(
                label = stringResource(R.string.gomoku_settings_ai_fast),
                value = aiConfig.fastSource.displayName(fastEngine.title.ifBlank { fastEngine.name }),
                onClick = { pickingSlot = AiSlot.FAST },
            )
            AiSourceRow(
                label = stringResource(R.string.gomoku_settings_ai_duel_a),
                value = aiConfig.duelASource.displayName(fastEngine.title.ifBlank { fastEngine.name }),
                onClick = { pickingSlot = AiSlot.DUEL_A },
            )
            AiSourceRow(
                label = stringResource(R.string.gomoku_settings_ai_duel_b),
                value = aiConfig.duelBSource.displayName(fastEngine.title.ifBlank { fastEngine.name }),
                onClick = { pickingSlot = AiSlot.DUEL_B },
            )
        }
    }
}

@Composable
private fun GomokuAiSource.displayName(workingModelTitle: String): String = when (this) {
    GomokuAiSource.WorkingModel -> stringResource(R.string.gomoku_ai_source_working_model) + " · " + workingModelTitle
    is GomokuAiSource.RemoteEngine -> stringResource(R.string.gomoku_ai_source_engine_name)
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
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
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
