package com.wanbaohe.xiangqi.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
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
import androidx.compose.material3.Switch
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.ui.ai.AIModelsPickerBottomSheet
import com.shifenmiao.model.tts.TTSConfig
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.data.XiangqiTTSTemplates
import com.wanbaohe.xiangqi.router.screenLogic.XiangqiRouterComponent
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRecordVoiceOver
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMemory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMusicNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStop

@Composable
fun XiangqiSettingsScreen(
    component: XiangqiRouterComponent,
    modifier: Modifier = Modifier,
) {
    val settings by component.xiangqiSettings.collectAsState()
    val fastEngine by component.currentAIEngine.collectAsState()
    val duelEngineA by component.duelEngineA.collectAsState()
    val duelEngineB by component.duelEngineB.collectAsState()
    val allEngines by component.allAiEngines.collectAsState()
    val modelsByProvider by component.modelsByProvider.collectAsState()
    val ttsConfig by component.ttsConfig.collectAsState(initial = TTSConfig())
    val runningSettingsActions by component.runningSettingsActions.collectAsState()
    var pickingSlot by remember { mutableStateOf<AiSlot?>(null) }

    val pickingSlotValue = pickingSlot
    if (pickingSlotValue != null) {
        val (slotEngine, slotTitleRes) = when (pickingSlotValue) {
            AiSlot.FAST -> fastEngine to R.string.xiangqi_settings_ai_picker_title
            AiSlot.DUEL_A -> duelEngineA to R.string.xiangqi_settings_ai_duel_a_picker_title
            AiSlot.DUEL_B -> duelEngineB to R.string.xiangqi_settings_ai_duel_b_picker_title
        }
        AIModelsPickerBottomSheet(
            visible = true,
            allEngines = allEngines,
            modelsByProvider = modelsByProvider,
            selectedEngineName = slotEngine.identityKey(),
            selectedModelName = slotEngine.model.name,
            title = stringResource(slotTitleRes),
            onSelected = { selectedEngine, selectedModel ->
                when (pickingSlotValue) {
                    AiSlot.FAST -> component.switchAiModel(selectedEngine, selectedModel)
                    AiSlot.DUEL_A -> component.switchDuelEngineA(selectedEngine, selectedModel)
                    AiSlot.DUEL_B -> component.switchDuelEngineB(selectedEngine, selectedModel)
                }
                pickingSlot = null
            },
            onDismiss = { pickingSlot = null },
            showSettings = true,
            onSettings = component::openAiModelSettings,
        )
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // TTS 音效生成
        SettingsSection(
            title = stringResource(R.string.xiangqi_settings_tts_title),
            subtitle = stringResource(R.string.xiangqi_settings_tts_subtitle),
            icon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRecordVoiceOver, contentDescription = null) },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.xiangqi_settings_tts_enabled),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = settings.ttsEnabled,
                    onCheckedChange = component::updateTTSEnabled,
                )
            }

            if (settings.ttsEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                CurrentTTSConfigCard(
                    ttsConfig = ttsConfig,
                    onPreview = {
                        ActionUtils.showLogin(source = "xiangqi_tts_preview") {
                            component.previewTTSConfig()
                        }
                    },
                    onOpenConfig = component::openTTSConfigSettings,
                    isPreviewing = XiangqiRouterComponent.SettingsAction.PreviewTTSConfig in runningSettingsActions,
                )

                XiangqiTTSTemplates.ALL.forEach { template ->
                    TTSTemplateRow(
                        template = template,
                        customText = settings.ttsTemplateTexts[template.tag] ?: "",
                        onTextChange = { component.updateTTSTemplateText(template.tag, it) },
                        onGenerate = {
                            ActionUtils.showLogin(source = "xiangqi_tts_generate") {
                                component.generateTTS(template, it)
                            }
                        },
                        onRegenerate = {
                            ActionUtils.showLogin(source = "xiangqi_tts_regenerate") {
                                component.regenerateTTS(template, it)
                            }
                        },
                        onPlay = { component.playTTSAudio(template, it) },
                        isGenerating = XiangqiRouterComponent.SettingsAction.GenerateTTS(template.tag) in runningSettingsActions,
                        isRegenerating = XiangqiRouterComponent.SettingsAction.RegenerateTTS(template.tag) in runningSettingsActions,
                        isPlaying = XiangqiRouterComponent.SettingsAction.PlayTTS(template.tag) in runningSettingsActions,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        SettingsSection(
            title = stringResource(R.string.xiangqi_settings_audio_title),
            subtitle = stringResource(R.string.xiangqi_settings_audio_subtitle),
            icon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMusicNote, contentDescription = null) },
        ) {
            AudioUrlField(
                label = stringResource(R.string.xiangqi_settings_move_sound),
                value = settings.moveSoundUrl,
                onValueChange = component::updateMoveSoundUrl,
                onPreview = component::previewMoveSound,
                isPreviewing = XiangqiRouterComponent.SettingsAction.PreviewMoveSound in runningSettingsActions,
            )
            AudioUrlField(
                label = stringResource(R.string.xiangqi_settings_background_music),
                value = settings.backgroundMusicUrl,
                onValueChange = component::updateBackgroundMusicUrl,
                onPreview = component::previewBackgroundMusic,
                isPreviewing = XiangqiRouterComponent.SettingsAction.PreviewBackgroundMusic in runningSettingsActions,
                trailingAction = {
                    GlassTonalButton(
                        onClick = component::stopBackgroundMusicPreview,
                        enabled = XiangqiRouterComponent.SettingsAction.StopBackgroundMusic !in runningSettingsActions,
                    ) {
                        Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStop, contentDescription = null)
                        Text(
                            stringResource(
                                if (XiangqiRouterComponent.SettingsAction.StopBackgroundMusic in runningSettingsActions) {
                                    R.string.xiangqi_settings_stop_loading
                                } else {
                                    R.string.xiangqi_settings_stop_preview
                                }
                            )
                        )
                    }
                },
            )
            AudioUrlField(
                label = stringResource(R.string.xiangqi_settings_check_sound),
                value = settings.checkSoundUrl,
                onValueChange = component::updateCheckSoundUrl,
                onPreview = component::previewCheckSound,
                isPreviewing = XiangqiRouterComponent.SettingsAction.PreviewCheckSound in runningSettingsActions,
            )
        }

        SettingsSection(
            title = stringResource(R.string.xiangqi_settings_ai_title),
            subtitle = stringResource(R.string.xiangqi_settings_ai_subtitle),
            icon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory, contentDescription = null) },
        ) {
            AiSlotRow(
                title = stringResource(R.string.xiangqi_settings_ai_fast_title),
                description = stringResource(R.string.xiangqi_settings_ai_fast_desc),
                engineName = fastEngine.title.ifBlank { fastEngine.name },
                modelName = fastEngine.model.title.ifBlank { fastEngine.model.name },
                onClick = { pickingSlot = AiSlot.FAST },
            )
            AiSlotRow(
                title = stringResource(R.string.xiangqi_settings_ai_duel_a_title),
                description = stringResource(R.string.xiangqi_settings_ai_duel_a_desc),
                engineName = duelEngineA.title.ifBlank { duelEngineA.name },
                modelName = duelEngineA.model.title.ifBlank { duelEngineA.model.name },
                onClick = { pickingSlot = AiSlot.DUEL_A },
            )
            AiSlotRow(
                title = stringResource(R.string.xiangqi_settings_ai_duel_b_title),
                description = stringResource(R.string.xiangqi_settings_ai_duel_b_desc),
                engineName = duelEngineB.title.ifBlank { duelEngineB.name },
                modelName = duelEngineB.model.title.ifBlank { duelEngineB.model.name },
                onClick = { pickingSlot = AiSlot.DUEL_B },
            )
        }

        SettingsSection(
            title = stringResource(R.string.xiangqi_settings_prompt_title),
            subtitle = stringResource(R.string.xiangqi_settings_prompt_subtitle),
            icon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRecordVoiceOver, contentDescription = null) },
        ) {
            GlassTonalButton(
                onClick = component::openXiangqiPromptSettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRecordVoiceOver, contentDescription = null)
                Text(stringResource(R.string.xiangqi_settings_open_prompt_settings))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                icon()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun AudioUrlField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onPreview: () -> Unit,
    isPreviewing: Boolean,
    trailingAction: (@Composable () -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GlassOutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(stringResource(R.string.xiangqi_settings_audio_url_hint)) },
            singleLine = false,
            maxLines = 3,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlassTonalButton(
                onClick = onPreview,
                enabled = !isPreviewing,
                modifier = Modifier.weight(1f),
            ) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle, contentDescription = null)
                Text(
                    stringResource(
                        if (isPreviewing) R.string.xiangqi_settings_preview_loading
                        else R.string.xiangqi_settings_preview
                    )
                )
            }
            trailingAction?.invoke()
        }
    }
}

