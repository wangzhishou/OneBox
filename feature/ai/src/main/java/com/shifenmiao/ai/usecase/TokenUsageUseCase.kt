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
