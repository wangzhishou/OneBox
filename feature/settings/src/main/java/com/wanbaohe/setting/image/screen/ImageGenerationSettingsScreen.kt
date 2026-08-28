package com.wanbaohe.setting.image.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import com.shifenmiao.imagegeneration.model.ImageProviderDescriptor
import com.shifenmiao.imagegeneration.service.ImageGenerationManager
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.settings.R
import com.shifenmiao.core.R as CoreR

@Composable
fun ImageGenerationSettingsScreen(
    manager: ImageGenerationManager,
    onGoBack: () -> Unit,
) {
    val configs by manager.observeConfigs().collectAsState(initial = manager.getConfigs())
    val activeConfig by manager.observeActiveConfig().collectAsState(initial = manager.getActiveConfig())
    val descriptors = remember { manager.getProviderDescriptors() }
    var selectedId by remember { mutableStateOf(activeConfig?.id ?: configs.firstOrNull()?.id) }

    LaunchedEffect(configs, activeConfig?.id) {
        if (configs.none { it.id == selectedId }) {
            selectedId = activeConfig?.id ?: configs.firstOrNull()?.id
        }
    }

    BaseScreen(
        title = stringResource(CoreR.string.profile_item_image_generation_settings),
        onGoBack = onGoBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSection(stringResource(R.string.image_settings_configs_section)) {
                if (configs.isEmpty()) {
                    Text(
                        text = stringResource(R.string.image_settings_no_config),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                configs.forEach { config ->
                    ConfigItem(
                        config = config,
                        descriptor = descriptors.firstOrNull { it.providerId == config.providerId },
                        selected = config.id == selectedId,
                        active = config.id == activeConfig?.id,
                        onClick = { selectedId = config.id },
                    )
                }
                Text(
                    text = stringResource(R.string.image_settings_add_provider),
                    style = MaterialTheme.typography.labelLarge,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    descriptors.forEach { descriptor ->
                        TextButton(
                            onClick = {
                                val config = manager.createDefaultConfig(descriptor.providerId)
                                manager.saveConfig(config, makeActive = configs.isEmpty())
                                selectedId = config.id
                            }
                        ) {
                            Text("+ ${descriptor.displayName}")
                        }
                    }
                }
            }

            val selected = configs.firstOrNull { it.id == selectedId }
            if (selected != null) {
                val descriptor = descriptors.firstOrNull { it.providerId == selected.providerId }
                if (descriptor != null) {
                    ConfigEditor(
                        savedConfig = selected,
                        descriptor = descriptor,
                        isActive = selected.id == activeConfig?.id,
                        onSave = { draft ->
                            manager.saveConfig(draft)
                            AppToastHost.showToast(R.string.image_settings_save_success)
                        },
                        onActivate = { draft ->
                            manager.saveConfig(draft, makeActive = true)
                            AppToastHost.showToast(R.string.image_settings_active_success)
                        },
                        onDelete = {
                            manager.deleteConfig(selected.id)
                            AppToastHost.showToast(R.string.image_settings_delete_success)
                        },
                    )
                } else {
                    SettingsSection(stringResource(R.string.image_settings_detail_section)) {
                        Text(stringResource(R.string.image_settings_provider_unavailable, selected.providerId))
                        TextButton(onClick = { manager.deleteConfig(selected.id) }) {
                            Text(
                                text = stringResource(R.string.image_settings_delete_action),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigItem(
    config: ImageProviderConfig,
    descriptor: ImageProviderDescriptor?,
    selected: Boolean,
    active: Boolean,
    onClick: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        style = GlassStyle.Regular,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column(modifier = Modifier.weight(1f)) {
                Text(config.displayName, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = descriptor?.displayName ?: config.providerId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (active) {
                Text(
                    text = stringResource(R.string.image_settings_active_badge),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ConfigEditor(
    savedConfig: ImageProviderConfig,
    descriptor: ImageProviderDescriptor,
    isActive: Boolean,
    onSave: (ImageProviderConfig) -> Unit,
    onActivate: (ImageProviderConfig) -> Unit,
    onDelete: () -> Unit,
) {
    var draft by remember(savedConfig) { mutableStateOf(savedConfig) }
    var tokenVisible by remember(savedConfig.id) { mutableStateOf(false) }

    SettingsSection(stringResource(R.string.image_settings_detail_section)) {
        Text(
            text = descriptor.displayName,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = buildList {
                if (descriptor.supportsGeneration) add(stringResource(R.string.image_settings_capability_generation))
                if (descriptor.supportsEditing) add(stringResource(R.string.image_settings_capability_editing))
            }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        GlassOutlinedTextField(
            value = draft.displayName,
            onValueChange = { draft = draft.copy(displayName = it) },
            label = { Text(stringResource(R.string.image_settings_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.image_settings_enabled_label), modifier = Modifier.weight(1f))
            Switch(
                checked = draft.enabled,
                onCheckedChange = { draft = draft.copy(enabled = it) },
            )
        }
    }

    SettingsSection(stringResource(R.string.image_settings_direct_section)) {
        GlassOutlinedTextField(
            value = draft.baseUrl,
            onValueChange = { draft = draft.copy(baseUrl = it) },
            label = { Text(stringResource(R.string.image_settings_base_url_label)) },
            placeholder = { Text(descriptor.defaultBaseUrl) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        GlassOutlinedTextField(
            value = draft.apiToken,
            onValueChange = { draft = draft.copy(apiToken = it) },
            label = { Text(stringResource(R.string.image_settings_token_label)) },
            visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { tokenVisible = !tokenVisible }) {
                    Text(stringResource(if (tokenVisible) R.string.image_settings_hide_token else R.string.image_settings_show_token))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Text(
            text = stringResource(R.string.image_settings_route_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    SettingsSection(stringResource(R.string.image_settings_model_section)) {
        GlassOutlinedTextField(
            value = draft.model,
            onValueChange = { draft = draft.copy(model = it) },
            label = { Text(stringResource(R.string.image_settings_model_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            descriptor.availableModels.forEach { model ->
                FilterChip(
                    selected = draft.model == model,
                    onClick = { draft = draft.copy(model = model) },
                    label = { Text(model) },
                )
            }
        }
    }

    // 后端代理由 App 内置使用，不对用户开放配置。

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { onSave(draft) },
            enabled = draft.displayName.isNotBlank() && draft.model.isNotBlank(),
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.image_settings_save_action))
        }
        Button(
            onClick = { onActivate(draft) },
            enabled = !isActive && draft.enabled && draft.displayName.isNotBlank() && draft.model.isNotBlank(),
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.image_settings_set_active_action))
        }
    }
    if (savedConfig.id != ImageGenerationManager.DEFAULT_CONFIG_ID) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onDelete) {
                Text(
                    text = stringResource(R.string.image_settings_delete_action),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            style = GlassStyle.Regular,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        }
    }
}
