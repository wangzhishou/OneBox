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

package com.wanbaohe.file.browser.model

import android.net.Uri
import java.util.Date

/**
 * Represents a file or directory in the file system
 *
 * @property uri The URI of the file/directory
 * @property name The display name
 * @property isDirectory Whether this is a directory
 * @property size The size in bytes (0 for directories)
 * @property lastModified The last modification date
 * @property createdAt The creation date if available (may be null)
 * @property mimeType The MIME type of the file
 * @property path The file path
 * @property isAccessible 元数据读得到、内容打得开；分区存储下非媒体文件可能只列得出名字
 */
data class FileItem(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Date,
    val createdAt: Date? = null,
    val mimeType: String?,
    val path: String,
    val isAccessible: Boolean = true,
    val isHighlighted: Boolean = false,
    val isSelected: Boolean = false
) {
    /**
     * Returns the file extension if available
     */
    val extension: String
        get() = if (!isDirectory && name.contains('.')) {
            name.substringAfterLast('.', "")
        } else ""

    /**
     * Returns a formatted size string
     */
    fun getFormattedSize(): String {
        if (isDirectory) return ""
        if (size < 0) return "—"

        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format("%.2f KB", size / 1024.0)
            size < 1024 * 1024 * 1024 -> String.format("%.2f MB", size / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
