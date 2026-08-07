package com.shifenmiao.base.utils

import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.storage.TokenStorage

object LoginUtils {

    private const val DEFAULT_ADMIN_VIP_LEVEL = 10

    fun getAdminVipLevel(): Int {
        return RemoteConfigStorage.getRemoteConfig().adminVipLevel ?: DEFAULT_ADMIN_VIP_LEVEL
    }

    fun isAdmin(): Boolean {
        val loginInfo = TokenStorage.getLoginInfo()
        return (loginInfo?.user?.vipLevel ?: 0) >= getAdminVipLevel()
    }
}