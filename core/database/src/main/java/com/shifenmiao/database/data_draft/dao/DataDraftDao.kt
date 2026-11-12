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

package com.shifenmiao.database.data_draft.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.data_draft.entity.DataDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DataDraftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draft: DataDraftEntity): Long

    @Update
    suspend fun update(draft: DataDraftEntity)

    @Query("SELECT * FROM data_draft WHERE id = :id")
    suspend fun getById(id: Long): DataDraftEntity?

    @Query("SELECT * FROM data_draft WHERE draft_type = :draftType ORDER BY update_time DESC")
    fun observeAllByType(draftType: Int): Flow<List<DataDraftEntity>>

    @Query(
        "SELECT * FROM data_draft WHERE draft_type = :draftType AND related_entity_id = :relatedEntityId ORDER BY update_time DESC LIMIT 1"
    )
    suspend fun getLatestByTypeAndRelatedEntityId(
        draftType: Int,
        relatedEntityId: Int,
    ): DataDraftEntity?

    @Query("SELECT * FROM data_draft ORDER BY update_time DESC")
    fun observeAll(): Flow<List<DataDraftEntity>>

    @Query("DELETE FROM data_draft WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM data_draft WHERE draft_type = :draftType")
    suspend fun deleteAllByType(draftType: Int)

    @Query("DELETE FROM data_draft")
    suspend fun deleteAll()
}

