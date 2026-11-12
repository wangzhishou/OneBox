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

package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ToolDeepLink
import com.shifenmiao.model.jsonStringOf
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * 日夜间模式设置工具(独立窄入口,只负责 Light / Dark / System)。
 *
 * 与 theme_setting 的 night_mode 字段不同,本工具专注日夜间模式读写;
 * 写入后经 settingsState Flow 全局即时生效,无需 recreate。
 */
class NightModeSettingTool @Inject constructor(
    private val settingsManager: SettingsManager,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "night_mode_setting"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_night_mode_setting)

    override val title: String = textProvider.string(R.string.agent_tool_night_mode_setting_title)

    override val summary: String = textProvider.string(R.string.agent_tool_night_mode_setting_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_night_mode_setting_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_night_mode_setting_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parallelizable: Boolean = false

    override val sortOrder: Int = -74

    override val deepLinks: List<ToolDeepLink> = listOf(
        ToolDeepLink(
            uri = AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "theme_settings",
            ),
            label = textProvider.string(R.string.agent_tool_night_mode_setting_deeplink_label),
            guidance = textProvider.string(R.string.agent_tool_night_mode_setting_deeplink_guidance),
            primary = true,
        )
    )

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_night_mode_setting_param_action),
                enum = listOf("get", "set"),
            ),
            "mode" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_night_mode_setting_param_mode),
                enum = listOf("Light", "Dark", "System"),
            ),
        ),
        required = listOf("action"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            when (params.action?.lowercase()) {
                "get", "list" -> handleGet()
                "set", "update", "switch" -> handleSet(params)
                null, "" -> errorResult(
                    action = "unknown",
                    reasonCode = "missing_action",
                    message = textProvider.string(R.string.agent_tool_night_mode_setting_missing_action),
                )
                else -> errorResult(
                    action = params.action,
                    reasonCode = "unknown_action",
                    message = textProvider.string(
                        R.string.agent_tool_night_mode_setting_unknown_action,
                        params.action,
                    ),
                )
            }
        }.getOrElse { error ->
            errorResult(
                action = "unknown",
                reasonCode = "exception",
                message = textProvider.string(
                    R.string.agent_tool_night_mode_setting_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
            )
        }
    }

    private suspend fun handleGet(): AgentToolResult {
        val nightMode = settingsManager.getSettingsState().nightMode
        return successResult(
            action = "get",
            message = textProvider.string(R.string.agent_tool_night_mode_setting_get_message, nightMode.name),
            nightMode = nightMode,
        )
    }

    private suspend fun handleSet(params: NightModeSettingParams): AgentToolResult {
        val raw = params.mode?.trim()
        if (raw.isNullOrEmpty()) {
            return errorResult(
                action = "set",
                reasonCode = "missing_mode",
                message = textProvider.string(R.string.agent_tool_night_mode_setting_missing_mode),
                validOptions = mapOf("nightModes" to NIGHT_MODE_NAMES),
            )
        }
        val nightMode = parseNightMode(raw) ?: return errorResult(
            action = "set",
            reasonCode = "invalid_night_mode",
            message = textProvider.string(R.string.agent_tool_night_mode_setting_invalid_mode, raw),
            validOptions = mapOf("nightModes" to NIGHT_MODE_NAMES),
        )
        settingsManager.setNightMode(nightMode)
        return successResult(
            action = "set",
            message = textProvider.string(R.string.agent_tool_night_mode_setting_set_message, nightMode.name),
            nightMode = nightMode,
        )
    }

    private fun parseNightMode(raw: String): NightMode? {
        return when (raw.lowercase()) {
            "light", "日间", "白天", "浅色", "亮色" -> NightMode.Light
            "dark", "夜间", "黑夜", "深色", "暗色" -> NightMode.Dark
            "system", "跟随系统", "系统", "自动" -> NightMode.System
            else -> NIGHT_MODES.firstOrNull { it.name.equals(raw, ignoreCase = true) }
        }
    }

    private fun parseArguments(arguments: String): NightModeSettingParams {
        if (arguments.isBlank()) return NightModeSettingParams()
        return runCatching { AppJson.decodeFromString<NightModeSettingParams>(arguments) }.getOrNull() ?: NightModeSettingParams()
    }

    private fun successResult(
        action: String,
        message: String,
        nightMode: NightMode,
    ): AgentToolResult {
        val payload = linkedMapOf<String, Any?>(
            "status" to "ok",
            "action" to action,
            "message" to message,
            "nightMode" to nightMode.name,
            "validNightModes" to NIGHT_MODE_NAMES,
        )
        return AgentToolResult(content = jsonStringOf(payload))
    }

    private fun errorResult(
        action: String,
        reasonCode: String,
        message: String,
        validOptions: Map<String, Any?>? = null,
    ): AgentToolResult {
        val payload = linkedMapOf<String, Any?>(
            "status" to "error",
            "action" to action,
            "reasonCode" to reasonCode,
            "message" to message,
        )
        if (validOptions != null) payload["validOptions"] = validOptions
        return AgentToolResult(content = jsonStringOf(payload), isError = true)
    }

    @Serializable
    private data class NightModeSettingParams(
        val action: String? = null,
        val mode: String? = null,
    )

    private companion object {
        val NIGHT_MODES = listOf(NightMode.Light, NightMode.Dark, NightMode.System)
        val NIGHT_MODE_NAMES = NIGHT_MODES.map { it.name }
    }
}
