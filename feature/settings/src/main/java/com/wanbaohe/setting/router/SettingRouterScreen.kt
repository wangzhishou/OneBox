package com.wanbaohe.setting.router

import androidx.compose.runtime.Composable
import com.wanbaohe.setting.ai.screen.AIAddEngineScreen
import com.wanbaohe.setting.ai.screen.AIEngineSettingsDetailScreen
import com.wanbaohe.setting.ai.screen.AIFeatureSettingsScreen
import com.wanbaohe.setting.ai.screen.AIPersonalizationScreen
import com.wanbaohe.setting.ai.screen.AIServiceHubScreen
import com.wanbaohe.setting.ai.screen.AIWorkingModelSettingsScreen
import com.wanbaohe.setting.authcode.screen.AuthCodeSettingsScreen
import com.wanbaohe.setting.display.screen.DisplaySettingsScreen
import com.wanbaohe.setting.display.screen.StartEntrySettingsScreen
import com.wanbaohe.setting.easter.screen.EasterEggScreen
import com.wanbaohe.setting.memory.screen.MemoryManagementScreen
import com.wanbaohe.setting.prompt.screen.SystemPromptDetailScreen
import com.wanbaohe.setting.prompt.screen.SystemPromptManagementScreen
import com.wanbaohe.setting.router.screenLogic.SettingRouterComponent
import com.wanbaohe.setting.skill.screen.SkillDetailScreen
import com.wanbaohe.setting.skill.screen.SkillManagementScreen
import com.wanbaohe.setting.theme.screen.ThemeSettingsScreen

@Composable
fun SettingRouterScreen(component: SettingRouterComponent) {
    when (val child = component.child) {
        is SettingRouterComponent.SettingChild.AIFeatureSettings -> AIFeatureSettingsScreen(child.component)
        is SettingRouterComponent.SettingChild.AIServiceHub -> AIServiceHubScreen(
            initialTab = child.initialTab,
            engineComponent = child.engineComponent,
            localModelComponent = child.localModelComponent,
            ttsService = child.ttsService,
            networkAudioPlayer = child.networkAudioPlayer,
            imageGenerationManager = child.imageGenerationManager,
            onGoBack = child.onGoBack,
        )
        is SettingRouterComponent.SettingChild.AIEngineDetail -> AIEngineSettingsDetailScreen(child.component)
        is SettingRouterComponent.SettingChild.AIWorkingModel -> AIWorkingModelSettingsScreen(child.component)
        is SettingRouterComponent.SettingChild.AIAddEngine -> AIAddEngineScreen(child.component)
        is SettingRouterComponent.SettingChild.ThemeSettings -> ThemeSettingsScreen(child.component)
        is SettingRouterComponent.SettingChild.SystemPromptManagement -> SystemPromptManagementScreen(child.component)
        is SettingRouterComponent.SettingChild.SystemPromptDetail -> SystemPromptDetailScreen(child.component)
        is SettingRouterComponent.SettingChild.MemoryManagement -> MemoryManagementScreen(child.component)
        is SettingRouterComponent.SettingChild.SkillManagement -> SkillManagementScreen(child.component)
        is SettingRouterComponent.SettingChild.SkillDetail -> SkillDetailScreen(child.component)
        is SettingRouterComponent.SettingChild.AIPersonalization -> AIPersonalizationScreen(
            promptComponent = child.promptComponent,
            memoryComponent = child.memoryComponent,
            skillComponent = child.skillComponent,
            onGoBack = child.onGoBack,
        )
        is SettingRouterComponent.SettingChild.DisplaySettings -> DisplaySettingsScreen(child.component)
        is SettingRouterComponent.SettingChild.EasterEgg -> EasterEggScreen(child.component)
        is SettingRouterComponent.SettingChild.AuthCodeSettings -> AuthCodeSettingsScreen(child.component)
        is SettingRouterComponent.SettingChild.StartEntrySettings -> StartEntrySettingsScreen(
            onGoBack = child.onGoBack,
        )
    }
}
