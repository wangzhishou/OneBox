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
import com.shifenmiao.ai.skill.SkillRepository
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * 内置工具：按 name/slug 加载技能的完整 SKILL.md 正文。
 *
 * prompt 只注入 `<available_skills>` 清单（name/description），
 * 正文由本工具按需加载并记录使用频次。隐式系统工具（visibleToUser = false）。
 */
class UseSkillTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
    private val skillRepository: SkillRepository,
) : AgentTool {

    companion object {
        const val TOOL_NAME = "use_skill"
    }

    override val name: String = TOOL_NAME

    override val description: String = textProvider.string(R.string.agent_tool_use_skill_description)

    override val category: ToolCategory = ToolCategory.SYSTEM

    /** 隐式系统工具，不在工具中心展示 */
    override val visibleToUser: Boolean = false

    /** SKILL.md 常见 5–15KB，默认 4096 会截断 */
    override val maxResultLength: Int = 16384

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_use_skill_param_name)
            )
        ),
        required = listOf("name")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = runCatching {
            AppJson.decodeFromString<UseSkillParams>(arguments)
        }.getOrNull()
        val key = params?.name?.trim().orEmpty()
        if (key.isEmpty()) {
            return AgentToolResult(
                content = "Parameter 'name' is required and must not be blank",
                isError = true
            )
        }

        skillRepository.loadSkillBody(key)?.let { body ->
            return AgentToolResult(content = body)
        }

        // 找不到（或已停用）时返回可用技能名清单，供模型改选
        val available = skillRepository.skills.value
            .filter { it.enabled }
            .joinToString(", ") { it.name }
            .ifEmpty { textProvider.string(R.string.agent_tool_use_skill_none_available) }
        return AgentToolResult(
            content = textProvider.string(R.string.agent_tool_use_skill_not_found, key, available)
        )
    }

    @Serializable
    private data class UseSkillParams(val name: String? = null)
}
