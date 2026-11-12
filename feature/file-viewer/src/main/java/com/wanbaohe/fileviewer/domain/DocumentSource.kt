/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
