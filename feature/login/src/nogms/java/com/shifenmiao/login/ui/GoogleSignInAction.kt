package com.shifenmiao.login.ui

import androidx.compose.runtime.Composable
import com.shifenmiao.base.utils.ActionUtils

/**
 * 非 google 渠道 (国内 + foss) stub: 不打包 GMS, 登录入口不可用。
 * 与 src/google 的真实实现签名保持一致, main 源集据此隐藏按钮。
 */
@Composable
fun rememberGoogleSignInAvailable(): Boolean = false

@Composable
fun rememberGoogleSignInAction(
    onIdToken: (String) -> Unit,
): () -> Unit = {
    ActionUtils.showToast("Google login is not available in this build")
}
