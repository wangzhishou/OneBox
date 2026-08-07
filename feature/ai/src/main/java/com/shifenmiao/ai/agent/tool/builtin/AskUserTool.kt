package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentToolExecutionContext
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.AgentUserQuestionItem
import com.shifenmiao.ai.agent.tool.AgentUserQuestionRequest
import com.shifenmiao.ai.agent.tool.ContextAwareAgentTool
import com.shifenmiao.ai.agent.tool.InteractiveAgentTool
import com.shifenmiao.ai.agent.tool.InteractiveToolResultFactory
import com.shifenmiao.ai.agent.tool.InteractiveToolRuntime
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject

class AskUserTool @Inject constructor(
    private val bridge: InteractiveToolRuntime,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : InteractiveAgentTool, ContextAwareAgentTool {

    override val name: String = "ask_user"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_ask_user)

    override val title: String =
        textProvider.string(R.string.agent_tool_ask_user_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_ask_user_summary)

    override val category: ToolCategory = ToolCategory.SYSTEM

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_ask_user_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_ask_user_examples)

    override val bootstrapModes: Set<ChatWorkingMode> = ChatWorkingMode.entries.toSet()

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    /**
     * 共享的「问题类型」枚举列表 —— request 级和 item 级都用同一份。
     * 避免两份枚举在 LLM 看来不一致（之前一份放在 request 级、一份藏在 item 描述里）。
     */
    private val questionTypeEnum = listOf(
        "text", "time", "time_range", "date", "date_range",
        "color", "city", "image", "file", "folder"
    )

    /**
     * 共享的 presentation 枚举。
     */
    private val presentationEnum = listOf("dialog", "bottom_sheet")

    /**
     * 复用的问题对象 schema —— request 级 `questions` 数组的 items 和显式声明都用同一份，
     * 避免 LLM 在 item 字段缺失时 (e.g. options 漏 value) 反复踩同一坑。
     */
    private val questionItemSchema = ToolParameterProperty(
        type = "object",
        description = textProvider.string(R.string.agent_tool_ask_user_param_questions),
        properties = mapOf(
            "name" to ToolParameterProperty(
                type = "string",
                description = "Output key for this question in the answers object. " +
                    "Use a snake_case identifier (e.g. 'image_path')."
            ),
            "header" to ToolParameterProperty(
                type = "string",
                description = "Short label rendered above the question (1-6 chars recommended)."
            ),
            "question" to ToolParameterProperty(
                type = "string",
                description = "Full question text shown to the user."
            ),
            "required" to ToolParameterProperty(
                type = "boolean",
                description = "Whether the user must answer this question to submit the form. " +
                    "Default false."
            ),
            "type" to ToolParameterProperty(
                type = "string",
                enum = questionTypeEnum,
                description = "Input control type. Default 'text'. " +
                    "Specialized types (image/file/folder) open a system picker on the right side " +
                    "of the text field and auto-transcribe the result to a file:// URI."
            ),
            "options" to ToolParameterProperty(
                type = "array",
                description = "Choice options. When non-empty the question becomes a " +
                    "single/multi select; otherwise it is a free-text input. " +
                    "Each option MUST contain both 'label' (display text) AND 'value' " +
                    "(machine-readable identifier) — omitting 'value' is the most common mistake.",
                items = ToolParameterProperty(
                    type = "object",
                    properties = mapOf(
                        "label" to ToolParameterProperty(
                            type = "string",
                            description = "Display text shown to the user."
                        ),
                        "value" to ToolParameterProperty(
                            type = "string",
                            description = "Machine-readable identifier returned in the answers " +
                                "object. REQUIRED when 'options' is used. " +
                                "Do NOT omit this field even if label looks like a value."
                        )
                    ),
                    required = listOf("label", "value")
                )
            ),
            "multiSelect" to ToolParameterProperty(
                type = "boolean",
                description = "When 'options' is non-empty, allow multiple selection. " +
                    "Default false."
            ),
            "placeholder" to ToolParameterProperty(
                type = "string",
                description = "Placeholder text inside the input field."
            ),
            "multiline" to ToolParameterProperty(
                type = "boolean",
                description = "Render the text field as multi-line. Default false."
            )
        ),
        required = listOf("name", "question")
    )

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "presentation" to ToolParameterProperty(
                type = "string",
                enum = presentationEnum,
                description = "UI presentation. 'dialog' (default) for short forms, " +
                    "'bottom_sheet' for long forms or many questions."
            ),
            "title" to ToolParameterProperty(
                type = "string",
                description = "Title shown in the dialog / bottom sheet header."
            ),
            "message" to ToolParameterProperty(
                type = "string",
                description = "Optional body text shown above the questions."
            ),
            "questions" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_ask_user_param_questions),
                items = questionItemSchema
            ),
            "confirmText" to ToolParameterProperty(
                type = "string",
                description = "Custom text for the confirm button. Optional."
            ),
            "cancelText" to ToolParameterProperty(
                type = "string",
                description = "Custom text for the cancel button. Optional."
            )
        ),
        required = listOf("questions")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return execute(
            arguments = arguments,
            context = AgentToolExecutionContext(
                toolCallId = "ask_user_${System.currentTimeMillis()}"
            )
        )
    }

    override suspend fun execute(
        arguments: String,
        context: AgentToolExecutionContext
    ): AgentToolResult {
        return runCatching {
            val request = parseArguments(arguments).copy(
                toolCallId = context.toolCallId ?: "ask_user_${System.currentTimeMillis()}",
                toolName = name,
                interactionOwnerId = context.interactionOwnerId
            )
            validateRequest(request)

            val answersJson = bridge.requestUserQuestion(request)
            if (answersJson.isNullOrBlank()) {
                InteractiveToolResultFactory.buildQuestionCancelledResult(gson)
            } else {
                InteractiveToolResultFactory.buildQuestionSubmittedResult(answersJson, gson)
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_ask_user_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private fun parseArguments(arguments: String): AgentUserQuestionRequest {
        if (arguments.isBlank()) {
            throw IllegalArgumentException(
                textProvider.string(R.string.agent_tool_ask_user_missing_questions)
            )
        }
        return gson.fromJson(arguments, AgentUserQuestionRequest::class.java)
    }

    private fun validateRequest(request: AgentUserQuestionRequest) {
        if (request.questions.isEmpty()) {
            throw IllegalArgumentException(
                textProvider.string(R.string.agent_tool_ask_user_missing_questions)
            )
        }

        val duplicateNames = request.questions
            .map { it.name.trim() }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        if (duplicateNames.isNotEmpty()) {
            throw IllegalArgumentException(
                textProvider.string(
                    R.string.agent_tool_ask_user_duplicate_question_names,
                    duplicateNames.joinToString(", ")
                )
            )
        }

        request.questions.forEach { item ->
            validateQuestionItem(item)
        }
    }

    private fun validateQuestionItem(item: AgentUserQuestionItem) {
        if (item.name.isBlank()) {
            throw IllegalArgumentException(
                textProvider.string(R.string.agent_tool_ask_user_question_name_required)
            )
        }
        if (item.question.isBlank()) {
            throw IllegalArgumentException(
                textProvider.string(
                    R.string.agent_tool_ask_user_question_text_required,
                    item.name
                )
            )
        }
        if (item.isChoiceQuestion) {
            val missingLabel = item.options.filter { it.label.isBlank() }
            val missingValue = item.options.filter { it.value.isBlank() }
            val emptyOptions = item.options.filter { it.label.isBlank() && it.value.isBlank() }
            when {
                missingValue.isNotEmpty() -> {
                    throw IllegalArgumentException(
                        textProvider.string(
                            R.string.agent_tool_ask_user_question_option_missing_value,
                            item.name,
                            missingValue.joinToString(", ") { it.label.ifBlank { "<empty>" } }
                        )
                    )
                }
                missingLabel.isNotEmpty() -> {
                    throw IllegalArgumentException(
                        textProvider.string(
                            R.string.agent_tool_ask_user_question_option_missing_label,
                            item.name,
                            missingLabel.joinToString(", ") { it.value.ifBlank { "<empty>" } }
                        )
                    )
                }
                emptyOptions.isNotEmpty() -> {
                    throw IllegalArgumentException(
                        textProvider.string(
                            R.string.agent_tool_ask_user_question_option_empty,
                            item.name
                        )
                    )
                }
            }
        }
    }
}
