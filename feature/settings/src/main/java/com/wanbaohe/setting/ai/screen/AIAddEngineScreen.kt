package com.wanbaohe.setting.ai.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.ClearTextFieldTrailingIcon
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.BottomSaveCancelBar
import com.shifenmiao.common.ui.ai.EngineFilterChip
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.setting.ai.component.AIAddEngineComponent
import com.wanbaohe.settings.R
import kotlinx.coroutines.launch
import com.shifenmiao.core.R as CoreR
import com.shifenmiao.common.R as CommonR

@Composable
fun AIAddEngineScreen(
    component: AIAddEngineComponent,
) {
    val draft by component.draft.collectAsState()
    val isSaving by component.isSaving.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showValidationErrors by remember { mutableStateOf(false) }

    val hasUnsavedChanges = component.hasDraftChanged()
    val trimmedTitle = draft.title.trim()
    val trimmedName = draft.name.trim()
    val trimmedRequestUrl = draft.requestUrl.trim()
    val trimmedRequestPath = draft.requestPath.trim()
    val isLocalProtocol = draft.requestProtocol == AiRequestProtocol.LOCAL_ON_DEVICE
    val isTitleError = showValidationErrors && trimmedTitle.isBlank()
    val isNameError = showValidationErrors && trimmedName.isBlank()
    // 本地协议不要求 URL/Path（由本地模型管理页处理），跳过校验避免阻止保存。
    val isRequestUrlError = !isLocalProtocol && showValidationErrors &&
        (trimmedRequestUrl.isBlank() || !StringUtils.isValidUrl(trimmedRequestUrl))
    val isRequestPathError = !isLocalProtocol && showValidationErrors && trimmedRequestPath.isBlank()
    // 名称/标题任何协议都必填；URL/Path 仅云端协议必填；本地协议下整段云端校验跳过。
    val hasValidationErrors = trimmedTitle.isBlank() || trimmedName.isBlank() ||
        (!isLocalProtocol && (
            trimmedRequestUrl.isBlank() ||
                !StringUtils.isValidUrl(trimmedRequestUrl) ||
                trimmedRequestPath.isBlank()
            ))

    BackHandler(enabled = hasUnsavedChanges) {
        showExitConfirmDialog = true
    }

    BaseScreen(
        title = stringResource(R.string.ai_engine_add_dialog_title),
        onGoBack = {
            if (hasUnsavedChanges) showExitConfirmDialog = true
            else component.onGoBack()
        },
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            OneBoxSectionCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    Text(
                        text = stringResource(R.string.ai_engine_add_dialog_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = stringResource(R.string.ai_engine_protocol_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 仅展示云端协议；LOCAL_ON_DEVICE 由独立的"本地模型管理"页处理（Phase 2）。
                items(AiRequestProtocol.cloudProtocols) { protocol ->
                    EngineFilterChip(
                        text = when (protocol) {
                            AiRequestProtocol.OPENAI_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_openai)
                            AiRequestProtocol.RESPONSES_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_responses)
                            AiRequestProtocol.ANTHROPIC_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_anthropic)
                            AiRequestProtocol.OWN_PROXY -> stringResource(R.string.ai_engine_protocol_proxy)
                            AiRequestProtocol.LOCAL_ON_DEVICE -> stringResource(R.string.ai_engine_protocol_local_on_device)
                        },
                        isSelected = draft.requestProtocol == protocol,
                        onClick = {
                            component.updateDraft { engine ->
                                val previousDefaultAuthType = AuthType.defaultFor(engine.requestProtocol)
                                val nextAuthType = if (engine.authType == previousDefaultAuthType) {
                                    AuthType.defaultFor(protocol)
                                } else {
                                    engine.authType
                                }
                                val fallbackPath = when (protocol) {
                                    AiRequestProtocol.RESPONSES_COMPATIBLE -> "/v1/responses"
                                    AiRequestProtocol.ANTHROPIC_COMPATIBLE -> "/v1/messages"
                                    else -> engine.requestPath.ifBlank { "/v1/chat/completions" }
                                }
                                engine.copy(
                                    requestProtocol = protocol,
                                    authType = nextAuthType,
                                    requestPath = engine.requestPath.ifBlank { fallbackPath },
                                )
                            }
                        },
                    )
                }
            }

            Text(
                text = stringResource(R.string.ai_engine_auth_type_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AuthType.entries) { type ->
                    EngineFilterChip(
                        text = authTypeLabel(type),
                        isSelected = draft.authType == type,
                        onClick = {
                            component.updateDraft { it.copy(authType = type) }
                        },
                    )
                }
            }

            OneBoxOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft.title,
                onValueChange = { component.updateDraft { e -> e.copy(title = it) } },
                label = { Text(stringResource(R.string.ai_engine_title_label)) },
                trailingIcon = {
                    ClearTextFieldTrailingIcon(
                        value = draft.title,
                        onClear = { component.updateDraft { it.copy(title = "") } },
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
                value = draft.name,
                onValueChange = { component.updateDraft { e -> e.copy(name = it) } },
                label = { Text(stringResource(R.string.ai_engine_name_label)) },
                trailingIcon = {
                    ClearTextFieldTrailingIcon(
                        value = draft.name,
                        onClear = { component.updateDraft { it.copy(name = "") } },
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
                value = draft.description,
                onValueChange = { component.updateDraft { e -> e.copy(description = it) } },
                label = { Text(stringResource(R.string.ai_engine_description_label)) },
                trailingIcon = {
                    ClearTextFieldTrailingIcon(
                        value = draft.description,
                        onClear = { component.updateDraft { it.copy(description = "") } },
                    )
                },
            )
            // 本地协议下 URL / Path / Auth Token 字段无意义，整体隐藏；
            // Phase 2 由"本地模型管理"页提供专属导入流程。
            if (!isLocalProtocol) {
                OneBoxOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = draft.requestUrl,
                    onValueChange = { component.updateDraft { e -> e.copy(requestUrl = it, isUrlError = false) } },
                    label = { Text(stringResource(R.string.ai_engine_api_url)) },
                    trailingIcon = {
                        ClearTextFieldTrailingIcon(
                            value = draft.requestUrl,
                            onClear = { component.updateDraft { it.copy(requestUrl = "", isUrlError = false) } },
                        )
                    },
                    isError = isRequestUrlError,
                    supportingText = {
                        if (isRequestUrlError) {
                            if (trimmedRequestUrl.isBlank()) {
                                Text(stringResource(CoreR.string.required_field))
                            } else {
                                Text(stringResource(CoreR.string.url_error_tips))
                            }
                        }
                    },
                )
                OneBoxOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = draft.requestPath,
                    onValueChange = { component.updateDraft { e -> e.copy(requestPath = it) } },
                    label = { Text(stringResource(R.string.ai_engine_api_path)) },
                    trailingIcon = {
                        ClearTextFieldTrailingIcon(
                            value = draft.requestPath,
                            onClear = { component.updateDraft { it.copy(requestPath = "") } },
                        )
                    },
                    isError = isRequestPathError,
                    supportingText = {
                        if (isRequestPathError) {
                            Text(stringResource(CoreR.string.required_field))
                        }
                    },
                )
                OneBoxOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = draft.authorizationCode,
                    onValueChange = { component.updateDraft { e -> e.copy(authorizationCode = it) } },
                    label = { Text(stringResource(R.string.ai_engine_auth_token)) },
                    trailingIcon = {
                        ClearTextFieldTrailingIcon(
                            value = draft.authorizationCode,
                            onClear = { component.updateDraft { it.copy(authorizationCode = "") } },
                        )
                    },
                )
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))
        }

        BottomSaveCancelBar(
            modifier = Modifier.imePadding(),
            cancelEnabled = true,
            saveEnabled = !isSaving,
            saveText = stringResource(R.string.ai_engine_save_action),
            onCancel = {
                if (hasUnsavedChanges) showExitConfirmDialog = true
                else component.onGoBack()
            },
            onSave = {
                showValidationErrors = true
                if (hasValidationErrors) {
                    coroutineScope.launch {
                        AppToastHost.showFailureToast(
                            getString(R.string.ai_engine_dialog_validation_failed)
                        )
                    }
                    return@BottomSaveCancelBar
                }
                component.save { success ->
                    coroutineScope.launch {
                        if (success) {
                            AppToastHost.showToast(
                                getString(R.string.ai_engine_add_success, draft.title.ifBlank { draft.name })
                            )
                            component.onGoBack()
                        } else {
                            AppToastHost.showFailureToast(
                                getString(R.string.ai_engine_add_failed)
                            )
                        }
                    }
                }
            }
        )
    }

    if (showExitConfirmDialog) {
        com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog(
            title = stringResource(R.string.ai_engine_discard_title),
            text = stringResource(R.string.ai_engine_discard_message),
            onExit = {
                showExitConfirmDialog = false
                component.onGoBack()
            },
            onDismiss = { showExitConfirmDialog = false },
            visible = showExitConfirmDialog,
        )
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


