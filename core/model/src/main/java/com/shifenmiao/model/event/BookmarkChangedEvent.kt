package com.shifenmiao.model.event

/**
 * 网址收藏条目被写入/删除后广播。
 *
 * 常驻在返回栈里的浏览器（收藏查看器）靠它感知「刚在编辑页改过的这条收藏」，
 * 从而重载快照而不是继续显示旧内容；[itemId] 传 0 表示整库有变更（如删除），
 * 订阅方按需全量刷新。
 */
data class BookmarkChangedEvent(
    val itemId: Int = 0,
    val url: String = "",
    val isDeleted: Boolean = false,
)
