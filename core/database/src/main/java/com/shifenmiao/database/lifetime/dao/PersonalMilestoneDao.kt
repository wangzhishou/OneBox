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

package com.shifenmiao.database.lifetime.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.lifetime.entity.PersonalMilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalMilestoneDao {

    @Query("SELECT * FROM personal_milestones ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllMilestones(): Flow<List<PersonalMilestoneEntity>>

    @Query("SELECT * FROM personal_milestones WHERE id = :id")
    suspend fun getMilestoneById(id: Long): PersonalMilestoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: PersonalMilestoneEntity): Long

    @Update
    suspend fun updateMilestone(milestone: PersonalMilestoneEntity)

    @Delete
    suspend fun deleteMilestone(milestone: PersonalMilestoneEntity)

    @Query("DELETE FROM personal_milestones WHERE id = :id")
    suspend fun deleteMilestoneById(id: Long)

    @Query("SELECT COUNT(*) FROM personal_milestones")
    suspend fun getMilestoneCount(): Int
}
