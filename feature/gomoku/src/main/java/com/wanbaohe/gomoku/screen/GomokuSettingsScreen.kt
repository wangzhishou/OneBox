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
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
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
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import com.shifenmiao.base.utils.ActionUtils
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.data.GomokuTTSTemplate
import com.wanbaohe.gomoku.data.GomokuTTSTemplates
import com.wanbaohe.gomoku.application.port.outbound.EngineSlot
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.ui.GomokuOpponentPicker
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
    val duelA by component.duelEngineA.collectAsState()
    val duelB by component.duelEngineB.collectAsState()
    val aiConfig by component.gomokuAiConfig.collectAsState()
    val runningActions by component.runningSettingsActions.collectAsState()
    var pickingSlot by remember { mutableStateOf<AiSlot?>(null) }

    val pickingSlotValue = pickingSlot
    if (pickingSlotValue != null) {
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
        val engines by component.allAiEngines.collectAsState()
        val models by component.modelsByProvider.collectAsState()
        GomokuOpponentPicker(
            config = GameAiPlayerConfig.capture(aiConfig.sourceFor(slot), engine),
            workingEngine = engine,
            allEngines = engines,
            modelsByProvider = models,
            onSourceSelected = { component.switchAiSource(slot, it) },
            onModelSelected = { selected, model ->
                when (slot) {
                    EngineSlot.FAST -> component.switchAiModel(selected, model)
                    EngineSlot.DUEL_A -> component.switchDuelEngineA(selected, model)
                    EngineSlot.DUEL_B -> component.switchDuelEngineB(selected, model)
                }
                component.switchAiSource(slot, GomokuAiSource.WorkingModel)
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
                    onClick = {
                        ActionUtils.showLogin(source = "gomoku_tts_preview") {
                            component.previewTTSConfig()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                PreviewButton(
                    label = stringResource(R.string.gomoku_settings_tts_config),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRecordVoiceOver,
                    running = false,
                    onClick = component::openTTSConfigSettings,
                    modifier = Modifier.fillMaxWidth(),
                )
                GomokuTTSTemplates.ALL.forEach { template ->
                    TtsLineRow(
                        template = template,
                        customText = settings.ttsTemplateTexts[template.tag] ?: "",
                        onTextChange = { component.updateTTSTemplateText(template.tag, it) },
                        onGenerate = {
                            ActionUtils.showLogin(source = "gomoku_tts_generate") {
                                component.generateTTS(template, it)
                            }
                        },
                        onRegenerate = {
                            ActionUtils.showLogin(source = "gomoku_tts_regenerate") {
                                component.regenerateTTS(template, it)
                            }
                        },
                        onPlay = { text ->
                            ActionUtils.showLogin(source = "gomoku_tts_play") {
                                component.playTTSAudio(template, text)
                            }
                        },
                        isGenerating = GomokuRouterComponent.SettingsAction.GenerateTTS(template.tag) in runningActions,
                        isRegenerating = GomokuRouterComponent.SettingsAction.RegenerateTTS(template.tag) in runningActions,
                        isPlaying = GomokuRouterComponent.SettingsAction.PlayTTS(template.tag) in runningActions,
                    )
                }
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
                value = aiConfig.fastSource.displayName(fastEngine.model.title.ifBlank { fastEngine.model.name }),
                onClick = { pickingSlot = AiSlot.FAST },
            )
            AiSourceRow(
                label = stringResource(R.string.gomoku_settings_ai_duel_a),
                value = aiConfig.duelASource.displayName(duelA.model.title.ifBlank { duelA.model.name }),
                onClick = { pickingSlot = AiSlot.DUEL_A },
            )
            AiSourceRow(
                label = stringResource(R.string.gomoku_settings_ai_duel_b),
                value = aiConfig.duelBSource.displayName(duelB.model.title.ifBlank { duelB.model.name }),
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
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        GlassSwitch(
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
        Text(label, modifier = Modifier.padding(start = 6.dp).weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            modifier = Modifier.weight(1f),
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

@Composable
private fun TtsLineRow(
    template: GomokuTTSTemplate,
    customText: String,
    onTextChange: (String) -> Unit,
    onGenerate: (String) -> Unit,
    onRegenerate: (String) -> Unit,
    onPlay: (String) -> Unit,
    isGenerating: Boolean,
    isRegenerating: Boolean,
    isPlaying: Boolean,
) {
    val fallback = stringResource(template.defaultTextRes)
    val text = customText.ifBlank { fallback }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(template.labelResId),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GlassOutlinedTextField(
            value = customText,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(fallback) },
            singleLine = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlassTonalButton(
                onClick = { onPlay(text) },
                enabled = !isPlaying,
                modifier = Modifier.weight(1f),
            ) {
                if (isPlaying) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlay, contentDescription = null)
                }
            }
            GlassTonalButton(
                onClick = { onGenerate(text) },
                enabled = !isGenerating,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    stringResource(
                        if (isGenerating) R.string.gomoku_settings_generate_loading
                        else R.string.gomoku_settings_generate
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            GlassTonalButton(
                onClick = { onRegenerate(text) },
                enabled = !isRegenerating,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    stringResource(
                        if (isRegenerating) R.string.gomoku_settings_regenerate_loading
                        else R.string.gomoku_settings_regenerate
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
