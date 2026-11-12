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
import com.shifenmiao.ai.agent.tool.FolderPickerRequest
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
 * 让用户通过系统目录选择器选取一个工作目录。
 *
 * 当 agent 需要知道文件输出目录、扫描目录或操作目录时，
 * 但不知道具体路径时，调用此工具让用户手动选取。
 *
 * 返回目录的 file:// URI，可直接传给其他文件操作工具。
 */
class PickFolderTool @Inject constructor(
    private val bridge: InteractiveToolRuntime,
    private val textProvider: AgentToolTextProvider,
    @ApplicationContext private val context: Context
) : InteractiveAgentTool, ContextAwareAgentTool {

    override val name: String = "pick_folder"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_pick_folder)

    override val title: String =
        textProvider.string(R.string.agent_tool_pick_folder_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_pick_folder_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_pick_folder_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_pick_folder_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    /** 目录选取不可并行——同时只能弹一个系统 picker */
    override val parallelizable: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "message" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_pick_folder_param_message)
            )
        ),
        required = emptyList()
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        error("PickFolderTool requires toolCallId context. Must be called via execute(arguments, context) through AgentToolRegistry.")
    }

    override suspend fun execute(
        arguments: String,
        context: AgentToolExecutionContext
    ): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            val request = FolderPickerRequest(
                toolCallId = context.toolCallId ?: "pick_folder_${System.currentTimeMillis()}",
                toolName = name,
                message = params.message.orEmpty(),
                interactionOwnerId = context.interactionOwnerId
            )

            val uriString = bridge.requestFolderPicker(request)
            if (uriString.isNullOrBlank()) {
                buildCancelledResult()
            } else {
                buildSuccessResult(uriString)
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_pick_folder_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private fun parseArguments(arguments: String): PickFolderParams {
        if (arguments.isBlank() || arguments.trim() == "{}") return PickFolderParams()
        return runCatching {
            runCatching { AppJson.decodeFromString<PickFolderParams>(arguments) }.getOrNull() ?: PickFolderParams()
        }.getOrElse { PickFolderParams() }
    }

    private fun buildSuccessResult(uriString: String): AgentToolResult {
        val fileUri = convertToFileUri(uriString) ?: uriString
        return AgentToolResult(
            content = jsonStringOf(
                mapOf(
                    "status" to "selected",
                    "folderUri" to fileUri
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
    private data class PickFolderParams(
        val message: String? = null
    )
}

