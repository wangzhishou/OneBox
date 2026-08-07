package com.shifenmiao.model.state

import androidx.compose.runtime.compositionLocalOf
import com.shifenmiao.interfaces.singleton.AppContext

/**
 * 聊天页面的整体 UI 状态。
 *
 * 状态分为两个维度，彼此完全解耦：
 * - [pageState]：页面级状态，控制页面是否可交互（顶栏按钮、输入框启用/禁用等）
 * - [chatActive]：聊天进行中标志，从用户点击发送到流式输出/Agent Loop 完全结束期间为 true，
 *   用于驱动停止按钮显示、守卫 placeholder 不被 DB 空数据覆盖等
 *
 * 时序示例：
 * ```
 * 用户点击发送 → pageState=IDLE, chatActive=true
 *   ↓ 首个 chunk 到达（消息级 RobotLoading → STREAMING）
 *   ↓ 流式输出中...
 *   ↓ isEnd / Agent Loop 完成
 * pageState=IDLE, chatActive=false
 * ```
 */
data class ChatUIState(
    /** 是否显示历史记录面板 */
    val showHistory: Boolean = false,
    /** 是否显示消息头像 */
    val showAvatar: Boolean = false,
    /** 是否在消息底部显示 token 用量 */
    val showTokens: Boolean = true,
    /**
     * 页面级状态 —— 只管「页面能不能交互」。
     *
     * - [PageState.INITIALIZING]：页面初始化中，禁用所有交互控件
     * - [PageState.IDLE]：空闲，可正常交互（无论聊天是否进行中）
     * - [PageState.ERROR]：页面级错误，显示错误 UI
     */
    val pageState: PageState = PageState.INITIALIZING,
    /**
     * 聊天是否进行中 —— 独立于 [pageState]。
     *
     * - true：从 [startChat][com.shifenmiao.ai.component.AIChatComponent.startChatWithStreaming] 调用开始，
     *   到流式输出完成 / Agent Loop 结束 / 取消 / 错误 时复位为 false
     * - 驱动 UI：发送按钮 ↔ 停止按钮切换、输入框发送禁用等
     * - 守卫数据：[chatActive=true] 时 [observeMessages][com.shifenmiao.ai.component.AIChatComponent.observeMessages]
     *   跳过 Room Flow 更新，避免 DB 空数据覆盖正在显示的 placeholder（RobotLoading）
     */
    val chatActive: Boolean = false,
    /** 页面级错误信息，仅在 [pageState]=[PageState.ERROR] 时有值 */
    val errorMessage: String = AppContext.getString(com.shifenmiao.core.R.string.error_message),
    /** 是否显示历史消息截断提示（超过 ACTIVE_CHAT_MESSAGE_LIMIT 条时） */
    val showMessageLimitNotice: Boolean = false,
)

val LocalChatUIState = compositionLocalOf<ChatUIState> { error("UIState not present") }

/**
 * 页面级状态枚举 —— 只描述「页面是否可交互」。
 *
 * 与消息级状态 [com.shifenmiao.model.ai.MessageUIState] 完全解耦：
 * - 消息级：LOADING(-1) → STREAMING(0) → NORMAL(1) / ERROR(-2)，控制单条消息的渲染方式
 *   （RobotLoading 占位块、打字机效果、错误卡片等）
 * - 页面级：本枚举，控制整个聊天页面的交互能力
 */
enum class PageState {
    /** 页面初始化中（加载配置/远程 Prompt 等），禁用顶栏按钮和输入框 */
    INITIALIZING,
    /** 空闲可交互，无论聊天是否进行中（聊天进行中由 [ChatUIState.chatActive] 单独标记） */
    IDLE,
    /** 页面级错误（网络不可用、引擎配置缺失等），显示错误 UI 和重试按钮 */
    ERROR
}
