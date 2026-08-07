package com.shifenmiao.ai.agent.tool.builtin

import android.content.Context
import androidx.core.net.toUri
import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentToolExecutionContext
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ContextAwareAgentTool
import com.shifenmiao.ai.agent.tool.FilePickerRequest
import com.shifenmiao.ai.agent.tool.InteractiveAgentTool
import com.shifenmiao.ai.agent.tool.InteractiveToolRuntime
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * 让用户通过系统文件选择器选取一个或多个文件。
 *
 * 当 agent 需要处理用户设备上的文件（如读取、转换、上传等），
 * 但不知道具体路径时，调用此工具让用户手动选取。
 *
 * 返回所选文件的 file:// URI 列表，可直接传给其他文件操作工具。
 */
class PickFilesTool @Inject constructor(
    private val bridge: InteractiveToolRuntime,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
    @ApplicationContext private val context: Context
) : InteractiveAgentTool, ContextAwareAgentTool {

    override val name: String = "pick_files"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_pick_files)

    override val title: String =
        textProvider.string(R.string.agent_tool_pick_files_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_pick_files_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_pick_files_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_pick_files_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    /** 文件选取不可并行——同时只能弹一个系统 picker */
    override val parallelizable: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "message" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_pick_files_param_message)
            ),
            "mimeType" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_pick_files_param_mime_type)
            ),
            "multiple" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_pick_files_param_multiple)
            )
        ),
        required = emptyList()
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        error("PickFilesTool requires toolCallId context. Must be called via execute(arguments, context) through AgentToolRegistry.")
    }

    override suspend fun execute(
        arguments: String,
        context: AgentToolExecutionContext
    ): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            val request = FilePickerRequest(
                toolCallId = requireNotNull(context.toolCallId) { "toolCallId must not be null in PickFilesTool; ensure it is provided by AgentToolRegistry" },
                toolName = name,
                message = params.message.orEmpty(),
                mimeType = params.mimeType?.takeIf { it.isNotBlank() } ?: "*/*",
                multiple = params.multiple ?: false,
                interactionOwnerId = context.interactionOwnerId
            )

            val urisCsv = bridge.requestFilePicker(request)
            if (urisCsv.isNullOrBlank()) {
                buildCancelledResult()
            } else {
                buildSuccessResult(urisCsv)
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_pick_files_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private fun parseArguments(arguments: String): PickFilesParams {
        if (arguments.isBlank() || arguments.trim() == "{}") return PickFilesParams()
        return runCatching {
            gson.fromJson(arguments, PickFilesParams::class.java) ?: PickFilesParams()
        }.getOrElse { PickFilesParams() }
    }

    private fun buildSuccessResult(urisCsv: String): AgentToolResult {
        val uris = urisCsv.split(",").filter { it.isNotBlank() }
        val fileUris = uris.map { raw -> convertToFileUri(raw) ?: raw }
        return AgentToolResult(
            content = gson.toJson(
                mapOf(
                    "status" to "selected",
                    "count" to fileUris.size,
                    "files" to fileUris
                )
            ),
            isError = false
        )
    }

    private fun convertToFileUri(raw: String): String? {
        return runCatching {
            SafUriUtils.toFileUri(context, raw.toUri())?.toString()
        }.getOrNull()
    }

    private fun buildCancelledResult(): AgentToolResult =
        AgentToolResult(
            content = gson.toJson(mapOf("status" to "cancelled")),
            isError = false
        )

    private data class PickFilesParams(
        val message: String? = null,
        val mimeType: String? = null,
        val multiple: Boolean? = null
    )
}

