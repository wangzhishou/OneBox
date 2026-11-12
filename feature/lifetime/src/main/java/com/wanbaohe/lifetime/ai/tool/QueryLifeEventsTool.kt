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
import com.shifenmiao.lifetime.data.LifeTimeRepository
import com.shifenmiao.lifetime.data.PersonalMilestoneRepository
import com.shifenmiao.lifetime.domain.CountdownCalculator
import com.shifenmiao.lifetime.domain.PersonalMilestoneCalculator
import com.shifenmiao.lifetime.domain.model.CountdownStatus
import com.shifenmiao.lifetime.domain.model.MilestoneStatus
import com.shifenmiao.lifetime.util.presetEventNameRes
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/**
 * AI Agent 工具:`query_life_events` — 查询倒数日与人生里程碑。
 *
 * `type` 过滤结果:countdown / milestone / all(默认);
 * `limit` 限制每类返回条数(默认 20,上限 50)。
 * 每条包含 id(可传给 delete_lifetime_entry)、名称、目标日期与剩余天数。
 */
class QueryLifeEventsTool @Inject constructor(
    private val countdownRepository: CountdownEventRepository,
    private val milestoneRepository: PersonalMilestoneRepository,
    private val lifeTimeRepository: LifeTimeRepository,
    private val countdownCalculator: CountdownCalculator,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "query_life_events"

    override val description: String =
        textProvider.string(R.string.agent_tool_query_life_events_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_query_life_events_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_query_life_events_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_query_life_events_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_query_life_events_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        properties = mapOf(
            "type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_query_life_events_param_type),
            ),
            "limit" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_query_life_events_param_limit),
            ),
        ),
        required = emptyList(),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val type = json.optString("type").trim().lowercase().ifEmpty { TYPE_ALL }
            require(type == TYPE_ALL || type == TYPE_COUNTDOWN || type == TYPE_MILESTONE) {
                textProvider.string(R.string.agent_tool_lifetime_invalid_type, type)
            }
            val limit = json.optInt("limit", DEFAULT_LIMIT).coerceIn(1, MAX_LIMIT)

            val countdowns = if (type == TYPE_MILESTONE) {
                emptyList()
            } else {
                countdownCalculator.calculateAll(countdownRepository.allCountdownsFlow.first()).take(limit)
            }
            val milestones = if (type == TYPE_COUNTDOWN) {
                emptyList()
            } else {
                val all = milestoneRepository.allMilestonesFlow.first()
                // 里程碑状态计算依赖出生日期;未设置时用今天兜底(仅影响无目标日期条目的兜底分支)
                val birthDate = lifeTimeRepository.birthDateFlow.first() ?: LocalDate.now()
                PersonalMilestoneCalculator.calculateAll(all, birthDate)
                    .sortedBy { it.daysUntil }
                    .take(limit)
            }

            val deeplink = lifetimeScreenDeeplink()
            lifetimeSuccessResult(
                markdown = buildMarkdown(countdowns, milestones, deeplink),
                deepLinks = listOf(
                    toolDeepLinkJson(
                        uri = deeplink,
                        label = textProvider.string(R.string.agent_tool_lifetime_open_link),
                        primary = true,
                    )
                ),
            ) {
                put("countdownCount", countdowns.size)
                put("milestoneCount", milestones.size)
                put("countdowns", JSONArray().apply {
                    countdowns.forEach { status -> put(countdownJson(status)) }
                })
                put("milestones", JSONArray().apply {
                    milestones.forEach { status -> put(milestoneJson(status)) }
                })
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_query_life_events_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_lifetime_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun countdownJson(status: CountdownStatus): JSONObject = JSONObject().apply {
        put("id", status.event.id)
        put("name", displayName(status.event.name, status.event.isPreset))
        status.nextOccurrence?.let { put("targetDate", it.toString()) }
        put("daysUntil", status.daysUntil)
        put("isToday", status.isToday)
        put("isPast", status.isPast)
        status.event.note?.let { put("note", it) }
    }

    private fun milestoneJson(status: MilestoneStatus): JSONObject = JSONObject().apply {
        put("id", status.milestone.id)
        put("name", status.milestone.name)
        status.milestone.targetDate?.let { put("targetDate", it.toString()) }
        put("isReached", status.isReached)
        put("daysUntil", status.daysUntil)
        put("daysSince", status.daysSince)
        status.milestone.note?.let { put("note", it) }
    }

    private fun buildMarkdown(
        countdowns: List<CountdownStatus>,
        milestones: List<MilestoneStatus>,
        deeplink: String,
    ): String {
        return buildString {
            appendLine("# ${sanitizeMarkdownText(title)}")
            appendLine()
            if (countdowns.isEmpty() && milestones.isEmpty()) {
                appendLine(textProvider.string(R.string.agent_tool_query_life_events_empty))
            }
            if (countdowns.isNotEmpty()) {
                appendLine("## ${textProvider.string(R.string.agent_tool_lifetime_section_countdown)}")
                countdowns.forEachIndexed { index, status ->
                    appendLine("${index + 1}. ${sanitizeMarkdownText(displayName(status.event.name, status.event.isPreset))} — ${countdownTimeText(status)} (id: ${status.event.id})")
                }
                appendLine()
            }
            if (milestones.isNotEmpty()) {
                appendLine("## ${textProvider.string(R.string.agent_tool_lifetime_section_milestone)}")
                milestones.forEachIndexed { index, status ->
                    appendLine("${index + 1}. ${sanitizeMarkdownText(status.milestone.name)} — ${milestoneTimeText(status)} (id: ${status.milestone.id})")
                }
                appendLine()
            }
            appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_lifetime_open_link), deeplink)}")
        }.trimEnd()
    }

    /**
     * 预置（节日）条目以中文 name 入库，返回给 AI 前映射为当前语言；
     * 用户自建条目与表外取值原样返回。
     */
    private fun displayName(name: String, isPreset: Boolean): String {
        if (!isPreset) return name
        val resId = presetEventNameRes(name) ?: return name
        return textProvider.string(resId)
    }

    private fun countdownTimeText(status: CountdownStatus): String {
        val dateText = status.nextOccurrence?.toString()
            ?: textProvider.string(R.string.agent_tool_lifetime_date_unknown)
        val daysText = when {
            status.isToday -> textProvider.string(R.string.agent_tool_lifetime_today)
            status.isPast -> textProvider.string(R.string.agent_tool_lifetime_days_ago, status.daysUntil)
            else -> textProvider.string(R.string.agent_tool_lifetime_days_left, status.daysUntil)
        }
        return "$dateText · $daysText"
    }

    private fun milestoneTimeText(status: MilestoneStatus): String {
        val dateText = status.milestone.targetDate?.toString()
            ?: textProvider.string(R.string.agent_tool_lifetime_date_unknown)
        val daysText = if (status.isReached) {
            textProvider.string(R.string.agent_tool_lifetime_reached)
        } else {
            textProvider.string(R.string.agent_tool_lifetime_days_left, status.daysUntil)
        }
        return "$dateText · $daysText"
    }

    companion object {
        private const val TYPE_ALL = "all"
        private const val TYPE_COUNTDOWN = "countdown"
        private const val TYPE_MILESTONE = "milestone"
        private const val DEFAULT_LIMIT = 20
        private const val MAX_LIMIT = 50
    }
}
