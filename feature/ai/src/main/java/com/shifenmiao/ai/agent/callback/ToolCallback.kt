package com.shifenmiao.ai.agent.callback

import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen

/**
 * 工具回调接口 - 通用的异步回调机制
 *
 * 设计原则：
 * 1. 支持参数传递和结果返回
 * 2. 支持嵌套调用（工具 A 调用工具 B）
 * 3. 支持 Screen 导航并等待结果
 * 4. 类型安全，编译期检查
 *
 * 使用示例：
 * ```kotlin
 * class MyTool : AgentTool {
 *     override suspend fun execute(arguments: String, callback: ToolCallback): AgentToolResult {
 *         // 调用子工具
 *         val subResult = callback.callTool("sub_tool", "{}")
 *
 *         // 导航到 Screen
 *         val screenResult = callback.navigateToScreen<FileBrowserResult> { onResult ->
 *             Screen.FileBrowser(onResult = onResult)
 *         }
 *
 *         return AgentToolResult(content = "完成")
 *     }
 * }
 * ```
 */
interface ToolCallback {

    /**
     * 调用子工具并等待结果
     *
     * @param toolName 目标工具名
     * @param arguments 传递给子工具的参数（JSON 字符串）
     * @return 子工具的执行结果
     */
    suspend fun callTool(toolName: String, arguments: String): CallbackResult

    /**
     * 导航到 Screen 并等待用户操作结果
     *
     * @param screenBuilder 构建 Screen 的 lambda，接收 onResult 回调
     * @return 用户操作的结果，如果取消则返回 null
     */
    suspend fun <T> navigateToScreen(
        screenBuilder: (onResult: (T) -> Unit) -> Screen
    ): T?

    /**
     * 打开一个 Screen，但不等待结果。
     *
     * 适用于：
     * 1. 只需要把用户带到某个页面
     * 2. 对应 Screen 没有结果回调协议
     * 3. 工具希望立即返回，而不是挂起等待
     */
    fun openScreen(screen: Screen)

    /**
     * 完成回调 - 由子工具或 Screen 调用
     *
     * @param callbackId 回调 ID
     * @param result 回调结果
     */
    fun completeCallback(callbackId: String, result: CallbackResult)

    /**
     * 取消所有待处理的回调
     */
    fun cancelAll()
}
