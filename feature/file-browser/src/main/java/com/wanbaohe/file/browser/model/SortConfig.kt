package com.wanbaohe.file.browser.model

/**
 * Sorting options for file list
 */
enum class SortType {
    NAME,
    DATE,
    SIZE,
    TYPE
}

/**
 * Sorting order
 */
enum class SortOrder {
    ASCENDING,
    DESCENDING
}

/**
 * Configuration for file sorting
 */
data class SortConfig(
    val type: SortType = SortType.NAME,
    val order: SortOrder = SortOrder.ASCENDING
) {
    /**
     * Toggles the sort order
     */
    fun toggleOrder(): SortConfig = copy(order = when (order) {
        SortOrder.ASCENDING -> SortOrder.DESCENDING
        SortOrder.DESCENDING -> SortOrder.ASCENDING
    })
}

