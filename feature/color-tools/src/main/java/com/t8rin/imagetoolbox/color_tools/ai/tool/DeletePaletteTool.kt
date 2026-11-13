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
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.feature.color_tools.R
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * AI Agent 工具：删除一组调色板（按名称或 id）。
 *
 * 删除不可撤销，执行前会走工具确认弹窗；找不到时把现有组名回给模型，
 * 让 Agent 能自己纠正（比如用户说的是「春季视觉」而不是「春季配色」）。
 */
class DeletePaletteTool @Inject constructor(
    private val settingsManager: SettingsManager,
    textProvider: AgentToolTextProvider
) : BasePaletteTool(textProvider) {

    override val name: String = "palette_delete"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_palette_delete)

    override val title: String =
        textProvider.string(R.string.agent_tool_palette_delete_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_palette_delete_summary)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val requiresConfirmation: Boolean = true

    override val parallelizable: Boolean = false

    override val sortOrder: Int = 49

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(
                    R.string.agent_tool_palette_delete_param_name
                )
            ),
            "id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(
                    R.string.agent_tool_palette_delete_param_id
                )
            )
        )
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            val id = params.id?.trim().orEmpty()
            val name = params.name?.trim().orEmpty()
            if (id.isEmpty() && name.isEmpty()) {
                return@runCatching errorResult(
                    action = "delete",
                    reasonCode = "missing_target",
                    message = textProvider.string(
                        R.string.agent_tool_palette_delete_missing_target
                    )
                )
            }

            val palettes = settingsManager.getSettingsState().customPalettes
            val target = if (id.isNotEmpty()) {
                palettes.firstOrNull { it.id == id }
            } else {
                palettes.firstOrNull { it.name.equals(name, ignoreCase = true) }
            }

            if (target == null) {
                return@runCatching errorResult(
                    action = "delete",
                    reasonCode = "not_found",
                    message = textProvider.string(
                        R.string.agent_tool_palette_delete_not_found
                    ),
                    validOptions = mapOf(
                        "names" to palettes.map { it.name },
                        "ids" to palettes.map { it.id }
                    )
                )
            }

            settingsManager.setCustomPalettes(
                palettes.filterNot { it.id == target.id }
            )

            successResult(
                action = "delete",
                message = textProvider.string(R.string.agent_tool_palette_delete_ok),
                extra = mapOf(
                    "deleted_id" to target.id,
                    "deleted_name" to target.name,
                    "remaining" to palettes.size - 1
                )
            )
        }.getOrElse { failureResult(action = "delete", error = it) }
    }

    private fun parseArguments(arguments: String): DeleteParams {
        if (arguments.isBlank()) return DeleteParams()
        return runCatching {
            AppJson.decodeFromString<DeleteParams>(arguments)
        }.getOrDefault(DeleteParams())
    }

    @Serializable
    private data class DeleteParams(
        val name: String? = null,
        val id: String? = null
    )
}
