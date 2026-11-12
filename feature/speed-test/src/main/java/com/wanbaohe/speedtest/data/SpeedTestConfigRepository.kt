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

import kotlinx.coroutines.flow.Flow

interface SpeedTestConfigRepository {
    fun getAll(): Flow<List<SpeedTestConfig>>
    suspend fun getActive(): SpeedTestConfig?
    suspend fun insert(config: SpeedTestConfig): Long
    suspend fun update(config: SpeedTestConfig)
    suspend fun deleteById(id: Long)
    suspend fun setActive(id: Long)
    /** 数据库为空时插入默认预设配置 */
    suspend fun initDefaultsIfEmpty()
}

