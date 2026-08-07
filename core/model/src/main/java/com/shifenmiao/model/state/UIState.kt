package com.shifenmiao.model.state

import androidx.compose.runtime.compositionLocalOf
import com.shifenmiao.model.BuildConfig
import com.shifenmiao.model.webview.WebViewParams

data class UIState(
    /**
     * 显示AI聊天对话框
     */
    val initAIChatAndShow: Boolean = false,
    /**
     * 打开微信小程序回调
     */
    val launchMiniProResp: Boolean = false,
    /**
     * 是不是Debug
     */
    val debug: Boolean = BuildConfig.DEBUG,

    /**
     * 是否能够分享
     */
    val showShare: Boolean = false,

    /**
     * 显示权限提示会话框
     */
    val showPermissionDialog: Boolean = false,

    /**
     * 显示打赏对话框
     */
    val showBuyCoffeeDialog: Boolean = false,

    /**
     * 显示AI模型对话框
     */
    val showAIModelsModalSheet: Boolean = false,

    /**
     * 显示抽屉
     */
    val showDrawer: Boolean = false,

    /**
     * 默认标题栏开始是折叠的
     */
    val topAppBarStartCollapsed: Boolean = true,

    /**
     * 要显示blog的ID
     */
    val blogId: Int = 0,
    /**
     * 打开webView
     */
    val openWebView: WebViewParams? = null,

    /**
     * 当前是否处于画中画（PiP）模式
     */
    val isInPipMode: Boolean = false,
)


val LocalUIState = compositionLocalOf<UIState> { error("UIState not present") }
