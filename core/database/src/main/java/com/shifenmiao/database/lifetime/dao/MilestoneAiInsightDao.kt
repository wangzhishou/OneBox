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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.lifetime.entity.MilestoneAiInsightEntity
import kotlinx.coroutines.flow.Flow

/**
 * 里程碑 AI 文案历史 DAO
 */
@Dao
interface MilestoneAiInsightDao {

    @Query("SELECT * FROM milestone_ai_insights WHERE milestoneId = :milestoneId ORDER BY generatedAt DESC")
    fun observeByMilestone(milestoneId: Long): Flow<List<MilestoneAiInsightEntity>>

    @Query("SELECT * FROM milestone_ai_insights WHERE id = :id")
    suspend fun getInsightById(id: Long): MilestoneAiInsightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: MilestoneAiInsightEntity): Long

    @Query("DELETE FROM milestone_ai_insights WHERE id = :id")
    suspend fun deleteInsightById(id: Long)

    @Query("DELETE FROM milestone_ai_insights WHERE milestoneId = :milestoneId")
    suspend fun deleteByMilestone(milestoneId: Long)

    @Query("SELECT COUNT(*) FROM milestone_ai_insights WHERE milestoneId = :milestoneId")
    suspend fun countByMilestone(milestoneId: Long): Int
}
