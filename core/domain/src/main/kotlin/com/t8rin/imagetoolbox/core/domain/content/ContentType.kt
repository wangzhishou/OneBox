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
    data class Archive(val mimeType: String?) : ContentType()
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
