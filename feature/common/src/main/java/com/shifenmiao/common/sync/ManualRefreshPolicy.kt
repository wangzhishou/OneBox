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
