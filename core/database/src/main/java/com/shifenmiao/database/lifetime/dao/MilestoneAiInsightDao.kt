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
