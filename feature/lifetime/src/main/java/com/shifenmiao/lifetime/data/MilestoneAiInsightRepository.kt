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

package com.shifenmiao.lifetime.data

import com.shifenmiao.database.lifetime.dao.MilestoneAiInsightDao
import com.shifenmiao.database.lifetime.entity.MilestoneAiInsightEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 里程碑 AI 文案历史仓库
 */
@Singleton
class MilestoneAiInsightRepository @Inject constructor(
    private val dao: MilestoneAiInsightDao
) {

    fun observeByMilestone(milestoneId: Long): Flow<List<MilestoneAiInsightEntity>> =
        dao.observeByMilestone(milestoneId)

    suspend fun addInsight(milestoneId: Long, content: String): Long {
        return dao.insertInsight(
            MilestoneAiInsightEntity(
                milestoneId = milestoneId,
                content = content,
                generatedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun deleteInsight(id: Long) {
        dao.deleteInsightById(id)
    }

    suspend fun deleteByMilestone(milestoneId: Long) {
        dao.deleteByMilestone(milestoneId)
    }
}
