package com.wanbaohe.fileviewer.domain

/**
 * 能交给 WebView 渲染的页面，以及这个页面支持哪些阅读能力。
 *
 * 每个页面都是 gander（MIT）里的一份离线 HTML，宿主只负责把文件喂给
 * `/doc/file.<ext>` 并把版本信息带上；页面自己决定要不要显示"请更新 WebView"。
 *
 * @property page `assets/viewer/` 下的文件名，大小写必须与磁盘逐字一致（APK 里大小写敏感）
 * @property webViewFloor 需要的最低 Chromium 主版本，0 表示没有要求
 * @property supportsFind 页面带 `find.js`，能通过 message port 做页内查找
 * @property supportsPages 页面会回报"第几页 / 共几页"，可以显示页码并跳页
 * @property supportsNight 页面支持独立夜间模式（不跟系统深浅色）
 */
enum class ViewerFormat(
    val page: String,
    val webViewFloor: Int,
    val supportsFind: Boolean = false,
    val supportsPages: Boolean = false,
    val supportsNight: Boolean = false,
) {
    /** Word 2007+ 与模板/宏包，docx-preview 渲染 */
    DOCX(
        page = "docx.html",
        webViewFloor = 80,
        supportsFind = true,
        supportsPages = true,
        supportsNight = true,
    ),

    /** Word 97-2003、OpenDocument Text、Rich Text，gander 自研 reader */
    PROSE("prose.html", 80, supportsFind = true),

    /** Excel / ODS / CSV，SheetJS 渲染 */
    XLSX("xlsx.html", 80, supportsFind = true),

    /** PowerPoint 2007+，PPTXjs 渲染 */
    PPTX("pptx.html", 80, supportsFind = true),

    /** EPUB（无 DRM），gander 自研 reader；进度用 `at <章> <偏移>` 回报 */
    EPUB("epub.html", 80, supportsFind = true),

    /** Markdown，marked + DOMPurify */
    MARKDOWN("md.html", 92, supportsFind = true),

    /** 纯文本 / 代码 / 日志，按 5MB 分页 */
    TEXT("text.html", 0, supportsFind = true),

    /** GIF / SVG / AVIF / ICO，交给 WebView 原生解码 */
    IMAGE("imgweb.html", 0),

    /** 打不开时的兜底页（含"按文本查看"入口） */
    UNSUPPORTED("unsupported.html", 0);

    companion object {

        private val DOCX_EXT = setOf("docx", "docm", "dotx")
        private val PROSE_EXT = setOf("doc", "dot", "odt", "ott", "fodt", "rtf")
        private val SHEET_EXT = setOf("xlsx", "xls", "xlsm", "xlsb", "xltx", "csv", "ods")
        private val SLIDE_EXT = setOf("pptx", "ppsx", "pptm", "potx")
        private val EPUB_EXT = setOf("epub")
        private val MARKDOWN_EXT = setOf("md", "markdown", "mkd", "mdown")
        private val IMAGE_EXT = setOf("gif", "svg", "avif", "ico")
        private val TEXT_EXT = setOf(
            "txt", "log", "json", "xml", "yaml", "yml", "ini", "cfg", "conf", "properties",
            "toml", "csv", "srt", "vtt", "m3u", "nfo", "md"
        )

        fun fromExtension(extension: String): ViewerFormat {
            val ext = extension.lowercase()
            return when {
                ext in DOCX_EXT -> DOCX
                ext in PROSE_EXT -> PROSE
                ext in SHEET_EXT -> XLSX
                ext in SLIDE_EXT -> PPTX
                ext in EPUB_EXT -> EPUB
                ext in MARKDOWN_EXT -> MARKDOWN
                ext in IMAGE_EXT -> IMAGE
                ext in TEXT_EXT -> TEXT
                else -> UNSUPPORTED
            }
        }

        /** 这些页面会把整个文件读进 JS 内存，超过上限就不该硬开。 */
        fun loadsWholeFile(format: ViewerFormat): Boolean = when (format) {
            DOCX, PROSE, XLSX, PPTX, EPUB, MARKDOWN -> true
            TEXT, IMAGE, UNSUPPORTED -> false
        }
    }
}
