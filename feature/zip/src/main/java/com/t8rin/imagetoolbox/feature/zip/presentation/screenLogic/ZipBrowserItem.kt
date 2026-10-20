package com.t8rin.imagetoolbox.feature.zip.presentation.screenLogic

import com.t8rin.imagetoolbox.feature.zip.domain.ZipArchiveEntry

/**
 * 压缩包浏览页的一行：目录或文件。
 */
sealed interface ZipBrowserItem {

    val path: String
    val name: String

    data class Folder(
        override val path: String,
        override val name: String,
        val fileCount: Int,
        val totalSize: Long
    ) : ZipBrowserItem

    data class File(
        val entry: ZipArchiveEntry
    ) : ZipBrowserItem {
        override val path: String = entry.path
        override val name: String = entry.name
    }
}
