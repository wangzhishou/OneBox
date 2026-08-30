package com.shifenmiao.model.remote



import com.shifenmiao.model.channel.FlavorType

enum class ShowBindPhoneConfig(val isShow: Boolean) {
    ENABLED(true),
    DISABLED(false);

    companion object {
        fun getConfigByFlavor(flavorType: FlavorType = FlavorType.fromName()): ShowBindPhoneConfig {
            return when (flavorType) {
                FlavorType.ONEBOX -> ENABLED
                FlavorType.XIAOMI -> ENABLED
                FlavorType.YYB -> DISABLED
                FlavorType.OPPO -> ENABLED
                FlavorType.VIVO -> ENABLED
                FlavorType.HUAWEI -> ENABLED
                // 海外渠道(google / foss)不提供绑定手机功能
                FlavorType.GOOGLE, FlavorType.FOSS -> DISABLED
                else -> {
                    DISABLED
                }
            }
        }
    }
}