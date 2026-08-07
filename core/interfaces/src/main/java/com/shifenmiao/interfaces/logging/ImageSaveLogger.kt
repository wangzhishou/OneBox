package com.shifenmiao.interfaces.logging

/**
 * 图片保存日志记录接口 — 定义在 :core:interfaces 避免循环依赖。
 *
 * 实现类在 :core:database 的 [ActivityLogRecorder] 中。
 */
interface ImageSaveLogger {
    suspend fun recordImageSave(
        screenId: String,
        screenName: String,
        description: String,
        fileUri: String = "",
        fileName: String = "",
        savePath: String = ""
    )
}

