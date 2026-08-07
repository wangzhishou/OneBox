package com.wanbaohe.habittracker.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.shifenmiao.base.ui.icon.IconRegistry
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWaterDrop

/**
 * 习惯可选图标目录 — 12 个常用 Line 图标 + "更多"入口的完整注册表。
 *
 * 存库 iconKey 为 [IconRegistry] 注册键原样(如 "Waterdrop"、"Home");
 * 历史数据为小写键(如 "waterdrop"),解析时先原样查注册表,
 * 失败再试首字母大写形式,最后回退默认水滴。
 */
object HabitIcons {

    const val DEFAULT_KEY = "waterdrop"

    /** 12 个常用存库 iconKey,顺序即编辑页选择器展示顺序 */
    val keys: List<String> = listOf(
        "waterdrop",
        "sunrise",
        "running",
        "book",
        "meditation",
        "moon",
        "toothbrush",
        "pill",
        "fruit",
        "writing",
        "piggybank",
        "dumbbell",
    )

    /** 编辑页 IconSelector 使用的注册键列表(与 [keys] 一一对应) */
    val registryKeys: List<String> = keys.map(::registryKeyOf)

    /** 存库 iconKey → IconRegistry 注册键(首字母大写) */
    fun registryKeyOf(key: String): String {
        return key.replaceFirstChar { it.uppercase() }
    }

    /** 解析图标:原样查注册表 → 首字母大写形式 → 默认水滴,保证新旧数据都能解析 */
    fun iconFor(key: String): ImageVector {
        return IconRegistry.resolve(key)
            ?: IconRegistry.resolve(registryKeyOf(key))
            ?: IconRegistry.resolve(registryKeyOf(DEFAULT_KEY))
            // 兜底:注册表条目缺失时直接引用资源,保证展示不崩
            ?: Icons.Outlined.LineWaterDrop
    }

    /** 存库 iconKey → 注册表中真实存在的键(用于选择器高亮),解析失败回退默认水滴键 */
    fun resolvedRegistryKey(key: String): String {
        return when {
            IconRegistry.resolve(key) != null -> key
            IconRegistry.resolve(registryKeyOf(key)) != null -> registryKeyOf(key)
            else -> registryKeyOf(DEFAULT_KEY)
        }
    }

    /** 是否合法存库键:12 个常用小写键,或注册表可解析的任意键 */
    fun isValidKey(key: String): Boolean {
        return IconRegistry.resolve(key) != null ||
            IconRegistry.resolve(registryKeyOf(key)) != null
    }
}
