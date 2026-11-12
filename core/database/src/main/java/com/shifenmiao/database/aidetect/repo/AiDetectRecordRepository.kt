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

package com.shifenmiao.database.aidetect.repo

import com.shifenmiao.database.aidetect.dao.AiDetectRecordDao
import com.shifenmiao.database.aidetect.entity.AiDetectRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * AI 检测历史仓库
 *
 * 只保留最近 [MAX_KEEP_COUNT] 条,插入后自动裁剪。删除类操作返回被移除的记录,
 * 由调用方负责清理关联的缩略图文件。
 */
class AiDetectRecordRepository(
    private val dao: AiDetectRecordDao,
) {

    fun observeAll(): Flow<List<AiDetectRecordEntity>> = dao.getAll()

    suspend fun getById(id: Long): AiDetectRecordEntity? = dao.getById(id)

    /** 插入记录并裁剪到上限,返回新记录 id 与被裁掉的记录 */
    suspend fun insert(record: AiDetectRecordEntity): Pair<Long, List<AiDetectRecordEntity>> {
        val newId = dao.insert(record)
        return newId to prune()
    }

    /** 删除指定记录,返回被删记录(不存在则为 null) */
    suspend fun delete(id: Long): AiDetectRecordEntity? {
        val removed = dao.getById(id) ?: return null
        dao.deleteById(id)
        return removed
    }

    /** 清空所有记录,返回被删记录 */
    suspend fun clearAll(): List<AiDetectRecordEntity> {
        val removedIds = dao.getIdsBeyond(0)
        if (removedIds.isEmpty()) return emptyList()
        val removed = removedIds.mapNotNull { dao.getById(it) }
        dao.deleteByIds(removedIds)
        return removed
    }

    /** 只保留最近 [MAX_KEEP_COUNT] 条,返回被裁掉的记录 */
    private suspend fun prune(): List<AiDetectRecordEntity> {
        val ids = dao.getIdsBeyond(MAX_KEEP_COUNT)
        if (ids.isEmpty()) return emptyList()
        val removed = ids.mapNotNull { dao.getById(it) }
        dao.deleteByIds(ids)
        return removed
    }

    companion object {
        const val MAX_KEEP_COUNT = 100
    }
}
