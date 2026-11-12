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

package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.Serializable

/**
 * skill 域 wire 模型(DSH 0.1.5-rc.2)。
 *
 * 命名空间是 **skills**(带 s):`skills/list` 的 args 是 `{request:{sessionId}}`,
 * 主机从会话头解析项目根目录;调用 = 内容恰好是单个 "/" 开头文本块的普通 prompt。
 */

/** 单个技能条目;[modelInvocable] = 是否允许模型自主调用 */
@Serializable
data class SkillEntry(
    val name: String,
    val description: String = "",
    val whenToUse: String? = null,
    val modelInvocable: Boolean = false,
    val path: String? = null
)

/** skills/list 的响应 value */
@Serializable
data class SkillListValue(
    val skills: List<SkillEntry> = emptyList()
)

/** skills/list 上行 request */
@Serializable
data class SkillListRequest(
    val sessionId: String
)
