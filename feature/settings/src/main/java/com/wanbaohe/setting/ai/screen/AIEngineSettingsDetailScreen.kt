package com.wanbaohe.setting.ai.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.wanbaohe.base.ui.ClearTextFieldTrailingIcon
import com.wanbaohe.base.ui.PasswordTextField
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
import com.shifenmiao.model.remote.AiEngineConfig
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.provider.LocalLoginState
import com.wanbaohe.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.glass.GlassCustomSlider
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxListItem
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHeat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHighQuality
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudDownload

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

    // 删除入口看「是否本地自有」。identityKey 含协议, 仅用草稿判断会在切换协议时
    // 对不上导致按钮消失; 同名任意本地协议记录都认。
    val canDeleteEngine = draftEngine?.let { engine ->
        localOwnedEngineKeys.any { key ->
            key == engine.identityKey() ||
                key.startsWith("${engine.name.trim().lowercase()}#")
        }
    } == true

    // 导出入口独立判定(F20): 目前与删除同为本地自有引擎, 拆开是为了后续可按协议收紧
    // (导出 codec 已带 proxyUrl/proxyPath, Jev 导出再导入可以完整还原)
    val canExportEngine = canDeleteEngine

    val coroutineScope = rememberCoroutineScope()

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showTestDialog by remember { mutableStateOf(false) }
    var pendingDeleteModel by remember { mutableStateOf<AiModel?>(null) }
    var pendingDeleteEngine by remember { mutableStateOf(false) }
    var showRemoteModelPicker by remember { mutableStateOf(false) }
    var showModelValidationErrors by remember { mutableStateOf(false) }
    var showModelExitConfirmDialog by remember { mutableStateOf(false) }
    // 保存成功后、引擎还不是当前对话引擎时, 弹「设为当前引擎」轻确认
    var showSetDefaultPrompt by rememberSaveable { mutableStateOf(false) }

    val hasUnsavedChanges = component.hasDraftChanged()
    val loginState = LocalLoginState.current

    BackHandler(enabled = hasUnsavedChanges) {
        showExitConfirmDialog = true
    }

    if (showTestDialog && draftEngine != null) {
        AiEngineTestDialog(
            aiEngine = draftEngine,
            onDismiss = { showTestDialog = false },
            onApiTestSuccess = { testedEngine ->
                component.markTestPassed()
                showTestDialog = false
                coroutineScope.launch {
                    AppToastHost.showToast(
                        getString(CoreR.string.connection_successful)
                    )
                }
                // 测通后顺手把服务商模型拉进来: 与模型卡「从服务商拉取模型」同一套门控
                // (isDetestPassed 刚由 markTestPassed 置真, 这里只需复核渠道能力与协议)。
                // 成功不直接弹选择器, 模型卡顶部出现「发现 N 个可用模型」行内提示, 用户点了再看
                val canLoadRemote = !testedEngine.requestProtocol.isNonChat &&
                    (AiEngineConfig.getCapabilities().canLoadRemoteModels ||
                        loginState.vipLevel == 10)
                if (canLoadRemote) {
                    // 自动触发的拉取失败时静默处理, 用户仍可走模型卡的手动入口看错误
                    component.loadRemoteModels()
                }
            }
        )
    }

    if (editingModelDraft != null) {
        val editing = editingModelDraft!!
        val trimmedName = editing.name.trim()
        val isNameError = showModelValidationErrors && trimmedName.isBlank()
        val hasValidationErrors = trimmedName.isBlank()
        var showModelMoreOptions by rememberSaveable { mutableStateOf(false) }
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
                        text = stringResource(R.string.ai_engine_model_dialog_primary_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    // 主输入: 模型 ID。显示名自动派生, 细节收进「更多选项」
                    OneBoxOutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = editing.name,
                        onValueChange = { value -> component.updateEditingModelName(value) },
                        label = { Text(stringResource(R.string.ai_engine_model_name)) },
                        trailingIcon = {
                            ClearTextFieldTrailingIcon(
                                value = editing.name,
                                onClear = { component.updateEditingModelName("") },
                            )
                        },
                        isError = isNameError,
                        supportingText = {
                            if (isNameError) {
                                Text(stringResource(CoreR.string.required_field))
                            } else {
                                Text(stringResource(R.string.ai_engine_model_name_hint))
                            }
                        },
                    )
                    CollapsibleSectionHeader(
                        title = stringResource(R.string.ai_engine_more_options),
                        expanded = showModelMoreOptions,
                        onToggle = { showModelMoreOptions = !showModelMoreOptions },
                        summary = editing.title.ifBlank { AIEngineSettingsDetailComponent.prettifyModelTitle(editing.name) },
                    )
                    AnimatedVisibility(visible = showModelMoreOptions) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OneBoxOutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = editing.title,
                                onValueChange = { value ->
                                    component.updateEditingModel { it.copy(title = value) }
                                },
                                label = { Text(stringResource(R.string.ai_engine_model_display_name)) },
                                trailingIcon = {
                                    ClearTextFieldTrailingIcon(
                                        value = editing.title,
                                        onClear = {
                                            component.updateEditingModel { it.copy(title = "") }
                                        },
                                    )
                                },
                                supportingText = {
                                    Text(stringResource(R.string.ai_engine_model_display_name_hint))
                                },
                            )
                            OneBoxOutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = editing.description,
                                onValueChange = { value ->
                                    component.updateEditingModel { it.copy(description = value) }
                                },
                                label = { Text(stringResource(R.string.ai_engine_model_description)) },
                                trailingIcon = {
                                    ClearTextFieldTrailingIcon(
                                        value = editing.description,
                                        onClear = {
                                            component.updateEditingModel { it.copy(description = "") }
                                        },
                                    )
                                },
                            )
                        }
                    }
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
                        component.updateEditingModel {
                            it.copy(title = it.title.ifBlank { AIEngineSettingsDetailComponent.prettifyModelTitle(it.name) })
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
        // 页面标题直接用品牌名, 加载完成前回退通用标题
        title = draftEngine?.title ?: stringResource(R.string.ai_engine_detail_title),
        onGoBack = {
            if (hasUnsavedChanges) showExitConfirmDialog = true
            else component.onGoBack()
        },
        actions = {
            IconButton(onClick = component::beginAddLocalModel) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.ai_engine_add_model),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
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

                // 服务商基本信息: 只留显示名。服务标识是内部键、基本无需改动,
                // 原来收在「更多选项」里反而多一层误导性的可操作感, 整块去掉
                EngineBasicInfoCard(
                    engine = engine,
                    onTitleChange = component::updateTitle,
                )

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
                            // 测试前规范化地址, 与保存/聊天链路保持一致
                            draftEngine?.let { current ->
                                val normalizedUrl = StringUtils.normalizeApiBaseUrl(current.requestUrl)
                                val normalizedPath = StringUtils.normalizeApiPath(current.requestPath)
                                if (normalizedUrl != current.requestUrl) {
                                    component.updateRequestUrl(normalizedUrl)
                                }
                                if (normalizedPath != current.requestPath) {
                                    component.updateRequestPath(normalizedPath)
                                }
                            }
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
                    remoteModelsAvailable = remoteModels.size,
                    onShowRemoteModels = { showRemoteModelPicker = true },
                    onModelSelected = component::updateModel,
                    onModelEdit = component::beginEditModel,
                    onModelDelete = { pendingDeleteModel = it },
                    onAddModel = component::beginAddLocalModel,
                    onLoadRemoteModels = {
                        // 弹窗自己展示加载/失败/重试, 失败不再打断页面
                        showRemoteModelPicker = true
                        component.loadRemoteModels()
                    },
                )

                if (!engine.requestProtocol.isNonChat) {
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
                }
            }
        }

        BottomSaveCancelBar(
            modifier = Modifier.imePadding(),
            cancelEnabled = true,
            saveEnabled = draftEngine != null && !isSaving,
            saveText = stringResource(R.string.ai_engine_save_action),
            extraActions = if (canExportEngine) {
                {
                    // 本地自有引擎支持导出配置到剪贴板(含 Token, 用于备份/分享);
                    // 两种形态: JSON 原文 / 导入深链(onebox://action/import_engine?config=...)
                    var showExportMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = stringResource(R.string.ai_engine_export_action),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ai_engine_export_menu_json)) },
                                onClick = {
                                    showExportMenu = false
                                    val exported = component.exportConfigToClipboard()
                                    coroutineScope.launch {
                                        if (exported) {
                                            AppToastHost.showToast(
                                                getString(R.string.ai_engine_export_success)
                                            )
                                        } else {
                                            AppToastHost.showFailureToast(
                                                getString(R.string.ai_engine_export_failed)
                                            )
                                        }
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ai_engine_export_menu_link)) },
                                onClick = {
                                    showExportMenu = false
                                    val exported = component.exportImportLinkToClipboard()
                                    coroutineScope.launch {
                                        if (exported) {
                                            AppToastHost.showToast(
                                                getString(R.string.ai_engine_export_success)
                                            )
                                        } else {
                                            AppToastHost.showFailureToast(
                                                getString(R.string.ai_engine_export_failed)
                                            )
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            } else {
                null
            },
            dangerText = if (canDeleteEngine) {
                stringResource(R.string.ai_engine_delete_action)
            } else {
                null
            },
            onDangerClick = if (canDeleteEngine) {
                { pendingDeleteEngine = true }
            } else {
                null
            },
            dangerEnabled = !isDeleting,
            onCancel = {
                if (hasUnsavedChanges) showExitConfirmDialog = true
                else component.onGoBack()
            },
            onSave = {
                val engine = draftEngine ?: return@BottomSaveCancelBar
                val isJev = engine.requestProtocol.isNonChat
                // 保存前再规范化一次, 防止用户没移开焦点就点保存
                val normalizedUrl = StringUtils.normalizeApiBaseUrl(engine.requestUrl)
                val normalizedPath = StringUtils.normalizeApiPath(engine.requestPath)
                if (normalizedUrl != engine.requestUrl) {
                    component.updateRequestUrl(normalizedUrl)
                }
                if (normalizedPath != engine.requestPath) {
                    component.updateRequestPath(normalizedPath)
                }
                val isUrlInvalid = !isJev && !StringUtils.isValidUrl(normalizedUrl)
                if (isUrlInvalid || (!isJev && normalizedPath.isBlank())) {
                    component.updateUrlValidation(isUrlInvalid)
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
                            // 已是当前对话引擎直接退出; 否则给用户一个「保存即用」的轻确认
                            if (component.isDraftCurrentDefault()) {
                                component.onGoBack()
                            } else {
                                showSetDefaultPrompt = true
                            }
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
            text = stringResource(R.string.ai_engine_discard_message_engine),
            onExit = {
                showExitConfirmDialog = false
                component.restoreDraft()
                component.onGoBack()
            },
            onDismiss = { showExitConfirmDialog = false },
            visible = showExitConfirmDialog
        )
    }

    if (showSetDefaultPrompt && draftEngine != null) {
        val engine = draftEngine!!
        val dismissAndExit = {
            showSetDefaultPrompt = false
            component.onGoBack()
        }
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = dismissAndExit,
            title = { Text(stringResource(R.string.ai_engine_saved_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.ai_engine_set_default_prompt_message,
                        engine.title.ifBlank { engine.name },
                    )
                )
            },
            confirmButton = {
                OnePrimaryButton(
                    text = stringResource(R.string.ai_engine_set_default_confirm),
                    onClick = {
                        if (!component.setDraftAsCurrentDefault()) {
                            coroutineScope.launch {
                                AppToastHost.showFailureToast(
                                    getString(R.string.ai_engine_set_default_failed)
                                )
                            }
                        }
                        dismissAndExit()
                    },
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(R.string.ai_engine_set_default_later),
                    onClick = dismissAndExit,
                )
            },
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
        ProviderModelsPickerDialog(
            models = remoteModels,
            isLoading = isLoadingRemoteModels,
            error = remoteModelsError,
            selectedModelId = draftEngine?.model?.name.orEmpty(),
            onDismiss = {
                showRemoteModelPicker = false
                component.clearRemoteModels()
            },
            onRefresh = { component.loadRemoteModels() },
            onSelect = { model ->
                // 只加本地模型: 拉回来的列表点一个就落一条本地模型, 不再提交服务端
                component.addProviderModelLocally(model) { success ->
                    coroutineScope.launch {
                        if (success) {
                            showRemoteModelPicker = false
                            AppToastHost.showToast(getString(R.string.ai_engine_model_add_success))
                        } else {
                            AppToastHost.showFailureToast(getString(R.string.ai_engine_model_add_failed))
                        }
                    }
                }
            },
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
private fun EngineBasicInfoCard(
    engine: AiEngine,
    onTitleChange: (String) -> Unit,
) {
    SettingCard {
        OneBoxOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = engine.title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.ai_engine_title_label)) },
            trailingIcon = {
                ClearTextFieldTrailingIcon(
                    value = engine.title,
                    onClear = { onTitleChange("") },
                )
            },
            singleLine = true,
            supportingText = {
                Text(stringResource(R.string.ai_engine_title_hint))
            },
        )
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
    SettingCard {
        val isJev = engine.requestProtocol.isNonChat

        if (!isJev) {
            // 协议是这屏最常改的一项, 改用选择器直接展示当前值: 原来的横排卡片里
            // 第二个选型会被卡片边缘裁掉, 又看不出还能横滑, 看着像坏了
            val selectableProtocols = remember(engine.requestProtocol) {
                // 「应用代理」是内置中转链路,不作为可选协议暴露;
                // JEV 仅海外可选(国内无备案), 且仅当前已是 JEV 的引擎保留该项
                val jevAllowed = com.shifenmiao.model.channel.FlavorType.fromName().isOverseas
                AiRequestProtocol.cloudProtocols
                    .filter { it != AiRequestProtocol.OWN_PROXY }
                    .filter { protocol ->
                        when (protocol) {
                            AiRequestProtocol.JEV ->
                                jevAllowed && engine.requestProtocol == AiRequestProtocol.JEV
                            else -> !protocol.isNonChat || engine.requestProtocol == protocol
                        }
                    }
            }
            SelectorField(
                label = stringResource(R.string.ai_engine_protocol_label),
                selected = engine.requestProtocol,
                options = selectableProtocols,
                optionLabel = { protocolLabel(it) },
                onSelect = onProtocolChange,
            )

            // 鉴权方式与协议同为二选一, 用同一种选择器: 当前值直接显示, 不藏在折叠里
            SelectorField(
                label = stringResource(R.string.ai_engine_auth_type_label),
                selected = engine.authType,
                options = AuthType.entries,
                optionLabel = { authTypeLabel(it) },
                onSelect = onAuthTypeChange,
            )
        }

        if (isJev) {
            Text(
                text = stringResource(R.string.ai_engine_jev_proxy_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OneBoxOutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && canEditApiSettings) {
                        val normalized = StringUtils.normalizeApiBaseUrl(engine.requestUrl)
                        if (normalized != engine.requestUrl) {
                            onUrlChange(normalized)
                        }
                    }
                },
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
                } else {
                    Text(text = stringResource(R.string.ai_engine_api_url_hint))
                }
            },
        )

        OneBoxOutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && canEditApiSettings) {
                        val normalized = StringUtils.normalizeApiPath(engine.requestPath)
                        if (normalized != engine.requestPath) {
                            onPathChange(normalized)
                        }
                    }
                },
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

        OnePrimaryButton(
            text = stringResource(CoreR.string.test_connection),
            onClick = onTestClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = canTest,
            leadingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLink,
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
private fun ModelSelectionCard(
    engine: AiEngine,
    models: List<AiModel>,
    remoteModelsAvailable: Int = 0,
    onShowRemoteModels: () -> Unit = {},
    onModelSelected: (AiModel) -> Unit,
    onModelEdit: (AiModel) -> Unit,
    onModelDelete: (AiModel) -> Unit,
    onAddModel: () -> Unit,
    onLoadRemoteModels: () -> Unit = {},
) {
    val loginState = LocalLoginState.current
    // 渠道能力(如 Google 全量放开)或高等级用户可从服务商拉取模型列表;
    // 但直连未验证(测试未通过)时拉取必然无权限,入口一并隐藏;
    // Jev 模型目录由客户端内置, 不支持从服务商拉取
    // 直连引擎(含用户自带 token)才有意义: 代理中转链路的 token 在服务端,
    // 客户端直接请求服务商只会 401, 这条链路的模型由服务端下发, 不露出入口
    val canLoadRemote = !engine.requestProtocol.isNonChat &&
            !engine.usesProxyRoute() &&
            (remember { AiEngineConfig.getCapabilities() }.canLoadRemoteModels ||
            loginState.vipLevel == 10) &&
            engine.requestUrl.isNotBlank()
    SettingCard {
        // 测通后自动拉到的服务商模型: 行内提示, 点了「查看」才弹选择器, 不打断用户
        if (remoteModelsAvailable > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.ai_engine_remote_models_found,
                        remoteModelsAvailable
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                OneSecondaryButton(
                    text = stringResource(R.string.ai_engine_view_models),
                    onClick = onShowRemoteModels
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
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
                OnePrimaryButton(
                    text = stringResource(R.string.ai_engine_load_from_provider),
                    onClick = onLoadRemoteModels,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudDownload,
                )
            }
        } else {
            // 代理中转链路下按模型倍率扣积分,在模型后展示倍率
            val showPointsMultiplier = engine.usesProxyRoute()
            val selectedModel = models.firstOrNull { it.id == engine.model.id } ?: engine.model

            // 模型两列网格,选中高亮
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                models.chunked(2).forEach { rowModels ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowModels.forEach { model ->
                            SelectGridCard(
                                title = model.title.ifBlank { model.name },
                                subtitle = if (showPointsMultiplier) {
                                    model.pointsMultiplierText()
                                } else {
                                    null
                                },
                                isSelected = model.id == selectedModel.id,
                                onClick = { onModelSelected(model) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowModels.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (selectedModel.canEdit) {
                        stringResource(R.string.ai_engine_model_local_hint)
                    } else {
                        stringResource(R.string.ai_engine_model_remote_hint)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.weight(1f),
                )
                // 远程下发模型没有编辑/删除权限, 这时整块按钮不渲染:
                // 禁用态图标只会让用户以为自己点错了, 左侧提示已说明「仅支持选择」
                if (selectedModel.canEdit) {
                    IconButton(
                        onClick = { onModelEdit(selectedModel) },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.ai_engine_edit_action),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(
                        onClick = { onModelDelete(selectedModel) },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.ai_engine_delete_action),
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            if (canLoadRemote) {
                OnePrimaryButton(
                    text = stringResource(R.string.ai_engine_load_from_provider),
                    onClick = onLoadRemoteModels,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudDownload,
                )
            }
        }
    }
}

// maxTokens 滑杆档位(F5): 线性 256..65536 会把常用区间(1k-8k)挤进滑轨最左 12%,
// 改离散档位让常用值均分行程; 不在档位上的存量值就近靠档、显示真实存储值, 精确值走输入弹窗
private val MaxTokensStops = listOf(256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536)

private fun maxTokensToSliderPosition(maxTokens: Int): Float {
    val index = MaxTokensStops.withIndex()
        .minByOrNull { kotlin.math.abs(it.value - maxTokens) }?.index ?: 0
    return index / (MaxTokensStops.size - 1).toFloat()
}

private fun sliderPositionToMaxTokens(position: Float): Int {
    // 四舍五入取档(R6): 截断会让最后一档只占 position==1.0 一个点, 其余档各占 12.5%
    val index = (position * (MaxTokensStops.size - 1))
        .roundToInt()
        .coerceIn(0, MaxTokensStops.size - 1)
    return MaxTokensStops[index]
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
    // 与「请求协议」同款折叠头, 默认收起
    SettingCard {
        var expanded by rememberSaveable { mutableStateOf(false) }
        CollapsibleSectionHeader(
            title = stringResource(R.string.ai_engine_parameters_title),
            expanded = expanded,
            onToggle = { expanded = !expanded },
        )
        AnimatedVisibility(visible = expanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
            ) {
                ParameterCardContent(
                    engine = engine,
                    cloudConnections = cloudConnections,
                    onTemperatureChange = onTemperatureChange,
                    onTopPChange = onTopPChange,
                    onMaxTokensChange = onMaxTokensChange,
                    onCanUploadFileChange = onCanUploadFileChange,
                    onCanNetworkChange = onCanNetworkChange,
                    onCanReasoningChange = onCanReasoningChange,
                    onCanImageChange = onCanImageChange,
                    onIsFastChange = onIsFastChange,
                    onIsCodeChange = onIsCodeChange,
                    onSupportToolCallsChange = onSupportToolCallsChange,
                    onStreamChange = onStreamChange,
                    onFileUploadStrategyChange = onFileUploadStrategyChange,
                    onCloudStorageConnectionIdChange = onCloudStorageConnectionIdChange,
                    onCloudStorageBucketChange = onCloudStorageBucketChange,
                    onCloudStoragePrefixChange = onCloudStoragePrefixChange,
                    onResetRemoteModelOverrides = onResetRemoteModelOverrides,
                    onNavigateToCloudStorage = onNavigateToCloudStorage,
                )
            }
        }
    }
}

@Composable
private fun ParameterCardContent(
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
    ParameterSliderRow(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHeat,
        title = stringResource(R.string.ai_engine_temperature),
        description = stringResource(R.string.ai_engine_temperature_desc),
        value = engine.model.temperature.toFloat(),
        onValueChange = onTemperatureChange,
        inputSpec = ParameterInputSpec(
            min = 0f,
            max = 1f,
            isInteger = false,
            onSubmit = onTemperatureChange,
        ),
    )

    ParameterSliderRow(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHighQuality,
        title = stringResource(R.string.ai_engine_top_p),
        description = stringResource(R.string.ai_engine_top_p_desc),
        value = engine.model.topP.toFloat(),
        onValueChange = onTopPChange,
        inputSpec = ParameterInputSpec(
            min = 0f,
            max = 1f,
            isInteger = false,
            onSubmit = onTopPChange,
        ),
    )

    ParameterSliderRow(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTune,
        title = stringResource(R.string.ai_engine_max_tokens),
        description = stringResource(R.string.ai_engine_max_tokens_desc),
        value = maxTokensToSliderPosition(engine.model.maxTokens),
        onValueChange = { onMaxTokensChange(sliderPositionToMaxTokens(it)) },
        displayText = "${engine.model.maxTokens}",
        inputSpec = ParameterInputSpec(
            min = 256f,
            max = 65536f,
            isInteger = true,
            onSubmit = { onMaxTokensChange(it.toInt()) },
        ),
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
                    color = MaterialTheme.colorScheme.onErrorContainer,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

/** 参数值标签的精确输入规格; 为 null 时值标签纯展示不可点击 */
private class ParameterInputSpec(
    val min: Float,
    val max: Float,
    val isInteger: Boolean,
    val onSubmit: (Float) -> Unit,
)

@Composable
private fun ParameterSliderRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    displayText: String? = null,
    inputSpec: ParameterInputSpec? = null,
) {
    OneBoxSectionCard(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(OneBoxDesignSystem.cardPadding),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
    ) {
        var showValueInputDialog by rememberSaveable { mutableStateOf(false) }
        val valueText = displayText ?: String.format(
            LocalLocale.current.platformLocale,
            "%.1f",
            value
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    // 触摸目标至少 48dp(F17): 原来只有文字行高 + 8dp padding, 约 28dp 太难点
                    .heightIn(min = 48.dp)
                    .glassBackground(
                        style = GlassStyle.Thin,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = OneBoxDesignSystem.pillShape,
                        borderWidth = 0.dp,
                    )
                    // 滑杆仍是主交互, 点值标签弹出精确输入作为增强
                    .then(
                        if (inputSpec != null) {
                            Modifier
                                .semantics { role = Role.Button }
                                .clickable { showValueInputDialog = true }
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
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

        if (showValueInputDialog && inputSpec != null) {
            ParameterValueInputDialog(
                title = title,
                initialText = valueText,
                spec = inputSpec,
                onDismiss = { showValueInputDialog = false },
            )
        }
    }
}

/** 参数精确输入对话框: 数值校验(合法数字 + 范围 + 整数约束), 非法输入就地提示不关闭 */
@Composable
private fun ParameterValueInputDialog(
    title: String,
    initialText: String,
    spec: ParameterInputSpec,
    onDismiss: () -> Unit,
) {
    var input by rememberSaveable { mutableStateOf(initialText) }
    // 错误态: null=无错误; Invalid=非法数字; OutOfRange=超出范围
    var inputError by remember { mutableStateOf<ParameterInputError?>(null) }
    val minText = formatParameterBound(spec.min)
    val maxText = formatParameterBound(spec.max)

    fun submit() {
        val parsed = input.trim().toFloatOrNull()
        when {
            parsed == null || (spec.isInteger && parsed % 1f != 0f) -> {
                inputError = ParameterInputError.Invalid
            }
            parsed < spec.min || parsed > spec.max -> {
                inputError = ParameterInputError.OutOfRange
            }
            else -> {
                spec.onSubmit(parsed)
                onDismiss()
            }
        }
    }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OneBoxOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = input,
                onValueChange = {
                    input = it
                    inputError = null
                },
                // 标题已在对话框顶部, 输入框不再重复 label(F16)
                placeholder = { Text(title) },
                singleLine = true,
                isError = inputError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (spec.isInteger) KeyboardType.Number else KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                supportingText = {
                    Text(
                        text = when (inputError) {
                            ParameterInputError.Invalid ->
                                stringResource(R.string.ai_engine_parameter_input_invalid)
                            ParameterInputError.OutOfRange ->
                                stringResource(R.string.ai_engine_parameter_input_out_of_range, minText, maxText)
                            null ->
                                stringResource(R.string.ai_engine_parameter_input_range_hint, minText, maxText)
                        },
                    )
                },
            )
        },
        confirmButton = {
            OnePrimaryButton(
                text = stringResource(CoreR.string.button_confirm),
                onClick = ::submit,
            )
        },
        dismissButton = {
            OneSecondaryButton(
                text = stringResource(CoreR.string.button_cancel),
                onClick = onDismiss,
            )
        },
    )
}

private enum class ParameterInputError { Invalid, OutOfRange }

private fun formatParameterBound(value: Float): String {
    return if (value % 1f == 0f) value.toInt().toString() else value.toString()
}


