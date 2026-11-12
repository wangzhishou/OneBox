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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import org.json.JSONObject

/**
 * AI Agent 工具:`find_household_item` — 模糊查找物品,返回完整位置路径。
 *
 * 结果附家庭物品主页 deeplink,用户可点击直达。
 */
class FindHouseholdItemTool @Inject constructor(
    private val service: HouseholdService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "find_household_item"

    override val description: String =
        textProvider.string(R.string.agent_tool_find_household_item_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_find_household_item_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_find_household_item_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_find_household_item_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_find_household_item_examples)

    override val requiresConfirmation: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "query" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_find_household_item_param_query),
            ),
        ),
        required = listOf("query"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val query = json.optString("query").trim()
            require(query.isNotEmpty()) { "query 不能为空" }

            val items = service.searchItems(query)
            val deeplink = AddHouseholdItemTool.householdItemsDeeplink()
            AgentToolResult(
                content = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    if (items.isEmpty()) {
                        appendLine(textProvider.string(R.string.agent_tool_find_household_item_empty))
                    } else {
                        items.forEach { item ->
                            val itemLink = buildMarkdownLink(
                                sanitizeMarkdownText(item.name),
                                AddHouseholdItemTool.householdItemEditDeeplink(item.id),
                            )
                            appendLine("- **$itemLink**")
                            val path = item.locationPath.ifBlank {
                                textProvider.string(R.string.household_no_location)
                            }
                            appendLine("  - ${textProvider.string(R.string.agent_tool_household_label_location)}: ${sanitizeMarkdownText(path)}")
                            item.category?.takeIf { it.isNotBlank() }?.let {
                                appendLine("  - ${textProvider.string(R.string.agent_tool_household_label_category)}: ${sanitizeMarkdownText(it)}")
                            }
                            appendLine("  - ${textProvider.string(R.string.agent_tool_household_label_expiry)}: ${expiryText(item.expireAt)}")
                            item.note?.takeIf { it.isNotBlank() }?.let {
                                appendLine("  - ${textProvider.string(R.string.agent_tool_household_label_note)}: ${sanitizeMarkdownText(it)}")
                            }
                        }
                    }
                    appendLine()
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_household_open_link), deeplink)}")
                }.trimEnd(),
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_find_household_item_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_household_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun expiryText(expireAt: Long?): String {
        if (expireAt == null) {
            return textProvider.string(R.string.agent_tool_household_expiry_none)
        }
        val date = Instant.ofEpochMilli(expireAt).atZone(ZoneId.systemDefault()).toLocalDate()
        val days = ChronoUnit.DAYS.between(LocalDate.now(), date)
        return when {
            days < 0 -> textProvider.string(R.string.agent_tool_household_expiry_expired, -days)
            days == 0L -> textProvider.string(R.string.agent_tool_household_expiry_today)
            else -> textProvider.string(R.string.agent_tool_household_expiry_days_left, days)
        }
    }
}
