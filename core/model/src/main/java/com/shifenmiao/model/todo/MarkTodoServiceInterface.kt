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
 * 待办清单业务 Service 接口。
 *
 * 供 UI Component 层与 AI AgentTool 层共同依赖的契约。
 * 实现类位于 feature/marktodo/service/。
 *
 * 职责:
 *  - 封装所有写操作（创建/更新/删除/重排/完成/收藏）
 *  - 提供只读查询（供 Agent 使用）
 */
interface MarkTodoServiceInterface {

    // ── 读操作（供 AgentTool 使用） ──────────────────────

    suspend fun getDashboardDto(): List<TodoCategoryDto>

    suspend fun getTaskDto(taskId: String): TodoTaskDto?

    suspend fun getInboxTasksDto(): List<TodoTaskDto>

    suspend fun searchTasks(query: String, limit: Int): List<TodoTaskDto>

    suspend fun findCategoryByTitle(title: String): CategoryLookup?

    // ── 分类操作 ────────────────────────────────────

    suspend fun createCategory(
        input: CategoryInput,
        source: String,
    ): Result<String>

    suspend fun updateCategory(
        categoryId: String,
        input: CategoryInput,
        source: String,
    ): Result<Unit>

    suspend fun deleteCategory(
        categoryId: String,
        categoryTitle: String,
        source: String,
    ): Result<Unit>

    suspend fun reorderCategories(
        orderedIds: List<String>,
        source: String,
    ): Result<Unit>

    // ── 任务操作 ────────────────────────────────────

    suspend fun createTask(
        input: TaskInput,
        source: String,
    ): Result<String>

    suspend fun updateTask(
        taskId: String,
        input: TaskInput,
        source: String,
    ): Result<Unit>

    /** 复制任务内容，生成一条默认未完成的新任务；关联日程提醒不复制。 */
    suspend fun duplicateTask(
        taskId: String,
        source: String,
    ): Result<String>

    suspend fun deleteTask(
        taskId: String,
        taskTitle: String,
        source: String,
    ): Result<Unit>

    suspend fun undoDeleteTask(
        taskId: String,
        source: String,
    ): Result<Unit>

    suspend fun toggleTaskComplete(
        taskId: String,
        isCompleted: Boolean,
        taskTitle: String,
        source: String,
    ): Result<Unit>

    suspend fun toggleTaskStar(
        taskId: String,
        isStarred: Boolean,
        taskTitle: String,
        source: String,
    ): Result<Unit>

    suspend fun reorderTasks(
        categoryId: String?,
        orderedIds: List<String>,
        source: String,
    ): Result<Unit>

    /**
     * 按名称确保标签存在（同名忽略大小写复用），返回标签 id。
     * Agent 写入任务标签时走这里，避免任务上的标签不在标签池里、筛选面板选不到。
     */
    suspend fun createTag(
        name: String,
        colorArgb: Int?,
        source: String,
    ): Result<String>
}
