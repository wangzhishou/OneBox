package com.wanbaohe.fileviewer.domain

import android.net.Uri
import java.io.InputStream

/**
 * 文档字节的唯一来源。
 *
 * 页面要的只是一个能反复读取的字节流（`/doc/file.<ext>` 可能被请求多次，
 * 大文件还会带 `Range`），所以这里只暴露流、长度和 MIME，不把 ContentResolver
 * 泄漏给 UI 层。
 */
interface DocumentSource {

    /** 用户看到的文件名，取不到时由调用方兜底。 */
    fun displayName(uri: Uri): String?

    /** 文件字节数；provider 给不出长度时返回 -1。 */
    fun length(uri: Uri): Long

    /** 用于 `/doc/` 响应的 MIME。 */
    fun mimeType(uri: Uri, extension: String): String

    /** 从头读整个文件。 */
    fun open(uri: Uri): InputStream?

    /**
     * 读 [start]..[endInclusive] 这一段（206 响应）。
     *
     * 刻意每次新开一条流：WebView 会并发、乱序地要很多段，
     * 复用同一个流必然互相踩。
     */
    fun openRange(uri: Uri, start: Long, endInclusive: Long): InputStream?
}
