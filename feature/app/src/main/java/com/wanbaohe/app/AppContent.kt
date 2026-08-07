package com.wanbaohe.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.ai.component.AgentToolCallUIState
import com.shifenmiao.base.ui.ConfirmDialog
import com.shifenmiao.base.utils.CoreUtils
import com.shifenmiao.common.components.FloatingRobotDragController
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.PrivacyPolicyDialog
import com.shifenmiao.common.ui.ai.AIModelsPickerBottomSheet
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.utils.getString
import kotlinx.coroutines.launch
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.login.BindPhoneSheet
import com.shifenmiao.login.ModalBottomSheetLogin
import com.shifenmiao.model.login.LoginChannelConfig
import com.shifenmiao.model.login.LoginState
import com.shifenmiao.model.state.UIState
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.webview.mermaid.ProvideMermaidRenderer
import com.t8rin.imagetoolbox.core.settings.domain.toSimpleSettingsInteractor
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.t8rin.imagetoolbox.core.domain.startup.StartupPhase
import com.t8rin.imagetoolbox.core.domain.startup.StartupPhaseController
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.findActivity
import com.t8rin.imagetoolbox.core.ui.utils.helper.isShellPortraitOrientationAsState
import com.t8rin.imagetoolbox.feature.root.presentation.components.BlogModalBottomSheet
import com.t8rin.imagetoolbox.feature.root.presentation.components.utils.uiSettingsState
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import com.wanbaohe.app.provider.ImageToolboxCompositionLocals
import com.wanbaohe.app.provider.LoginStateCompositionLocals
import com.wanbaohe.app.screen.ScreenSelector
import com.wanbaohe.app.ui.GlobalAIHost
import com.wanbaohe.app.ui.StartupTraceOverlay
import com.wanbaohe.app.ui.GlobalToolInteractionHost
import com.wanbaohe.app.ui.WebViewModalBottomSheet
import com.wanbaohe.profile.screen.BuyCoffeeDialogModalSheet
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun AppContent(
    rootComponent: RootComponent,
    appComponent: AppComponent,
    startupPhaseController: StartupPhaseController,
) {
    ImageToolboxCompositionLocals(
        settingsState = rootComponent.uiSettingsState(),
        component = rootComponent,
        simpleSettingsInteractor = rootComponent.settingsManager.toSimpleSettingsInteractor()
    ) {
        val context = LocalContext.current
        val isPortrait by isShellPortraitOrientationAsState()
        val showAIChatOverlay = appComponent.collectUiStateField(initial = false) { it.initAIChatAndShow }
        val isInPipMode = appComponent.collectUiStateField(initial = false) { it.isInPipMode }
        val childStack by rootComponent.childStack.subscribeAsState()
        val hasPersistentAIPane = !isPortrait

        // 启动阶段状态机：由 StartupPhaseController 统一调度。
        // 各阶段定义见 StartupPhase 文档。
        val startupPhase by startupPhaseController.phase.collectAsState()

        LaunchedEffect(appComponent) {
            StartupTrace.markOnce("app_content_attached", "AppContent.attached")
            appComponent.startPostStartupInitIfNeeded()
            StartupTrace.markOnce("app_content_first_frame", "AppContent.first_frame")

            // 首帧 composition 完成后推进到 CONTENT_HYDRATED，触发真实业务内容填充
            startupPhaseController.advanceTo(StartupPhase.CONTENT_HYDRATED)

            context.findActivity()?.reportFullyDrawn()
            StartupTrace.markOnce("report_fully_drawn", "Activity.reportFullyDrawn")

            // Overlay 在内容填充后挂载
            startupPhaseController.advanceTo(StartupPhase.OVERLAY_MOUNTED)
        }

        val isAITabChatScreen =
            childStack.active.configuration is com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.AITabChatScreen

        // 全局 AI 宿主保持懒挂载：
        // - AI 浮层首次打开时初始化
        // - 进入 AITabChatScreen 时初始化
        // 一旦初始化，后续关闭浮层也继续保活，保证流式输出和工具等待状态能在后台延续。
        val shouldAttachGlobalAIHost = rememberShouldAttachGlobalAIHost(
            showAIChatOverlay = showAIChatOverlay,
            isAITabChatScreen = isAITabChatScreen,
            hasPersistentAIPane = hasPersistentAIPane,
        )

        // 工具交互宿主比聊天宿主更轻，但当前工具确认/表单请求仍主要由全局 AI 链路触发，
        // 因此先沿用同一套首次激活条件；后续若接入后台 Agent/非聊天入口，
        // 可以把这里扩展成独立的触发条件而不影响聊天宿主。
        val shouldAttachGlobalToolInteractionHost = rememberShouldAttachGlobalToolInteractionHost(
            showAIChatOverlay = showAIChatOverlay,
            isAITabChatScreen = isAITabChatScreen,
            hasPersistentAIPane = hasPersistentAIPane,
        )

        // 机器人悬浮层需要感知流式输出，但仍然保持懒获取：
        // 只有全局 AI 宿主已被启用后，才真正持有全局 AIChatComponent。
        val aiChatComponent: AIChatComponent? = if (shouldAttachGlobalAIHost) {
            rootComponent.globalAIChatComponent
        } else null

        // 只订阅机器人状态，其余流在下方按需订阅
        val robotState by appComponent.robotState.collectAsState()

        AppShellContent(
            rootComponent = rootComponent,
            appComponent = appComponent,
            isPortrait = isPortrait,
            isInPipMode = isInPipMode,
            showAIChatOverlay = showAIChatOverlay,
            isAITabChatScreen = isAITabChatScreen,
            shouldAttachGlobalAIHost = shouldAttachGlobalAIHost,
            shouldAttachGlobalToolInteractionHost = shouldAttachGlobalToolInteractionHost,
            aiChatComponent = aiChatComponent,
            robotState = robotState,
        )

        if (startupPhase.isAtLeast(StartupPhase.OVERLAY_MOUNTED)) {
            AppOverlayHost(
                rootComponent = rootComponent,
                appComponent = appComponent
            )
        }
    }
}

