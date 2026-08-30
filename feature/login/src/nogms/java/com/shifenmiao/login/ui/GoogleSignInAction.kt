package com.shifenmiao.login.ui

import androidx.compose.runtime.Composable
import com.shifenmiao.base.utils.ActionUtils

/**
 * 非 google 渠道 (国内 + foss) stub: 不打包 GMS, Google 登录按钮本就由
 * channelConfig.enableGms 隐藏 (仅 google 渠道开启); onebox debug 下手动点开时提示不可用。
 * 与 src/google 的真实实现签名保持一致。
 */
@Composable
fun rememberGoogleSignInAction(
    onIdToken: (String) -> Unit,
): () -> Unit = {
    ActionUtils.showToast("Google login is not available in this build")
}
