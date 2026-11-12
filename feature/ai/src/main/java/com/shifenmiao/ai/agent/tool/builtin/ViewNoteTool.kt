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
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.note.NoteService
import org.json.JSONObject
import javax.inject.Inject

/**
 * 查看笔记工具 — 读取笔记内容
 *
 * 提供 note_id 即可获取笔记的标题、描述和正文。
 * 只读操作，不会修改笔记。
 */
class ViewNoteTool @Inject constructor(
    private val noteService: NoteService,
    private val textProvider: AgentToolTextProvider
) : AgentTool {
    override val name = "view_note"
    override val description = textProvider.string(R.string.agent_tool_view_note_description)
    override val title: String = textProvider.string(R.string.agent_tool_view_note_title)
    override val summary: String = textProvider.string(R.string.agent_tool_view_note_summary)
    override val category: ToolCategory = ToolCategory.KNOWLEDGE
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE
    override val parametersSchema = ToolParameters(
        properties = mapOf(
            "note_id" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_view_note_param_note_id)
            )
        ),
        required = listOf("note_id")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val json = JSONObject(arguments)
            val noteId = json.getInt("note_id")

            val note = noteService.getNoteById(noteId)
            if (note == null) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_view_note_not_found, noteId),
                    isError = true
                )
            }

            AgentToolResult(
                content = buildString {
                    appendLine(textProvider.string(R.string.agent_tool_view_note_line_title, note.title))
                    appendLine(textProvider.string(R.string.agent_tool_view_note_line_description, note.description))
                    appendLine("---")
                    appendLine(note.data)
                }
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_view_note_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }
}
