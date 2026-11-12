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
import com.shifenmiao.model.file.AgentGlobFilesParams
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class GlobFilesTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "glob_files"
    override val description: String = textProvider.raw(R.raw.agent_tool_description_glob_files)
    override val title: String = textProvider.string(R.string.agent_tool_glob_files_title)
    override val summary: String = textProvider.string(R.string.agent_tool_glob_files_summary)
    override val category: ToolCategory = ToolCategory.FILE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_glob_files_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_glob_files_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "directory_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_glob_files_param_directory_uri),
            ),
            "glob_pattern" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_glob_files_param_glob_pattern),
            ),
            "include_directories" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_glob_files_param_include_directories),
            ),
            "include_files" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_glob_files_param_include_files),
            ),
            "max_results" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_glob_files_param_max_results),
            ),
        ),
        required = listOf("glob_pattern"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) GlobFilesToolParams() else {
                AppJson.decodeFromString<GlobFilesToolParams>(arguments)
            }
            val globPattern = params.glob_pattern?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_glob_files_missing_glob_pattern),
                    isError = true,
                )
            when (
                val result = agentFileService.globFiles(
                    AgentGlobFilesParams(
                        directoryUri = params.directory_uri,
                        globPattern = globPattern,
                        includeDirectories = params.include_directories == true,
                        includeFiles = params.include_files != false,
                        maxResults = params.max_results ?: 200,
                    )
                )
            ) {
                is AgentFileOperationResult.Success -> AgentToolResult(AppJson.encodeToString(result.data))
                is AgentFileOperationResult.Error -> failure(result.message)
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_glob_files_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun failure(message: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_glob_files_failed,
                message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) },
            ),
            isError = true,
        )
    }
}

@Serializable
private data class GlobFilesToolParams(
    val directory_uri: String? = null,
    val glob_pattern: String? = null,
    val include_directories: Boolean? = null,
    val include_files: Boolean? = null,
    val max_results: Int? = null,
)

