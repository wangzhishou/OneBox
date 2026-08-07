package com.shifenmiao.model.channel

import com.shifenmiao.model.BuildConfig

enum class FlavorType(val flavorName: String) {
    ONEBOX("onebox"),
    XIAOMI("xiaomi"),
    YYB("yyb"),
    OPPO("oppo"),
    VIVO("vivo"),
    GOOGLE("google"),
    HUAWEI("huawei");

    companion object {
        fun fromName(name: String = BuildConfig.FLAVOR): FlavorType {
            return entries.find { it.flavorName == name } ?: ONEBOX
        }
    }
}