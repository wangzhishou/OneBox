package com.shifenmiao.model.common

data class ImportOrExportProgress(
    val visible: Boolean = false,
    val progress: Float = 0f, // 0f 到 1f，表示 0% 到 100%
    val message: String = "",
    val type: ProgressType = ProgressType.IMPORT,
)

enum class ProgressType {
    EXPORT,
    IMPORT,
}