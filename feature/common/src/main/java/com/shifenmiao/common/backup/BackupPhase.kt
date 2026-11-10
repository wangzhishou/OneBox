package com.shifenmiao.common.backup

/**
 * 备份/恢复过程中的阶段标记。
 *
 * 只描述「正在做什么」，具体文案由 UI 层映射到 string resources，
 * 避免在这一层硬编码用户可见文字。
 */
enum class BackupPhase {
    CHECKPOINT,
    DATABASES,
    SETTINGS,
    HISTORIES,
    FILES,
    PACKING,

    VALIDATING,
    SNAPSHOT,
    REMOVING_OLD,
    EXTRACTING_DATABASES,
    EXTRACTING_SETTINGS,
    EXTRACTING_HISTORIES,
    EXTRACTING_FILES,
    ROLLING_BACK,
}
