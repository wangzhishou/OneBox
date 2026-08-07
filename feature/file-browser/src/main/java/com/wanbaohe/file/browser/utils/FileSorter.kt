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

