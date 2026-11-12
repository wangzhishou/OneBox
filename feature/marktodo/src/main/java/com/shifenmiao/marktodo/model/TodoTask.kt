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

package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable
import com.shifenmiao.marktodo.domain.TodoTaskTimeRules
import java.time.LocalDate

@Immutable
data class TodoTask(
    val id: String,
    val title: String,
    val categoryId: String? = null,
    val note: String? = null,
    val startDate: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val hasDueTime: Boolean = false,
    val tags: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val isStarred: Boolean = false,
    val priority: Int = PRIORITY_MEDIUM,
    val completedAt: Long? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = startDate,
    val updatedAt: Long = createdAt,
) {
    val isOverdue: Boolean
        get() = TodoTaskTimeRules.isOverdue(
            dueDateMillis = dueDate,
            hasDueTime = hasDueTime,
            isCompleted = isCompleted,
            now = System.currentTimeMillis(),
            today = LocalDate.now(),
        )

    val isDueSoon: Boolean
        get() = TodoTaskTimeRules.isDueSoon(
            dueDateMillis = dueDate,
            hasDueTime = hasDueTime,
            isCompleted = isCompleted,
            now = System.currentTimeMillis(),
            today = LocalDate.now(),
        )

    @Deprecated("Use dueDate instead", ReplaceWith("dueDate"))
    val date: Long? get() = dueDate

    companion object {
        const val PRIORITY_LOW = 0
        const val PRIORITY_MEDIUM = 1
        const val PRIORITY_HIGH = 2
    }
}
