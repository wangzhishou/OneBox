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

package com.wanbaohe.speedtest.domain

/** 测速进度密封类，通过 Flow 向 Component 推送 */
sealed class SpeedTestPhase {
    /** 测量延迟中 */
    data object MeasuringLatency : SpeedTestPhase()

    /** 下载测速中，liveMbps = 实时速度，progress = 0~1 */
    data class Downloading(
        val liveMbps: Float,
        val progress: Float,
        val latencyMs: Int = -1
    ) : SpeedTestPhase()

    /** 上传测速中，liveMbps = 实时速度，progress = 整体进度 0~1 */
    data class Uploading(
        val liveMbps: Float,
        val progress: Float,
        val latencyMs: Int = -1
    ) : SpeedTestPhase()

    /** 测速完成 */
    data class Done(val record: SpeedTestRecord) : SpeedTestPhase()

    /** 测速出错 */
    data class Error(val message: String) : SpeedTestPhase()
}

