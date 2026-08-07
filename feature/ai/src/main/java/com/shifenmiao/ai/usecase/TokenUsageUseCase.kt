package com.shifenmiao.ai.usecase

import com.shifenmiao.database.ai.dao.MessageDao
import com.shifenmiao.database.ai.dao.ModelUsageStat
import com.shifenmiao.database.ai.dao.TopQueryStat
import com.shifenmiao.database.ai.dao.TokenUsageSummary
import javax.inject.Inject

/**
 * Token 使用统计用例
 *
 * 封装 DAO 聚合查询，提供 UI 层所需的 domain model。
 * 统计口径：排除 expired 和 zero-token 记录，按 completionId 去重（取 assistant 侧）。
 */
class TokenUsageUseCase @Inject constructor(
    private val messageDao: MessageDao
) {

    suspend fun getSummary(): TokenUsageSummary {
        return messageDao.getTokenUsageSummary()
            ?: TokenUsageSummary()
    }

    suspend fun getModelDistribution(): List<ModelUsageStat> {
        return messageDao.getTokenUsageByModel()
    }

    suspend fun getTopQueries(limit: Int = 10): List<TopQueryStat> {
        return messageDao.getTopTokenQueries(limit)
    }
}
