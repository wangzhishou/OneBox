package com.wanbaohe.setting.router

sealed class SettingsRoute {
    data object AIFeatureSettings : SettingsRoute()
    data object AIEngineList : SettingsRoute()
    data class AIEngineDetail(val engineName: String, val requestProtocol: String) : SettingsRoute()
    data object AIWorkingModel : SettingsRoute()
    data object AIAddEngine : SettingsRoute()
    data object ThemeSettings : SettingsRoute()
    data object SystemPromptManagement : SettingsRoute()
    data class SystemPromptDetail(val promptId: Int) : SettingsRoute()
    data object DisplaySettings : SettingsRoute()
    data object EasterEgg : SettingsRoute()
    data object TTSSettings : SettingsRoute()
    data object ImageGenerationSettings : SettingsRoute()
    data object AuthCodeSettings : SettingsRoute()
}
