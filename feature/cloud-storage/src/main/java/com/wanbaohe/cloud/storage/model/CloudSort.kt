package com.wanbaohe.cloud.storage.model

/** 云盘列表的排序方式（目录永远排在文件前面）。 */
enum class CloudSortMode {
    NAME,
    DATE,
    SIZE,
}

data class CloudSort(
    val mode: CloudSortMode = CloudSortMode.NAME,
    val ascending: Boolean = true,
)
