package com.wanbaohe.decisionwheel.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.shifenmiao.database.decision_wheel.entity.WheelHistoryEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.com.color.ColorGenerator
import com.wanbaohe.decisionwheel.data.DecisionWheelPresetsProvider
import com.wanbaohe.decisionwheel.data.WheelRepository
import com.wanbaohe.decisionwheel.data.WheelSettingsHolder
import com.wanbaohe.decisionwheel.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import com.shifenmiao.theme.AppTheme

/**
 * 底部 tab：转盘是主目的地，历史与设置是平级入口 —— 三者之间横切，不进返回栈。
 */
enum class WheelTab { SPIN, HISTORY, SETTINGS }

/**
 * 决策转盘模块的内部路由。
 *
 * 分两层：
 * - **底部 tab**（[WheelTab]）：转盘 / 历史 / 设置。横切切换，不进返回栈，切回来转盘角度与结果都还在。
 * - **返回栈**（[Route]）：编辑、我的转盘。从转盘页压入，覆盖整个 tab 区域，返回键 pop 回来。
 *
 * 之前全靠 `showXxx: Boolean` 驱动的 Dialog 与 BottomSheet，导致
 * 返回键在弹窗打开时直接退出整个模块、编辑流程弹窗套弹窗、状态无法在进程重建后恢复。
 */
class DecisionWheelRouterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onGoBack: () -> Unit,
    @Assisted private val onNavigate: (com.wanbaohe.core.ui.utils.navigation.Screen) -> Unit,
    @Assisted private val initialWheelId: String?,
    private val repository: WheelRepository,
    private val presetsProvider: DecisionWheelPresetsProvider,
    private val settingsHolder: WheelSettingsHolder,
    spinFactory: DecisionWheelSpinComponent.Factory,
    private val editorFactory: DecisionWheelEditorComponent.Factory,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    sealed interface Child {
        data class Spin(val component: DecisionWheelSpinComponent) : Child
        data class Editor(val component: DecisionWheelEditorComponent) : Child
        data object WheelList : Child
    }

    @Serializable
    sealed interface Route {
        @Serializable
        @SerialName("Spin")
        data object Spin : Route

        @Serializable
        @SerialName("Editor")
        data class Editor(val wheelId: String) : Route

        @Serializable
        @SerialName("WheelList")
        data object WheelList : Route
    }

    private val _currentTab = MutableStateFlow(WheelTab.SPIN)
    val currentTab: StateFlow<WheelTab> = _currentTab.asStateFlow()

    /**
     * Spin 是栈底常驻页：保留它的实例，旋转角度与结果态才不会在页面切换后丢失。
     */
    val spinComponent: DecisionWheelSpinComponent = spinFactory(
        componentContext = componentContext.childContext("decision_wheel_spin"),
        onGoBack = ::handleSpinBack,
        onOpenWheelList = ::openWheelList,
        onNavigate = ::handleSpinNavigation
    )

    private val navigation = StackNavigation<Route>()

    val childStack: Value<ChildStack<Route, Child>> = childStack(
        source = navigation,
        serializer = Route.serializer(),
        initialConfiguration = Route.Spin,
        handleBackButton = true,
        childFactory = ::createChild
    )

    // ─── 共享数据（列表页 / 历史页用） ──────────────────────────────────────

    val wheels: StateFlow<List<DecisionWheel>> = repository.getAllWheels()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history: StateFlow<List<WheelHistoryEntity>> = repository.observeAllHistory()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val settings: StateFlow<WheelSettings> = settingsHolder.settings

    init {
        // 深链直达(onebox://screen/decision_wheel?wheel_id=xxx):
        // 确认转盘存在再切到它的转动页;不存在则静默走默认流程。
        // Route.Spin 本来就是栈底常驻页,只需切换当前转盘,不用动路由栈。
        if (!initialWheelId.isNullOrBlank()) {
            componentScope.launch {
                val wheel = repository.getWheelById(initialWheelId)
                if (wheel != null) {
                    spinComponent.switchWheel(wheel.id)
                    _currentTab.value = WheelTab.SPIN
                }
            }
        }
    }

    // ─── 导航 ──────────────────────────────────────────────────────────────

    fun openWheelList() {
        navigation.pushNew(Route.WheelList)
    }

    fun switchTab(tab: WheelTab) {
        _currentTab.value = tab
    }

    /**
     * 非首 tab 按返回先回到转盘 tab，符合底部导航的返回预期；
     * 已经在转盘 tab 才退出整个模块。
     */
    fun onTabBack() {
        if (_currentTab.value != WheelTab.SPIN) switchTab(WheelTab.SPIN) else onGoBack()
    }

    fun openEditor() {
        val wheelId = spinComponent.uiState.value.currentWheel?.id ?: return
        navigation.pushNew(Route.Editor(wheelId))
    }

    fun navigateBack() {
        navigation.pop()
    }

    /**
     * 带填充词跳到 AI 助手 Tab。
     *
     * 与经期 / 笔记 / 记账等模块走同一条路：构造一个 ASSISTANT 会话，
     * 把填充词塞进 [Conversation.template]，AI 助手页会预填到输入框。
     */
    fun openAiAssistant(prompt: String) {
        onNavigate(
            Screen.AITabChatScreen(
                Conversation(
                    entryType = AIConversationEntryType.ASSISTANT,
                    title = AppContext.getString(R.string.ai_assistant_title),
                    template = prompt
                )
            )
        )
    }

    private fun handleSpinNavigation(destination: SpinDestination) {
        when (destination) {
            SpinDestination.Editor -> openEditor()
            SpinDestination.History -> switchTab(WheelTab.HISTORY)
            SpinDestination.Settings -> switchTab(WheelTab.SETTINGS)
        }
    }

    private fun handleSpinBack() {
        if (childStack.value.items.size > 1) navigation.pop() else onGoBack()
    }

    private fun createChild(route: Route, context: ComponentContext): Child = when (route) {
        Route.Spin -> Child.Spin(spinComponent)
        is Route.Editor -> Child.Editor(
            editorFactory(
                componentContext = context,
                wheelId = route.wheelId,
                onGoBack = ::navigateBack,
                onOpenAiAssistant = ::openAiAssistant
            )
        )

        Route.WheelList -> Child.WheelList
    }

    // ─── 转盘列表操作 ──────────────────────────────────────────────────────

    fun selectWheel(wheelId: String) {
        spinComponent.switchWheel(wheelId)
        navigateBack()
    }

    fun createWheel() {
        componentScope.launch {
            val baseColor = Color(AppTheme.colorScheme.primaryContainer.toArgb())
            val names = presetsProvider.defaultNewWheelOptionNames()
            val colors = ColorGenerator.generateSegmentBackgrounds(baseColor, names.size)
            val wheel = DecisionWheel(
                title = presetsProvider.defaultNewWheelTitle(),
                options = names.mapIndexed { i, name ->
                    WheelOption(name = name, color = colors.getOrNull(i) ?: baseColor)
                }
            )
            repository.saveWheel(wheel)
            spinComponent.switchWheel(wheel.id)
            openEditor()
        }
    }

    fun duplicateWheel(wheelId: String) {
        componentScope.launch {
            val source = repository.getWheelById(wheelId) ?: return@launch
            val copy = DecisionWheel(
                title = source.title + presetsProvider.copyTitleSuffix(),
                options = source.options.map { it.copy(id = UUID.randomUUID().toString()) },
                createdAt = System.currentTimeMillis()
            )
            repository.saveWheel(copy)
        }
    }

    fun deleteWheel(wheelId: String) {
        componentScope.launch {
            repository.deleteWheel(wheelId)
            if (spinComponent.uiState.value.currentWheel?.id == wheelId) {
                val first = repository.getAllWheels().firstOrNull()?.firstOrNull()
                first?.let { spinComponent.switchWheel(it.id) }
            }
        }
    }

    fun deleteHistory(historyId: Long) {
        componentScope.launch { repository.deleteHistory(historyId) }
    }

    fun clearHistory() {
        componentScope.launch { repository.clearHistory() }
    }

    fun updateSettings(settings: WheelSettings) {
        settingsHolder.update(settings)
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (com.wanbaohe.core.ui.utils.navigation.Screen) -> Unit,
            initialWheelId: String?
        ): DecisionWheelRouterComponent
    }
}
