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

import com.wanbaohe.file.browser.model.FileItem
import com.wanbaohe.file.browser.model.SortConfig
import com.wanbaohe.file.browser.model.SortOrder
import com.wanbaohe.file.browser.model.SortType

/**
 * Utility object for sorting file lists
 */
object FileSorter {

    /**
     * Sorts a list of files according to the sort configuration
     */
    fun sort(files: List<FileItem>, config: SortConfig): List<FileItem> {
        val comparator = getComparator(config)

        // Always keep directories first
        val directories = files.filter { it.isDirectory }.sortedWith(comparator)
        val regularFiles = files.filter { !it.isDirectory }.sortedWith(comparator)

        return directories + regularFiles
    }

    /**
     * Gets the appropriate comparator based on sort configuration
     */
    private fun getComparator(config: SortConfig): Comparator<FileItem> {
        val baseComparator = when (config.type) {
            SortType.NAME -> compareBy<FileItem> { it.name.lowercase() }
            SortType.DATE -> compareBy { it.lastModified }
            SortType.SIZE -> compareBy { it.size }
            SortType.TYPE -> compareBy { it.extension.lowercase() }
        }

        return when (config.order) {
            SortOrder.ASCENDING -> baseComparator
            SortOrder.DESCENDING -> baseComparator.reversed()
        }
    }
}

