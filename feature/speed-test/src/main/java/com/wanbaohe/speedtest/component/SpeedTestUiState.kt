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

package com.wanbaohe.speedtest.component

import androidx.compose.runtime.Immutable
import com.wanbaohe.speedtest.data.SpeedTestConfig
import com.wanbaohe.speedtest.domain.SpeedTestRecord

/** 当前测速 UI 阶段 */
enum class SpeedTestStatus { IDLE, MEASURING, DONE }

@Immutable
data class SpeedTestUiState(
    val status: SpeedTestStatus = SpeedTestStatus.IDLE,
    /** 当前阶段的实时速度 (Mbps) */
    val liveMbps: Float = 0f,
    /** 实时下载速度 (Mbps)，-1 表示尚未测量 */
    val downloadMbps: Float = -1f,
    /** 实时上传速度 (Mbps)，-1 表示尚未测量 */
    val liveUploadMbps: Float = -1f,
    /** 整体测速进度 0~1，仅 MEASURING 时有意义 */
    val progress: Float = 0f,
    val latencyMs: Int = -1,
    val measuringLatency: Boolean = false,
    val isUploading: Boolean = false,
    /** 本轮测速使用的配置，不随设置变更而改变。 */
    val testConfig: SpeedTestConfig? = null,
    /** 最终测速结果，仅 DONE 时非空 */
    val result: SpeedTestRecord? = null,
    /** 历史记录列表 */
    val history: List<SpeedTestRecord> = emptyList(),
    /** 当前网络类型文本 */
    val networkType: String = "",
    /** 当前激活的测速配置 */
    val config: SpeedTestConfig = SpeedTestConfig(),
    /** 所有配置列表 */
    val configList: List<SpeedTestConfig> = emptyList(),
    /** 错误提示 */
    val errorMsg: String? = null
)
