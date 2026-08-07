package com.shifenmiao.common.sync

import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.storage.RemoteConfigStorage

/**
 * 控制全量同步的触发频率。
 *
 * - 默认间隔 3 天；
 * - 服务端可通过 [RemoteConfig.cacheTimeout]（单位：秒）动态调整，无需改客户端代码。
 */
object SyncIntervalPolicy {

    /**
     * 默认同步间隔：3 天。
     */
    private const val DEFAULT_SYNC_INTERVAL_MS = 3 * 24 * 60 * 60 * 1000L

    /**
     * 当前应使用的同步间隔（毫秒）。
     * 优先读取 [RemoteConfig.appLaunchSyncIntervalSeconds]，未配置或无效时回退到默认值。
     */
    fun getIntervalMs(): Long {
        val seconds = RemoteConfigStorage.getRemoteConfig().appLaunchSyncIntervalSeconds
        return if (seconds != null && seconds > 0) {
            seconds * 1000L
        } else {
            DEFAULT_SYNC_INTERVAL_MS
        }
    }

    /**
     * 是否到了该全量同步的时间。
     *
     * - 从未同步过（lastSyncAt <= 0）：需要同步；
     * - 距上次同步超过 [getIntervalMs]：需要同步；
     * - 否则跳过。
     */
    fun shouldSync(): Boolean {
        val lastSync = AppSharedStorage.loadFullSyncLastAt()
        if (lastSync <= 0) return true
        return System.currentTimeMillis() - lastSync >= getIntervalMs()
    }
}
