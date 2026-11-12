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

package com.shifenmiao.ai.agent.tool.builtin.network

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.RetryPolicy
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.network.service.FileDownloadService
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * Agent 工具：下载文件
 *
 * 从 URL 下载文件到本地存储
 */
class DownloadFileTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
    private val fileDownloadService: FileDownloadService,
) : AgentTool {

    override val name: String = "download_file"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_download_file)

    override val title: String =
        textProvider.string(R.string.agent_tool_download_file_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_download_file_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_download_file_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_download_file_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS

    // 声明仅供参考: requiresConfirmation=true, 执行层安全约束会强制跳过重试
    override val retryPolicy: RetryPolicy = RetryPolicy.NETWORK_DEFAULT

    override val requiresConfirmation: Boolean = true

    override val confirmationTitle: String =
        textProvider.string(R.string.agent_tool_download_file_confirm_title)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "url" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_download_file_param_url)
            ),
            "filename" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_download_file_param_filename)
            ),
            "save_path" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_download_file_param_save_path)
            )
        ),
        required = listOf("url")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = AppJson.decodeFromString<DownloadFileParams>(arguments)

            fileDownloadService.downloadSync(
                url = params.url,
                fileName = params.filename,
                savePath = params.save_path
            ).fold(
                onSuccess = { result ->
                    AgentToolResult(
                        content = buildString {
                            appendLine(
                                textProvider.string(
                                    R.string.agent_tool_download_file_success,
                                    result.fileName
                                )
                            )
                            appendLine()
                            appendLine("文件名: ${result.fileName}")
                            appendLine("文件大小: ${formatFileSize(result.fileSize)}")
                            appendLine("下载URL: ${result.downloadUrl}")
                            appendLine("保存路径: ${result.savedPath}")
                        }
                    )
                },
                onFailure = { error ->
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_download_file_failed,
                            error.message ?: "未知错误"
                        ),
                        isError = true
                    )
                }
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_download_file_failed,
                    e.message ?: "未知错误"
                ),
                isError = true
            )
        }
    }

    /**
     * 格式化文件大小
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${"%.2f".format(bytes / (1024.0 * 1024.0))} MB"
            else -> "${"%.2f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }

    @Serializable
    private data class DownloadFileParams(
        val url: String,
        val filename: String?,
        val save_path: String?
    )
}
