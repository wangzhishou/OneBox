package com.wanbaohe.period.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.period.R
import com.wanbaohe.period.model.PeriodFlow
import com.wanbaohe.period.model.PeriodMood
import com.wanbaohe.period.model.PeriodSymptom
import com.wanbaohe.period.service.PeriodService
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject
class AddPeriodRecordTool @Inject constructor(
    private val service: PeriodService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "add_period_record"

    override val description: String =
        textProvider.string(R.string.agent_tool_add_period_record_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_add_period_record_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_add_period_record_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_add_period_record_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_add_period_record_examples)

    override val requiresConfirmation: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "date" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_period_param_date),
            ),
            "is_period_start" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_period_param_is_start),
            ),
            "is_period_end" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_period_param_is_end),
            ),
            "flow_intensity" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_period_param_flow),
            ),
            "symptoms" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_period_param_symptoms),
            ),
            "mood" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_period_param_mood),
            ),
            "note" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_period_param_note),
            ),
        ),
        required = listOf("date"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val date = parseDate(json.optString("date"))
            val input = PeriodService.RecordInput(
                recordDate = date,
                isPeriodStart = json.optBoolean("is_period_start", false),
                isPeriodEnd = json.optBoolean("is_period_end", false),
                flowIntensity = PeriodFlow.fromName(json.optString("flow_intensity", "NONE")),
                symptoms = parseSymptoms(json.optJSONArray("symptoms")),
                mood = PeriodMood.fromName(json.optString("mood", "NORMAL")),
                note = json.optString("note").takeIf { it.isNotBlank() },
            )
            val recordId = service.addRecord(
                input = input,
                actor = PeriodService.ACTOR_AGENT,
                source = PeriodService.SOURCE_AGENT_ADD,
            ).getOrThrow()
            ActionUtils.showToast(R.string.period_toast_recorded)
            val listDeeplink = periodListDeeplink()
            val detailDeeplink = periodDetailDeeplink(recordId)
            AgentToolResult(
                content = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine(
                        "- **${textProvider.string(R.string.agent_tool_period_label_date)}**: $date"
                    )
                    appendLine(
                        "- **${textProvider.string(R.string.agent_tool_period_label_flow)}**: " +
                            input.flowIntensity.name
                    )
                    if (input.symptoms.isNotEmpty()) {
                        appendLine(
                            "- **${textProvider.string(R.string.agent_tool_period_label_symptoms)}**: " +
                                sanitizeMarkdownText(input.symptoms.joinToString(", ") { it.name })
                        )
                    }
                    appendLine()
                    appendLine(
                        "- ${buildMarkdownLink(
                            textProvider.string(R.string.agent_tool_period_open_link),
                            listDeeplink,
                        )}"
                    )
                    appendLine(
                        "- ${buildMarkdownLink(
                            textProvider.string(R.string.agent_tool_period_detail_link),
                            detailDeeplink,
                        )}"
                    )
                }.trimEnd(),
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_add_period_record_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_period_unknown_error),
                ),
                isError = true,
            )
        }
    }

    companion object {
        fun parseDate(raw: String): LocalDate {
            val text = raw.trim()
            require(text.isNotEmpty()) { "date 不能为空" }
            return runCatching { LocalDate.parse(text) }
                .getOrElse { error("date 格式错误,需 yyyy-MM-dd") }
        }

        fun parseSymptoms(array: JSONArray?): List<PeriodSymptom> {
            if (array == null) return emptyList()
            return (0 until array.length()).mapNotNull { index ->
                PeriodSymptom.fromName(array.optString(index))
            }.distinct()
        }

        fun periodListDeeplink(): String =
            AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.Period().routeKey,
            )

        fun periodDetailDeeplink(recordId: String): String =
            AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.Period().routeKey,
                params = mapOf("type" to "detail", "record_id" to recordId),
            )

        fun periodEditDeeplink(recordId: String): String =
            AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.Period().routeKey,
                params = mapOf("type" to "edit", "record_id" to recordId),
            )
    }
}
