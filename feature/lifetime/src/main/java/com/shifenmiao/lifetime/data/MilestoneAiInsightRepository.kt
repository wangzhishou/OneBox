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
