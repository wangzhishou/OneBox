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

package com.shifenmiao.ai.agent.tool

/**
 * 文件选取请求，由 [PickFilesTool] 发布，由 UI 层的系统文件选择器消费。
 *
 * @param toolCallId    关联的工具调用 ID，用于 UI 回填时精确匹配
 * @param toolName      工具名称（供日志使用）
 * @param message       弹出文件选择器前展示的提示语（可为空）
 * @param mimeType      MIME 类型过滤，默认 all
 * @param multiple      是否允许多选，true 时使用 OpenMultipleDocuments
 * @param interactionOwnerId  所属会话 ID，用于清理孤立请求
 */
data class FilePickerRequest(
    val toolCallId: String = "",
    val toolName: String = "",
    val message: String = "",
    val mimeType: String = "*/*",
    val multiple: Boolean = false,
    val interactionOwnerId: String? = null
)

