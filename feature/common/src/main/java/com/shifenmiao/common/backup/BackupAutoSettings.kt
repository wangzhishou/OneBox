package com.shifenmiao.common.backup

import com.tencent.mmkv.MMKV
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 自动备份的偏好与运行记录。
 *
 * 没有引入 WorkManager：这个 App 的数据只在用户使用过程中变化，
 * 「每次打开 App 检查一次是否到期」已经够用，也不会有后台唤醒和电量问题。
 */
@Singleton
class BackupAutoSettings @Inject constructor() {

    private val mmkv: MMKV by lazy { MMKV.mmkvWithID(NAME) }

    var isEnabled: Boolean
        get() = mmkv.decodeBool(KEY_ENABLED, false)
        set(value) { mmkv.encode(KEY_ENABLED, value) }

    /** 间隔天数；0 表示每天，只接受 [INTERVAL_OPTIONS] 里的值。 */
    var intervalDays: Int
        get() = mmkv.decodeInt(KEY_INTERVAL, DEFAULT_INTERVAL).takeIf { it in INTERVAL_OPTIONS }
            ?: DEFAULT_INTERVAL
        set(value) { mmkv.encode(KEY_INTERVAL, value) }

    var uploadsToCloud: Boolean
        get() = mmkv.decodeBool(KEY_UPLOAD_CLOUD, false)
        set(value) { mmkv.encode(KEY_UPLOAD_CLOUD, value) }

    var lastRunAt: Long
        get() = mmkv.decodeLong(KEY_LAST_RUN_AT, 0L)
        set(value) { mmkv.encode(KEY_LAST_RUN_AT, value) }

    var lastRunSucceeded: Boolean
        get() = mmkv.decodeBool(KEY_LAST_RUN_OK, true)
        set(value) { mmkv.encode(KEY_LAST_RUN_OK, value) }

    val isDue: Boolean
        get() {
            if (!isEnabled) return false
            val elapsed = System.currentTimeMillis() - lastRunAt
            return elapsed >= intervalDays * DAY_MILLIS
        }

    companion object {
        private const val NAME = "backup_auto"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_INTERVAL = "interval_days"
        private const val KEY_UPLOAD_CLOUD = "upload_to_cloud"
        private const val KEY_LAST_RUN_AT = "last_run_at"
        private const val KEY_LAST_RUN_OK = "last_run_ok"
        private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

        const val DEFAULT_INTERVAL = 7

        val INTERVAL_OPTIONS = listOf(1, 3, 7)
    }
}
