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

package com.wanbaohe.speedtest.data

import com.shifenmiao.database.speedtest.dao.SpeedTestConfigDao
import com.shifenmiao.database.speedtest.entity.SpeedTestConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeedTestConfigRepositoryImpl @Inject constructor(
    private val dao: SpeedTestConfigDao
) : SpeedTestConfigRepository {

    override fun getAll(): Flow<List<SpeedTestConfig>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getActive(): SpeedTestConfig? =
        dao.getActive()?.toDomain()

    /** 插入时永远以 inactive 存入，需要激活时再调 setActive */
    override suspend fun insert(config: SpeedTestConfig): Long =
        dao.insert(config.copy(isActive = false).toEntity())

    /** 更新时保留 isActive 原值（isActive 只由 setActive 控制） */
    override suspend fun update(config: SpeedTestConfig) {
        val currentlyActive = dao.getActive()?.id == config.id
        dao.update(config.copy(isActive = currentlyActive).toEntity())
    }

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun setActive(id: Long) = dao.setActive(id)

    override suspend fun initDefaultsIfEmpty() {
        if (dao.getCount() == 0) {
            SpeedTestConfig.DEFAULT_CONFIGS.forEachIndexed { index, config ->
                dao.insert(config.copy(isActive = index == 0).toEntity())
            }
        }
    }

    // ── 映射函数 ──────────────────────────────────────────────────────────

    private fun SpeedTestConfigEntity.toDomain() = SpeedTestConfig(
        id = id,
        name = name,
        testUrl = testUrl,
        estimatedDataMb = estimatedDataMb,
        durationSeconds = durationSeconds,
        isPreset = isPreset,
        isActive = isActive          // ← 关键：映射 isActive
    )

    private fun SpeedTestConfig.toEntity() = SpeedTestConfigEntity(
        id = id,
        name = name,
        testUrl = testUrl,
        estimatedDataMb = estimatedDataMb,
        durationSeconds = durationSeconds,
        isActive = isActive,
        isPreset = isPreset
    )
}
