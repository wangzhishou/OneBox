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

package com.wanbaohe.lifetime.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.data.PersonalMilestoneRepository
import com.shifenmiao.lifetime.domain.model.PersonalMilestone
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject
import org.json.JSONObject

/**
 * AI Agent 工具:`add_life_milestone` — 记录一条人生里程碑。
 *
 * target_date 只接受纯日期(yyyy-MM-dd);
 * 数据层支持的「出生第 N 天」(targetDays)模式不通过本工具创建,保持参数简单。
 * 非法输入返回 isError 并给出格式说明。
 */
class AddLifeMilestoneTool @Inject constructor(
    private val repository: PersonalMilestoneRepository,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "add_life_milestone"

    override val description: String =
        textProvider.string(R.string.agent_tool_add_life_milestone_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_add_life_milestone_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_add_life_milestone_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parallelizable: Boolean = false

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_add_life_milestone_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_add_life_milestone_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        properties = mapOf(
            "name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_life_milestone_param_name),
            ),
            "target_date" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_life_milestone_param_target_date),
            ),
            "note" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_life_milestone_param_note),
            ),
        ),
        required = listOf("name", "target_date"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val milestoneName = json.optString("name").trim()
            require(milestoneName.isNotEmpty()) {
                textProvider.string(R.string.agent_tool_add_life_milestone_name_required)
            }

            val targetDateText = json.optString("target_date").trim()
            val targetDate = parseIsoDate(targetDateText)
                ?: error(textProvider.string(R.string.agent_tool_lifetime_invalid_date, targetDateText))

            val note = json.optString("note").takeIf { it.isNotBlank() }

            val milestoneId = repository.addMilestone(
                PersonalMilestone(
                    name = milestoneName,
                    targetDate = targetDate,
                    note = note,
                )
            )

            val deeplink = lifetimeScreenDeeplink()
            lifetimeSuccessResult(
                markdown = buildString {
                    appendLine("# ${sanitizeMarkdownText(milestoneName)}")
                    appendLine()
                    appendLine("- ${textProvider.string(R.string.agent_tool_lifetime_label_target_date)}: $targetDate")
                    note?.let {
                        appendLine("- ${textProvider.string(R.string.agent_tool_lifetime_label_note)}: ${sanitizeMarkdownText(it)}")
                    }
                    appendLine()
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_lifetime_open_link), deeplink)}")
                }.trimEnd(),
                deepLinks = listOf(
                    toolDeepLinkJson(
                        uri = deeplink,
                        label = textProvider.string(R.string.agent_tool_lifetime_open_link),
                        primary = true,
                    )
                ),
            ) {
                put("milestoneId", milestoneId)
                put("targetDate", targetDate.toString())
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_add_life_milestone_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_lifetime_unknown_error),
                ),
                isError = true,
            )
        }
    }
}
