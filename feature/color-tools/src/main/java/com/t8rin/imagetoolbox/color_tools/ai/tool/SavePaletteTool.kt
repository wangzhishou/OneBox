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
import com.t8rin.imagetoolbox.core.domain.model.ColorPalette
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.feature.color_tools.R
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * AI Agent 工具：保存一组颜色为调色板（同名即更新）。
 *
 * 写入走 SettingsManager，和设置页、选色面板共用同一份 DataStore，
 * 保存后全局立即可用，不用重进 App。
 */
class SavePaletteTool @Inject constructor(
    private val settingsManager: SettingsManager,
    textProvider: AgentToolTextProvider
) : BasePaletteTool(textProvider) {

    override val name: String = "palette_save"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_palette_save)

    override val title: String =
        textProvider.string(R.string.agent_tool_palette_save_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_palette_save_summary)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parallelizable: Boolean = false

    override val sortOrder: Int = 48

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(
                    R.string.agent_tool_palette_save_param_name
                )
            ),
            "colors" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(
                    R.string.agent_tool_palette_save_param_colors
                ),
                items = ToolParameterProperty(type = "string")
            ),
            "mode" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(
                    R.string.agent_tool_palette_save_param_mode
                ),
                enum = listOf(MODE_REPLACE, MODE_APPEND)
            )
        ),
        required = listOf("name", "colors")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            val name = params.name
                ?.trim()
                ?.take(ColorPalette.MAX_NAME_LENGTH)
                .orEmpty()
            if (name.isEmpty()) {
                return@runCatching errorResult(
                    action = "save",
                    reasonCode = "missing_name",
                    message = textProvider.string(
                        R.string.agent_tool_palette_save_missing_name
                    )
                )
            }

            val colors = params.colors
                .orEmpty()
                .mapNotNull(PaletteToolSupport::parseHex)
                .distinct()
                .take(ColorPalette.MAX_COLORS)
            if (colors.isEmpty()) {
                return@runCatching errorResult(
                    action = "save",
                    reasonCode = "missing_colors",
                    message = textProvider.string(
                        R.string.agent_tool_palette_save_missing_colors
                    )
                )
            }

            val mode = params.mode?.lowercase() ?: MODE_REPLACE
            val palettes = settingsManager.getSettingsState().customPalettes
            val existing = palettes.firstOrNull { it.name.equals(name, ignoreCase = true) }

            if (existing == null && palettes.size >= ColorPalette.MAX_PALETTES) {
                return@runCatching errorResult(
                    action = "save",
                    reasonCode = "palette_limit",
                    message = textProvider.string(
                        R.string.agent_tool_palette_save_too_many
                    )
                )
            }

            val now = System.currentTimeMillis()
            val updated = when {
                existing == null -> ColorPalette(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    colors = colors,
                    updatedAtMillis = now
                )

                mode == MODE_APPEND -> colors.fold(existing) { acc, argb ->
                    acc.appending(argb, now)
                }.copy(name = name, updatedAtMillis = now)

                else -> existing.copy(
                    name = name,
                    colors = colors,
                    updatedAtMillis = now
                )
            }

            val newPalettes = if (existing == null) {
                palettes + updated
            } else {
                palettes.map { if (it.id == existing.id) updated else it }
            }
            settingsManager.setCustomPalettes(newPalettes)

            successResult(
                action = "save",
                message = "",
                extra = mapOf(
                    "created" to (existing == null),
                    "mode" to mode,
                    "palette" to PaletteToolSupport.paletteJson(
                        palette = updated,
                        includeColors = true
                    )
                )
            )
        }.getOrElse { failureResult(action = "save", error = it) }
    }

    private fun parseArguments(arguments: String): SaveParams {
        if (arguments.isBlank()) return SaveParams()
        return runCatching {
            AppJson.decodeFromString<SaveParams>(arguments)
        }.getOrDefault(SaveParams())
    }

    @Serializable
    private data class SaveParams(
        val name: String? = null,
        val colors: List<String>? = null,
        val mode: String? = null
    )

    private companion object {
        const val MODE_REPLACE = "replace"
        const val MODE_APPEND = "append"
    }
}
