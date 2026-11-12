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
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * 字体大小(字体缩放)设置工具。
 *
 * 生效机制:字体缩放在 ComposeActivity.attachBaseContext 应用;ComposeActivity
 * 监听 settingsState,检测到 fontScale 与已应用值不一致时会自动 recreate,
 * 因此工具写入后约 1 秒内界面自动刷新生效,成功结果的 message 附带生效说明。
 */
class FontScaleSettingTool @Inject constructor(
    private val settingsManager: SettingsManager,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "font_scale_setting"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_font_scale_setting)

    override val title: String = textProvider.string(R.string.agent_tool_font_scale_setting_title)

    override val summary: String = textProvider.string(R.string.agent_tool_font_scale_setting_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_font_scale_setting_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_font_scale_setting_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parallelizable: Boolean = false

    override val sortOrder: Int = -73

    override val deepLinks: List<ToolDeepLink> = listOf(
        ToolDeepLink(
            uri = AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "display_settings",
            ),
            label = textProvider.string(R.string.agent_tool_font_scale_setting_deeplink_label),
            guidance = textProvider.string(R.string.agent_tool_font_scale_setting_deeplink_guidance),
            primary = true,
        )
    )

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_font_scale_setting_param_action),
                enum = listOf("get", "set"),
            ),
            "scale" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_font_scale_setting_param_scale),
            ),
        ),
        required = listOf("action"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            when (params.action?.lowercase()) {
                "get", "list" -> handleGet()
                "set", "update" -> handleSet(params)
                null, "" -> errorResult(
                    action = "unknown",
                    reasonCode = "missing_action",
                    message = textProvider.string(R.string.agent_tool_font_scale_setting_missing_action),
                )
                else -> errorResult(
                    action = params.action,
                    reasonCode = "unknown_action",
                    message = textProvider.string(
                        R.string.agent_tool_font_scale_setting_unknown_action,
                        params.action,
                    ),
                )
            }
        }.getOrElse { error ->
            errorResult(
                action = "unknown",
                reasonCode = "exception",
                message = textProvider.string(
                    R.string.agent_tool_font_scale_setting_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
            )
        }
    }

    private suspend fun handleGet(): AgentToolResult {
        val fontScale = settingsManager.getSettingsState().fontScale
        return successResult(
            action = "get",
            message = textProvider.string(R.string.agent_tool_font_scale_setting_get_message),
            fontScale = fontScale,
        )
    }

    private suspend fun handleSet(params: FontScaleSettingParams): AgentToolResult {
        val raw = params.scale?.trim()
        if (raw.isNullOrEmpty()) {
            return errorResult(
                action = "set",
                reasonCode = "missing_scale",
                message = textProvider.string(R.string.agent_tool_font_scale_setting_missing_scale),
                validOptions = mapOf("scaleRange" to SCALE_RANGE_LABEL, "keywords" to SYSTEM_KEYWORDS),
            )
        }
        if (raw.lowercase() in SYSTEM_KEYWORDS) {
            // scale <= 0f 表示跟随系统
            settingsManager.setFontScale(0f)
            return successResult(
                action = "set",
                message = textProvider.string(R.string.agent_tool_font_scale_setting_set_system_message),
                fontScale = null,
            )
        }
        val scale = raw.toFloatOrNull()
        if (scale == null || scale.isNaN() || scale !in MIN_SCALE..MAX_SCALE) {
            return errorResult(
                action = "set",
                reasonCode = "scale_out_of_range",
                message = textProvider.string(
                    R.string.agent_tool_font_scale_setting_out_of_range,
                    raw,
                ),
                validOptions = mapOf("scaleRange" to SCALE_RANGE_LABEL, "keywords" to SYSTEM_KEYWORDS),
            )
        }
        settingsManager.setFontScale(scale)
        return successResult(
            action = "set",
            message = textProvider.string(R.string.agent_tool_font_scale_setting_set_message, scale),
            fontScale = scale,
        )
    }

    private fun parseArguments(arguments: String): FontScaleSettingParams {
        if (arguments.isBlank()) return FontScaleSettingParams()
        return runCatching { AppJson.decodeFromString<FontScaleSettingParams>(arguments) }.getOrNull() ?: FontScaleSettingParams()
    }

    private fun successResult(
        action: String,
        message: String,
        fontScale: Float?,
    ): AgentToolResult {
        val payload = linkedMapOf<String, Any?>(
            "status" to "ok",
            "action" to action,
            "message" to message,
            "fontScale" to fontScale,
            "followSystem" to (fontScale == null),
            "effectiveHint" to textProvider.string(R.string.agent_tool_font_scale_setting_effective_hint),
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
    private data class FontScaleSettingParams(
        val action: String? = null,
        val scale: String? = null,
    )

    private companion object {
        const val MIN_SCALE = 0.45f
        const val MAX_SCALE = 1.5f
        const val SCALE_RANGE_LABEL = "0.45..1.5"
        val SYSTEM_KEYWORDS = listOf("system", "default", "auto", "跟随系统", "系统", "默认")
    }
}
