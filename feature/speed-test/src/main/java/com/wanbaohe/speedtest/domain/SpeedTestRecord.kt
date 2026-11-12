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

/** 测速历史领域模型（UI / 业务层使用，不依赖 Room） */
data class SpeedTestRecord(
    val id: Long = 0,
    /** 网络类型，如 "5G移动网络"、"WiFi" */
    val networkType: String,
    /** 下载速度 (Mbps) */
    val downloadMbps: Float,
    /** 上传速度 (Mbps)，-1 表示未测量 */
    val uploadMbps: Float = -1f,
    /** 网络延迟 (ms)，-1 表示测量失败 */
    val latencyMs: Int,
    val clientIp: String = "",
    val clientLocation: String = "",
    val carrier: String = "",
    val serverHost: String = "",
    val latencyNode: String = "",
    val downloadNode: String = "",
    val uploadNode: String = "",
    /** 记录时间戳 (ms) */
    val recordedAt: Long = System.currentTimeMillis()
) {
    /** 以 MB/s 表示的下载速度 */
    val downloadMbPerSec: Float get() = downloadMbps / 8f

    /** 延迟显示文本 */
    val latencyDisplay: String get() = if (latencyMs >= 0) "${latencyMs}ms" else "--"
}

