package com.wanbaohe.blessingwall.model

/**
 * 祈福 tab 文案的三级解析：用户自定义快照 → 远程下发 → 本地兜底。
 */
fun resolveTabText(custom: String?, remote: String?, fallback: String): String {
    return custom?.takeIf { it.isNotBlank() }
        ?: remote?.takeIf { it.isNotBlank() }
        ?: fallback
}
