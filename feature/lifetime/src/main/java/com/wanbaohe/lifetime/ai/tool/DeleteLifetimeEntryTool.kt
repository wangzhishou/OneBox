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
import com.shifenmiao.lifetime.data.CountdownEventRepository
import com.shifenmiao.lifetime.data.PersonalMilestoneRepository
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject
import org.json.JSONObject

/**
 * AI Agent 工具:`delete_lifetime_entry` — 按 id 删除一条倒数日或人生里程碑。
 *
 * 删除不可逆,requiresConfirmation=true,执行前必须由用户确认。
 * id 通常来自 `query_life_events` 的查询结果;type 指定条目类型(countdown / milestone)。
 */
class DeleteLifetimeEntryTool @Inject constructor(
    private val countdownRepository: CountdownEventRepository,
    private val milestoneRepository: PersonalMilestoneRepository,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "delete_lifetime_entry"

    override val description: String =
        textProvider.string(R.string.agent_tool_delete_lifetime_entry_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_delete_lifetime_entry_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_delete_lifetime_entry_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS

    override val requiresConfirmation: Boolean = true

    override val confirmationTitle: String =
        textProvider.string(R.string.agent_tool_delete_lifetime_entry_confirm_title)

    override val confirmationToolPresentation: String =
        textProvider.string(R.string.agent_tool_delete_lifetime_entry_confirm_presentation)

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_delete_lifetime_entry_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_delete_lifetime_entry_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        properties = mapOf(
            "type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_delete_lifetime_entry_param_type),
            ),
            "id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_delete_lifetime_entry_param_id),
            ),
        ),
        required = listOf("type", "id"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val type = json.optString("type").trim().lowercase()
            require(type == TYPE_COUNTDOWN || type == TYPE_MILESTONE) {
                textProvider.string(R.string.agent_tool_lifetime_invalid_type, type)
            }
            val entryId = json.optString("id").trim().toLongOrNull()
                ?: error(textProvider.string(R.string.agent_tool_delete_lifetime_entry_id_required))

            val entryName = when (type) {
                TYPE_COUNTDOWN -> {
                    val event = countdownRepository.getCountdownById(entryId)
                        ?: error(textProvider.string(R.string.agent_tool_delete_lifetime_entry_not_found, entryId))
                    countdownRepository.deleteCountdownById(entryId)
                    event.name
                }

                else -> {
                    val milestone = milestoneRepository.getMilestoneById(entryId)
                        ?: error(textProvider.string(R.string.agent_tool_delete_lifetime_entry_not_found, entryId))
                    milestoneRepository.deleteMilestoneById(entryId)
                    milestone.name
                }
            }

            val deeplink = lifetimeScreenDeeplink()
            lifetimeSuccessResult(
                markdown = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine(
                        textProvider.string(
                            R.string.agent_tool_delete_lifetime_entry_deleted,
                            sanitizeMarkdownText(entryName),
                        )
                    )
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
                put("type", type)
                put("entryId", entryId)
                put("deletedName", entryName)
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_delete_lifetime_entry_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_lifetime_unknown_error),
                ),
                isError = true,
            )
        }
    }

    companion object {
        private const val TYPE_COUNTDOWN = "countdown"
        private const val TYPE_MILESTONE = "milestone"
    }
}
