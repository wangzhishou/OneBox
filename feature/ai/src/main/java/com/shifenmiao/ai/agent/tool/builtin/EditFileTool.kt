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
import com.shifenmiao.model.file.AgentApplyRangePatchParams
import com.shifenmiao.model.file.AgentApplyTextPatchParams
import com.shifenmiao.model.file.AgentEditFileData
import com.shifenmiao.model.file.AgentEditFileParams
import com.shifenmiao.model.file.AgentFileOperationResult
import com.shifenmiao.model.file.AgentFileService
import com.shifenmiao.model.file.AgentRangePatchHunk
import com.shifenmiao.model.file.AgentTextPatchHunk
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * 文件编辑工具（合并原 edit_file / apply_text_patch / apply_range_patch 三者能力）。
 *
 * 通过 action 区分编辑模式：
 * - 单点编辑：replace_text / replace_lines / insert_before_line / insert_after_line / append / prepend
 * - 多段补丁：apply_text_patch（按 old_text/new_text 精准匹配）、apply_range_patch（按起止行号）
 */
class EditFileTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "edit_file"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_edit_file)

    override val title: String = textProvider.string(R.string.agent_tool_edit_file_title)

    override val summary: String = textProvider.string(R.string.agent_tool_edit_file_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_edit_file_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_edit_file_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS

    override val requiresConfirmation: Boolean = true

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "file_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_file_param_file_uri),
            ),
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_file_param_action),
                enum = listOf(
                    "replace_text",
                    "replace_lines",
                    "insert_before_line",
                    "insert_after_line",
                    "append",
                    "prepend",
                    "apply_text_patch",
                    "apply_range_patch",
                ),
            ),
            "old_text" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_file_param_old_text),
            ),
            "new_text" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_file_param_new_text),
            ),
            "start_line" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_edit_file_param_start_line),
            ),
            "end_line" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_edit_file_param_end_line),
            ),
            "line" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_edit_file_param_line),
            ),
            "replace_all" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_edit_file_param_replace_all),
            ),
            "hunks" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_edit_file_param_hunks),
            ),
        ),
        required = listOf("file_uri", "action"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = AppJson.decodeFromString<EditFileToolParams>(arguments)
            val fileUri = params.file_uri?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_edit_file_missing_file_uri),
                    isError = true,
                )
            val action = params.action?.trim().orEmpty()
            if (action.isBlank()) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_edit_file_missing_action),
                    isError = true,
                )
            }
            val validationError = validate(params)
            if (validationError != null) {
                return AgentToolResult(content = validationError, isError = true)
            }

            when (action) {
                ACTION_APPLY_TEXT_PATCH -> applyTextPatch(fileUri, params)
                ACTION_APPLY_RANGE_PATCH -> applyRangePatch(fileUri, params)
                else -> editFile(fileUri, action, params)
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_edit_file_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private suspend fun editFile(
        fileUri: String,
        action: String,
        params: EditFileToolParams,
    ): AgentToolResult {
        return when (
            val result = agentFileService.editFile(
                AgentEditFileParams(
                    fileUri = fileUri,
                    action = action,
                    newText = params.new_text,
                    oldText = params.old_text,
                    startLine = params.start_line,
                    endLine = params.end_line,
                    line = params.line,
                    replaceAll = params.replace_all == true,
                )
            )
        ) {
            is AgentFileOperationResult.Success -> success(result.data)
            is AgentFileOperationResult.Error -> failure(result.message)
        }
    }

    private suspend fun applyTextPatch(
        fileUri: String,
        params: EditFileToolParams,
    ): AgentToolResult {
        val hunks = params.hunks.orEmpty().map { hunk ->
            AgentTextPatchHunk(
                oldText = hunk.old_text.orEmpty(),
                newText = hunk.new_text.orEmpty(),
                replaceAll = hunk.replace_all == true,
            )
        }
        return when (
            val result = agentFileService.applyTextPatch(
                AgentApplyTextPatchParams(
                    fileUri = fileUri,
                    hunks = hunks,
                )
            )
        ) {
            is AgentFileOperationResult.Success -> AgentToolResult(AppJson.encodeToString(result.data))
            is AgentFileOperationResult.Error -> failure(result.message)
        }
    }

    private suspend fun applyRangePatch(
        fileUri: String,
        params: EditFileToolParams,
    ): AgentToolResult {
        val hunks = params.hunks.orEmpty().map { hunk ->
            AgentRangePatchHunk(
                startLine = hunk.start_line ?: 0,
                endLine = hunk.end_line ?: 0,
                newText = hunk.new_text.orEmpty(),
                oldText = hunk.old_text,
            )
        }
        return when (
            val result = agentFileService.applyRangePatch(
                AgentApplyRangePatchParams(
                    fileUri = fileUri,
                    hunks = hunks,
                )
            )
        ) {
            is AgentFileOperationResult.Success -> AgentToolResult(AppJson.encodeToString(result.data))
            is AgentFileOperationResult.Error -> failure(result.message)
        }
    }

    private fun validate(params: EditFileToolParams): String? {
        return when (params.action?.trim()) {
            "replace_text" -> {
                when {
                    params.old_text.isNullOrEmpty() -> textProvider.string(R.string.agent_tool_edit_file_missing_old_text)
                    params.new_text == null -> textProvider.string(R.string.agent_tool_edit_file_missing_new_text)
                    else -> null
                }
            }

            "replace_lines" -> {
                when {
                    params.new_text == null -> textProvider.string(R.string.agent_tool_edit_file_missing_new_text)
                    params.start_line == null -> textProvider.string(R.string.agent_tool_edit_file_missing_start_line)
                    params.end_line == null -> textProvider.string(R.string.agent_tool_edit_file_missing_end_line)
                    else -> null
                }
            }

            "insert_before_line", "insert_after_line" -> {
                when {
                    params.new_text == null -> textProvider.string(R.string.agent_tool_edit_file_missing_new_text)
                    params.line == null -> textProvider.string(R.string.agent_tool_edit_file_missing_line)
                    else -> null
                }
            }

            "append", "prepend" -> {
                if (params.new_text == null) {
                    textProvider.string(R.string.agent_tool_edit_file_missing_new_text)
                } else {
                    null
                }
            }

            ACTION_APPLY_TEXT_PATCH -> validateHunks(params) { hunk ->
                hunk.old_text != null && hunk.new_text != null
            }

            ACTION_APPLY_RANGE_PATCH -> validateHunks(params) { hunk ->
                hunk.start_line != null && hunk.end_line != null && hunk.new_text != null
            }

            else -> textProvider.string(
                R.string.agent_tool_edit_file_invalid_action,
                params.action.orEmpty(),
            )
        }
    }

    private inline fun validateHunks(
        params: EditFileToolParams,
        isHunkValid: (EditFileHunkPayload) -> Boolean,
    ): String? {
        val hunks = params.hunks
        if (hunks.isNullOrEmpty()) {
            return textProvider.string(R.string.agent_tool_edit_file_missing_hunks)
        }
        val invalidIndex = hunks.indexOfFirst { !isHunkValid(it) }
        if (invalidIndex >= 0) {
            return textProvider.string(R.string.agent_tool_edit_file_invalid_hunk, invalidIndex + 1)
        }
        return null
    }

    private fun success(data: AgentEditFileData): AgentToolResult {
        return AgentToolResult(
            content = AppJson.encodeToString(
                EditFileResult(
                    action = data.action,
                    success = true,
                    requiresConfirmation = requiresConfirmation,
                    fileUri = data.fileUri,
                    preview = data.preview,
                    replacedCount = data.replacedCount,
                    startLine = data.startLine,
                    endLine = data.endLine,
                    line = data.line,
                    deeplink = data.parentDirectoryUri?.let { "dir://$it" },
                )
            )
        )
    }

    private fun failure(message: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_edit_file_failed,
                message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) },
            ),
            isError = true,
        )
    }

    private companion object {
        const val ACTION_APPLY_TEXT_PATCH = "apply_text_patch"
        const val ACTION_APPLY_RANGE_PATCH = "apply_range_patch"
    }
}

@Serializable
private data class EditFileToolParams(
    val file_uri: String? = null,
    val action: String? = null,
    val old_text: String? = null,
    val new_text: String? = null,
    val start_line: Int? = null,
    val end_line: Int? = null,
    val line: Int? = null,
    val replace_all: Boolean? = null,
    val hunks: List<EditFileHunkPayload>? = null,
)

/** 补丁段负载：apply_text_patch 用 old_text/new_text/replace_all，apply_range_patch 用 start_line/end_line/new_text/old_text */
@Serializable
private data class EditFileHunkPayload(
    val old_text: String? = null,
    val new_text: String? = null,
    val replace_all: Boolean? = null,
    val start_line: Int? = null,
    val end_line: Int? = null,
)

@Serializable
private data class EditFileResult(
    val action: String,
    val success: Boolean,
    val requiresConfirmation: Boolean,
    val fileUri: String,
    val preview: String,
    val replacedCount: Int,
    val startLine: Int?,
    val endLine: Int?,
    val line: Int?,
    val deeplink: String?,
)