@Composable
private fun TTSTemplateRow(
    template: com.wanbaohe.xiangqi.data.XiangqiTTSTemplate,
    customText: String,
    onTextChange: (String) -> Unit,
    onGenerate: (String) -> Unit,
    onRegenerate: (String) -> Unit,
    onPlay: (String) -> Unit,
    isGenerating: Boolean,
    isRegenerating: Boolean,
    isPlaying: Boolean,
) {
    val label = stringResource(template.labelResId)
    val text = customText.ifBlank { template.defaultText }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GlassOutlinedTextField(
            value = customText,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(template.defaultText) },
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
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle, contentDescription = null)
                Text(
                    stringResource(
                        if (isPlaying) R.string.xiangqi_settings_play_loading
                        else R.string.xiangqi_settings_preview
                    )
                )
            }
            GlassTonalButton(
                onClick = { onGenerate(text) },
                enabled = !isGenerating,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    stringResource(
                        if (isGenerating) R.string.xiangqi_settings_generate_loading
                        else R.string.xiangqi_settings_generate
                    )
                )
            }
            GlassTonalButton(
                onClick = { onRegenerate(text) },
                enabled = !isRegenerating,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    stringResource(
                        if (isRegenerating) R.string.xiangqi_settings_regenerate_loading
                        else R.string.xiangqi_settings_regenerate
                    )
                )
            }
        }
    }
}

