package com.wanbaohe.setting.ai.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.ClearTextFieldTrailingIcon
import com.shifenmiao.base.ui.PasswordTextField
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.BottomSaveCancelBar
import com.shifenmiao.core.constants.UrlConstants
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

// 服务标识(name)是引擎的身份键, 对用户不直观; 默认由显示名称派生出 ASCII slug
private fun deriveServiceKey(title: String): String {
    return title.trim().lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}

// 各云端协议的默认 API 路径, 用于切换协议时识别"用户没改过的默认值"
private val PROTOCOL_DEFAULT_PATHS = setOf("/v1/chat/completions", "/v1/responses", "/v1/messages")

@Composable
fun AIAddEngineScreen(
    component: AIAddEngineComponent,
) {
    val draft by component.draft.collectAsState()
    val isSaving by component.isSaving.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showExitConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showValidationErrors by rememberSaveable { mutableStateOf(false) }
    // 协议与鉴权默认收起(默认 OpenAI 兼容已覆盖大多数场景), 行尾摘要展示当前协议
    var protocolExpanded by rememberSaveable { mutableStateOf(false) }
    // 服务标识/描述等可选字段收进"更多选项", 默认收起
    var moreExpanded by rememberSaveable { mutableStateOf(false) }
    // 用户手动改过服务标识后停止跟随显示名称自动派生
    var serviceKeyEdited by rememberSaveable { mutableStateOf(false) }

    val hasUnsavedChanges = component.hasDraftChanged()
    val trimmedTitle = draft.title.trim()
    val trimmedRequestUrl = draft.requestUrl.trim()
    val trimmedRequestPath = draft.requestPath.trim()
    val isLocalProtocol = draft.requestProtocol == AiRequestProtocol.LOCAL_ON_DEVICE
    // Jev / Pikafish 仅走 App 代理, 不要求直连 URL/Path/Token
    val isJevProtocol = draft.requestProtocol == AiRequestProtocol.JEV
    val isPikafishProtocol = draft.requestProtocol == AiRequestProtocol.PIKAFISH
    val isTitleError = showValidationErrors && trimmedTitle.isBlank()
    // 本地协议不要求 URL/Path（由本地模型管理页处理），跳过校验避免阻止保存；
    // 服务标识留空时保存前自动派生, 不参与必填校验。
    val skipCloudValidation = isLocalProtocol || isJevProtocol || isPikafishProtocol
    val isRequestUrlError = !skipCloudValidation && showValidationErrors &&
        (trimmedRequestUrl.isBlank() || !StringUtils.isValidUrl(trimmedRequestUrl))
    val isRequestPathError = !skipCloudValidation && showValidationErrors && trimmedRequestPath.isBlank()
    // 标题任何协议都必填；URL/Path 仅云端协议必填；本地协议下整段云端校验跳过。
    val hasValidationErrors = trimmedTitle.isBlank() ||
        (!skipCloudValidation && (
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
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))

            OneBoxSectionCard {
                Text(
                    text = stringResource(R.string.ai_engine_add_dialog_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SettingCard {
                OneBoxOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = draft.title,
                    onValueChange = { value ->
                        component.updateDraft { engine ->
                            engine.copy(
                                title = value,
                                name = if (serviceKeyEdited) engine.name else deriveServiceKey(value),
                            )
                        }
                    },
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

                CollapsibleSectionHeader(
                    title = stringResource(R.string.ai_engine_more_options),
                    expanded = moreExpanded,
                    onToggle = { moreExpanded = !moreExpanded },
                )
                AnimatedVisibility(visible = moreExpanded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
                    ) {
                        OneBoxOutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = draft.name,
                            onValueChange = { value ->
                                serviceKeyEdited = true
                                component.updateDraft { it.copy(name = value) }
                            },
                            label = { Text(stringResource(R.string.ai_engine_name_label)) },
                            trailingIcon = {
                                ClearTextFieldTrailingIcon(
                                    value = draft.name,
                                    onClear = {
                                        serviceKeyEdited = false
                                        component.updateDraft {
                                            it.copy(name = deriveServiceKey(it.title))
                                        }
                                    },
                                )
                            },
                            supportingText = {
                                Text(stringResource(R.string.ai_engine_name_auto_hint))
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
                    }
                }
            }

            // 本地协议下 URL / Path / Auth Token 字段无意义，整体隐藏；
            // Jev 展示直连字段(官网地址/key), 仅跳过必填校验(留空走 App 代理兜底)。
            // Phase 2 由"本地模型管理"页提供专属导入流程。
            if (!isLocalProtocol) {
                SettingCard {
                    CollapsibleSectionHeader(
                        title = stringResource(R.string.ai_engine_protocol_label),
                        expanded = protocolExpanded,
                        onToggle = { protocolExpanded = !protocolExpanded },
                        summary = protocolLabel(draft.requestProtocol),
                    )
                    AnimatedVisibility(visible = protocolExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
                        ) {
                            // 仅展示云端协议；LOCAL_ON_DEVICE 由独立的"本地模型管理"页处理（Phase 2）;
                            // JEV 引擎仅在 Jev tab 新增, 普通入口的选择器排除 JEV;
                            // 从 Jev tab 进入时协议锁定为 JEV, 只展示一个固定选中项。
                            // Jev(TypeSafe) 国内无备案, 协议选择器仅海外暴露;
                            // 「应用代理」是内置中转链路、Pikafish 是象棋专用通道, 均不支持用户自建, 不作为可选协议暴露
                            val jevAllowed = com.shifenmiao.model.channel.FlavorType.fromName().isOverseas
                            val selectableProtocols = when {
                                isJevProtocol -> listOf(AiRequestProtocol.JEV)
                                isPikafishProtocol -> listOf(AiRequestProtocol.PIKAFISH)
                                else -> AiRequestProtocol.cloudProtocols.filter {
                                    it != AiRequestProtocol.OWN_PROXY &&
                                        it != AiRequestProtocol.PIKAFISH &&
                                        (it != AiRequestProtocol.JEV || jevAllowed)
                                }
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(selectableProtocols) { protocol ->
                                    SelectGridCard(
                                        title = protocolLabel(protocol),
                                        subtitle = null,
                                        isSelected = draft.requestProtocol == protocol,
                                        onClick = {
                                            if (isJevProtocol || isPikafishProtocol) return@SelectGridCard
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
                                                    else -> "/v1/chat/completions"
                                                }
                                                // 路径还是上一个协议的默认值时一并跟随切换, 避免留下错配的旧协议路径;
                                                // 用户手动改过的自定义路径保持不动
                                                val currentPath = engine.requestPath.trim()
                                                val isDefaultPath = currentPath.isBlank() || currentPath in PROTOCOL_DEFAULT_PATHS
                                                engine.copy(
                                                    requestProtocol = protocol,
                                                    authType = nextAuthType,
                                                    requestPath = if (protocol == AiRequestProtocol.PIKAFISH) {
                                                        ""
                                                    } else if (isDefaultPath) {
                                                        fallbackPath
                                                    } else {
                                                        engine.requestPath
                                                    },
                                                    proxyUrl = if (protocol == AiRequestProtocol.JEV || protocol == AiRequestProtocol.PIKAFISH) {
                                                        engine.proxyUrl.ifBlank { UrlConstants.RELEASE_URL }
                                                    } else {
                                                        engine.proxyUrl
                                                    },
                                                    proxyPath = when (protocol) {
                                                        AiRequestProtocol.JEV -> engine.proxyPath.ifBlank { UrlConstants.JEV_PROXY_PATH }
                                                        AiRequestProtocol.PIKAFISH -> engine.proxyPath.ifBlank { UrlConstants.XIANGQI_ENGINE_PROXY_PATH }
                                                        else -> engine.proxyPath
                                                    },
                                                )
                                            }
                                        },
                                        // LazyRow 主轴无界, SelectGridCard 内部 weight(1f) 会塌成 0 宽,
                                        // 用内容固有宽度定宽才能正确测量文字
                                        modifier = Modifier.width(IntrinsicSize.Max),
                                    )
                                }
                            }

                            if (!isJevProtocol && !isPikafishProtocol) {
                                Text(
                                    text = stringResource(R.string.ai_engine_auth_type_label),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(AuthType.entries) { type ->
                                        SelectGridCard(
                                            title = authTypeLabel(type),
                                            subtitle = null,
                                            isSelected = draft.authType == type,
                                            onClick = {
                                                component.updateDraft { it.copy(authType = type) }
                                            },
                                            modifier = Modifier.width(IntrinsicSize.Max),
                                        )
                                    }
                                }
                            }

                            if (isJevProtocol) {
                                Text(
                                    text = stringResource(R.string.ai_engine_jev_proxy_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

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
                        singleLine = true,
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
                        singleLine = true,
                        isError = isRequestPathError,
                        supportingText = {
                            if (isRequestPathError) {
                                Text(stringResource(CoreR.string.required_field))
                            }
                        },
                    )
                    PasswordTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = draft.authorizationCode,
                        onValueChange = { component.updateDraft { e -> e.copy(authorizationCode = it) } },
                        label = stringResource(R.string.ai_engine_auth_token),
                        onClearValue = { component.updateDraft { it.copy(authorizationCode = "") } },
                        imeAction = ImeAction.Done,
                    )
                }
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
                // 服务标识留空时自动派生; 纯中文标题派生不出 slug, 退化为时间戳后缀兜底
                val resolvedName = draft.name.trim().ifBlank {
                    deriveServiceKey(trimmedTitle).ifBlank {
                        "engine-${System.currentTimeMillis() % 1_000_000}"
                    }
                }
                if (resolvedName != draft.name) {
                    component.updateDraft { it.copy(name = resolvedName) }
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
