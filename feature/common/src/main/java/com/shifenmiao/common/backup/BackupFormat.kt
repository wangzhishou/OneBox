package com.shifenmiao.common.backup

import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.storage.MMKVName

/**
 * 备份容器 v2 的固定约定。
 *
 * 容器是一个普通 zip：
 * ```
 * manifest.json                       # 版本、语言、内容摘要
 * db/app.db, db/app.db-wal, db/app.db-shm
 * db/feature.db, ...
 * mmkv/<storeId>, mmkv/<storeId>.crc
 * settings/datastore.preferences
 * ```
 *
 * 数据库条目按 **role** 而不是文件名存档：恢复时再映射到当前语言的库名
 * （见 [AppDatabase.dbNameForLocale]）。旧版备份直接把 `one_box_zh-Hans-CN.db`
 * 这种带 locale 的文件名写进 zip，语言一变就恢复不了，这是 v2 要修的核心问题。
 */
object BackupFormat {

    const val SCHEMA = 2

    const val MANIFEST_ENTRY = "manifest.json"
    const val DB_DIR = "db"
    const val MMKV_DIR = "mmkv"
    const val FILES_DIR = "files"
    const val SETTINGS_ENTRY = "settings/datastore.preferences"

    const val ROLE_APP = "app"
    const val ROLE_FEATURE = "feature"

    const val FILE_EXTENSION = "zip"
    const val FILE_PREFIX = "onebox_backup"

    /** 旧版（v1）备份文件名后缀，仅用于识别与提示。 */
    const val LEGACY_FILE_SUFFIX = "_one_box_backup.zip"

    val ROLES = listOf(ROLE_APP, ROLE_FEATURE)

    /**
     * 进备份的 MMKV 库：只挑用户自己的历史记录。
     *
     * 刻意排除 `token` / `device`（登录态与设备标识）——备份文件是要分享到网盘的，
     * 带上登录 token 等于把账号一起送出去。
     */
    val MMKV_WHITELIST = listOf(
        MMKVName.COLOR_PICK_HISTORY,
        MMKVName.SEARCH_HISTORY,
        MMKVName.SCAN_HISTORY,
        MMKVName.MARKDOWN_HISTORY,
        MMKVName.RECENT_LIST,
        // feature/translator 的翻译历史；该模块自己持有 id 常量，未跨模块暴露
        "translator",
    )

    /**
     * 存在应用私有目录里的用户文档（不是缓存、不是可重新下载的资源）。
     * 这些目录不进备份的话，text-card / 图片工作室的项目换个手机就没了。
     */
    val USER_FILE_DIRS = listOf(
        "text-card",
        "image-studio",
        "signatures",
        "customFonts",
        "ocr_history",
    )

    fun dbFileNameOf(role: String): String = when (role) {
        ROLE_APP -> AppDatabase.dbNameForLocale()
        ROLE_FEATURE -> FeatureDatabase.dbNameForLocale()
        else -> error("Unknown database role: $role")
    }

    /**
     * 旧格式 zip 里的条目名本身就是库文件名，只能按前缀猜角色。
     * 猜出来之后同样映射到当前语言，旧备份因此也能跨语言恢复。
     */
    fun roleOfLegacyEntry(entryName: String): String? = when {
        entryName.startsWith("${AppDatabase.DB_NAME_PREFIX}_") -> ROLE_APP
        entryName.startsWith("${FeatureDatabase.DB_NAME_PREFIX}_") -> ROLE_FEATURE
        else -> null
    }
}
