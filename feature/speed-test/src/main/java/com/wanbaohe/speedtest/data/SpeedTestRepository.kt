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

import com.wanbaohe.speedtest.domain.SpeedTestPhase
import com.wanbaohe.speedtest.domain.SpeedTestRecord
import kotlinx.coroutines.flow.Flow

interface SpeedTestRepository {
    /** 开始测速，返回进度 Flow（MeasuringLatency → Downloading → Uploading（可选）→ Done | Error） */
    fun startTest(config: SpeedTestConfig, networkType: String): Flow<SpeedTestPhase>

    /** 持久化一条测速结果 */
    suspend fun saveRecord(record: SpeedTestRecord)

    /** 获取所有历史记录（Flow 实时更新） */
    fun getHistory(): Flow<List<SpeedTestRecord>>

    /** 清除所有历史 */
    suspend fun clearHistory()
}

