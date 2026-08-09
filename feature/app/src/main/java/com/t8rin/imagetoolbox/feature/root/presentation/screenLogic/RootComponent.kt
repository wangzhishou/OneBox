/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.root.presentation.screenLogic

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.ai.component.GlobalToolUiHost
import com.shifenmiao.base.utils.Navigation
import com.shifenmiao.common.handle.UrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.model.ExtraDataType
import com.t8rin.imagetoolbox.core.domain.model.ImageModel
import com.t8rin.imagetoolbox.core.domain.remote.AnalyticsManager
import com.t8rin.imagetoolbox.core.domain.resource.ResourceManager
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.settings.domain.model.SettingsState
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.content.ContentRouter
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.handleDeeplinks
import com.t8rin.imagetoolbox.core.ui.utils.helper.isShellPortraitOrientation
import com.t8rin.imagetoolbox.core.ui.utils.helper.toImageModel
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.ChildProvider
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild
import com.t8rin.imagetoolbox.feature.root.presentation.components.utils.BackEventObserver
import com.t8rin.logger.makeLog
import com.wanbaohe.profile.viewmodel.PayComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Provider

class RootComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    val settingsManager: SettingsManager,
    val themeRepository: com.t8rin.imagetoolbox.core.settings.domain.ThemeRepository,
    val childProvider: ChildProvider,
    private val analyticsManager: AnalyticsManager,
    fileController: FileController,
    dispatchersHolder: DispatchersHolder,
    resourceManager: ResourceManager,
    appComponentFactory: AppComponent.Factory,
    loginComponentFactory: LoginComponent.Factory,
    payComponentFactory: PayComponent.Factory,
    val loginStateHolder: com.shifenmiao.login.state.LoginStateHolder,
    val authorizationCodeStateHolder: com.shifenmiao.base.auth.AuthorizationCodeStateHolder,
    private val globalToolUiHostProvider: Provider<GlobalToolUiHost>,
    private val effectHostProvider: Provider<com.shifenmiao.ai.component.EffectHost>,
    val dataDraftHelper: com.shifenmiao.database.data_draft.DataDraftHelper,
    val cityPickerDataHolder: com.shifenmiao.base.ui.picker.viewmodel.CityPickerDataHolder,
    val imageShareProvider: ImageShareProvider<Bitmap>,
    val contentRouter: ContentRouter,
) : BaseComponent(dispatchersHolder, componentContext), ResourceManager by resourceManager {

    private val _isUpdateAvailable: MutableValue<Boolean> = MutableValue(false)
    val isUpdateAvailable: Value<Boolean> = _isUpdateAvailable

    private val _settingsState = mutableStateOf(SettingsState.Default)
    val settingsState: SettingsState by _settingsState
    private var startupSettingsHandled: Boolean = false

    val navController = StackNavigation<Screen>()

    private val _uris = mutableStateOf<List<Uri>?>(null)
    val uris by _uris

    private val _shouldShowExitDialog = mutableStateOf(true)
    val shouldShowDialog by _shouldShowExitDialog

    private val _filterPreviewModel: MutableState<ImageModel> =
        mutableStateOf(R.drawable.filter_preview_source.toImageModel())
    val filterPreviewModel by _filterPreviewModel

    private val _canSetDynamicFilterPreview: MutableState<Boolean> =
        mutableStateOf(false)
    val canSetDynamicFilterPreview by _canSetDynamicFilterPreview
    val appComponent: AppComponent = appComponentFactory(
        componentContext = componentContext,
        onGoBack = ::navigateBack,
        onNavigate = { navigateTo(it) },
        onNavigateReplacingCurrent = ::navigateReplacingCurrent,
    )
    val loginComponent: LoginComponent by lazy {
        loginComponentFactory(
            componentContext = componentContext
        )
    }

    val payComponent: PayComponent by lazy {
        payComponentFactory(
            componentContext = componentContext
        )
    }

    /**
     * 全生命周期单例 AI 聊天 Component（**懒加载**：首次被 Compose 订阅或访问时才创建）。
     *
     * 生命周期与 [RootComponent] 相同（通过 [retainedComponent] 跨配置变更保留），
     * 供 [com.wanbaohe.app.ui.AIChatModalBottomSheet] 和
     * [com.shifenmiao.ai.screen.AITabChatScreen] 共用，从而保证流式输出在
     * 界面切换、底部弹窗关闭期间持续运行而不中断。
     *
     * 导航栈中的 [com.shifenmiao.ai.screen.AIChatScreen] 保持独立组件实例。
     */
    private var _globalAIChatComponent: AIChatComponent? = null
    val globalAIChatComponent: AIChatComponent
        get() = _globalAIChatComponent ?: childProvider.homeFactories.aiChatComponentFactory(
            componentContext = componentContext.childContext(key = "global_ai_chat"),
            conversation = Conversation(entryType = AIConversationEntryType.ASSISTANT),
            interactionOwnerId = "global_ai_chat",
            ownsInteractiveRuntimeLifecycle = false
        ).also { _globalAIChatComponent = it }

    val isGlobalAIChatComponentInitialized: Boolean
        get() = _globalAIChatComponent != null

    /**
     * 全局工具 UI 宿主也保持懒获取。
     *
     * App 冷启动阶段不会主动创建该对象，只有真正挂载全局 AI Host
     * 或 AI 组件内部首次访问时才会从 DI 容器取出，避免把 AI 运行时链路提前拉起。
     */
    val globalToolUiHost: GlobalToolUiHost by lazy {
        globalToolUiHostProvider.get()
    }

    val effectHost: com.shifenmiao.ai.component.EffectHost by lazy {
        effectHostProvider.get()
    }

    val childStack: Value<ChildStack<Screen, NavigationChild>> by lazy {
        childStack(
            source = navController,
            initialConfiguration = startEntry(),
            serializer = Screen.serializer(),
            handleBackButton = true,
            childFactory = { screen, context ->
                screen.let {
                    AppContext.setCurrentScreen(it.id.toString())
                    if (it.title > 0) {
                        AppContext.setCurrentScreenName(AppContext.getString(it.title))
                    } else {
                        AppContext.setCurrentScreenName(it.id.toString())
                    }
                }
                with(childProvider) {
                    createChild(
                        config = screen,
                        componentContext = context,
                        appComponent = appComponent,
                        loginComponent = loginComponent
                    )
                }
            }
        )
    }

    init {
        com.t8rin.imagetoolbox.core.domain.performance.StartupTrace.mark("RootComponent.ctor.end")
        settingsManager
            .settingsState
            .onEach { state ->
                _settingsState.value = state
                if (!startupSettingsHandled) {
                    startupSettingsHandled = true
                    if (state.clearCacheOnLaunch) fileController.clearCache()
                }
            }
            .launchIn(componentScope)
    }

    /**
     * Resolves the initial top-level destination from the same adaptive tab model used by the
     * app shell.
     *
     * A preferred screen id is stored alongside the legacy index so startup remains stable even
     * when portrait and landscape expose different tab sets.
     */
    fun startEntry(): Screen {
        val context = AppContext.getContext()
        return Navigation.resolveStartEntry(
            preferredScreenId = AppSharedStorage.loadStartEntryScreenId(),
            legacyIndex = AppSharedStorage.loadStartEntryIndex(),
            isPortrait = context.isShellPortraitOrientation(),
        )
    }

    fun hideSelectDialog() {
        _uris.update { null }
    }

    fun updateUris(uris: List<Uri>) {
        _uris.value = uris
    }

    fun updateExtraDataType(type: ExtraDataType?) {
    }

    fun showToast(
        message: String,
        icon: ImageVector? = null,
        duration: ToastDuration = ToastDuration.Short
    ) {
        componentScope.launch {
            AppToastHost.showToast(
                message = message,
                icon = icon,
                duration = duration
            )
        }
    }

    fun cancelShowingExitDialog() {
        _shouldShowExitDialog.update { false }
    }

    fun onWantGithubReview() {
    }

    fun navigateTo(screen: Screen) {
        componentScope.launch {
            hideSelectDialog()
            screen.simpleName.makeLog("Navigator")
            analyticsManager.logEvent("item_click", mapOf("item_name" to screen.simpleName))
            val containsScreen = childStack.value.items.any { it.configuration == screen }
            if (containsScreen) {
                navController.pushToFront(screen)
            } else {
                navController.pushNew(screen)
            }
        }
    }

    fun navigateToNew(screen: Screen) {
        componentScope.launch {
            if (childStack.items.lastOrNull()?.configuration != startEntry()) {
                navigateBackInternal()
            }
            screen.simpleName.makeLog("Navigator").also(analyticsManager::registerScreenOpen)
            navController.pushNew(screen)
        }
    }

    fun navigateReplacingCurrent(screen: Screen) {
        componentScope.launch {
            if (childStack.items.lastOrNull()?.configuration != startEntry()) {
                navigateBackInternal()
            }
            screen.simpleName.makeLog("Navigator").also(analyticsManager::registerScreenOpen)
            navController.pushNew(screen)
        }
    }

    private val backEventsObservers: MutableList<BackEventObserver> = mutableListOf()

    private fun navigateBackInternal() {
        val closedScreen = childStack.items.lastOrNull()?.configuration
        backEventsObservers.forEach { observer ->
            observer.onBack(closedScreen)
        }
        hideSelectDialog()
        "Pop ${closedScreen?.simpleName}".makeLog("Navigator")
        navController.pop()
    }

    fun navigateBack() {
        componentScope.launch {
            navigateBackInternal()
        }
    }

    fun addBackEventsObserver(
        observer: BackEventObserver
    ) {
        backEventsObservers.add(observer)
    }

    fun removeBackEventsObserver(
        observer: BackEventObserver
    ) {
        backEventsObservers.remove(observer)
    }

    fun handleDeeplinks(intent: Intent?, context: Context) {
        /**
         * 有限使用 Deeplink 打开 App 时的 URL 导航
         */
        val url = intent?.dataString
        val urlNavigator = UrlNavigator(context, ::navigateTo, contentRouter)
        if (urlNavigator.navigate(url)) {
            return
        }

        intent.handleDeeplinks(
            context = context,
            onStart = ::hideSelectDialog,
            onHasExtraDataType = ::updateExtraDataType,
            onColdStart = ::cancelShowingExitDialog,
            onGetUris = ::updateUris,
            onShowToast = ::showToast,
            onNavigate = ::navigateTo,
            isHasUris = !uris.isNullOrEmpty(),
            onWantGithubReview = ::onWantGithubReview,
            isOpenEditInsteadOfPreview = settingsState.openEditInsteadOfPreview,
            contentRouter = contentRouter,
        )
    }


    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): RootComponent
    }

}
