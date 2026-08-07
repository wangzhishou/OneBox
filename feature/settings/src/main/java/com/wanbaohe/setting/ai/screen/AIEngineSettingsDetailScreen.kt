package com.wanbaohe.setting.ai.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.ClearTextFieldTrailingIcon
import com.shifenmiao.base.ui.PasswordTextField
import com.shifenmiao.base.utils.LoginUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.BottomSaveCancelBar
import com.shifenmiao.common.ui.ai.AiEngineTestDialog
import com.shifenmiao.common.ui.ai.EngineFilterChip
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.ai.openai.OpenAIModelItem
import com.shifenmiao.model.remote.AiEngineConfig
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDangerButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxLeadingIconBadge
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxListItem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.setting.ai.component.AIEngineSettingsDetailComponent
import com.wanbaohe.settings.R
import kotlinx.coroutines.launch
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.t8rin.imagetoolbox.core.resources.icons.line.LineModelTraining
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHeat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDatasetLinked
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHighQuality

@Composable
fun AIEngineSettingsDetailScreen(
    component: AIEngineSettingsDetailComponent,
) {
    val draftEngine by component.draftEngine.collectAsState()
    val modelsByProvider by component.modelsByProvider.collectAsState()
    val editingModelDraft by component.editingModelDraft.collectAsState()
    val localOwnedEngineKeys by component.localOwnedEngineKeys.collectAsState()
    val remoteModels by component.remoteModels.collectAsState()
    val isLoadingRemoteModels by component.isLoadingRemoteModels.collectAsState()
    val remoteModelsError by component.remoteModelsError.collectAsState()
    val isSaving by component.isSaving.collectAsState()
    val isDeleting by component.isDeleting.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showTestDialog by remember { mutableStateOf(false) }
    var pendingDeleteModel by remember { mutableStateOf<AiModel?>(null) }
    var pendingDeleteEngine by remember { mutableStateOf(false) }
    var showRemoteModelPicker by remember { mutableStateOf(false) }
    var showModelValidationErrors by remember { mutableStateOf(false) }
    var showModelExitConfirmDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = component.hasDraftChanged()

    BackHandler(enabled = hasUnsavedChanges) {
        showExitConfirmDialog = true
    }

    if (showTestDialog && draftEngine != null) {
        AiEngineTestDialog(
            aiEngine = draftEngine,
            onDismiss = { showTestDialog = false },
            onApiTestSuccess = {
                component.markTestPassed()
                showTestDialog = false
                coroutineScope.launch {
                    AppToastHost.showToast(
                        getString(CoreR.string.connection_successful)
                    )
                }
            }
        )
    }

    if (editingModelDraft != null) {
        val editing = editingModelDraft!!
        val trimmedName = editing.name.trim()
        val trimmedTitle = editing.title.trim()
        val isNameError = showModelValidationErrors && trimmedName.isBlank()
        val isTitleError = showModelValidationErrors && trimmedTitle.isBlank()
        val hasValidationErrors = trimmedName.isBlank() || trimmedTitle.isBlank()
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = {
                if (component.hasEditingModelChanged()) {
                    showModelExitConfirmDialog = true
                } else {
                    component.dismissModelEditor()
                }
            },
            title = {
                Text(
                    stringResource(
                        if (editing.id > 0) R.string.ai_engine_model_edit_dialog_title
                        else R.string.ai_engine_model_dialog_title
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ai_engine_dialog_required_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    OneBoxOutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = editing.name,
                        onValueChange = { value -> component.updateEditingModel { it.copy(name = value) } },
                        label = { Text(stringResource(R.string.ai_engine_model_name)) },
                        trailingIcon = {
                            ClearTextFieldTrailingIcon(
                                value = editing.name,
                                onClear = { component.updateEditingModel { it.copy(name = "") } },
                            )
                        },
                        isError = isNameError,
                        supportingText = {
                            if (isNameError) {
                                Text(stringResource(CoreR.string.required_field))
                            }
                        },
                    )
                    OneBoxOutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = editing.title,
                        onValueChange = { value -> component.updateEditingModel { it.copy(title = value) } },
                        label = { Text(stringResource(R.string.ai_engine_model_display_name)) },
                        trailingIcon = {
                            ClearTextFieldTrailingIcon(
                                value = editing.title,
                                onClear = { component.updateEditingModel { it.copy(title = "") } },
                            )
                        },
                        isError = isTitleError,
                        supportingText = {
                            if (isTitleError) {
                                Text(stringResource(CoreR.string.required_field))
                            }
                        },
                    )
                    OneBoxOutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = editing.description,
                        onValueChange = { value ->
                            component.updateEditingModel {
                                it.copy(
                                    description = value
                                )
                            }
                        },
                        label = { Text(stringResource(R.string.ai_engine_model_description)) },
                        trailingIcon = {
                            ClearTextFieldTrailingIcon(
                                value = editing.description,
                                onClear = {
                                    component.updateEditingModel {
                                        it.copy(description = "")
                                    }
                                },
                            )
                        },
                    )
                }
            },
            confirmButton = {
                OnePrimaryButton(
                    text = stringResource(R.string.ai_engine_save_action),
                    onClick = {
                        showModelValidationErrors = true
                        if (hasValidationErrors) {
                            coroutineScope.launch {
                                AppToastHost.showFailureToast(
                                    getString(R.string.ai_engine_dialog_validation_failed)
                                )
                            }
                            return@OnePrimaryButton
                        }
                        component.persistModelDraft { success ->
                            coroutineScope.launch {
                                if (success) {
                                    showModelValidationErrors = false
                                    AppToastHost.showToast(getString(R.string.ai_engine_model_add_success))
                                } else {
                                    AppToastHost.showFailureToast(getString(R.string.ai_engine_model_add_failed))
                                }
                            }
                        }
                    },
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(CoreR.string.button_cancel),
                    onClick = {
                        showModelValidationErrors = false
                        if (component.hasEditingModelChanged()) {
                            showModelExitConfirmDialog = true
                        } else {
                            component.dismissModelEditor()
                        }
                    },
                )
            },
        )
    }

    BaseScreen(
        title = stringResource(R.string.ai_engine_detail_title),
        onGoBack = {
            if (hasUnsavedChanges) showExitConfirmDialog = true
            else component.onGoBack()
        },
        actions = {
            IconButton(onClick = component::beginAddLocalModel) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.ai_engine_add_model),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
    ) {
        val engine = draftEngine
        if (engine == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                    )
                    Text(
                        text = stringResource(R.string.ai_engine_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = OneBoxDesignSystem.screenPadding),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
            ) {
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))

                EngineInfoHeader(engine = engine)

                // 渠道能力(如 Google 全量放开)或远程开关开放时, 展示服务器/Token 设置
                val capabilities = remember { AiEngineConfig.getCapabilities() }
                if (capabilities.canEditToken || RemoteConfigStorage.getRemoteConfig().canSetAiToken == true) {
                    val canEditApiSettings = capabilities.canEditUrl || engine.apiCanSet || LoginUtils.isAdmin()
                    ServerConnectivityCard(
                        engine = engine,
                        canEditApiSettings = canEditApiSettings,
                        onProtocolChange = component::updateRequestProtocol,
                        onAuthTypeChange = component::updateAuthType,
                        onUrlChange = component::updateRequestUrl,
                        onPathChange = component::updateRequestPath,
                        onTokenChange = component::updateAuthorizationCode,
                        onTestClick = {
                            val canTestDirectly = draftEngine?.let {
                                it.authType == AuthType.NONE ||
                                        it.requestProtocol == AiRequestProtocol.OWN_PROXY ||
                                        it.authorizationCode.isNotBlank()
                            } == true
                            if (canTestDirectly) {
                                showTestDialog = true
                            } else {
                                coroutineScope.launch {
                                    AppToastHost.showFailureToast(
                                        getString(R.string.ai_engine_test_button_disabled_hint)
                                    )
                                }
                            }
                        },
                    )
                }

                ModelSelectionCard(
                    engine = engine,
                    models = modelsByProvider[engine.name.lowercase()].orEmpty(),
                    onModelSelected = component::updateModel,
                    onModelEdit = component::beginEditModel,
                    onModelDelete = { pendingDeleteModel = it },
                    onAddModel = component::beginAddLocalModel,
                    onLoadRemoteModels = {
                        component.loadRemoteModels { success ->
                            if (success) {
                                showRemoteModelPicker = true
                            } else {
                                coroutineScope.launch {
                                    AppToastHost.showFailureToast(
                                        remoteModelsError
                                            ?: getString(R.string.ai_engine_remote_models_load_failed)
                                    )
                                }
                            }
                        }
                    },
                )

                ParameterCard(
                    engine = engine,
                    cloudConnections = component.cloudConnections,
                    onTemperatureChange = component::updateTemperature,
                    onTopPChange = component::updateTopP,
                    onMaxTokensChange = component::updateMaxTokens,
                    onCanUploadFileChange = component::updateModelCanUploadFile,
                    onCanNetworkChange = component::updateModelCanNetwork,
                    onCanReasoningChange = component::updateModelCanReasoning,
                    onCanImageChange = component::updateModelCanImage,
                    onIsFastChange = component::updateModelIsFast,
                    onIsCodeChange = component::updateModelIsCode,
                    onSupportToolCallsChange = component::updateModelSupportToolCalls,
                    onStreamChange = component::updateStream,
                    onFileUploadStrategyChange = component::updateFileUploadStrategy,
                    onCloudStorageConnectionIdChange = component::updateCloudStorageConnectionId,
                    onCloudStorageBucketChange = component::updateCloudStorageBucket,
                    onCloudStoragePrefixChange = component::updateCloudStoragePrefix,
                    onResetRemoteModelOverrides = {
                        component.resetRemoteModelOverrides { success ->
                            coroutineScope.launch {
                                if (success) {
                                    AppToastHost.showToast(
                                        getString(R.string.ai_engine_model_restore_defaults_success)
                                    )
                                } else {
                                    AppToastHost.showFailureToast(
                                        getString(R.string.ai_engine_model_restore_defaults_failed)
                                    )
                                }
                            }
                        }
                    },
                    onNavigateToCloudStorage = {
                        component.onNavigate(Screen.CloudStorage())
                    },
                )

                if (localOwnedEngineKeys.contains(engine.identityKey())) {
                    Spacer(modifier = Modifier.height(24.dp))
                    OneBoxDangerButton(
                        text = stringResource(R.string.ai_engine_delete_action),
                        onClick = { pendingDeleteEngine = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        BottomSaveCancelBar(
            modifier = Modifier.imePadding(),
            cancelEnabled = true,
            saveEnabled = draftEngine != null && !isSaving,
            saveText = stringResource(R.string.ai_engine_save_action),
            onCancel = {
                if (hasUnsavedChanges) showExitConfirmDialog = true
                else component.onGoBack()
            },
            onSave = {
                val engine = draftEngine ?: return@BottomSaveCancelBar
                val updatedEngine = engine.copy(
                    isUrlError = !StringUtils.isValidUrl(engine.requestUrl)
                )
                if (!StringUtils.isValidUrl(updatedEngine.requestUrl) || updatedEngine.requestPath.isBlank()) {
                    component.updateUrlValidation(!StringUtils.isValidUrl(updatedEngine.requestUrl))
                    coroutineScope.launch {
                        AppToastHost.showFailureToast(
                            getString(R.string.ai_engine_invalid_server)
                        )
                    }
                    return@BottomSaveCancelBar
                }
                component.persistDraft { success ->
                    coroutineScope.launch {
                        if (success) {
                            AppToastHost.showToast(
                                getString(R.string.ai_engine_save_success)
                            )
                            component.onGoBack()
                        } else {
                            AppToastHost.showFailureToast(
                                getString(R.string.ai_engine_save_failed)
                            )
                        }
                    }
                }
            }
        )
    }

    if (showExitConfirmDialog) {
        ExitWithoutSavingDialog(
            title = stringResource(R.string.ai_engine_discard_title),
            text = stringResource(R.string.ai_engine_discard_message),
            onExit = {
                showExitConfirmDialog = false
                component.restoreDraft()
                component.onGoBack()
            },
            onDismiss = { showExitConfirmDialog = false },
            visible = showExitConfirmDialog
        )
    }

    pendingDeleteModel?.let { model ->
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { pendingDeleteModel = null },
            title = { Text(stringResource(R.string.ai_engine_model_delete_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.ai_engine_model_delete_confirm_message,
                        model.title.ifBlank { model.name })
                )
            },
            confirmButton = {
                OneBoxDangerButton(
                    text = stringResource(R.string.ai_engine_delete_action),
                    enabled = !isDeleting,
                    onClick = {
                        component.deleteLocalModel(model) { success ->
                            coroutineScope.launch {
                                if (success) {
                                    AppToastHost.showToast(
                                        getString(
                                            R.string.ai_engine_model_delete_success,
                                            model.title.ifBlank { model.name },
                                        )
                                    )
                                    pendingDeleteModel = null
                                } else {
                                    AppToastHost.showFailureToast(
                                        getString(R.string.ai_engine_model_delete_failed)
                                    )
                                }
                            }
                        }
                    },
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(CoreR.string.button_cancel),
                    onClick = { pendingDeleteModel = null },
                )
            },
        )
    }

    if (pendingDeleteEngine && draftEngine != null) {
        val engine = draftEngine!!
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { pendingDeleteEngine = false },
            title = { Text(stringResource(R.string.ai_engine_delete_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.ai_engine_delete_confirm_message,
                        engine.title.ifBlank { engine.name })
                )
            },
            confirmButton = {
                OneBoxDangerButton(
                    text = stringResource(R.string.ai_engine_delete_action),
                    enabled = !isDeleting,
                    onClick = {
                        component.deleteLocalEngine { success ->
                            coroutineScope.launch {
                                if (success) {
                                    AppToastHost.showToast(
                                        getString(
                                            R.string.ai_engine_delete_success,
                                            engine.title.ifBlank { engine.name },
                                        )
                                    )
                                    pendingDeleteEngine = false
                                    component.onGoBack()
                                } else {
                                    AppToastHost.showFailureToast(
                                        getString(R.string.ai_engine_delete_failed)
                                    )
                                }
                            }
                        }
                    },
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(CoreR.string.button_cancel),
                    onClick = { pendingDeleteEngine = false },
                )
            },
        )
    }

    if (showRemoteModelPicker) {
        RemoteModelPickerDialog(
            models = remoteModels,
            isLoading = isLoadingRemoteModels,
            error = remoteModelsError,
            onDismiss = {
                showRemoteModelPicker = false
                component.clearRemoteModels()
            },
            onRefresh = {
                component.loadRemoteModels { }
            },
            onSubmit = { selectedIds ->
                component.submitSelectedModels(selectedIds) { success ->
                    coroutineScope.launch {
                        if (success) {
                            AppToastHost.showToast(getString(R.string.ai_engine_remote_models_submit_success))
                            showRemoteModelPicker = false
                            component.clearRemoteModels()
                        } else {
                            AppToastHost.showFailureToast(getString(R.string.ai_engine_remote_models_submit_failed))
                        }
                    }
                }
            }
        )
    }

    if (showModelExitConfirmDialog) {
        ExitWithoutSavingDialog(
            title = stringResource(R.string.ai_engine_discard_title),
            text = stringResource(R.string.ai_engine_discard_message),
            onExit = {
                showModelExitConfirmDialog = false
                showModelValidationErrors = false
                component.dismissModelEditor()
            },
            onDismiss = { showModelExitConfirmDialog = false },
            visible = showModelExitConfirmDialog,
        )
    }
}

