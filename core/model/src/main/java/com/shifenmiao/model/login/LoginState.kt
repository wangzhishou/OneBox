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
    val isLoggingIn: Boolean = false,
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
)

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

