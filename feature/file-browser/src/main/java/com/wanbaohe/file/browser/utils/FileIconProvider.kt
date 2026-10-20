package com.wanbaohe.file.browser.utils

import androidx.compose.ui.graphics.vector.ImageVector
import com.wanbaohe.core.ui.utils.file.FileTypeIconProvider

/**
 * Provides appropriate icons for files based on their extension or MIME type.
 *
 * 文件类型 → 图标的映射已收口到 core-ui 的 [FileTypeIconProvider]，
 * 压缩包、文件浏览等页面共用同一套图标；这里保留原有入口以兼容调用方。
 */
object FileIconProvider {

    /**
     * Gets an icon for a file based on its properties
     * @param isDirectory Whether the file is a directory
     * @param extension The file extension (without dot)
     * @param mimeType The MIME type of the file
     * @return The appropriate Material Icon
     */
    fun getIcon(
        isDirectory: Boolean,
        extension: String,
        mimeType: String?
    ): ImageVector = FileTypeIconProvider.iconFor(
        FileTypeIconProvider.categoryOf(
            isDirectory = isDirectory,
            extension = extension,
            mimeType = mimeType
        )
    )
}
