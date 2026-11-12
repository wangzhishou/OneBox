/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.wanbaohe.setting.ai.screen

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSProviderDescriptor
import com.shifenmiao.model.tts.TTSProviderType
import com.shifenmiao.tts.service.TTSService
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.core.ui.widget.system.SelectorField
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.settings.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibilityOff

@Composable
fun TTSSettingsScreen(
    ttsService: TTSService,
    networkAudioPlayer: NetworkAudioPlayer,
    onGoBack: () -> Unit,
) {
    val config by ttsService.observeConfig().collectAsState(initial = TTSConfig())

    BaseScreen(
        title = stringResource(com.shifenmiao.core.R.string.profile_item_tts_settings),
        onGoBack = onGoBack,
    ) {
        TTSSettingsContent(
            config = config,
            ttsService = ttsService,
            networkAudioPlayer = networkAudioPlayer,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun TTSSettingsContent(
    config: TTSConfig,
    ttsService: TTSService,
    networkAudioPlayer: NetworkAudioPlayer,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val providerDescriptors = remember(ttsService) {
        ttsService.getProviderDescriptors().filter { it.isSelectable }
    }
    var providerType by remember(config.providerType) {
        mutableStateOf(config.providerType)
    }
    var baseUrl by remember(config.baseUrl) { mutableStateOf(config.baseUrl) }
    var apiToken by remember(config.apiToken) { mutableStateOf(config.apiToken) }
    var model by remember(config.model) { mutableStateOf(config.model) }
    var voice by remember(config.defaultVoice) { mutableStateOf(config.defaultVoice) }
    var speed by remember(config.defaultSpeed) { mutableStateOf(config.defaultSpeed.toString()) }
    var isApiTokenVisible by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isResetting by remember { mutableStateOf(false) }
    var isPreviewing by remember { mutableStateOf(false) }
    var isClearingCache by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableStateOf<Long?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val isActionRunning = isSaving || isResetting || isPreviewing || isClearingCache
    val draftConfig = config.copy(
        providerType = providerType,
        baseUrl = baseUrl.trim(),
        apiToken = apiToken.trim(),
        model = model.trim(),
        defaultVoice = voice.trim(),
        defaultSpeed = speed.toDoubleOrNull() ?: 1.0,
    )
    val providerDescriptor = providerDescriptors.firstOrNull { it.type == providerType }

    suspend fun refreshCacheSize() {
        cacheSize = withContext(Dispatchers.IO) { ttsService.getCacheSize() }
    }

    LaunchedEffect(Unit) {
        refreshCacheSize()
    }

    fun saveDraftConfig() {
        coroutineScope.launch {
            isSaving = true
            runCatching {
                withContext(Dispatchers.IO) {
                    ttsService.updateConfig(draftConfig)
                }
            }.onSuccess {
                AppToastHost.showToast(getString(R.string.tts_settings_save_success))
            }.onFailure {
                AppToastHost.showFailureToast(it)
            }
            isSaving = false
        }
    }

    fun runPreview() {
        coroutineScope.launch {
            isPreviewing = true
            runCatching {
                val file = withContext(Dispatchers.IO) {
                    ttsService.synthesizeWithConfig(
                        config = draftConfig,
                        text = getString(R.string.tts_settings_preview_text),
                        tag = "tts-test",
                        forceRefresh = true,
                    ).getOrThrow()
                }
                networkAudioPlayer.playLocalFile(file)
            }.onSuccess {
                refreshCacheSize()
                AppToastHost.showToast(getString(R.string.tts_settings_preview_success))
            }.onFailure {
                AppToastHost.showFailureToast(it)
            }
            isPreviewing = false
        }
    }

    fun previewCurrentVoice() {
        if (ttsService.isProxyRoute(draftConfig)) {
            ActionUtils.showLogin(source = "tts_preview", onSuccess = ::runPreview)
        } else {
            runPreview()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsSection(title = stringResource(R.string.tts_settings_engine_section)) {
            ProviderSelector(
                descriptors = providerDescriptors,
                selected = providerType,
                onSelect = { selectedType ->
                    providerType = selectedType
                    val defaults = ttsService.createDefaultConfig(selectedType)
                    model = defaults.model
                    voice = defaults.defaultVoice
                },
            )
        }

        SettingsSection(title = stringResource(R.string.tts_settings_connection_section)) {
            GlassOutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text(stringResource(R.string.tts_settings_api_url_label)) },
                placeholder = {
                    Text(
                        text = stringResource(providerType.endpointPlaceholderRes()),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                supportingText = {
                    Text(stringResource(R.string.tts_settings_api_url_hint))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            GlassOutlinedTextField(
                value = apiToken,
                onValueChange = { apiToken = it },
                label = { Text(stringResource(R.string.tts_settings_api_token_label)) },
                placeholder = { Text(stringResource(R.string.tts_settings_api_token_placeholder)) },
                trailingIcon = {
                    IconButton(onClick = { isApiTokenVisible = !isApiTokenVisible }) {
                        Icon(
                            imageVector = if (isApiTokenVisible) {
                                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility
                            } else {
                                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibilityOff
                            },
                            contentDescription = stringResource(
                                if (isApiTokenVisible) {
                                    R.string.tts_settings_hide_token
                                } else {
                                    R.string.tts_settings_show_token
                                }
                            ),
                        )
                    }
                },
                visualTransformation = if (isApiTokenVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            if (ttsService.isProxyRoute(draftConfig)) {
                Text(
                    text = stringResource(R.string.tts_settings_proxy_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SettingsSection(title = stringResource(R.string.tts_settings_model_section)) {
            GlassOutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text(stringResource(R.string.tts_settings_model_label)) },
                placeholder = {
                    Text(
                        text = providerDescriptor?.defaultModel.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            GlassOutlinedTextField(
                value = voice,
                onValueChange = { voice = it },
                label = { Text(stringResource(R.string.tts_settings_voice_label)) },
                placeholder = {
                    Text(
                        text = providerDescriptor?.defaultVoice.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            val supportedVoices = providerDescriptor?.supportedVoices.orEmpty()
            if (supportedVoices.isNotEmpty()) {
                VoicePresetPicker(
                    voices = supportedVoices,
                    selectedVoice = voice,
                    onSelect = { voice = it },
                    action = {
                        PreviewActionButton(
                            onClick = ::previewCurrentVoice,
                            enabled = !isActionRunning,
                            isPreviewing = isPreviewing,
                        )
                    },
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    PreviewActionButton(
                        onClick = ::previewCurrentVoice,
                        enabled = !isActionRunning,
                        isPreviewing = isPreviewing,
                    )
                }
            }
            GlassOutlinedTextField(
                value = speed,
                onValueChange = { speed = it },
                label = { Text(stringResource(R.string.tts_settings_speed_label)) },
                placeholder = { Text("1.0") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlassTonalButton(
                onClick = ::saveDraftConfig,
                enabled = !isActionRunning,
                modifier = Modifier.weight(1f),
            ) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(
                        if (isSaving) R.string.tts_settings_save_loading
                        else R.string.tts_settings_save_action
                    )
                )
            }

            GlassTonalButton(
                onClick = { showResetConfirmDialog = true },
                enabled = !isActionRunning,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(
                        if (isResetting) R.string.tts_settings_reset_loading
                        else R.string.tts_settings_reset_action
                    )
                )
            }
        }

        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            style = GlassStyle.Thin,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.tts_settings_cache_size_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = cacheSize?.let {
                            stringResource(
                                R.string.tts_settings_cache_size_value,
                                Formatter.formatFileSize(context, it),
                            )
                        } ?: stringResource(R.string.tts_settings_cache_size_loading),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                GlassTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            isClearingCache = true
                            runCatching {
                                val clearedSize = withContext(Dispatchers.IO) { ttsService.getCacheSize() }
                                withContext(Dispatchers.IO) { ttsService.clearAllCache() }
                                clearedSize
                            }.onSuccess { clearedSize ->
                                val message = if (clearedSize > 0L) {
                                    getString(
                                        R.string.tts_settings_clear_cache_success_with_size,
                                        Formatter.formatFileSize(context, clearedSize),
                                    )
                                } else {
                                    getString(R.string.tts_settings_clear_cache_success)
                                }
                                refreshCacheSize()
                                AppToastHost.showToast(message)
                            }.onFailure {
                                AppToastHost.showFailureToast(it)
                            }
                            isClearingCache = false
                        }
                    },
                    enabled = !isActionRunning,
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh, contentDescription = null)
                    Text(
                        stringResource(
                            if (isClearingCache) R.string.tts_settings_clear_cache_loading
                            else R.string.tts_settings_clear_cache_action
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showResetConfirmDialog) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text(stringResource(R.string.tts_settings_reset_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.tts_settings_reset_confirm_message,
                        stringResource(providerType.labelRes()),
                    )
                )
            },
            confirmButton = {
                OneBoxDangerButton(
                    text = stringResource(R.string.tts_settings_reset_action),
                    onClick = {
                        coroutineScope.launch {
                            showResetConfirmDialog = false
                            isResetting = true
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    ttsService.updateConfig(
                                        ttsService.createDefaultConfig(providerType)
                                    )
                                }
                            }.onSuccess {
                                AppToastHost.showToast(getString(R.string.tts_settings_reset_success))
                            }.onFailure {
                                AppToastHost.showFailureToast(it)
                            }
                            isResetting = false
                        }
                    },
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(CoreR.string.button_cancel),
                    onClick = { showResetConfirmDialog = false },
                )
            },
        )
    }
}

@Composable
private fun PreviewActionButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isPreviewing: Boolean,
    modifier: Modifier = Modifier,
) {
    GlassTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            stringResource(
                if (isPreviewing) R.string.tts_settings_preview_loading
                else R.string.tts_settings_preview_action
            )
        )
    }
}

private val VOICE_LABELS = mapOf(
    "mimo_default" to "MiMo-默认",
    "冰糖" to "冰糖 · 中文女",
    "茉莉" to "茉莉 · 中文女",
    "苏打" to "苏打 · 中文男",
    "白桦" to "白桦 · 中文男",
    "Mia" to "Mia · 英文女",
    "Chloe" to "Chloe · 英文女",
    "Milo" to "Milo · 英文男",
    "Dean" to "Dean · 英文男",
)

@Composable
private fun VoicePresetPicker(
    voices: List<String>,
    selectedVoice: String,
    onSelect: (String) -> Unit,
    action: (@Composable () -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.tts_settings_presets_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            action?.invoke()
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            voices.forEach { id ->
                val isSelected = selectedVoice == id
                GlassFilterChip(
                    selected = isSelected,
                    onClick = { onSelect(id) },
                    label = {
                        Text(
                            text = VOICE_LABELS[id] ?: id,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ProviderSelector(
    descriptors: List<TTSProviderDescriptor>,
    selected: TTSProviderType,
    onSelect: (TTSProviderType) -> Unit,
) {
    SelectorField(
        selected = selected,
        options = descriptors.map { it.type },
        optionLabel = { stringResource(it.labelRes()) },
        onSelect = onSelect,
    )
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            content()
        }
    }
}

private fun TTSProviderType.endpointPlaceholderRes(): Int = when (this) {
    TTSProviderType.MIMO -> R.string.tts_settings_mimo_endpoint_placeholder
    TTSProviderType.OPENAI_COMPATIBLE -> R.string.tts_settings_openai_endpoint_placeholder
}

private fun TTSProviderType.labelRes(): Int = when (this) {
    TTSProviderType.MIMO -> R.string.tts_settings_provider_mimo
    TTSProviderType.OPENAI_COMPATIBLE -> R.string.tts_settings_provider_openai
}

