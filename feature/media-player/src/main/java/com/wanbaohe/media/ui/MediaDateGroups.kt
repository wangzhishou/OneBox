package com.wanbaohe.media.ui

import com.wanbaohe.media.component.MediaFilter
import com.wanbaohe.media.domain.MediaEntry
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

/** 按天分组后的一组媒体；[dayStart] 是当天 00:00 的时间戳，0 表示时间未知。 */
internal data class MediaDateGroup(
    val dayStart: Long,
    val title: String,
    val entries: List<MediaEntry>,
)

/**
 * 右侧日期锚点的一条：跳到 [itemIndex]（LazyColumn 的 item 下标），显示 [label]。
 */
internal data class MediaDateAnchor(
    val itemIndex: Int,
    val label: String,
)

/**
 * 把手里的条目按"天"分组，保持传入顺序。
 *
 * "最近"筛选用打开时间分组，其余用拍摄时间（拿不到则退回打开时间）。
 */
internal fun groupMediaByDay(
    entries: List<MediaEntry>,
    filter: MediaFilter,
    todayLabel: String,
    yesterdayLabel: String,
    unknownLabel: String,
    dateFormat: DateFormat,
): List<MediaDateGroup> {
    if (entries.isEmpty()) return emptyList()
    val today = startOfLocalDay(System.currentTimeMillis())
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = today
        add(Calendar.DAY_OF_YEAR, -1)
    }.timeInMillis
    val buckets = LinkedHashMap<Long, MutableList<MediaEntry>>()
    entries.forEach { entry ->
        val millis = if (filter == MediaFilter.RECENT) {
            entry.accessedAt.takeIf { it > 0L } ?: entry.sortTimeMillis
        } else {
            entry.sortTimeMillis
        }
        val day = if (millis > 0L) startOfLocalDay(millis) else 0L
        buckets.getOrPut(day) { mutableListOf() }.add(entry)
    }
    return buckets.map { (day, dayEntries) ->
        MediaDateGroup(
            dayStart = day,
            title = when (day) {
                0L -> unknownLabel
                today -> todayLabel
                yesterday -> yesterdayLabel
                else -> dateFormat.format(Date(day))
            },
            entries = dayEntries,
        )
    }
}

/**
 * 把按天分组的列表压缩成"月"锚点。
 *
 * 一天一个锚点对 1000 条视频来说太密；这里只保留每月第一天所在组的 item 下标，
 * 点月份就能滚到那个月的第一条。日期未知的组单独一个锚点。
 */
internal fun buildMediaDateAnchors(
    groups: List<MediaDateGroup>,
    baseItemIndex: Int,
    monthFormatter: DateFormat,
    unknownLabel: String,
): List<MediaDateAnchor> {
    if (groups.isEmpty()) return emptyList()
    val anchors = ArrayList<MediaDateAnchor>(groups.size)
    var itemIndex = baseItemIndex
    var lastMonth = Long.MIN_VALUE
    groups.forEach { group ->
        val month = monthKey(group.dayStart)
        if (month != lastMonth) {
            lastMonth = month
            anchors += MediaDateAnchor(
                itemIndex = itemIndex,
                label = if (group.dayStart > 0L) {
                    monthFormatter.format(Date(group.dayStart))
                } else {
                    unknownLabel
                },
            )
        }
        // 每组占一个标题 item + 组内所有行
        itemIndex += 1 + group.entries.size
    }
    return anchors
}

private fun startOfLocalDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

/** 年*12+月，用来判断相邻日期组是否跨月；0 表示时间未知。 */
private fun monthKey(dayStart: Long): Long {
    if (dayStart <= 0L) return 0L
    val calendar = Calendar.getInstance().apply { timeInMillis = dayStart }
    return calendar.get(Calendar.YEAR) * 12L + calendar.get(Calendar.MONTH)
}
