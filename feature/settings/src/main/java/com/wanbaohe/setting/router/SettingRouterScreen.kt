package com.wanbaohe.setting.router

import androidx.compose.runtime.Composable
import com.wanbaohe.setting.ai.screen.AIAddEngineScreen
import com.wanbaohe.setting.ai.screen.AIEngineSettingsDetailScreen
import com.wanbaohe.setting.ai.screen.AIEngineSettingsScreen
import com.wanbaohe.setting.ai.screen.AIFeatureSettingsScreen
import com.wanbaohe.setting.ai.screen.AIWorkingModelSettingsScreen
import com.wanbaohe.setting.authcode.screen.AuthCodeSettingsScreen
import com.wanbaohe.setting.display.screen.DisplaySettingsScreen
import com.wanbaohe.setting.easter.screen.EasterEggScreen
import com.wanbaohe.setting.image.screen.ImageGenerationSettingsScreen
import com.wanbaohe.setting.prompt.screen.SystemPromptDetailScreen
import com.wanbaohe.setting.prompt.screen.SystemPromptManagementScreen
import com.wanbaohe.setting.router.screenLogic.SettingRouterComponent
import com.wanbaohe.setting.ai.screen.TTSSettingsScreen
import com.wanbaohe.setting.theme.screen.ThemeSettingsScreen

@Composable
fun SettingRouterScreen(component: SettingRouterComponent) {
    when (val child = component.child) {
        is SettingRouterComponent.SettingChild.AIFeatureSettings -> AIFeatureSettingsScreen(child.component)
        is SettingRouterComponent.SettingChild.AIEngineList -> AIEngineSettingsScreen(child.component)
        is SettingRouterComponent.SettingChild.AIEngineDetail -> AIEngineSettingsDetailScreen(child.component)
        is SettingRouterComponent.SettingChild.AIWorkingModel -> AIWorkingModelSettingsScreen(child.component)
        is SettingRouterComponent.SettingChild.AIAddEngine -> AIAddEngineScreen(child.component)
        is SettingRouterComponent.SettingChild.ThemeSettings -> ThemeSettingsScreen(child.component)
        is SettingRouterComponent.SettingChild.SystemPromptManagement -> SystemPromptManagementScreen(child.component)
        is SettingRouterComponent.SettingChild.SystemPromptDetail -> SystemPromptDetailScreen(child.component)
        is SettingRouterComponent.SettingChild.DisplaySettings -> DisplaySettingsScreen(child.component)
        is SettingRouterComponent.SettingChild.EasterEgg -> EasterEggScreen(child.component)
        is SettingRouterComponent.SettingChild.TTSSettings -> TTSSettingsScreen(
            child.ttsService,
            child.networkAudioPlayer,
            child.onGoBack,
        )
        is SettingRouterComponent.SettingChild.ImageGenerationSettings -> ImageGenerationSettingsScreen(
            manager = child.manager,
            onGoBack = child.onGoBack,
        )
        is SettingRouterComponent.SettingChild.AuthCodeSettings -> AuthCodeSettingsScreen(child.component)
    }
}
