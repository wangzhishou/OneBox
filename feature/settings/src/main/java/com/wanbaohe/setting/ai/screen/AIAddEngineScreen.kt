package com.wanbaohe.setting.ai.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.ClearTextFieldTrailingIcon
import com.wanbaohe.base.ui.PasswordTextField
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.manager.AddEngineResult
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.BottomSaveCancelBar
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxListItem
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.setting.ai.component.AIAddEngineComponent
import com.wanbaohe.setting.ai.component.AIEngineSettingsDetailComponent
import com.wanbaohe.setting.ai.component.EngineImportResult
import com.wanbaohe.settings.R
import kotlinx.coroutines.launch
import com.shifenmiao.core.R as CoreR

// 服务标识(name)是引擎的身份键, 对用户不直观; 默认由显示名称派生出 ASCII slug
private fun deriveServiceKey(title: String): String {
    return title.trim().lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}

/**
 * 自动派生用的服务标识。
 *
 * 中文标题压出来的 slug 常常短到没有区分度 —— "我的AI" 和 "阿里ai" 都会变成 `ai`,
 * 两条自定义引擎撞同一个 key, 后加的那条要么被拒、要么(旧版本里)把前一条覆盖掉。
 * 所以短于 [MIN_SERVICE_KEY_LENGTH] 的一律视为派生失败, 交给调用方用时间戳兜底。
 */
private fun deriveAutoServiceKey(title: String): String {
    return deriveServiceKey(title).takeIf { it.length >= MIN_SERVICE_KEY_LENGTH }.orEmpty()
}

private const val MIN_SERVICE_KEY_LENGTH = 3

