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

package com.shifenmiao.model.todo

/**
 * 待办清单 Service 的写入参数与结果模型。
 *
 * 这些类型由 Service 接口引用，供 UI 层和 AgentTool 层共用。
 */

// ── 写入参数 ─────────────────────────────────────────

data class CategoryInput(
    val title: String,
    val iconKey: String,
    val sortOrder: Int = 0,
    val description: String? = null,
    val colorArgb: Int? = null,
    val isPinned: Boolean = false,
    /** 可选：调用方预生成的分类 id（用于「按 ID 分配默认色」与最终入库 id 一致）；null = 服务端生成 */
    val id: String? = null,
)

/**
 * @param priority 0=低，1=中（默认），2=高
 */
data class TaskInput(
    val categoryId: String?,
    val title: String,
    val note: String?,
    val dueDateMillis: Long?,
    val tags: List<String>,
    val priority: Int = 1,
    val hasDueTime: Boolean = false,
)

// ── 读取 DTO（供 AgentTool 使用，不依赖 Compose） ──────────

data class CategoryLookup(
    val id: String,
    val title: String,
)

data class TodoCategoryDto(
    val id: String,
    val title: String,
    val iconKey: String,
    val tasks: List<TodoTaskDto>,
    val description: String? = null,
    val colorArgb: Int? = null,
    val isPinned: Boolean = false,
)

data class TodoTaskDto(
    val id: String,
    val title: String,
    val note: String?,
    val startDate: Long,
    val dueDate: Long?,
    val tags: List<String>,
    val isCompleted: Boolean,
    val isStarred: Boolean,
    val priority: Int = 1,
    val completedAt: Long? = null,
    val categoryId: String? = null,
    val hasDueTime: Boolean = false,
    val createdAt: Long = startDate,
)
