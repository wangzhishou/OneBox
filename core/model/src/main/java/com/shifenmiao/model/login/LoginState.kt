package com.shifenmiao.model.login

import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.user.DefaultUser


/**
 * Login State holding ui input values
 */
data class LoginState(
    val userId: Int = 0,
    val jwt: String = "",
    val username: String = DefaultUser.normal.username ?: "",
    val nickname: String = DefaultUser.normal.nickname ?: "",
    val avatar: String? = DefaultUser.normal.avatar,
    val emailOrMobile: String = "",
    val password: String = "",
    val verificationCode: String = "",
    val errorState: LoginErrorState = LoginErrorState(),
    val isLogin: Boolean = false,
    /**
     * 当前正在进行的登录方式；null 表示没有登录请求在跑。
     *
     * 只用一个字段而不是 `isLoggingIn: Boolean`，是为了让 UI 能把 loading 画在
     * 真正被点击的那个入口上（微信按钮不会再跟着 Google 按钮一起转圈），
     * 同时保留 [isLoggingIn] 供「锁住表单、禁止并发登录」的判断使用。
     */
    val loggingInMethod: LoginMethod? = null,
    /**
     * 已拉起微信、还在等微信客户端回传授权结果（此时连 code 都还没有）。
     * 用户取消/返回时如果微信没有回调，靠它把 loading 兜底解除。
     */
    val isAwaitingWechatAuth: Boolean = false,
    val showLogin: Boolean = false,
    val loginStyle: LoginStyle = LoginStyle.LOGIN,
    val points: Int = 0,
    val invitationCode: String = "",
    val phone: String = "",
    val showBind: Boolean = false,
    val isWechat: Boolean = false,
    val role: Int = 0,
    val vipLevel: Int = 0,
    val totalRechargeAmount: Double = 0.0,
    /** 邮箱是否已验证(邮箱注册用户登录后为 false,验证成功后置 true) */
    val confirmed: Boolean = false,
) {
    /** 是否有登录请求在跑（任一登录方式），UI 据此锁住表单避免并发登录 */
    val isLoggingIn: Boolean get() = loggingInMethod != null
}

/**
 * 登录方式，用于把 loading 指到对应入口，并在流程中断时精确复位。
 */
enum class LoginMethod {
    ACCOUNT,
    PHONE,
    CODE,
    REGISTER,
    WECHAT,
    GOOGLE,
}

enum class LoginStyle(val value: Int) {
    LOGIN(1),
    REGISTRATION(2),
    FORGOT(3);
    companion object {
        fun fromInt(value: Int): LoginStyle? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

enum class LoginType(val id: Int, val title: String, val desc: String) {
    CODE_LOGIN(1, AppContext.getString(R.string.free_login), ""),
    PHONE_LOGIN(2, AppContext.getString(R.string.phone_login), ""),
    EMAIL_LOGIN(3, AppContext.getString(R.string.email_login),"");

    companion object {
        fun fromId(id: Int): LoginType? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

/**
 * Error state in login holding respective
 * text field validation errors
 */
data class LoginErrorState(
    val emailOrMobileErrorState: ErrorState = ErrorState(),
    val passwordErrorState: ErrorState = ErrorState(),
    val verificationCodeErrorState: ErrorState = ErrorState(),
)

