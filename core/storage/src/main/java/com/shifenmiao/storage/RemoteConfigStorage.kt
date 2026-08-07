package com.shifenmiao.storage

import com.shifenmiao.model.remote.RemoteConfig
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RemoteConfigStorage {

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.REMOTE_CONFIG)
    private var remoteConfigCache: RemoteConfig? = null

    // 升级 key：RemoteConfig 新增 adminVipLevel 等中间字段后，旧的 Parcelable 缓存格式已不兼容，
    // 继续使用旧缓存会导致字段错位/列表中出现 null。改为新 key 后首次读取会回退到默认 RemoteConfig。
    private const val KEY_REMOTE_CONFIG = "one_remote_config"

    /**
     * 远程配置写入事件。replay=1 让新订阅者立刻收到"最近一次"事件以兜底
     * (订阅早于 saveRemoteConfigToLocalStorage 发生的场景)。
     */
    private val _rulesChanged: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 1)

    /** 远程配置变更通知。订阅方收到后应重新读取 [getRemoteConfig] 并刷新派生状态。 */
    val rulesChanged: SharedFlow<Unit> = _rulesChanged.asSharedFlow()

    fun saveRemoteConfigToLocalStorage(remoteConfig: RemoteConfig) {
        remoteConfigCache = remoteConfig
        mmkv.encode(KEY_REMOTE_CONFIG, remoteConfig)
        _rulesChanged.tryEmit(Unit)
    }

    private fun getRemoteConfigFromLocalStorage(): RemoteConfig {
        var remoteConfig = mmkv.decodeParcelable(KEY_REMOTE_CONFIG, RemoteConfig::class.java)
        if (remoteConfig == null) {
            remoteConfig = RemoteConfig()
        }
        remoteConfigCache = remoteConfig
        return remoteConfig
    }

    fun getRemoteConfig(): RemoteConfig {
        if (remoteConfigCache == null) {
            remoteConfigCache = getRemoteConfigFromLocalStorage()
        }

        return remoteConfigCache ?: RemoteConfig()
    }

    fun clearRemoteConfig() {
        remoteConfigCache = null
        mmkv.remove(KEY_REMOTE_CONFIG)
        _rulesChanged.tryEmit(Unit)
    }
}