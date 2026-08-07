package com.wanbaohe.blessingwall.model

enum class BlessingType(val key: String) {
    WOODEN_FISH("woodenFish"),
    WEALTH_GOD("wealthGod"),
    GUANYIN("guanyin"),
    INCENSE("incense"),
    ;

    companion object {
        fun fromKey(key: String): BlessingType? = entries.find { it.key == key }
    }
}
