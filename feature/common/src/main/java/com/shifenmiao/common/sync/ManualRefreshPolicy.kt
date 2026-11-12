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

package com.shifenmiao.common.sync

import com.shifenmiao.storage.RemoteConfigStorage

/**
 * 用户手动刷新（下拉刷新）的会话内冷却策略。
 *
 * 防止用户连续快速下拉导致请求风暴。冷却时间读取 [RemoteConfig.manualRefreshCooldown]
 *（单位：秒），服务端可动态调整；未配置或无效时回退到 30 秒。
 *
 * 冷却时间仅在当前 App 生命周期内有效，进程被杀死后自动重置。
 */
object ManualRefreshPolicy {

    /**
     * 默认手动刷新冷却时间：30 秒。
     */
    private const val DEFAULT_COOLDOWN_MS = 30 * 1000L

    private var lastRefreshAt: Long = 0

    /**
     * 当前配置的冷却时间（毫秒）。
     */
    private fun getCooldownMs(): Long {
        val seconds = RemoteConfigStorage.getRemoteConfig().manualRefreshCooldown
        return if (seconds != null && seconds > 0) {
            seconds * 1000L
        } else {
            DEFAULT_COOLDOWN_MS
        }
    }

    /**
     * 当前是否可以执行手动刷新。
     */
    @Synchronized
    fun canRefresh(): Boolean {
        return System.currentTimeMillis() - lastRefreshAt >= getCooldownMs()
    }

    /**
     * 标记本次手动刷新时间。
     */
    @Synchronized
    fun markRefreshed() {
        lastRefreshAt = System.currentTimeMillis()
    }
}
