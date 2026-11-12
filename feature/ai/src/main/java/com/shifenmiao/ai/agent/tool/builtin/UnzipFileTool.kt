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

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.file.AgentFileOperationResult
import com.shifenmiao.model.file.AgentFileService
import com.shifenmiao.model.file.AgentUnzipFileParams
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * zip 解压工具:把 zip 压缩包解压到目标目录(默认解压到压缩包同名文件夹),
 * 返回解压出的文件列表。只做解压一件事,后续处理(如 import_font 导入字体)
 * 由调用方按需组合。
 */
class UnzipFileTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "unzip_file"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_unzip_file)

    override val title: String = textProvider.string(R.string.agent_tool_unzip_file_title)

    override val summary: String = textProvider.string(R.string.agent_tool_unzip_file_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_unzip_file_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_unzip_file_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parallelizable: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "source_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_unzip_file_param_source_uri)
            ),
            "destination_dir_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_unzip_file_param_destination_dir_uri)
            )
        ),
        required = listOf("source_uri")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) UnzipFileParams() else {
                AppJson.decodeFromString<UnzipFileParams>(arguments)
            }
            val sourceUri = params.source_uri?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_unzip_file_missing_source_uri),
                    isError = true
                )
            when (
                val result = agentFileService.unzipFile(
                    AgentUnzipFileParams(
                        sourceUri = sourceUri,
                        destinationDirectoryUri = params.destination_dir_uri?.takeIf { it.isNotBlank() },
                    )
                )
            ) {
                is AgentFileOperationResult.Success -> AgentToolResult(
                    content = AppJson.encodeToString(result.data)
                )
                is AgentFileOperationResult.Error -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_unzip_file_failed,
                        result.message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) }
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_unzip_file_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }
}

@Serializable
private data class UnzipFileParams(
    val source_uri: String? = null,
    val destination_dir_uri: String? = null
)
