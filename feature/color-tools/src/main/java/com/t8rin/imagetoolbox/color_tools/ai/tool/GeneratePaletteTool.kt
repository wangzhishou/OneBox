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
import com.t8rin.imagetoolbox.feature.color_tools.R
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * AI Agent 工具：从一个种子颜色生成一组协调配色（不落盘）。
 *
 * 生成走本地色相/明度阶梯，不调用模型、不发网络请求；用户看中后再让
 * Agent 调 palette_save 落盘，避免「说一句就多出一组调色板」。
 */
class GeneratePaletteTool @Inject constructor(
    textProvider: AgentToolTextProvider
) : BasePaletteTool(textProvider) {

    override val name: String = "palette_generate"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_palette_generate)

    override val title: String =
        textProvider.string(R.string.agent_tool_palette_generate_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_palette_generate_summary)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val sortOrder: Int = 47

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "seed_color" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(
                    R.string.agent_tool_palette_generate_param_seed
                )
            ),
            "harmony" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(
                    R.string.agent_tool_palette_generate_param_harmony
                ),
                enum = PaletteToolSupport.harmonyNames
            ),
            "count" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(
                    R.string.agent_tool_palette_generate_param_count
                )
            )
        ),
        required = listOf("seed_color")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            val seed = PaletteToolSupport.parseHex(params.seedColor)
                ?: return@runCatching errorResult(
                    action = "generate",
                    reasonCode = "invalid_seed_color",
                    message = textProvider.string(
                        R.string.agent_tool_palette_generate_invalid_seed
                    )
                )

            val harmony = params.harmony?.lowercase() ?: DEFAULT_HARMONY
            val colors = PaletteToolSupport.generate(
                seed = seed,
                harmony = harmony,
                count = PaletteToolSupport.normalizeCount(params.count)
            ) ?: return@runCatching errorResult(
                action = "generate",
                reasonCode = "invalid_harmony",
                message = textProvider.string(
                    R.string.agent_tool_palette_generate_invalid_harmony
                ),
                validOptions = mapOf("harmony" to PaletteToolSupport.harmonyNames)
            )

            successResult(
                action = "generate",
                message = "",
                extra = mapOf(
                    "seed" to PaletteToolSupport.hexOf(seed),
                    "harmony" to harmony,
                    "count" to colors.size,
                    "colors" to colors.map(PaletteToolSupport::hexWithAlphaOf)
                )
            )
        }.getOrElse { failureResult(action = "generate", error = it) }
    }

    private fun parseArguments(arguments: String): GenerateParams {
        if (arguments.isBlank()) return GenerateParams()
        return runCatching {
            AppJson.decodeFromString<GenerateParams>(arguments)
        }.getOrDefault(GenerateParams())
    }

    @Serializable
    private data class GenerateParams(
        @SerialName("seed_color")
        val seedColor: String? = null,
        val harmony: String? = null,
        val count: Int? = null
    )

    private companion object {
        const val DEFAULT_HARMONY = "triadic"
    }
}