@Composable
private fun CurrentTTSConfigCard(
    ttsConfig: TTSConfig,
    onPreview: () -> Unit,
    onOpenConfig: () -> Unit,
    isPreviewing: Boolean,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Thin,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassSurface(
                    style = GlassStyle.Medium,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRecordVoiceOver,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.xiangqi_settings_tts_current_summary_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.xiangqi_settings_tts_current_summary_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

            }

            TTSConfigSummaryItem(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory,
                label = stringResource(R.string.xiangqi_settings_tts_current_engine),
                value = stringResource(ttsConfig.providerType.labelResId()),
            )
            TTSConfigSummaryItem(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory,
                label = stringResource(R.string.xiangqi_settings_tts_current_model),
                value = ttsConfig.model.ifBlank { stringResource(R.string.xiangqi_settings_empty_value) },
            )
            TTSConfigSummaryItem(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMusicNote,
                label = stringResource(R.string.xiangqi_settings_tts_current_voice),
                value = ttsConfig.defaultVoice.ifBlank { stringResource(R.string.xiangqi_settings_empty_value) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GlassTonalButton(
                    onClick = onPreview,
                    enabled = ttsConfig.isValid() && !isPreviewing,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle, contentDescription = null)
                    Text(
                        stringResource(
                            if (isPreviewing) R.string.xiangqi_settings_preview_loading
                            else R.string.xiangqi_settings_preview
                        )
                    )
                }

                GlassTonalButton(
                    onClick = onOpenConfig,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory, contentDescription = null)
                    Text(stringResource(R.string.xiangqi_settings_config))
                }
            }
        }
    }
}

@Composable
private fun TTSConfigSummaryItem(
    icon: ImageVector,
    label: String,
    value: String,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Medium,
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassSurface(
                style = GlassStyle.Thin,
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private enum class AiSlot { FAST, DUEL_A, DUEL_B }

private fun com.shifenmiao.model.tts.TTSProviderType.labelResId(): Int = when (this) {
    com.shifenmiao.model.tts.TTSProviderType.MIMO -> R.string.xiangqi_settings_tts_provider_mimo
    com.shifenmiao.model.tts.TTSProviderType.OPENAI_COMPATIBLE -> R.string.xiangqi_settings_tts_provider_openai
}

@Composable
private fun AiSlotRow(
    title: String,
    description: String,
    engineName: String,
    modelName: String,
    onClick: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Thin,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = engineName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = modelName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

