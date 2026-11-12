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

package com.shifenmiao.ai.component

import com.shifenmiao.model.ai.tool.ToolCatalogItem

/**
 * 工具中心面板状态：
 * 聚合会话选择和工具目录，供聊天页工具面板直接渲染。
 */
data class ToolCenterUiState(
    val isLoading: Boolean = false,
    val allTools: List<ToolCatalogItem> = emptyList(),
    val bootstrapToolNames: List<String> = emptyList(),
    val enabledToolNames: List<String> = emptyList(),
    val systemToolNames: List<String> = emptyList(),
    val disabledSystemToolTitles: List<String> = emptyList(),
    /** 会话级记忆开关（仅会话表值，不含全局） */
    val memoryEnabled: Boolean = true,
    /** 会话级技能开关（仅会话表值，不含全局） */
    val skillsEnabled: Boolean = true,
    /** 全局记忆总开关（false 时会话行禁用展示） */
    val memoryGlobalEnabled: Boolean = true,
    /** 全局技能总开关（false 时会话行禁用展示） */
    val skillsGlobalEnabled: Boolean = true
)
