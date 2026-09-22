package com.wanbaohe.setting.router

sealed class SettingsRoute {
    data object AIFeatureSettings : SettingsRoute()
    // 「服务与模型」聚合页: 文本引擎 / Jev / 本地模型 / 多模态 Tab
    data class AIServiceHub(val initialTab: AIServiceHubTab = AIServiceHubTab.Text) : SettingsRoute()
    data class AIEngineDetail(val engineName: String, val requestProtocol: String) : SettingsRoute()
    data object AIWorkingModel : SettingsRoute()
    /** initialProtocol 非空时按该协议预选(如从 Jev tab 进入时预选 JEV) */
    data class AIAddEngine(val initialProtocol: String = "") : SettingsRoute()
    data object ThemeSettings : SettingsRoute()
    data object SystemPromptManagement : SettingsRoute()
    data class SystemPromptDetail(val promptId: Int) : SettingsRoute()
    data object MemoryManagement : SettingsRoute()
    data object SkillManagement : SettingsRoute()
    data class SkillDetail(val skillId: String) : SettingsRoute()
    // 「提示词与个性化」聚合入口页
    data object AIPersonalization : SettingsRoute()
    data object DisplaySettings : SettingsRoute()
    data object StartEntrySettings : SettingsRoute()
    data object EasterEgg : SettingsRoute()
    data object AuthCodeSettings : SettingsRoute()
}

/** 「服务与模型」聚合页的 Tab 标识 */
enum class AIServiceHubTab {
    Text,
    Jev,
    Local,
    Multimodal,
}
