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
import com.shifenmiao.model.file.AgentBrowseFilesData
import com.shifenmiao.model.file.AgentBrowseFilesParams
import com.shifenmiao.model.file.AgentFileOperationResult
import com.shifenmiao.model.file.AgentFileService
import com.shifenmiao.model.file.AgentLocateFileData
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class BrowseFilesTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "browse_files"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_browse_files)

    override val title: String = textProvider.string(R.string.agent_tool_browse_files_title)

    override val summary: String = textProvider.string(R.string.agent_tool_browse_files_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_browse_files_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_browse_files_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browse_files_param_action),
                enum = listOf("list", "locate")
            ),
            "directory_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browse_files_param_directory_uri)
            ),
            "target_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browse_files_param_target_uri)
            ),
            "limit" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_browse_files_param_limit)
            ),
            "include_directories" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_browse_files_param_include_directories)
            ),
            "include_files" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_browse_files_param_include_files)
            )
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) BrowseFilesParams() else {
                AppJson.decodeFromString<BrowseFilesParams>(arguments)
            }
            when (params.action?.trim()) {
                "list" -> executeList(params)
                "locate" -> executeLocate(params)
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_browse_files_invalid_action,
                        params.action.orEmpty()
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_browse_files_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun executeList(params: BrowseFilesParams): AgentToolResult {
        return when (
            val result = agentFileService.browseFiles(
                AgentBrowseFilesParams(
                    directoryUri = params.directory_uri,
                    limit = params.limit ?: 20,
                    includeDirectories = params.include_directories != false,
                    includeFiles = params.include_files != false,
                )
            )
        ) {
            is AgentFileOperationResult.Success -> successListResult(result.data)
            is AgentFileOperationResult.Error -> failure(result.message)
        }
    }

    private suspend fun executeLocate(params: BrowseFilesParams): AgentToolResult {
        val targetUri = params.target_uri?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_browse_files_missing_target_uri),
                isError = true
            )
        return when (val result = agentFileService.locateFile(targetUri)) {
            is AgentFileOperationResult.Success -> successLocateResult(result.data)
            is AgentFileOperationResult.Error -> failure(result.message)
        }
    }

    private fun successListResult(data: AgentBrowseFilesData): AgentToolResult {
        return AgentToolResult(
            content = AppJson.encodeToString(
                BrowseFilesListResult(
                    action = "list",
                    directoryUri = data.directoryUri,
                    displayPath = data.displayPath,
                    parentDirectoryUri = data.parentDirectoryUri,
                    returnedCount = data.returnedCount,
                    items = data.items.map { item ->
                        BrowseFileItemPayload(
                            uri = item.uri,
                            name = item.name,
                            isDirectory = item.isDirectory,
                            sizeBytes = item.sizeBytes,
                            formattedSize = item.formattedSize,
                            mimeType = item.mimeType,
                            path = item.path,
                            lastModified = item.lastModified,
                        )
                    },
                    deeplink = "dir://${data.directoryUri}",
                )
            )
        )
    }

    private fun successLocateResult(data: AgentLocateFileData): AgentToolResult {
        return AgentToolResult(
            content = AppJson.encodeToString(
                BrowseFilesLocateResult(
                    action = "locate",
                    targetUri = data.targetUri,
                    fileName = data.fileName,
                    directoryUri = data.directoryUri,
                    displayPath = data.displayPath,
                    parentDirectoryUri = data.parentDirectoryUri,
                    deeplink = "dir://${data.directoryUri}",
                )
            )
        )
    }

    private fun failure(message: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_browse_files_failed,
                message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) }
            ),
            isError = true
        )
    }
}

@Serializable
private data class BrowseFilesParams(
    val action: String? = null,
    val directory_uri: String? = null,
    val target_uri: String? = null,
    val limit: Int? = null,
    val include_directories: Boolean? = null,
    val include_files: Boolean? = null
)

@Serializable
private data class BrowseFilesListResult(
    val action: String,
    val directoryUri: String?,
    val displayPath: String,
    val parentDirectoryUri: String?,
    val returnedCount: Int,
    val items: List<BrowseFileItemPayload>,
    val deeplink: String?
)

@Serializable
private data class BrowseFilesLocateResult(
    val action: String,
    val targetUri: String,
    val fileName: String,
    val directoryUri: String,
    val displayPath: String,
    val parentDirectoryUri: String?,
    val deeplink: String?
)

@Serializable
private data class BrowseFileItemPayload(
    val uri: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val formattedSize: String,
    val mimeType: String?,
    val path: String,
    val lastModified: String
)
