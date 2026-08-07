package com.shifenmiao.model.remote

import com.shifenmiao.model.channel.FlavorType

enum class AgentSensitiveCheckConfig(val isEnabled: Boolean) {
    ENABLED(true),
    DISABLED(false);

    companion object {
        fun getConfigByFlavor(flavorType: FlavorType = FlavorType.fromName()): AgentSensitiveCheckConfig {
            return when (flavorType) {
                FlavorType.ONEBOX -> DISABLED
                FlavorType.XIAOMI -> DISABLED
                FlavorType.YYB -> DISABLED
                FlavorType.OPPO -> ENABLED
                FlavorType.VIVO -> ENABLED
                FlavorType.HUAWEI -> ENABLED
                else -> {
                    DISABLED
                }
            }
        }
    }
}