@Composable
fun AIAddEngineScreen(
    component: AIAddEngineComponent,
) {
    val draft by component.draft.collectAsState()
    val isSaving by component.isSaving.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showExitConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showValidationErrors by rememberSaveable { mutableStateOf(false) }
    // 鉴权方式跟随协议默认, 收进折叠; 行尾摘要展示当前取值
    var authExpanded by rememberSaveable { mutableStateOf(false) }
    // 服务标识/描述等可选字段收进"更多选项", 默认收起
    var moreExpanded by rememberSaveable { mutableStateOf(false) }
    // 用户手动改过服务标识后停止跟随显示名称自动派生
    var serviceKeyEdited by rememberSaveable { mutableStateOf(false) }
    // 「保存后设为当前引擎」默认关(F1): 新增页没有测试入口,
    // 未验证的新引擎不能直接顶掉正在用的引擎, 由用户显式勾选
    var setCurrentAfterSave by rememberSaveable { mutableStateOf(false) }

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

    fun onImportResult(result: EngineImportResult) {
        when (result) {
            EngineImportResult.EmptyClipboard -> coroutineScope.launch {
                AppToastHost.showFailureToast(getString(R.string.ai_engine_import_empty_clipboard))
            }
            EngineImportResult.Invalid -> coroutineScope.launch {
                AppToastHost.showFailureToast(getString(R.string.ai_engine_import_invalid))
            }
            is EngineImportResult.Success -> {
                // 导入的名字是显式配置, 不再跟随显示名派生
                serviceKeyEdited = true
                coroutineScope.launch {
                    AppToastHost.showToast(
                        getString(
                            if (result.renamed) {
                                R.string.ai_engine_import_success_renamed
                            } else {
                                R.string.ai_engine_import_success
                            },
                            result.displayName,
                        )
                    )
                }
            }
        }
    }

    // 深链 onebox://action/import_engine 带入的配置, 进页消费一次
    LaunchedEffect(Unit) {
        val pending = component.takePendingImportConfig()
        if (pending.isNotBlank()) {
            component.applyImportedConfig(pending, ::onImportResult)
        }
    }

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

            // 导入是独立动作, 不塞在说明卡里(F22)
            OneSecondaryButton(
                text = stringResource(R.string.ai_engine_import_action),
                onClick = {
                    component.importConfigFromClipboard(::onImportResult)
                },
                modifier = Modifier.fillMaxWidth(),
            )

            SettingCard {
                OneBoxOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = draft.title,
                    onValueChange = { value ->
                        component.updateDraft { engine ->
                            engine.copy(
                                title = value,
                                name = if (serviceKeyEdited) engine.name else deriveAutoServiceKey(value),
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
                                            it.copy(name = deriveAutoServiceKey(it.title))
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
                    // 新增引擎的真实场景是自建/中转服务, 核心动作是选 API 兼容协议,
                    // 协议选择器放在首屏显眼位置, 不再折叠
                    ProtocolSelector(component = component, draft = draft)

                    if (!isJevProtocol && !isPikafishProtocol) {
                        CollapsibleSectionHeader(
                            title = stringResource(R.string.ai_engine_auth_type_label),
                            expanded = authExpanded,
                            onToggle = { authExpanded = !authExpanded },
                            summary = authTypeLabel(draft.authType),
                        )
                        AnimatedVisibility(visible = authExpanded) {
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
                    }

                    if (isJevProtocol) {
                        Text(
                            text = stringResource(R.string.ai_engine_jev_proxy_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    CloudUrlPathFields(
                        component = component,
                        draft = draft,
                        isRequestUrlError = isRequestUrlError,
                        isRequestPathError = isRequestPathError,
                        trimmedRequestUrl = trimmedRequestUrl,
                    )

                    TokenField(component = component, draft = draft)

                    // 自定义服务的模型 ID 客户端猜不出来。填了这里, 新引擎加完就能直接发消息;
                    // 留空则只能靠 AiProvider.Default 的兜底模型(名字和真实服务对不上)。
                    if (!isJevProtocol && !isPikafishProtocol) {
                        ModelIdField(component = component, draft = draft)
                    }
                }
            }

            // 保存后可选把新引擎设为当前对话引擎(默认关, 见 F1)
            SettingCard {
                OneBoxListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.ai_engine_set_current_after_save),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    contained = true,
                    trailingContent = {
                        GlassSwitch(
                            checked = setCurrentAfterSave,
                            onCheckedChange = { setCurrentAfterSave = it },
                            colors = AppTheme.colors.switchColors()
                        )
                    }
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
                // 保存前再规范化一次, 防止用户没移开焦点就点保存;
                // 校验用规范化后的值本地判断, 不依赖组合里尚未重组的 draft
                val normalizedUrl = StringUtils.normalizeApiBaseUrl(draft.requestUrl)
                val normalizedPath = StringUtils.normalizeApiPath(draft.requestPath)
                component.updateDraft { e ->
                    e.copy(requestUrl = normalizedUrl, requestPath = normalizedPath)
                }
                showValidationErrors = true
                val saveTitle = draft.title.trim()
                val skipCloud = isLocalProtocol || isJevProtocol || isPikafishProtocol
                val invalid = saveTitle.isBlank() ||
                    (!skipCloud && (
                        normalizedUrl.isBlank() ||
                            !StringUtils.isValidUrl(normalizedUrl) ||
                            normalizedPath.isBlank()
                        ))
                if (invalid) {
                    coroutineScope.launch {
                        AppToastHost.showFailureToast(
                            getString(R.string.ai_engine_dialog_validation_failed)
                        )
                    }
                    return@BottomSaveCancelBar
                }
                // 服务标识留空时自动派生; 纯中文标题派生不出可用的 slug, 退化为时间戳后缀兜底
                val resolvedName = draft.name.trim().ifBlank {
                    deriveAutoServiceKey(saveTitle).ifBlank {
                        "engine-${System.currentTimeMillis() % 1_000_000}"
                    }
                }
                if (resolvedName != draft.name) {
                    component.updateDraft { it.copy(name = resolvedName) }
                }
                component.save { result ->
                    coroutineScope.launch {
                        when (result) {
                            AddEngineResult.Success -> {
                                AppToastHost.showToast(
                                    getString(R.string.ai_engine_add_success, draft.title.ifBlank { draft.name })
                                )
                                // 设为当前引擎失败不阻塞退出, 仅提示
                                if (setCurrentAfterSave && !component.setDraftAsCurrentEngine()) {
                                    AppToastHost.showFailureToast(
                                        getString(R.string.ai_engine_set_default_failed)
                                    )
                                }
                                component.onGoBack()
                            }
                            // 名称重复是可以自己解决的, 别让用户对着"请稍后重试"反复重试;
                            // 服务标识在"更多选项"折叠区里, 自动展开让用户看到要改的字段(F18)
                            AddEngineResult.NameTaken -> {
                                moreExpanded = true
                                AppToastHost.showFailureToast(
                                    getString(R.string.ai_engine_name_conflict)
                                )
                            }
                            AddEngineResult.NotVisible,
                            AddEngineResult.Failed -> AppToastHost.showFailureToast(
                                getString(R.string.ai_engine_add_failed)
                            )
                        }
                    }
                }
            }
        )
    }

    if (showExitConfirmDialog) {
        com.wanbaohe.core.ui.widget.dialogs.ExitWithoutSavingDialog(
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

/**
 * 协议选择器(首屏常驻, 不再折叠)。
 *
 * 仅展示云端协议；LOCAL_ON_DEVICE 由独立的"本地模型管理"页处理（Phase 2）;
 * 锁定条件看入口协议而非当前草稿协议(F4): 从 Jev/Pikafish 专属入口进来(protocolLocked)
 * 时只展示一个固定选中项; 普通入口始终展示全量列表、可自由来回切换, 切到 Jev 的
 * 预填由 Component 的 updateRequestProtocol 负责(与详情页同一套)。
 * Jev(TypeSafe) 国内无备案, 协议选择器仅海外暴露;
 * 「应用代理」是内置中转链路、Pikafish 是象棋专用通道, 均不支持用户自建, 不作为可选协议暴露。
 */
@Composable
private fun ProtocolSelector(
    component: AIAddEngineComponent,
    draft: AiEngine,
) {
    val jevAllowed = com.shifenmiao.model.channel.FlavorType.fromName().isOverseas
    val selectableProtocols = when {
        component.protocolLocked -> listOf(draft.requestProtocol)
        else -> AiRequestProtocol.cloudProtocols.filter {
            it != AiRequestProtocol.OWN_PROXY &&
                it != AiRequestProtocol.PIKAFISH &&
                (it != AiRequestProtocol.JEV || jevAllowed)
        }
    }

    Text(
        text = stringResource(R.string.ai_engine_protocol_label),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(selectableProtocols) { protocol ->
            SelectGridCard(
                title = protocolLabel(protocol),
                subtitle = null,
                isSelected = draft.requestProtocol == protocol,
                onClick = {
                    // 锁定入口或点击已选中的 chip 都是无操作(F3):
                    // 默认值靠进页预填拿到, 不靠"再点一次"
                    if (component.protocolLocked || draft.requestProtocol == protocol) {
                        return@SelectGridCard
                    }
                    component.updateRequestProtocol(protocol)
                },
                // LazyRow 主轴无界, SelectGridCard 内部 weight(1f) 会塌成 0 宽,
                // 用内容固有宽度定宽才能正确测量文字
                modifier = Modifier.width(IntrinsicSize.Max),
            )
        }
    }
}

/** URL/Path 字段(失焦时规范化) */
@Composable
private fun CloudUrlPathFields(
    component: AIAddEngineComponent,
    draft: AiEngine,
    isRequestUrlError: Boolean,
    isRequestPathError: Boolean,
    trimmedRequestUrl: String,
) {
    // URL 示例按协议给(F3): 用户照着抄结构, 不用猜各服务商的 baseUrl 形状
    val urlExample = when (draft.requestProtocol) {
        AiRequestProtocol.ANTHROPIC_COMPATIBLE -> "https://api.anthropic.com/"
        AiRequestProtocol.JEV -> UrlConstants.TYPESAFE_AI_BASE_URL
        else -> "https://api.openai.com/"
    }
    OneBoxOutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    component.updateDraft { e ->
                        val normalized = StringUtils.normalizeApiBaseUrl(e.requestUrl)
                        if (normalized != e.requestUrl) {
                            e.copy(requestUrl = normalized, isUrlError = false, isDetestPassed = false)
                        } else {
                            e
                        }
                    }
                }
            },
        value = draft.requestUrl,
        onValueChange = {
            // 改 URL 连同已验证标记一并复位(F10), 与详情页 updateRequestUrl 行为对齐
            component.updateDraft { e ->
                e.copy(requestUrl = it, isUrlError = false, isDetestPassed = false)
            }
        },
        label = { Text(stringResource(R.string.ai_engine_api_url)) },
        trailingIcon = {
            ClearTextFieldTrailingIcon(
                value = draft.requestUrl,
                onClear = {
                    component.updateDraft {
                        it.copy(requestUrl = "", isUrlError = false, isDetestPassed = false)
                    }
                },
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
            } else {
                Column {
                    Text(stringResource(R.string.ai_engine_api_url_hint))
                    Text(stringResource(R.string.ai_engine_api_url_example, urlExample))
                }
            }
        },
    )
    OneBoxOutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    component.updateDraft { e ->
                        val normalized = StringUtils.normalizeApiPath(e.requestPath)
                        if (normalized != e.requestPath) {
                            e.copy(requestPath = normalized)
                        } else {
                            e
                        }
                    }
                }
            },
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
}

/** Token 输入 + 按 key 前缀识别的协议切换提示 chip(识别不了就不提示, 绝不自动切换) */
@Composable
private fun TokenField(
    component: AIAddEngineComponent,
    draft: AiEngine,
) {
    PasswordTextField(
        modifier = Modifier.fillMaxWidth(),
        value = draft.authorizationCode,
        onValueChange = { raw ->
            val cleaned = StringUtils.sanitizeApiToken(raw)
            component.updateDraft { e -> e.copy(authorizationCode = cleaned) }
        },
        label = stringResource(R.string.ai_engine_auth_token),
        onClearValue = { component.updateDraft { it.copy(authorizationCode = "") } },
        imeAction = ImeAction.Done,
    )

    // protocolLocked(Jev/Pikafish 专属入口)下点了也切不动, chip 直接不渲染(R7)
    val detectedProtocol = component.detectTokenProtocol(draft.authorizationCode)
    if (!component.protocolLocked &&
        detectedProtocol != null && detectedProtocol != draft.requestProtocol
    ) {
        GlassSurface(
            onClick = { component.updateRequestProtocol(detectedProtocol) },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer),
        ) {
            Text(
                text = stringResource(
                    R.string.ai_engine_token_detected_protocol_hint,
                    protocolLabel(detectedProtocol),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

/** 模型 ID 输入: 显示名自动派生, 用户只需填/选模型 ID */
@Composable
private fun ModelIdField(
    component: AIAddEngineComponent,
    draft: AiEngine,
) {
    OneBoxOutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = draft.model.name,
        onValueChange = { value ->
            component.updateDraft { engine ->
                val autoTitle = AIEngineSettingsDetailComponent.prettifyModelTitle(value)
                val titleWasAuto = engine.model.title.isBlank() ||
                    engine.model.title == AIEngineSettingsDetailComponent.prettifyModelTitle(engine.model.name)
                engine.copy(
                    model = engine.model.copy(
                        name = value,
                        title = if (titleWasAuto) autoTitle else engine.model.title,
                    )
                )
            }
        },
        label = { Text(stringResource(R.string.ai_engine_model_name)) },
        singleLine = true,
        supportingText = {
            Text(stringResource(R.string.ai_engine_model_name_hint))
        },
    )
}
