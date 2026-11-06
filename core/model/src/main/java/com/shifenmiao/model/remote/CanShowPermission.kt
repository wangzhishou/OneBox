package com.shifenmiao.model.remote

import com.shifenmiao.model.channel.FlavorType

enum class CanShowPermission(val canShow: Boolean) {
    ENABLED(true),
    DISABLED(false);

    companion object {
        fun getConfigByFlavor(flavorType: FlavorType = FlavorType.fromName()): CanShowPermission {
            // 国内渠道全部先弹用途说明再申请系统权限(应用商店隐私合规要求,同屏告知目的)
            return if (flavorType.isOverseas) DISABLED else ENABLED
        }
    }
}