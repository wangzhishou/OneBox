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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.color_tools.ai.tool

import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.jsonStringOf
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.feature.color_tools.R
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * AI Agent 工具：列出用户保存的调色板。
 *
 * 只读，用于回答「我有哪些配色」「上次那套品牌色是什么」这类问题。
 */
class ListColorPalettesTool @Inject constructor(
    private val settingsManager: SettingsManager,
    textProvider: AgentToolTextProvider
) : BasePaletteTool(textProvider) {

    override val name: String = "palette_list"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_palette_list)

    override val title: String =
        textProvider.string(R.string.agent_tool_palette_list_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_palette_list_summary)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val sortOrder: Int = 46

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "include_colors" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(
                    R.string.agent_tool_palette_list_param_include_colors
                )
            )
        )
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            val palettes = settingsManager.getSettingsState().customPalettes
            val payload = linkedMapOf<String, Any?>(
                "status" to "ok",
                "action" to "list",
                "count" to palettes.size,
                "palettes" to palettes.map {
                    PaletteToolSupport.paletteJson(
                        palette = it,
                        includeColors = params.includeColors
                    )
                }
            )
            if (palettes.isEmpty()) {
                payload["message"] = textProvider.string(R.string.agent_tool_palette_list_empty)
            }
            AgentToolResult(content = jsonStringOf(payload))
        }.getOrElse { failureResult(action = "list", error = it) }
    }

    private fun parseArguments(arguments: String): ListParams {
        if (arguments.isBlank()) return ListParams()
        return runCatching {
            AppJson.decodeFromString<ListParams>(arguments)
        }.getOrDefault(ListParams())
    }

    @Serializable
    private data class ListParams(
        @SerialName("include_colors")
        val includeColors: Boolean = true
    )
}
