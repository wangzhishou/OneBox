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

package com.wanbaohe.householditems.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.householditems.R
import com.wanbaohe.householditems.service.HouseholdService
import javax.inject.Inject
import org.json.JSONObject

/**
 * AI Agent 工具:`move_household_item` — 修改物品存放位置。
 *
 * 物品按名称模糊匹配(先精确后模糊取第一条);新位置路径不存在时自动逐级创建。
 */
class MoveHouseholdItemTool @Inject constructor(
    private val service: HouseholdService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "move_household_item"

    override val description: String =
        textProvider.string(R.string.agent_tool_move_household_item_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_move_household_item_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_move_household_item_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_move_household_item_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_move_household_item_examples)

    override val requiresConfirmation: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_move_household_item_param_name),
            ),
            "location_path" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_move_household_item_param_location_path),
            ),
        ),
        required = listOf("name", "location_path"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val itemName = json.optString("name").trim()
            require(itemName.isNotEmpty()) { "name 不能为空" }
            val locationPath = AddHouseholdItemTool.parseLocationPath(json.optString("location_path"))
            require(!locationPath.isNullOrEmpty()) { "location_path 不能为空" }

            val item = service.findItemByName(itemName)
                ?: error(textProvider.string(R.string.agent_tool_move_household_item_not_found, itemName))

            service.moveItem(
                itemId = item.id,
                locationPath = locationPath,
                actor = HouseholdService.ACTOR_AGENT,
                source = HouseholdService.SOURCE_AGENT_MOVE,
            ).getOrThrow()

            val newPath = service.buildLocationPath(
                service.getItemById(item.id)?.locationId
            )
            val deeplink = AddHouseholdItemTool.householdItemsDeeplink()
            AgentToolResult(
                content = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine("- **${textProvider.string(R.string.agent_tool_household_label_name)}**: ${sanitizeMarkdownText(item.name)}")
                    appendLine("- **${textProvider.string(R.string.agent_tool_household_label_location)}**: ${sanitizeMarkdownText(newPath)}")
                    appendLine()
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_household_open_link), deeplink)}")
                }.trimEnd(),
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_move_household_item_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_household_unknown_error),
                ),
                isError = true,
            )
        }
    }
}
