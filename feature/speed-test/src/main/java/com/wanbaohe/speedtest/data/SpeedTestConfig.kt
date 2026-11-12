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

import java.net.URI

/** 测速配置（运行时 + 持久化） */
data class SpeedTestConfig(
    val id: Long = 0,
    /** 配置名称，显示在主界面 */
    val name: String = "Cloudflare 50MB",
    /** 测速文件下载 URL */
    val testUrl: String = DEFAULT_TEST_URL,
    /** 预估消耗流量（MB），仅供界面展示 */
    val estimatedDataMb: Int = DEFAULT_ESTIMATED_MB,
    /** 最大测速时长（秒） */
    val durationSeconds: Int = DEFAULT_DURATION_SECONDS,
    /** 是否为内置预设（预设不允许删除） */
    val isPreset: Boolean = false,
    /** 是否为当前激活配置（DB 写入的 isActive 字段镜像） */
    val isActive: Boolean = false
) {
    val serverHost: String
        get() = runCatching { URI(testUrl).host }.getOrNull().orEmpty()

    val supportsUpload: Boolean
        get() = serverHost == CLOUDFLARE_HOST

    companion object {
        const val CLOUDFLARE_HOST = "speed.cloudflare.com"
        const val DEFAULT_TEST_URL = "https://speed.cloudflare.com/__down?bytes=52428800"
        const val DEFAULT_ESTIMATED_MB = 50
        const val DEFAULT_DURATION_SECONDS = 15

        /** 内置预设配置列表（首次启动时写入数据库） */
        val DEFAULT_CONFIGS: List<SpeedTestConfig> = listOf(
            SpeedTestConfig(
                name = "Cloudflare 50MB",
                testUrl = "https://speed.cloudflare.com/__down?bytes=52428800",
                estimatedDataMb = 50,
                durationSeconds = 15,
                isPreset = true
            ),
            SpeedTestConfig(
                name = "阿里云镜像 50MB",
                testUrl = "https://mirrors.aliyun.com/kernel/v5.x/patch-5.15.176.xz",
                estimatedDataMb = 50,
                durationSeconds = 15,
                isPreset = true
            ),
            SpeedTestConfig(
                name = "腾讯CDN 50MB",
                testUrl = "https://dldir1.qq.com/weixin/Windows/WeChatSetup.exe",
                estimatedDataMb = 50,
                durationSeconds = 15,
                isPreset = true
            )
        )
    }
}
