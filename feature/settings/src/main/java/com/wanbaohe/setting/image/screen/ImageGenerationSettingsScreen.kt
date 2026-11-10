package com.wanbaohe.setting.image.screen

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import com.shifenmiao.imagegeneration.model.ImageProviderDescriptor
import com.shifenmiao.imagegeneration.service.ImageGenerationManager
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibilityOff
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.core.ui.widget.system.SelectorField
import com.wanbaohe.settings.R
import com.shifenmiao.core.R as CoreR

@Composable
fun ImageGenerationSettingsScreen(
    manager: ImageGenerationManager,
    onGoBack: () -> Unit,
) {
    BaseScreen(
        title = stringResource(CoreR.string.profile_item_image_generation_settings),
        onGoBack = onGoBack,
    ) {
        ImageGenerationSettingsContent(
            manager = manager,
            // 限定高度为顶栏以下的剩余空间,否则滚动视口会延伸到屏幕外
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ImageGenerationSettingsContent(
    manager: ImageGenerationManager,
    modifier: Modifier = Modifier,
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            // imePadding 必须在 verticalScroll 之前:让滚动视口整体收缩到键盘上方,
            // 放在 scroll 之后只会给内容加底部 padding,视口仍被键盘遮挡
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsSection(stringResource(R.string.image_settings_configs_section)) {
            val selectedConfig = configs.firstOrNull { it.id == selectedId }
            SelectorField(
                selected = selectedConfig,
                options = configs,
                optionLabel = { config ->
                    if (config == null) {
                        stringResource(R.string.image_settings_no_config)
                    } else {
                        val providerName = descriptors.firstOrNull {
                            it.providerId == config.providerId
                        }?.displayName.orEmpty()
                        val displayLabel = if (providerName.isEmpty() || config.displayName == providerName) {
                            config.displayName
                        } else {
                            "${config.displayName} · $providerName"
                        }
                        if (config.id == activeConfig?.id) {
                            "$displayLabel · ${stringResource(R.string.image_settings_active_badge)}"
                        } else {
                            displayLabel
                        }
                    }
                },
                onSelect = { config -> config?.let { selectedId = it.id } },
                menuFooter = { dismiss ->
                    descriptors.forEach { descriptor ->
                        DropdownMenuItem(
                            text = { Text(descriptor.displayName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                dismiss()
                                val config = manager.createDefaultConfig(descriptor.providerId)
                                manager.saveConfig(config, makeActive = configs.isEmpty())
                                selectedId = config.id
                            },
                        )
                    }
                },
            )
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
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
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
            color = MaterialTheme.colorScheme.onPrimaryContainer,
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
            GlassSwitch(
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
                IconButton(onClick = { tokenVisible = !tokenVisible }) {
                    Icon(
                        imageVector = if (tokenVisible) {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility
                        } else {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibilityOff
                        },
                        contentDescription = stringResource(
                            if (tokenVisible) R.string.image_settings_hide_token
                            else R.string.image_settings_show_token
                        ),
                    )
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
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            descriptor.availableModels.forEach { model ->
                GlassFilterChip(
                    selected = draft.model == model,
                    onClick = { draft = draft.copy(model = model) },
                    label = {
                        Text(
                            text = model,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    colors = filterChipColors(),
                )
            }
        }
    }

    // 后端代理由 App 内置使用，不对用户开放配置。

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GlassTonalButton(
            onClick = { onSave(draft) },
            enabled = draft.displayName.isNotBlank() && draft.model.isNotBlank(),
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.image_settings_save_action))
        }
        GlassTonalButton(
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
                    color = MaterialTheme.colorScheme.onErrorContainer,
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

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
)