@Composable
private fun RemoteModelPickerDialog(
    models: List<OpenAIModelItem>,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onSubmit: (List<String>) -> Unit,
) {
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ai_engine_remote_models_picker_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.ai_engine_remote_models_loading),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    error != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OneSecondaryButton(
                                text = stringResource(R.string.ai_engine_remote_models_retry),
                                onClick = onRefresh
                            )
                        }
                    }

                    models.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.ai_engine_remote_models_empty),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            models.forEach { model ->
                                val isSelected = model.id in selectedIds
                                OneBoxListItem(
                                    headlineContent = {
                                        Text(
                                            text = model.id,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    subtitle = {
                                        Text(
                                            text = "${model.ownedBy} · ${model.obj}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    contained = true,
                                    trailingContent = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier.clickable {
                                        selectedIds = if (isSelected) {
                                            selectedIds - model.id
                                        } else {
                                            selectedIds + model.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OnePrimaryButton(
                text = stringResource(
                    R.string.ai_engine_remote_models_submit_count,
                    selectedIds.size
                ),
                onClick = { onSubmit(selectedIds.toList()) },
                enabled = selectedIds.isNotEmpty() && !isLoading
            )
        },
        dismissButton = {
            OneSecondaryButton(
                text = stringResource(CoreR.string.button_cancel),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun EngineInfoHeader(engine: AiEngine) {
    OneBoxSectionCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
        ) {
            Text(
                text = engine.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = engine.description.ifBlank { stringResource(R.string.ai_engine_detail_desc_default) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ServerConnectivityCard(
    engine: AiEngine,
    canEditApiSettings: Boolean,
    onProtocolChange: (AiRequestProtocol) -> Unit,
    onAuthTypeChange: (AuthType) -> Unit,
    onUrlChange: (String) -> Unit,
    onPathChange: (String) -> Unit,
    onTokenChange: (String) -> Unit,
    onTestClick: () -> Unit,
) {
    SettingCard(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDatasetLinked,
        title = stringResource(R.string.ai_engine_server_title),
        description = stringResource(R.string.ai_engine_server_desc),
    ) {
        Text(
            text = stringResource(R.string.ai_engine_protocol_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AiRequestProtocol.cloudProtocols) { protocol ->
                EngineFilterChip(
                    text = when (protocol) {
                        AiRequestProtocol.OPENAI_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_openai)
                        AiRequestProtocol.RESPONSES_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_responses)
                        AiRequestProtocol.ANTHROPIC_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_anthropic)
                        AiRequestProtocol.OWN_PROXY -> stringResource(R.string.ai_engine_protocol_proxy)
                        // 仅云端协议出现在本选择器；
                        // LOCAL_ON_DEVICE 由独立的"本地模型管理"页处理（Phase 2）。
                        AiRequestProtocol.LOCAL_ON_DEVICE -> stringResource(R.string.ai_engine_protocol_local_on_device)
                    },
                    isSelected = engine.requestProtocol == protocol,
                    onClick = { onProtocolChange(protocol) }
                )
            }
        }

        Text(
            text = stringResource(R.string.ai_engine_auth_type_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AuthType.entries) { authType ->
                EngineFilterChip(
                    text = authTypeLabel(authType),
                    isSelected = engine.authType == authType,
                    onClick = { onAuthTypeChange(authType) },
                )
            }
        }

        OneBoxOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = engine.requestUrl,
            onValueChange = if (canEditApiSettings) onUrlChange else {
                {}
            },
            label = { Text(text = stringResource(R.string.ai_engine_api_url)) },
            trailingIcon = {
                if (canEditApiSettings) {
                    ClearTextFieldTrailingIcon(
                        value = engine.requestUrl,
                        onClear = { onUrlChange("") },
                    )
                }
            },
            singleLine = true,
            isError = engine.isUrlError,
            readOnly = !canEditApiSettings,
            supportingText = {
                if (engine.isUrlError) {
                    Text(text = stringResource(CoreR.string.url_error_tips))
                }
            },
        )

        OneBoxOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = engine.requestPath,
            onValueChange = if (canEditApiSettings) onPathChange else {
                {}
            },
            label = { Text(text = stringResource(R.string.ai_engine_api_path)) },
            trailingIcon = {
                if (canEditApiSettings) {
                    ClearTextFieldTrailingIcon(
                        value = engine.requestPath,
                        onClear = { onPathChange("") },
                    )
                }
            },
            singleLine = true,
            readOnly = !canEditApiSettings,
        )

        PasswordTextField(
            modifier = Modifier.fillMaxWidth(),
            value = engine.authorizationCode,
            onValueChange = onTokenChange,
            label = stringResource(R.string.ai_engine_auth_token),
            onClearValue = { onTokenChange("") },
            imeAction = ImeAction.Done,
        )

        val canTest = engine.authType == AuthType.NONE ||
                engine.requestProtocol == AiRequestProtocol.OWN_PROXY ||
                engine.authorizationCode.isNotBlank()

        OneSecondaryButton(
            text = stringResource(CoreR.string.test_connection),
            onClick = onTestClick,
            enabled = canTest,
        )
        if (!canTest) {
            Text(
                text = stringResource(R.string.ai_engine_test_button_disabled_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun authTypeLabel(authType: AuthType): String {
    return when (authType) {
        AuthType.BEARER -> stringResource(R.string.ai_engine_auth_type_bearer)
        AuthType.API_KEY -> stringResource(R.string.ai_engine_auth_type_api_key)
        AuthType.NONE -> stringResource(R.string.ai_engine_auth_type_none)
    }
}

@Composable
private fun ModelSelectionCard(
    engine: AiEngine,
    models: List<AiModel>,
    onModelSelected: (AiModel) -> Unit,
    onModelEdit: (AiModel) -> Unit,
    onModelDelete: (AiModel) -> Unit,
    onAddModel: () -> Unit,
    onLoadRemoteModels: () -> Unit = {},
) {
    val loginState = LocalLoginState.current
    // 渠道能力(如 Google 全量放开)或高等级用户可从服务商拉取模型列表
    val canLoadRemote = remember { AiEngineConfig.getCapabilities() }.canLoadRemoteModels ||
            loginState.vipLevel == 10
    SettingCard(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineModelTraining,
        title = stringResource(R.string.ai_engine_model_title),
        description = stringResource(R.string.ai_engine_model_desc),
    ) {
        if (models.isEmpty()) {
            Text(
                text = stringResource(R.string.ai_engine_model_no_items),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OneSecondaryButton(
                text = stringResource(R.string.ai_engine_add_model),
                onClick = onAddModel
            )
            if (canLoadRemote) {
                Spacer(modifier = Modifier.height(8.dp))
                OneSecondaryButton(
                    text = stringResource(R.string.ai_engine_load_from_provider),
                    onClick = onLoadRemoteModels
                )
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
                items(models, key = { it.id }) { model ->
                    EngineFilterChip(
                        text = model.title.ifBlank { model.name },
                        isSelected = model.id == engine.model.id,
                        onClick = { onModelSelected(model) },
                    )
                }
            }

            val selectedModel = models.firstOrNull { it.id == engine.model.id } ?: engine.model
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.ai_engine_model_selected_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline,
            )
            OneBoxListItem(
                headlineContent = {
                    Text(
                        text = selectedModel.title.ifBlank { selectedModel.name },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                },
                subtitle = {
                    Text(
                        text = if (selectedModel.canEdit) {
                            stringResource(R.string.ai_engine_model_local_hint)
                        } else {
                            stringResource(R.string.ai_engine_model_remote_hint)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                contained = true,
                trailingContent = {
                    if (selectedModel.canEdit) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onModelEdit(selectedModel) }) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                                    contentDescription = stringResource(R.string.ai_engine_edit_action),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            IconButton(onClick = { onModelDelete(selectedModel) }) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.ai_engine_delete_action),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            )
            if (canLoadRemote) {
                Spacer(modifier = Modifier.height(8.dp))
                OneSecondaryButton(
                    text = stringResource(R.string.ai_engine_load_from_provider),
                    onClick = onLoadRemoteModels
                )
            }
        }
    }
}

@Composable
private fun ParameterCard(
    engine: AiEngine,
    cloudConnections: List<com.wanbaohe.cloud.storage.model.CloudStorageConnection>,
    onTemperatureChange: (Float) -> Unit,
    onTopPChange: (Float) -> Unit,
    onMaxTokensChange: (Int) -> Unit,
    onCanUploadFileChange: (Boolean) -> Unit,
    onCanNetworkChange: (Boolean) -> Unit,
    onCanReasoningChange: (Boolean) -> Unit,
    onCanImageChange: (Boolean) -> Unit,
    onIsFastChange: (Boolean) -> Unit,
    onIsCodeChange: (Boolean) -> Unit,
    onSupportToolCallsChange: (Boolean) -> Unit,
    onStreamChange: (Boolean) -> Unit,
    onFileUploadStrategyChange: (com.shifenmiao.model.ai.FileUploadStrategy) -> Unit,
    onCloudStorageConnectionIdChange: (String?) -> Unit,
    onCloudStorageBucketChange: (String?) -> Unit,
    onCloudStoragePrefixChange: (String) -> Unit,
    onResetRemoteModelOverrides: () -> Unit,
    onNavigateToCloudStorage: () -> Unit = {},
) {
    SettingCard(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTune,
        title = stringResource(R.string.ai_engine_parameters_title),
        description = stringResource(R.string.ai_engine_parameters_desc),
    ) {
        ParameterSliderRow(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHeat,
            title = stringResource(R.string.ai_engine_temperature),
            description = stringResource(R.string.ai_engine_temperature_desc),
            value = engine.model.temperature.toFloat(),
            onValueChange = onTemperatureChange,
        )

        ParameterSliderRow(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHighQuality,
            title = stringResource(R.string.ai_engine_top_p),
            description = stringResource(R.string.ai_engine_top_p_desc),
            value = engine.model.topP.toFloat(),
            onValueChange = onTopPChange,
        )

        ParameterSliderRow(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTune,
            title = stringResource(R.string.ai_engine_max_tokens),
            description = stringResource(R.string.ai_engine_max_tokens_desc),
            value = (engine.model.maxTokens.coerceAtMost(8192) / 8192f),
            onValueChange = { onMaxTokensChange((it * 8192).toInt().coerceAtLeast(256)) },
            displayText = "${engine.model.maxTokens.coerceAtMost(8192)}",
        )

        CapabilitySwitchRow(
            stringResource(R.string.ai_engine_capability_upload),
            engine.model.canUploadFile,
            onCanUploadFileChange
        )
        CapabilitySwitchRow(
            stringResource(R.string.ai_engine_capability_network),
            engine.model.canNetwork,
            onCanNetworkChange
        )
        CapabilitySwitchRow(
            stringResource(R.string.ai_engine_capability_reasoning),
            engine.model.canReasoning,
            onCanReasoningChange
        )
        CapabilitySwitchRow(
            stringResource(R.string.ai_engine_capability_image),
            engine.model.canImage,
            onCanImageChange
        )
        CapabilitySwitchRow(
            stringResource(R.string.ai_engine_capability_tools),
            engine.model.supportToolCalls,
            onSupportToolCallsChange
        )

        Text(
            text = stringResource(R.string.ai_engine_file_upload_strategy_title),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        val strategies = com.shifenmiao.model.ai.FileUploadStrategy.entries
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(strategies) { strategy ->
                EngineFilterChip(
                    text = when (strategy) {
                        com.shifenmiao.model.ai.FileUploadStrategy.BASE64 -> stringResource(R.string.ai_engine_file_upload_strategy_base64)
                        com.shifenmiao.model.ai.FileUploadStrategy.CLOUD -> stringResource(R.string.ai_engine_file_upload_strategy_cloud)
                    },
                    isSelected = engine.fileUploadStrategy == strategy,
                    onClick = { onFileUploadStrategyChange(strategy) }
                )
            }
        }

        AnimatedVisibility(visible = engine.fileUploadStrategy == com.shifenmiao.model.ai.FileUploadStrategy.CLOUD) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (cloudConnections.isEmpty()) {
                    Text(
                        text = stringResource(R.string.ai_engine_cloud_storage_no_connection),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OneSecondaryButton(
                        text = stringResource(R.string.ai_engine_cloud_storage_go_config),
                        onClick = {
                            onNavigateToCloudStorage()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = stringResource(R.string.ai_engine_cloud_storage_connection_label),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cloudConnections) { connection ->
                            val bucket = when (connection) {
                                is com.wanbaohe.cloud.storage.model.CloudStorageConnection.S3Compat -> connection.bucket
                                is com.wanbaohe.cloud.storage.model.CloudStorageConnection.WebDav -> connection.rootPath
                                is com.wanbaohe.cloud.storage.model.CloudStorageConnection.Smb -> connection.share
                            }
                            EngineFilterChip(
                                text = "${connection.displayName} (${bucket})",
                                isSelected = engine.cloudStorageConnectionId == connection.id,
                                onClick = {
                                    onCloudStorageConnectionIdChange(connection.id)
                                    onCloudStorageBucketChange(bucket)
                                }
                            )
                        }
                    }

                    OneBoxOutlinedTextField(
                        value = engine.cloudStorageBucket.orEmpty(),
                        onValueChange = { onCloudStorageBucketChange(it) },
                        label = { Text(stringResource(R.string.ai_engine_cloud_storage_bucket_label)) },
                        singleLine = true,
                        trailingIcon = {
                            ClearTextFieldTrailingIcon(
                                value = engine.cloudStorageBucket.orEmpty(),
                                onClear = { onCloudStorageBucketChange("") },
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OneBoxOutlinedTextField(
                        value = engine.cloudStoragePrefix,
                        onValueChange = { onCloudStoragePrefixChange(it) },
                        label = { Text(stringResource(R.string.ai_engine_cloud_storage_prefix_label)) },
                        singleLine = true,
                        trailingIcon = {
                            ClearTextFieldTrailingIcon(
                                value = engine.cloudStoragePrefix,
                                onClear = { onCloudStoragePrefixChange("") },
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OneSecondaryButton(
                        text = stringResource(R.string.ai_engine_cloud_storage_go_config),
                        onClick = {
                            onNavigateToCloudStorage()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }



        if (!engine.model.canEdit) {
            OneSecondaryButton(
                text = stringResource(R.string.ai_engine_model_restore_defaults_action),
                onClick = onResetRemoteModelOverrides,
                enabled = engine.model.hasLocalOverrides,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.ai_engine_model_restore_defaults_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun CapabilitySwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    OneBoxListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        contained = true,
        trailingContent = {
            GlassSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = AppTheme.colors.switchColors()
            )
        }
    )
}

@Composable
private fun ParameterSliderRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    displayText: String? = null,
) {
    OneBoxSectionCard(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(OneBoxDesignSystem.cardPadding),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .glassBackground(
                        style = GlassStyle.Thin,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = OneBoxDesignSystem.pillShape,
                        borderWidth = 0.dp,
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = displayText ?: String.format(
                        LocalLocale.current.platformLocale,
                        "%.1f",
                        value
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        GlassCustomSlider(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
        )
    }
}

@Composable
private fun SettingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    OneBoxSectionCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            ) {
                OneBoxLeadingIconBadge(icon = icon)

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            content()
        }
    }
}
