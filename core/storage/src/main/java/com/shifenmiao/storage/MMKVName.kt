package com.shifenmiao.storage

import com.t8rin.imagetoolbox.core.utils.LocaleUtils
import com.tencent.mmkv.MMKV

/**
 * 版本变化的时候，这里可以改下key
 */
object MMKVName {
    const val APP_SHARED: String = "app"
    const val TOKEN: String = "token"
    const val DEVICE: String = "device"
    const val RECENT_LIST: String = "recent"
    const val SCAN_HISTORY: String = "scan_history"
    const val MARQUEE_SETTING: String = "marquee"
    const val AI_CHAT_SETTING: String = "ai_chat"
    const val AI_ENGINE_SETTING: String = "ai_engine"
    const val SEARCH_HISTORY: String = "search_history"
    const val REMOTE_CONFIG: String = "remote_config"
    const val ANNOUNCEMENT_LIST: String = "announcement"
    const val BLOG_LIST = "blog"
    const val MARKDOWN_HISTORY: String = "markdown_history"
    const val MERMAID_CACHE: String = "mermaid_cache"
}

/**
 * 按当前语言隔离的 MMKV 文件 id："<base>_<localeTag>"，与 Room 分库命名同规则
 * （见 AppDatabase.dbNameForLocale）。语言切换后进程会冷重启（见 LocaleSwitchWatcher），
 * 这里仍在每次访问时动态解析：MMKV 内部按 id 缓存实例，开销可忽略，
 * 且极端情况下（重启前）也能立即读写新语言的文件。
 *
 * 只用于"内容随语言下发/与分库数据绑定"的缓存（同步水位线、远程配置、引擎目录等），
 * 用户私有数据（token、搜索历史等）不要使用。
 */
fun localizedMmkvId(base: String): String = "${base}_${LocaleUtils.getCurrentLocaleTag()}"

fun localizedMmkv(base: String): MMKV = MMKV.mmkvWithID(localizedMmkvId(base))
