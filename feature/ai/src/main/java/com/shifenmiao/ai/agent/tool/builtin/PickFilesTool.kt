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

package com.shifenmiao.ai.agent.tool.builtin

import android.content.Context
import androidx.core.net.toUri
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentToolExecutionContext
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ContextAwareAgentTool
import com.shifenmiao.ai.agent.tool.FilePickerRequest
import com.shifenmiao.ai.agent.tool.InteractiveAgentTool
import com.shifenmiao.ai.agent.tool.InteractiveToolRuntime
import com.shifenmiao.model.jsonStringOf
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

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
            runCatching { AppJson.decodeFromString<PickFilesParams>(arguments) }.getOrNull() ?: PickFilesParams()
        }.getOrElse { PickFilesParams() }
    }

    private fun buildSuccessResult(urisCsv: String): AgentToolResult {
        val uris = urisCsv.split(",").filter { it.isNotBlank() }
        val fileUris = uris.map { raw -> convertToFileUri(raw) ?: raw }
        return AgentToolResult(
            content = jsonStringOf(
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
            content = jsonStringOf(mapOf("status" to "cancelled")),
            isError = false
        )

    @Serializable
    private data class PickFilesParams(
        val message: String? = null,
        val mimeType: String? = null,
        val multiple: Boolean? = null
    )
}

