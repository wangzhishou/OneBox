package com.t8rin.imagetoolbox.core.ui.utils.file

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAudioFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCodeEditor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDescription
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMovie
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePdf
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTableChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTextSnippet
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZip
import com.t8rin.imagetoolbox.core.ui.theme.mixedContainer
import com.t8rin.imagetoolbox.core.ui.theme.onMixedContainer

/**
 * 文件类型视觉分类 —— 全 App 唯一的「文件长相」判定：
 * 一个文件对应哪个图标、用哪个容器色，文件浏览、压缩包、分享列表都从这里取，
 * 避免各页面各写一套扩展名映射。
 */
enum class FileVisualCategory {
    Folder,
    Image,
    Video,
    Audio,
    Pdf,
    Document,
    Spreadsheet,
    Archive,
    Code,
    Text,
    Unknown
}

@Immutable
data class FileVisualStyle(
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)

object FileTypeIconProvider {

    private val archiveExtensions = setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "apk", "jar")
    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "heic", "heif", "avif")
    private val videoExtensions = setOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v", "3gp", "mpeg", "mpg")
    private val audioExtensions = setOf("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus", "ape")
    private val documentExtensions = setOf("doc", "docx", "odt", "rtf", "ppt", "pptx", "odp", "epub")
    private val spreadsheetExtensions = setOf("xls", "xlsx", "ods", "csv")
    private val textExtensions = setOf("txt", "md", "log")
    private val codeExtensions = setOf(
        "kt", "java", "cpp", "c", "h", "hpp", "py", "js", "ts", "html", "css", "xml",
        "json", "yaml", "yml", "gradle", "sh", "bat", "cmd", "php", "rb", "go", "rs",
        "swift", "m", "mm", "sql"
    )

    fun categoryOf(
        isDirectory: Boolean,
        extension: String,
        mimeType: String?
    ): FileVisualCategory {
        if (isDirectory) return FileVisualCategory.Folder

        val ext = extension.lowercase()
        when {
            ext in imageExtensions -> return FileVisualCategory.Image
            ext in videoExtensions -> return FileVisualCategory.Video
            ext in audioExtensions -> return FileVisualCategory.Audio
            ext == "pdf" -> return FileVisualCategory.Pdf
            ext in documentExtensions -> return FileVisualCategory.Document
            ext in spreadsheetExtensions -> return FileVisualCategory.Spreadsheet
            ext in archiveExtensions -> return FileVisualCategory.Archive
            ext in codeExtensions -> return FileVisualCategory.Code
            ext in textExtensions -> return FileVisualCategory.Text
        }

        return when {
            mimeType == null -> FileVisualCategory.Unknown
            mimeType.startsWith("image/") -> FileVisualCategory.Image
            mimeType.startsWith("video/") -> FileVisualCategory.Video
            mimeType.startsWith("audio/") -> FileVisualCategory.Audio
            mimeType.startsWith("text/") -> FileVisualCategory.Text
            mimeType == "application/pdf" -> FileVisualCategory.Pdf
            mimeType.contains("document") -> FileVisualCategory.Document
            mimeType.contains("spreadsheet") -> FileVisualCategory.Spreadsheet
            mimeType.contains("zip") || mimeType.contains("compressed") -> FileVisualCategory.Archive
            else -> FileVisualCategory.Unknown
        }
    }

    fun iconFor(category: FileVisualCategory): ImageVector = when (category) {
        FileVisualCategory.Folder -> Icons.Outlined.LineFolder
        FileVisualCategory.Image -> Icons.Outlined.LineImage
        FileVisualCategory.Video -> Icons.Outlined.LineMovie
        FileVisualCategory.Audio -> Icons.Outlined.LineAudioFile
        FileVisualCategory.Pdf -> Icons.Outlined.LinePdf
        FileVisualCategory.Document -> Icons.Outlined.LineDescription
        FileVisualCategory.Spreadsheet -> Icons.Outlined.LineTableChart
        FileVisualCategory.Archive -> Icons.Outlined.LineZip
        FileVisualCategory.Code -> Icons.Outlined.LineCodeEditor
        FileVisualCategory.Text -> Icons.Outlined.LineTextSnippet
        FileVisualCategory.Unknown -> Icons.Outlined.LineFile
    }

    /**
     * 文件图标的容器色/前景色。只用主题里的 Container 家族，
     * 保证换主题、深色模式下都跟随 App 配色，而不是写死文件类型红黄蓝。
     */
    @Composable
    fun styleFor(category: FileVisualCategory): FileVisualStyle {
        val colorScheme = MaterialTheme.colorScheme
        val (container, content) = when (category) {
            FileVisualCategory.Folder -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
            FileVisualCategory.Image -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
            FileVisualCategory.Video -> colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer
            FileVisualCategory.Audio -> colorScheme.secondaryContainer to colorScheme.onSecondaryContainer
            FileVisualCategory.Pdf -> colorScheme.errorContainer to colorScheme.onErrorContainer
            FileVisualCategory.Document -> colorScheme.secondaryContainer to colorScheme.onSecondaryContainer
            FileVisualCategory.Spreadsheet -> colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer
            FileVisualCategory.Archive -> colorScheme.mixedContainer to colorScheme.onMixedContainer
            FileVisualCategory.Code,
            FileVisualCategory.Text,
            FileVisualCategory.Unknown -> colorScheme.surfaceContainer to colorScheme.onSurfaceVariant
        }
        return FileVisualStyle(
            icon = iconFor(category),
            containerColor = container,
            contentColor = content
        )
    }
}
