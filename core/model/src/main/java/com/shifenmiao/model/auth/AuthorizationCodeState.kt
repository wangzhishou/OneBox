package com.shifenmiao.model.auth

import androidx.compose.runtime.Immutable

/**
 * 全局授权码锁屏状态。
 *
 * 由 [com.shifenmiao.base.auth.AuthorizationCodeStateHolder] 持有,
 * 在 [com.shifenmiao.model.event.AppEventBus] 接收到
 * [RequestAuthorizationCodeEvent] 时切换为 [showAuthCode] = true,
 * 完成或取消后回到默认状态。
 *
 * [error] 字段使用 [AuthCodeError] 枚举表示,UI 层根据当前语言翻译为字符串
 * (core/r/values/strings.xml + 各 locale 翻译);这样 holder 不直接依赖
 * Android 资源,符合 "核心逻辑不依赖 IO / 系统资源" 的设计约定。
 */
@Immutable
data class AuthorizationCodeState(
    val showAuthCode: Boolean = false,
    val mode: AuthCodeMode = AuthCodeMode.Unlock,
    val setupStep: AuthCodeSetupStep = AuthCodeSetupStep.Enter,
    val error: AuthCodeError? = null,
)

/**
 * 首次设置授权码时的两步状态机。
 */
enum class AuthCodeSetupStep {
    Enter,
    Confirm,
}

/**
 * 锁屏上的错误类型,UI 层根据此枚举映射到字符串资源。
 */
enum class AuthCodeError {
    WrongCode,
    Mismatch,
}
