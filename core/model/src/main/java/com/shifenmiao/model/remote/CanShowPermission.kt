package com.shifenmiao.model.remote

import com.shifenmiao.model.channel.FlavorType

enum class CanShowPermission(val canShow: Boolean) {
    ENABLED(true),
    DISABLED(false);

    companion object {
        fun getConfigByFlavor(flavorType: FlavorType = FlavorType.fromName()): CanShowPermission {
            return when (flavorType) {
                FlavorType.ONEBOX -> DISABLED
                FlavorType.XIAOMI -> DISABLED
                FlavorType.YYB -> DISABLED
                FlavorType.OPPO -> DISABLED
                FlavorType.VIVO -> DISABLED
                FlavorType.HUAWEI -> ENABLED
                else -> {
                    DISABLED
                }
            }
        }
    }
}