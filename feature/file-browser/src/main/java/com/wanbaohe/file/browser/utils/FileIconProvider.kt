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

package com.wanbaohe.file.browser.utils

import androidx.compose.ui.graphics.vector.ImageVector
import com.wanbaohe.core.ui.utils.file.FileTypeIconProvider

/**
 * Provides appropriate icons for files based on their extension or MIME type.
 *
 * 文件类型 → 图标的映射已收口到 core-ui 的 [FileTypeIconProvider]，
 * 压缩包、文件浏览等页面共用同一套图标；这里保留原有入口以兼容调用方。
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
    ): ImageVector = FileTypeIconProvider.iconFor(
        FileTypeIconProvider.categoryOf(
            isDirectory = isDirectory,
            extension = extension,
            mimeType = mimeType
        )
    )
}
