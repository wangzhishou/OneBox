/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.wanbaohe.setting.router

sealed class SettingsRoute {
    data object AIFeatureSettings : SettingsRoute()
    // 「服务与模型」聚合页: 文本引擎 / Jev / 本地模型 / 多模态 Tab
    data class AIServiceHub(val initialTab: AIServiceHubTab = AIServiceHubTab.Text) : SettingsRoute()
    data class AIEngineDetail(val engineName: String, val requestProtocol: String) : SettingsRoute()
    data object AIWorkingModel : SettingsRoute()
    /** initialProtocol 非空时按该协议预选(如从 Jev tab 进入时预选 JEV); importConfig 为深链导入的配置文本 */
    data class AIAddEngine(val initialProtocol: String = "", val importConfig: String = "") : SettingsRoute()
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
