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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
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
import com.shifenmiao.core.R as CoreR
import com.shifenmiao.model.tts.TTSConfig
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource
import com.wanbaohe.xiangqi.data.XiangqiTTSTemplates
import com.wanbaohe.xiangqi.data.local.XiangqiEngineWeights
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDeleteForever
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownloadForOffline
import com.wanbaohe.xiangqi.router.screenLogic.XiangqiRouterComponent
import com.wanbaohe.xiangqi.ui.XiangqiAiPickerBottomSheet
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePlay
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
    val aiConfig by component.xiangqiAiConfig.collectAsState()
    val ttsConfig by component.ttsConfig.collectAsState(initial = TTSConfig())
    val runningSettingsActions by component.runningSettingsActions.collectAsState()
    val localEngineState by component.localEngineInstallState.collectAsState()
    var pickingSlot by remember { mutableStateOf<AiSlot?>(null) }
    var confirmingEngineDelete by remember { mutableStateOf(false) }

    val pickingSlotValue = pickingSlot
    if (pickingSlotValue != null) {
        val (selectedSource, slotTitleRes) = when (pickingSlotValue) {
            AiSlot.FAST -> aiConfig.fastSource to R.string.xiangqi_settings_ai_picker_title
            AiSlot.DUEL_A -> aiConfig.duelASource to R.string.xiangqi_settings_ai_duel_a_picker_title
            AiSlot.DUEL_B -> aiConfig.duelBSource to R.string.xiangqi_settings_ai_duel_b_picker_title
        }
        XiangqiAiPickerBottomSheet(
            visible = true,
            selected = selectedSource,
            workingModelTitle = sourceSubtitle(XiangqiAiSource.WorkingModel, fastEngine),
            title = stringResource(slotTitleRes),
            onSelected = { source ->
                val slot = when (pickingSlotValue) {
                    AiSlot.FAST -> EngineSlot.FAST
                    AiSlot.DUEL_A -> EngineSlot.DUEL_A
                    AiSlot.DUEL_B -> EngineSlot.DUEL_B
                }
                component.switchAiSource(slot, source)
                pickingSlot = null
            },
            onDismiss = { pickingSlot = null },
        )
    }

    EnhancedAlertDialog(
        visible = confirmingEngineDelete,
        onDismissRequest = { confirmingEngineDelete = false },
        title = { Text(text = stringResource(R.string.xiangqi_local_engine_delete_confirm_title)) },
        text = {
            Text(
                text = stringResource(
                    R.string.xiangqi_local_engine_delete_confirm_message,
                    formatEngineSize(XiangqiEngineWeights.EXPECTED_BYTES),
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    confirmingEngineDelete = false
                    component.deleteLocalEngine()
                }
            ) {
                Text(text = stringResource(CoreR.string.button_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { confirmingEngineDelete = false }) {
                Text(text = stringResource(CoreR.string.button_cancel))
            }
        },
    )

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.xiangqi_settings_sound_enabled),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = settings.soundEnabled,
                    onCheckedChange = component::updateSoundEnabled,
                )
            }
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
                engineName = sourceLabel(aiConfig.fastSource),
                modelName = sourceSubtitle(aiConfig.fastSource, fastEngine),
                onClick = { pickingSlot = AiSlot.FAST },
            )
            AiSlotRow(
                title = stringResource(R.string.xiangqi_settings_ai_duel_a_title),
                description = stringResource(R.string.xiangqi_settings_ai_duel_a_desc),
                engineName = sourceLabel(aiConfig.duelASource),
                modelName = sourceSubtitle(aiConfig.duelASource, fastEngine),
                onClick = { pickingSlot = AiSlot.DUEL_A },
            )
            AiSlotRow(
                title = stringResource(R.string.xiangqi_settings_ai_duel_b_title),
                description = stringResource(R.string.xiangqi_settings_ai_duel_b_desc),
                engineName = sourceLabel(aiConfig.duelBSource),
                modelName = sourceSubtitle(aiConfig.duelBSource, fastEngine),
                onClick = { pickingSlot = AiSlot.DUEL_B },
            )
        }

        SettingsSection(
            title = stringResource(R.string.xiangqi_settings_local_engine_title),
            subtitle = stringResource(R.string.xiangqi_settings_local_engine_subtitle),
            icon = { Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownloadForOffline, contentDescription = null) },
        ) {
            LocalEngineRow(
                packaged = component.isLocalEnginePackaged,
                state = localEngineState,
                onDownload = component::downloadLocalEngine,
                onCancel = component::cancelLocalEngineDownload,
                onDelete = { confirmingEngineDelete = true },
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


/**
 * 离线引擎三态行：未下载 → 下载；下载中 → 进度 + 取消；已装 → 体积 + 删除。
 *
 * [packaged] 为编译期事实：没把引擎二进制打进包的构建（如本仓库未 vendor 源码时）直接说明不可用，
 * 不给下载入口 —— 免得用户下完 10.74MB 权重却发现引擎本身没在包里。
 */
@Composable
private fun LocalEngineRow(
    packaged: Boolean,
    state: XiangqiEngineWeights.InstallState,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when {
            !packaged -> Text(
                text = stringResource(R.string.xiangqi_local_engine_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            state is XiangqiEngineWeights.InstallState.Downloading -> {
                val fraction =
                    if (state.total > 0) (state.downloaded.toFloat() / state.total).coerceIn(0f, 1f) else 0f
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.xiangqi_local_engine_downloading, (fraction * 100).toInt()),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = formatEngineSize(state.downloaded) + " / " + formatEngineSize(state.total),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth(),
                )
                GlassTonalButton(onClick = onCancel) {
                    Text(stringResource(CoreR.string.button_cancel))
                }
            }

            state is XiangqiEngineWeights.InstallState.Installed -> {
                Text(
                    text = stringResource(
                        R.string.xiangqi_local_engine_installed,
                        formatEngineSize(state.bytes),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                GlassTonalButton(onClick = onDelete) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDeleteForever, contentDescription = null)
                    Text(stringResource(R.string.xiangqi_local_engine_delete))
                }
            }

            else -> {
                Text(
                    text = stringResource(
                        R.string.xiangqi_local_engine_not_installed,
                        formatEngineSize(XiangqiEngineWeights.EXPECTED_BYTES),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                GlassTonalButton(onClick = onDownload) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload, contentDescription = null)
                    Text(stringResource(R.string.xiangqi_local_engine_download))
                }
            }
        }
    }
}

/** 权重体积展示：够用即可，不做本地化单位（与本地模型页的 "~585MB" 风格一致） */
private fun formatEngineSize(bytes: Long): String =
    "%.1f MB".format(bytes / 1048576.0)


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
                // 播放中只切换图标, 不换文字, 避免按钮宽度变化引起页面跳动
                if (isPreviewing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlay, contentDescription = null)
                }
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
                if (isPlaying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
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
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    if (isPreviewing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlay, contentDescription = null)
                    }
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
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
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

@Composable
private fun sourceLabel(source: XiangqiAiSource): String = when (source) {
    XiangqiAiSource.WorkingModel -> stringResource(R.string.xiangqi_ai_source_working_model)
    XiangqiAiSource.Jev -> stringResource(R.string.xiangqi_ai_source_jev)
    is XiangqiAiSource.RemoteEngine -> when (source.engineId) {
        XiangqiAiSource.RemoteEngine.PIKAFISH -> stringResource(R.string.xiangqi_ai_source_pikafish)
        else -> source.engineId
    }
}

@Composable
private fun sourceSubtitle(
    source: XiangqiAiSource,
    workingModel: com.shifenmiao.model.ai.AiEngine,
): String = when (source) {
    XiangqiAiSource.WorkingModel ->
        (workingModel.title.ifBlank { workingModel.name }) +
            " · " + (workingModel.model.title.ifBlank { workingModel.model.name })
    XiangqiAiSource.Jev -> stringResource(R.string.xiangqi_ai_source_jev_desc)
    is XiangqiAiSource.RemoteEngine -> stringResource(R.string.xiangqi_ai_source_engine_desc)
}

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
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

