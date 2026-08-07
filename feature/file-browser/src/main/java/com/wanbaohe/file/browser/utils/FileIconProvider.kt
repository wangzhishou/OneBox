package com.wanbaohe.file.browser.utils

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDescription
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePdf
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCodeEditor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMovie
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTableChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAudioFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTextSnippet
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZip

/**
 * Provides appropriate icons for files based on their extension or MIME type
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
    ): ImageVector {
        if (isDirectory) {
            return com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder
        }

        // First try to match by extension
        val iconByExtension = getIconByExtension(extension.lowercase())
        if (iconByExtension != null) {
            return iconByExtension
        }

        // Fall back to MIME type
        return getIconByMimeType(mimeType)
    }

    /**
     * Gets icon based on file extension
     */
    private fun getIconByExtension(extension: String): ImageVector? {
        return when (extension) {
            // Images
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "heic", "heif" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage

            // Videos
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v", "3gp", "mpeg", "mpg" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMovie

            // Audio
            "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus", "ape" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAudioFile

            // Documents
            "pdf" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf
            "doc", "docx", "odt", "rtf" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription
            "txt", "md", "log" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTextSnippet

            // Spreadsheets
            "xls", "xlsx", "ods", "csv" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTableChart

            // Archives
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "apk", "jar" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineZip

            // Code files
            "kt", "java", "cpp", "c", "h", "hpp", "py", "js", "ts", "html", "css", "xml",
            "json", "yaml", "yml", "gradle", "sh", "bat", "cmd", "php", "rb", "go", "rs",
            "swift", "m", "mm", "sql" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor

            else -> null
        }
    }

    /**
     * Gets icon based on MIME type as fallback
     */
    private fun getIconByMimeType(mimeType: String?): ImageVector {
        return when {
            mimeType == null -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFile
            mimeType.startsWith("image/") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage
            mimeType.startsWith("video/") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMovie
            mimeType.startsWith("audio/") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAudioFile
            mimeType.startsWith("text/") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTextSnippet
            mimeType == "application/pdf" -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf
            mimeType.contains("document") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription
            mimeType.contains("spreadsheet") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTableChart
            mimeType.contains("zip") || mimeType.contains("compressed") -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineZip
            else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFile
        }
    }
}
