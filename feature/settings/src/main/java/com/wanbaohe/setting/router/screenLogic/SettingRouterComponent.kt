package com.wanbaohe.setting.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.common.logic.AppComponent
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.setting.ai.component.AIFeatureSettingsComponent
import com.wanbaohe.setting.ai.component.AIAddEngineComponent
import com.wanbaohe.setting.ai.component.AIEngineSettingsComponent
import com.wanbaohe.setting.ai.component.AIEngineSettingsDetailComponent
import com.wanbaohe.setting.ai.component.AIWorkingModelSettingsComponent
import com.wanbaohe.setting.authcode.component.AuthCodeSettingsComponent
import com.wanbaohe.setting.display.component.DisplaySettingsComponent
import com.wanbaohe.setting.easter.component.EasterEggComponent
import com.wanbaohe.setting.prompt.component.SystemPromptDetailComponent
import com.shifenmiao.tts.service.TTSService
import com.wanbaohe.setting.prompt.component.SystemPromptManagementComponent
import com.wanbaohe.setting.router.SettingsRoute
import com.wanbaohe.setting.theme.component.ThemeSettingsComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class SettingRouterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val route: SettingsRoute,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted val appComponent: AppComponent?,
    private val aiFeatureSettingsComponentFactory: AIFeatureSettingsComponent.Factory,
    private val aiEngineSettingsComponentFactory: AIEngineSettingsComponent.Factory,
    private val aiEngineSettingsDetailComponentFactory: AIEngineSettingsDetailComponent.Factory,
    private val aiWorkingModelSettingsComponentFactory: AIWorkingModelSettingsComponent.Factory,
    private val aiAddEngineComponentFactory: AIAddEngineComponent.Factory,
    private val themeSettingsComponentFactory: ThemeSettingsComponent.Factory,
    private val systemPromptManagementComponentFactory: SystemPromptManagementComponent.Factory,
    private val systemPromptDetailComponentFactory: SystemPromptDetailComponent.Factory,
    private val displaySettingsComponentFactory: DisplaySettingsComponent.Factory,
    private val easterEggComponentFactory: EasterEggComponent.Factory,
    private val authCodeSettingsComponentFactory: AuthCodeSettingsComponent.Factory,
    private val ttsService: TTSService,
    private val networkAudioPlayer: NetworkAudioPlayer,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val child: SettingChild = when (route) {
        is SettingsRoute.AIFeatureSettings -> SettingChild.AIFeatureSettings(
            aiFeatureSettingsComponentFactory(
                componentContext = componentContext.childContext("ai_feature_settings"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is SettingsRoute.AIEngineList -> SettingChild.AIEngineList(
            aiEngineSettingsComponentFactory(
                componentContext = componentContext.childContext("ai_engine_list"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is SettingsRoute.AIEngineDetail -> SettingChild.AIEngineDetail(
            aiEngineSettingsDetailComponentFactory(
                componentContext = componentContext.childContext("ai_engine_detail"),
                engineName = route.engineName,
                requestProtocol = route.requestProtocol,
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is SettingsRoute.AIWorkingModel -> SettingChild.AIWorkingModel(
            aiWorkingModelSettingsComponentFactory(
                componentContext = componentContext.childContext("ai_working_model"),
                onGoBack = onGoBack,
            )
        )

        is SettingsRoute.AIAddEngine -> SettingChild.AIAddEngine(
            aiAddEngineComponentFactory(
                componentContext = componentContext.childContext("ai_add_engine"),
                onGoBack = onGoBack,
            )
        )

        is SettingsRoute.ThemeSettings -> SettingChild.ThemeSettings(
            themeSettingsComponentFactory(
                componentContext = componentContext.childContext("theme_settings"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is SettingsRoute.SystemPromptManagement -> SettingChild.SystemPromptManagement(
            systemPromptManagementComponentFactory(
                componentContext = componentContext.childContext("system_prompt_mgmt"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is SettingsRoute.SystemPromptDetail -> SettingChild.SystemPromptDetail(
            systemPromptDetailComponentFactory(
                componentContext = componentContext.childContext("system_prompt_detail"),
                promptId = route.promptId,
                onGoBack = onGoBack,
            )
        )

        is SettingsRoute.DisplaySettings -> SettingChild.DisplaySettings(
            displaySettingsComponentFactory(
                componentContext = componentContext.childContext("display_settings"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
                appComponent = appComponent ?: error("AppComponent required for DisplaySettings"),
            )
        )

        is SettingsRoute.EasterEgg -> SettingChild.EasterEgg(
            easterEggComponentFactory(
                componentContext = componentContext.childContext("easter_egg"),
                onGoBack = onGoBack,
            )
        )

        is SettingsRoute.TTSSettings -> SettingChild.TTSSettings(
            ttsService = ttsService,
            networkAudioPlayer = networkAudioPlayer,
            onGoBack = onGoBack,
        )

        is SettingsRoute.AuthCodeSettings -> SettingChild.AuthCodeSettings(
            authCodeSettingsComponentFactory(
                componentContext = componentContext.childContext("auth_code_settings"),
                onGoBack = onGoBack,
            )
        )
    }

    sealed interface SettingChild {
        class AIFeatureSettings(val component: AIFeatureSettingsComponent) : SettingChild
        class AIEngineList(val component: AIEngineSettingsComponent) : SettingChild
        class AIEngineDetail(val component: AIEngineSettingsDetailComponent) : SettingChild
        class AIWorkingModel(val component: AIWorkingModelSettingsComponent) : SettingChild
        class AIAddEngine(val component: AIAddEngineComponent) : SettingChild
        class ThemeSettings(val component: ThemeSettingsComponent) : SettingChild
        class SystemPromptManagement(val component: SystemPromptManagementComponent) : SettingChild
        class SystemPromptDetail(val component: SystemPromptDetailComponent) : SettingChild
        class DisplaySettings(val component: DisplaySettingsComponent) : SettingChild
        class EasterEgg(val component: EasterEggComponent) : SettingChild
        class TTSSettings(
            val ttsService: TTSService,
            val networkAudioPlayer: NetworkAudioPlayer,
            val onGoBack: () -> Unit,
        ) : SettingChild
        class AuthCodeSettings(val component: AuthCodeSettingsComponent) : SettingChild
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            route: SettingsRoute,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
            appComponent: AppComponent?,
        ): SettingRouterComponent
    }
}