@Composable
private fun AppShellContent(
    rootComponent: RootComponent,
    appComponent: AppComponent,
    isPortrait: Boolean,
    isInPipMode: Boolean,
    showAIChatOverlay: Boolean,
    isAITabChatScreen: Boolean,
    shouldAttachGlobalAIHost: Boolean,
    shouldAttachGlobalToolInteractionHost: Boolean,
    aiChatComponent: AIChatComponent?,
    robotState: com.shifenmiao.model.state.RobotState,
) {
    // ── PiP 模式：只显示机器人，跳过所有导航 UI ───────────────────────
    if (isInPipMode) {
        // PiP 窗口比全屏小得多，保存的 percent 值是按全屏容器计算的，直接用会越界。
        // 重置到安全的居中位置：percentX=0.5（水平居中）percentY=0.3（偏上）。
        // hasBeenDragged=false 确保 DragController 不走拖拽偏移逻辑。
        // scale=1.0 避免标题栏收缩时记录的 0.6 倍缩放在小窗里让机器人过小。
        val pipRobotState = robotState.copy(
            visible = true,
            isInitialized = true,
            hasComputedInitialPosition = true,
            hasBeenDragged = false,
            offsetXPercent = 0.5f,
            offsetYPercent = 0.7f,
            scale = 1.0f,
            isDragging = false
        )
        StreamingRobotController(
            appComponent = appComponent,
            robotState = pipRobotState,
            aiChatComponent = aiChatComponent,
            isViewingAIChat = false
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppNavigationHost(
            rootComponent = rootComponent,
            appComponent = appComponent,
        )

        if (shouldAttachGlobalAIHost) {
            GlobalAIHost(rootComponent, appComponent, showAIChatOverlay, isPortrait)
        }

        if (shouldAttachGlobalToolInteractionHost) {
            GlobalToolInteractionHost(
                rootComponent = rootComponent,
                appComponent = appComponent
            )
        }

        // 唯一的机器人实例：只有 enabled 时才入组合树，流式状态在内部按需订阅
        // isViewingAIChat：用户正在查看聊天界面时不应把机器人叠加其上，
        // 包括聊天浮层已展开（showAIChatOverlay）和 AITabChatScreen 全屏展示两种情况。
        if (!robotState.disabled) {
            StreamingRobotController(
                appComponent = appComponent,
                robotState = robotState,
                aiChatComponent = aiChatComponent,
                isViewingAIChat = showAIChatOverlay || isAITabChatScreen
            )
        }

        StartupTraceOverlay()
    }
}

@Composable
private fun AppNavigationHost(
    rootComponent: RootComponent,
    appComponent: AppComponent,
) {
    val loginState by rootComponent.loginStateHolder.loginState.collectAsState()

    LoginStateCompositionLocals(loginState = loginState) {
        ScreenSelector(
            rootComponent = rootComponent,
            appComponent = appComponent,
        )
    }
}

@Composable
private fun AppOverlayHost(
    rootComponent: RootComponent,
    appComponent: AppComponent,
) {
    val loginState by rootComponent.loginStateHolder.loginState.collectAsState()
    val showAIModelsModalSheet = appComponent.collectUiStateField(initial = false) { it.showAIModelsModalSheet }
    val showBuyCoffeeDialog = appComponent.collectUiStateField(initial = false) { it.showBuyCoffeeDialog }
    val blogId = appComponent.collectUiStateField(initial = 0) { it.blogId }
    val showPermissionDialog = appComponent.collectUiStateField(initial = false) { it.showPermissionDialog }
    val openWebView = appComponent.collectUiStateField(initial = null) { it.openWebView }
    val showLogin = rootComponent.loginStateHolder.collectLoginStateField(initial = false) { it.showLogin }
    val showBind = rootComponent.loginStateHolder.collectLoginStateField(initial = false) { it.showBind }
    val authCodeState by rootComponent.authorizationCodeStateHolder.state.collectAsState()

    if (showAIModelsModalSheet) {
        GlobalAIModelsModalSheet(appComponent = appComponent)
    }

    if (showBuyCoffeeDialog) {
        val uiState by appComponent.uiState.collectAsState()
        LoginStateCompositionLocals(loginState = loginState) {
            BuyCoffeeDialogModalSheet(
                uiState = uiState,
                appComponent = appComponent,
                loginComponent = rootComponent.loginComponent,
                payComponent = rootComponent.payComponent
            )
        }
    }

    if (showLogin) {
        LoginStateCompositionLocals(loginState = loginState) {
            ModalBottomSheetLogin(rootComponent.loginComponent)
        }
    }

    if (showBind) {
        LoginStateCompositionLocals(loginState = loginState) {
            BindPhoneSheet(
                rootComponent.loginComponent,
                appComponent
            )
        }
    }

    if (authCodeState.showAuthCode) {
        com.t8rin.imagetoolbox.core.ui.widget.enhanced.AuthorizationCodeScreen(
            mode = authCodeState.mode,
            setupStep = authCodeState.setupStep,
            onSubmit = rootComponent.authorizationCodeStateHolder::submit,
            onCancel = rootComponent.authorizationCodeStateHolder::cancel,
            error = authCodeState.error,
        )
    }

    if (blogId > 0) {
        BlogModalBottomSheet(
            appComponent = appComponent,
            rootComponent = rootComponent,
            blogId = blogId
        )
    }

    LaunchedEffect(loginState) {
        // 海外渠道不提供绑定手机功能, 不受远端 forceBindPhone 影响
        if (LoginChannelConfig.getConfigByFlavor().bindPhoneSupported &&
            RemoteConfigStorage.getRemoteConfig().forceBindPhone == true
        ) {
            if (loginState.isLogin && loginState.phone.trim().isEmpty()) {
                rootComponent.loginComponent.showBindPhone()
            }
        }
    }

    if (showPermissionDialog) {
        val showPermissionDialogState = remember { mutableStateOf(true) }
        appComponent.requestPermissionEvent?.let { requestPermissionEvent ->
            if (requestPermissionEvent.permissionRequest.description.isNotEmpty()) {
                ConfirmDialog(
                    showDialog = showPermissionDialogState,
                    title = requestPermissionEvent.permissionRequest.title,
                    message = requestPermissionEvent.permissionRequest.description,
                    confirmButtonText = stringResource(id = R.string.button_request_permission),
                    dismissButtonText = stringResource(id = R.string.button_cancel),
                    onConfirm = {
                        requestPermissionEvent.onRequest()
                    },
                    onDismiss = {
                        appComponent.hidePromptPermissionDialog()
                        requestPermissionEvent.onFailed()
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                )
            }
        }
    }

    val showPrivacyPolicyDialog = remember { mutableStateOf(CoreUtils.isShowPrivacyPolicyDialog()) }
    ProvideMermaidRenderer {
        PrivacyPolicyDialog(
            showPrivacyPolicyDialog,
            onConfirm = {
                appComponent.agreeFirstLaunch()
            },
            onUrlClick = { url ->
                appComponent.showWebView(
                    webViewParams = WebViewParams(
                        url = url,
                        title = AppContext.getString(R.string.privacy_title),
                        enableShare = false,
                        enableCustomTouch = false
                    )
                )
            }
        )
    }

    openWebView?.let { webViewParams ->
        val webViewComponent = remember(webViewParams) {
            rootComponent.childProvider.homeFactories.webViewComponentFactory(
                componentContext = rootComponent.componentContext,
                webViewParams = webViewParams,
                onGoBack = {
                    appComponent.hideWebView()
                }
            )
        }
        WebViewModalBottomSheet(
            appComponent = appComponent,
            webViewComponent = webViewComponent,
        )
    }

}


@Composable
private fun rememberShouldAttachGlobalAIHost(
    showAIChatOverlay: Boolean,
    isAITabChatScreen: Boolean,
    hasPersistentAIPane: Boolean,
): Boolean {
    var hasGlobalAIHostBeenInitialized by rememberSaveable { mutableStateOf(false) }

    if (!hasGlobalAIHostBeenInitialized && (showAIChatOverlay || isAITabChatScreen || hasPersistentAIPane)) {
        hasGlobalAIHostBeenInitialized = true
    }

    return hasGlobalAIHostBeenInitialized
}

@Composable
private fun rememberShouldAttachGlobalToolInteractionHost(
    showAIChatOverlay: Boolean,
    isAITabChatScreen: Boolean,
    hasPersistentAIPane: Boolean,
): Boolean {
    var hasGlobalToolInteractionHostBeenInitialized by rememberSaveable { mutableStateOf(false) }

    if (!hasGlobalToolInteractionHostBeenInitialized && (showAIChatOverlay || isAITabChatScreen || hasPersistentAIPane)) {
        hasGlobalToolInteractionHostBeenInitialized = true
    }

    return hasGlobalToolInteractionHostBeenInitialized
}

/**
 * 机器人悬浮层的状态收集点。
 *
 * 只有机器人实际入组合树且 [aiChatComponent] 已初始化时才订阅 [AIChatComponent] 的流式状态，
 * 避免 [AppContent] 在 AIChatComponent 尚未创建时浪费订阅开销。
 *
 * @param isViewingAIChat 用户当前是否正在查看聊天界面（overlay 展开或 AITabChatScreen 全屏）。
 *   为 true 时不把机器人叠加在聊天 UI 之上，也不触发强制可见逻辑。
 */
@Composable
private fun StreamingRobotController(
    appComponent: AppComponent,
    robotState: com.shifenmiao.model.state.RobotState,
    aiChatComponent: AIChatComponent?,
    isViewingAIChat: Boolean
) {
    if (aiChatComponent != null) {
        val chatUIState by aiChatComponent.chatUIState.collectAsState()
        val streamingText by aiChatComponent.streamingAnswerText.collectAsState()
        val agentToolCallStatus by aiChatComponent.agentToolCallStatus.collectAsState()

        // 聊天活跃且用户不在聊天界面时展示流式文字；否则置 null 恢复机器人默认泡泡
        val isChatActiveOutsideView = chatUIState.chatActive && !isViewingAIChat

        // 当流式文本为空时，根据 Agent 状态派生提示文字（让用户知道发生了什么）
        val agentStatusHint: String? = if (isChatActiveOutsideView && streamingText.isEmpty()) {
            when (val s = agentToolCallStatus) {
                is AgentToolCallUIState.WaitingUserInput ->
                    if (s.requestType == "CONFIRMATION") "需要你确认操作" else "需要你回答问题"
                is AgentToolCallUIState.Planning -> "规划工具调用中…"
                is AgentToolCallUIState.Executing ->
                    s.currentToolName?.let { "执行: $it" } ?: "工具执行中…"
                is AgentToolCallUIState.WaitingLLM -> "等待 AI 回复…"
                is AgentToolCallUIState.MaxIterationsReached -> "等待你的指示"
                is AgentToolCallUIState.Idle -> null
            }
        } else null

        val streamingMessage = if (isChatActiveOutsideView) {
            streamingText.ifEmpty { agentStatusHint }
        } else null

        // 当 AI 流式输出进行中且用户已退出聊天界面时，强制让机器人可见。
        LaunchedEffect(isChatActiveOutsideView) {
            if (isChatActiveOutsideView && !robotState.disabled) {
                appComponent.forceShowRobotForStreaming()
            }
        }

        FloatingRobotDragController(
            appComponent = appComponent,
            robotState = robotState,
            isOutsideChatView = !isViewingAIChat,
            streamingMessage = streamingMessage,
        )
    } else {
        FloatingRobotDragController(
            appComponent = appComponent,
            robotState = robotState,
            isOutsideChatView = !isViewingAIChat,
            streamingMessage = null
        )
    }
}

@Composable
private inline fun <T> AppComponent.collectUiStateField(
    initial: T,
    crossinline selector: (UIState) -> T,
): T {
    val flow = remember(this) {
        uiState.map(selector).distinctUntilChanged()
    }

    return flow.collectAsState(initial = initial).value
}

@Composable
private fun GlobalAIModelsModalSheet(
    appComponent: AppComponent,
) {
    val allEngines by appComponent.aiEngineCatalogManager.observeAvailableEngines()
        .collectAsState(initial = emptyList())
    val currentAIEngine by appComponent.aiEngineManager.currentAIEngine.collectAsState()
    val currentAIModel by appComponent.aiEngineManager.currentAIModel.collectAsState()
    val modelsByProvider by appComponent.aiEngineCatalogManager.observeModelsByProvider()
        .collectAsState(initial = emptyMap())

    val isRefreshing = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AIModelsPickerBottomSheet(
        visible = true,
        allEngines = allEngines,
        modelsByProvider = modelsByProvider,
        selectedEngineName = currentAIEngine.identityKey(),
        selectedModelName = currentAIModel.name,
        title = stringResource(id = R.string.ai_bottom_sheet_title),
        showRefresh = true,
        isRefreshing = isRefreshing.value,
        onRefresh = {
            if (!isRefreshing.value) {
                isRefreshing.value = true
                appComponent.aiEngineSyncManager.refreshEnginesFromRemote(
                    forceUpdate = true,
                    onSuccess = {
                        isRefreshing.value = false
                        coroutineScope.launch {
                            AppToastHost.showToast(
                                getString(R.string.ai_refresh_success)
                            )
                        }
                    },
                    onFailure = {
                        isRefreshing.value = false
                        coroutineScope.launch {
                            AppToastHost.showFailureToast(
                                getString(R.string.ai_refresh_failed)
                            )
                        }
                    }
                )
            }
        },
        showSettings = RemoteConfigStorage.getRemoteConfig().aiCanSetting == true,
        onSettings = { appComponent.showAIChatSettings() },
        onSelected = { engine, model ->
            appComponent.aiEngineManager.switchModel(engine, model)
            appComponent.hideAIModelsModalSheet()
        },
        onDismiss = { appComponent.hideAIModelsModalSheet() },
    )
}

@Composable
private inline fun <T> com.shifenmiao.login.state.LoginStateHolder.collectLoginStateField(
    initial: T,
    crossinline selector: (LoginState) -> T,
): T {
    val flow = remember(this) {
        loginState.map(selector).distinctUntilChanged()
    }

    return flow.collectAsState(initial = initial).value
}

