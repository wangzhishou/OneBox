/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.core.domain.content

/**
 * 内容类型枚举 — 统一描述一个 URI 指向的内容类别。
 *
 * 作为 [ContentTypeResolver] 的输出和 [ContentRouter] 的输入，
 * 把"文件类型判断"与"打开方式决策"解耦。
 */
sealed class ContentType {

    data class Image(val mimeType: String?) : ContentType()
    data class Pdf(val mimeType: String?) : ContentType()
    data class Text(val mimeType: String?) : ContentType()
    data class Markdown(val mimeType: String?) : ContentType()
    data class Audio(val mimeType: String?) : ContentType()
    data class Video(val mimeType: String?) : ContentType()
    data class Archive(
        val mimeType: String?,
        /** 文件扩展名（小写、不带点），用于区分 zip 与 rar/7z 等不支持内部浏览的格式 */
        val extension: String? = null
    ) : ContentType()
    data class Document(val mimeType: String?) : ContentType()
    data class Html(val mimeType: String?) : ContentType()
    object Directory : ContentType()
    data class Unknown(val mimeType: String?) : ContentType()

    companion object {

        /** 图片文件扩展名白名单（MIME 类型不可靠时的 fallback） */
        val IMAGE_EXTENSIONS = setOf(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif",
            "tiff", "tif", "svg", "raw", "arw", "cr2", "nrw", "k25",
            "dib", "ico", "jfif", "jpe", "pjp", "avif", "jxl", "qoi"
        )

        /** PDF 扩展名 */
        val PDF_EXTENSIONS = setOf("pdf")

        /** Markdown 扩展名 */
        val MARKDOWN_EXTENSIONS = setOf("md", "markdown", "mkd", "mdown")

        /** 纯文本文件扩展名 */
        val TEXT_EXTENSIONS = setOf(
            "txt", "log", "json", "xml", "yaml", "yml",
            "css", "js", "ts", "kt", "java",
            "py", "rb", "go", "rs", "c", "cpp", "h", "hpp",
            "sh", "bash", "zsh", "fish", "conf", "ini", "cfg",
            "properties", "gradle", "toml", "csv", "config",
            "gitignore", "editorconfig", "sql", "dart", "tex",
            "clj", "bf", "cs", "swift", "php", "ps1", "bat"
        )

        /** 音频文件扩展名 */
        val AUDIO_EXTENSIONS = setOf(
            "mp3", "wav", "ogg", "flac", "aac", "m4a", "wma",
            "opus", "aiff", "au", "ra", "ram"
        )

        /** 视频文件扩展名 */
        val VIDEO_EXTENSIONS = setOf(
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm",
            "m4v", "3gp", "mpeg", "mpg", "ts", "m3u8"
        )

        /** 压缩包扩展名 */
        val ARCHIVE_EXTENSIONS = setOf(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz",
            "lz", "lzma", "z", "tgz", "tbz", "tbz2", "txz"
        )

        /** 文档扩展名（Office / OpenDocument 等） */
        val DOCUMENT_EXTENSIONS = setOf(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "odt", "ods", "odp", "rtf", "epub"
        )

        /** HTML 文件扩展名 */
        val HTML_EXTENSIONS = setOf("html", "htm", "xhtml")
    }
}
