package com.shifenmiao.common.backup

/**
 * 恢复的结果。
 *
 * 刻意用「问题枚举」而不是现成文案：文案在 UI 层映射到 string resources，
 * 引擎层不持有用户可见字符串。
 */
sealed interface RestoreOutcome {

    data object Success : RestoreOutcome

    data class Rejected(
        val issue: RestoreIssue,
        val requiredVersion: String? = null,
    ) : RestoreOutcome

    data class Failed(
        val message: String?,
        val rolledBack: Boolean,
    ) : RestoreOutcome
}

enum class RestoreIssue {
    /** 文件不是 zip / 缺清单 / 读不出来 */
    UNREADABLE,

    /** 备份来自更新版本的备份格式 */
    NEWER_SCHEMA,

    /** 备份来自更新版本的 App，旧版打开新库会崩或丢字段 */
    NEWER_APP,

    /** 容器里没有主数据库 */
    MISSING_APP_DATABASE,

    /** 历史记录里的本机副本已被清理，也没有可读的云端授权 */
    MISSING_LOCAL_COPY,
}
