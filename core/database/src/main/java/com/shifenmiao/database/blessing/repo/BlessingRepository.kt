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

package com.shifenmiao.database.blessing.repo

import com.shifenmiao.database.blessing.dao.BlessingRecordDao
import com.shifenmiao.database.blessing.dao.BlessingTabConfigDao
import com.shifenmiao.database.blessing.dao.BlessingWishDao
import com.shifenmiao.database.blessing.entity.BlessingRecordEntity
import com.shifenmiao.database.blessing.entity.BlessingTabConfigEntity
import com.shifenmiao.database.blessing.entity.BlessingWishEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlessingRepository @Inject constructor(
    private val recordDao: BlessingRecordDao,
    private val wishDao: BlessingWishDao,
    private val tabConfigDao: BlessingTabConfigDao,
) {

    suspend fun incrementCount(date: String, type: String) {
        recordDao.increment(
            id = UUID.randomUUID().toString(),
            date = date,
            type = type,
            updatedAt = System.currentTimeMillis(),
        )
    }

    fun observeByDate(date: String): Flow<List<BlessingRecordEntity>> {
        return recordDao.observeByDate(date)
    }

    fun observeInDateRange(startDate: String, endDate: String): Flow<List<BlessingRecordEntity>> {
        return recordDao.observeInDateRange(startDate, endDate)
    }

    fun observeAllWishes(): Flow<List<BlessingWishEntity>> {
        return wishDao.observeAll()
    }

    fun observeWishesByDate(date: String): Flow<List<BlessingWishEntity>> {
        return wishDao.observeByDate(date)
    }

    fun observeWishesInDateRange(
        startDate: String,
        endDate: String,
    ): Flow<List<BlessingWishEntity>> {
        return wishDao.observeInDateRange(startDate = startDate, endDate = endDate)
    }

    suspend fun saveWish(date: String, type: String, content: String) {
        wishDao.save(
            BlessingWishEntity(
                date = date,
                type = type,
                content = content,
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    fun observeTabConfigs(): Flow<List<BlessingTabConfigEntity>> {
        return tabConfigDao.observeAll()
    }

    suspend fun saveTabConfig(date: String, type: String, title: String, subtitle: String) {
        tabConfigDao.save(
            BlessingTabConfigEntity(
                date = date,
                type = type,
                title = title,
                subtitle = subtitle,
                updatedAt = System.currentTimeMillis(),
            )
        )
    }
}
