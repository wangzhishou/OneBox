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
import com.shifenmiao.model.file.AgentGrepFilesParams
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class GrepFilesTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "grep_files"
    override val description: String = textProvider.raw(R.raw.agent_tool_description_grep_files)
    override val title: String = textProvider.string(R.string.agent_tool_grep_files_title)
    override val summary: String = textProvider.string(R.string.agent_tool_grep_files_summary)
    override val category: ToolCategory = ToolCategory.FILE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_grep_files_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_grep_files_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "directory_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_grep_files_param_directory_uri),
            ),
            "glob_pattern" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_grep_files_param_glob_pattern),
            ),
            "query" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_grep_files_param_query),
            ),
            "is_regex" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_grep_files_param_is_regex),
            ),
            "case_sensitive" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_grep_files_param_case_sensitive),
            ),
            "max_matches" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_grep_files_param_max_matches),
            ),
        ),
        required = listOf("query"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) GrepFilesToolParams() else {
                AppJson.decodeFromString<GrepFilesToolParams>(arguments)
            }
            val query = params.query?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_grep_files_missing_query),
                    isError = true,
                )
            when (
                val result = agentFileService.grepFiles(
                    AgentGrepFilesParams(
                        directoryUri = params.directory_uri,
                        globPattern = params.glob_pattern,
                        query = query,
                        isRegex = params.is_regex == true,
                        caseSensitive = params.case_sensitive == true,
                        maxMatches = params.max_matches ?: 200,
                    )
                )
            ) {
                is AgentFileOperationResult.Success -> AgentToolResult(AppJson.encodeToString(result.data))
                is AgentFileOperationResult.Error -> failure(result.message)
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_grep_files_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun failure(message: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_grep_files_failed,
                message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) },
            ),
            isError = true,
        )
    }
}

@Serializable
private data class GrepFilesToolParams(
    val directory_uri: String? = null,
    val glob_pattern: String? = null,
    val query: String? = null,
    val is_regex: Boolean? = null,
    val case_sensitive: Boolean? = null,
    val max_matches: Int? = null,
)

