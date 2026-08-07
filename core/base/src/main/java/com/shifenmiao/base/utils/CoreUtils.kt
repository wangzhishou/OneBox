package com.shifenmiao.base.utils

import com.shifenmiao.core.BuildConfig
import com.shifenmiao.core.constants.Constants
import com.shifenmiao.model.channel.NeedPrivacyPolicyDialog
import com.shifenmiao.model.remote.CanShowPermission
import com.shifenmiao.storage.AppSharedStorage
import kotlinx.coroutines.delay

object CoreUtils {

    fun isShowPrivacyPolicyDialog():Boolean {
        // 分渠道: google 渠道不要求首启隐私弹窗, 直接视为已同意
        if (!NeedPrivacyPolicyDialog.getConfigByFlavor().need) return false
        return AppSharedStorage.loadPrivacyPolicyVersion() < Constants.PRIVACY_POLICY_VERSION
    }

    /**
     * 阻塞直到用户同意隐私政策。用于需要在同意后才开始执行的敏感网络请求。
     */
    suspend fun awaitPrivacyPolicyAccepted() {
        while (isShowPrivacyPolicyDialog()) {
            delay(300)
        }
    }

    fun isOneBoxDebug():Boolean {
        return BuildConfig.DEBUG && BuildConfig.FLAVOR == "onebox"
    }

    fun isGoogleDebug():Boolean {
        return BuildConfig.DEBUG && BuildConfig.FLAVOR == "google"
    }

    fun isHuawei():Boolean {
        return CanShowPermission.getConfigByFlavor().canShow
    }
}