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

package com.shifenmiao.database.recordcenter.repo

import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.database.recordcenter.entity.HealthRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRecordRepository @Inject constructor(
    private val database: FeatureDatabase,
) {

    suspend fun upsert(record: HealthRecordEntity) {
        database.healthRecordDao().upsert(record)
    }

    suspend fun deleteById(id: String) {
        database.healthRecordDao().deleteById(id)
    }

    fun observeByType(type: String): Flow<List<HealthRecordEntity>> {
        return database.healthRecordDao().observeByType(type)
    }

    fun observeRange(type: String, from: Long, to: Long): Flow<List<HealthRecordEntity>> {
        return database.healthRecordDao().observeRange(type, from, to)
    }

    fun observeLatestPerType(): Flow<List<HealthRecordEntity>> {
        return database.healthRecordDao().observeLatestPerType()
    }

    fun observeSinceAllTypes(from: Long): Flow<List<HealthRecordEntity>> {
        return database.healthRecordDao().observeSinceAllTypes(from)
    }

    suspend fun getById(id: String): HealthRecordEntity? {
        return database.healthRecordDao().getById(id)
    }

    suspend fun getRangeOnce(type: String, from: Long, to: Long): List<HealthRecordEntity> {
        return database.healthRecordDao().getRangeOnce(type, from, to)
    }

    suspend fun getLatestPerTypeOnce(): List<HealthRecordEntity> {
        return database.healthRecordDao().getLatestPerTypeOnce()
    }
}
