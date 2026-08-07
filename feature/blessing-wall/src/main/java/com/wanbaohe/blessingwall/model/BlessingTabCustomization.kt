package com.wanbaohe.blessingwall.model

/**
 * 用户对单个祈福 tab 的自定义文案。
 * 空串表示未自定义，展示时回退到远程下发 / 本地兜底文案。
 */
data class BlessingTabCustomization(
    val title: String = "",
    val subtitle: String = "",
)

/**
 * 一次标题/副标题编辑产生的按日期快照。
 */
data class BlessingTabCustomizationSnapshot(
    val date: String,
    val type: BlessingType,
    val title: String,
    val subtitle: String,
)

/**
 * 计算 [date] 当天生效的自定义文案：每个类型取快照日期不晚于 [date] 的最近一条。
 * 日期均为 `yyyy-MM-dd` 格式，可直接按字典序比较。
 */
fun List<BlessingTabCustomizationSnapshot>.effectiveAt(
    date: String,
): Map<BlessingType, BlessingTabCustomization> {
    return filter { it.date <= date }
        .groupBy { it.type }
        .mapValues { (_, snapshots) ->
            val latest = snapshots.maxBy { it.date }
            BlessingTabCustomization(title = latest.title, subtitle = latest.subtitle)
        }
}
