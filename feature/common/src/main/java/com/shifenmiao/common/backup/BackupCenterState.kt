package com.shifenmiao.common.backup

import android.net.Uri

/**
 * 备份中心页面状态。只描述「发生了什么」，文案由 Screen 层映射到 string resources。
 */
data class BackupCenterUiState(
    val records: List<BackupRecord> = emptyList(),
    val preRestore: BackupRecord? = null,
    val operation: BackupOperation? = null,
    val pendingRestore: PendingRestore? = null,
    val notice: BackupNotice? = null,
    val cloud: CloudBackupUi = CloudBackupUi(),
)

data class CloudBackupUi(
    val available: Boolean = false,
    val label: String? = null,
)

data class BackupOperation(
    val mode: BackupOperationMode,
    val phase: BackupPhase? = null,
)

enum class BackupOperationMode {
    CREATING,
    EXPORTING,
    RESTORING,
}

/**
 * 待确认的恢复来源：要么选了一条历史记录，要么刚从一个外部文件体检通过。
 */
data class PendingRestore(
    val record: BackupRecord? = null,
    val inspection: BackupInspection? = null,
    val uri: Uri? = null,
) {

    val createdAt: Long get() = record?.createdAt ?: inspection?.createdAt ?: 0L
    val appVersionName: String get() = record?.appVersionName ?: inspection?.appVersionName.orEmpty()
    val sizeBytes: Long get() = record?.sizeBytes ?: inspection?.sizeBytes ?: 0L
    val isLegacy: Boolean get() = record?.isLegacy ?: inspection?.isLegacy ?: false
    val mmkvStoreCount: Int get() = record?.mmkvStoreCount ?: inspection?.manifest?.mmkvStores?.size ?: 0
    val hasUserFiles: Boolean
        get() = record?.hasUserFiles ?: (inspection?.manifest?.userDirs?.isNotEmpty() ?: false)
    val hasSettings: Boolean get() = record?.hasSettings ?: inspection?.manifest?.hasSettings ?: false
}

sealed interface BackupNotice {
    data class Created(val sizeBytes: Long) : BackupNotice
    data object CreateFailed : BackupNotice
    data class Exported(val destinationId: String) : BackupNotice
    data object ExportFailed : BackupNotice
    data object Deleted : BackupNotice
    data object RestoreSucceeded : BackupNotice
    data class RestoreRejected(
        val issue: RestoreIssue,
        val requiredVersion: String? = null,
    ) : BackupNotice
    data class RestoreFailed(val rolledBack: Boolean) : BackupNotice
}
