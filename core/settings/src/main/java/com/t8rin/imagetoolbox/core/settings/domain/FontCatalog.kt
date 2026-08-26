package com.t8rin.imagetoolbox.core.settings.domain

import com.t8rin.imagetoolbox.core.settings.domain.model.DownloadableFont
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType

/**
 * 可下载字体目录(全局):内置清单 + 下载状态查询 + 下载(产出 FontType.File)。
 * 实现侧负责持久化已下载清单与字体文件。
 */
interface FontCatalog {

    /** 内置可下载字体清单 */
    val fonts: List<DownloadableFont>

    /** 已下载则返回对应 FontType.File,否则 null(文件丢失时返回 null 并清理记录) */
    fun downloadedFont(font: DownloadableFont): FontType.File?

    /** 按本地文件路径反查清单项(命中下载清单时取本地化名称,如「霞鹜文楷体」) */
    fun fontForFile(path: String): DownloadableFont? =
        fonts.find { downloadedFont(it)?.path == path }

    /**
     * 下载字体并产出 FontType.File。
     * [onProgress] 回调 0..1 进度(无 Content-Length 时只回调 0 与 1)。
     */
    suspend fun download(
        font: DownloadableFont,
        onProgress: (Float) -> Unit,
    ): Result<FontType.File>
}
