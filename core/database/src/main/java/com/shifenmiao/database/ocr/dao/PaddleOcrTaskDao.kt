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

package com.shifenmiao.database.ocr.dao

import androidx.room.*
import com.shifenmiao.database.ocr.entity.PaddleOcrTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaddleOcrTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PaddleOcrTaskEntity): Long

    @Update
    suspend fun updateTask(task: PaddleOcrTaskEntity)

    @Query("SELECT * FROM ocr_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<PaddleOcrTaskEntity>>

    @Query("SELECT * FROM ocr_tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getTaskByTaskId(taskId: String): PaddleOcrTaskEntity?

    @Query("SELECT * FROM ocr_tasks WHERE status IN ('pending', 'processing')")
    suspend fun getActiveTasks(): List<PaddleOcrTaskEntity>

    @Query("UPDATE ocr_tasks SET updatedAt = :updatedAt WHERE taskId = :taskId")
    suspend fun touchUpdatedAt(taskId: String, updatedAt: Long)

    @Query("UPDATE ocr_tasks SET errorMsg = :errorMsg, updatedAt = :updatedAt WHERE taskId = :taskId")
    suspend fun setErrorMsg(taskId: String, errorMsg: String?, updatedAt: Long)

    @Delete
    suspend fun deleteTask(task: PaddleOcrTaskEntity)

    @Query("DELETE FROM ocr_tasks WHERE taskId = :taskId")
    suspend fun deleteTaskByTaskId(taskId: String)
}
