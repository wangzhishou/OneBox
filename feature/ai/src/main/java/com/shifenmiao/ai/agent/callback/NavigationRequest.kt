package com.shifenmiao.ai.agent.callback

import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen

/**
 * 导航请求密封类
 *
 * ToolCallbackRouter 发出的导航请求，由 AIChatComponent 观察并处理。
 */
sealed class NavigationRequest {
    /**
     * 子工具调用请求
     *
     * @param callbackId 回调唯一标识
     * @param toolName 目标工具名
     * @param arguments 传递给子工具的参数（JSON 字符串）
     */
    data class ToolCall(
        val callbackId: String,
        val toolName: String,
        val arguments: String
    ) : NavigationRequest()

    /**
     * Screen 导航请求
     *
     * @param callbackId 回调唯一标识
     * @param screen 要导航到的 Screen（已注入 onResult lambda）
     */
    data class ScreenNavigation(
        val callbackId: String,
        val screen: Screen
    ) : NavigationRequest()

    /**
     * 仅打开 Screen，不等待结果回调。
     */
    data class OpenScreen(
        val requestId: String,
        val screen: Screen
    ) : NavigationRequest()
}
