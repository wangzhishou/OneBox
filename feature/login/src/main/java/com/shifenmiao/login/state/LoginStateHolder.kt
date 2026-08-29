package com.shifenmiao.login.state

import com.shifenmiao.model.login.LoginState
import com.shifenmiao.model.user.Login
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.remote.AnalyticsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 轻量级登录状态持有者，@Singleton 生命周期。
 *
 * 启动时从 TokenStorage（MMKV）同步读取登录状态，无需 LoginComponent 初始化。
 * LoginComponent 创建后通过此类读写状态，保持单一数据源。
 */
@Singleton
class LoginStateHolder @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val loginInfo = TokenStorage.getLoginInfoFromLocalStorage()
            if (loginInfo?.jwt?.isNotEmpty() == true) {
                val user = loginInfo.user
                _loginState.value = _loginState.value.copy(
                    jwt = loginInfo.jwt,
                    isLogin = true,
                    userId = user.id,
                    username = user.username ?: "",
                    nickname = user.nickname ?: "",
                    avatar = user.avatar,
                    emailOrMobile = user.email ?: "",
                    points = user.points ?: 0,
                    invitationCode = user.invitationCode ?: "",
                    phone = user.phone ?: "",
                    isWechat = user.openid?.isNotEmpty() ?: false,
                    vipLevel = user.vipLevel ?: 0,
                    totalRechargeAmount = user.totalRechargeAmount ?: 0.0,
                    confirmed = user.confirmed,
                )
                syncAnalyticsUserProperties(
                    vipLevel = user.vipLevel ?: 0,
                    isWechat = user.openid?.isNotEmpty() ?: false
                )
            }
        }
    }

    fun updateLoginState(login: Login) {
        TokenStorage.saveTokenToLocalStorage(login)
        val user = login.user
        _loginState.value = _loginState.value.copy(
            jwt = login.jwt,
            isLogin = true,
            showLogin = false,
            userId = user.id,
            username = user.username ?: "",
            nickname = user.nickname ?: "",
            avatar = user.avatar,
            emailOrMobile = user.email ?: "",
            points = user.points ?: 0,
            invitationCode = user.invitationCode ?: "",
            phone = user.phone ?: "",
            isWechat = user.openid?.isNotEmpty() ?: false,
            vipLevel = user.vipLevel ?: 0,
            totalRechargeAmount = user.totalRechargeAmount ?: 0.0,
            confirmed = user.confirmed,
        )
        val isWechat = user.openid?.isNotEmpty() ?: false
        analyticsManager.logEvent(
            "login",
            mapOf("method" to if (isWechat) "wechat" else "account")
        )
        syncAnalyticsUserProperties(
            vipLevel = user.vipLevel ?: 0,
            isWechat = isWechat
        )
    }

    fun clearLoginState() {
        TokenStorage.clearLoginInfo()
        _loginState.value = LoginState()
        analyticsManager.setUserProperty("vip_level", "0")
        analyticsManager.setUserProperty("login_method", null)
    }

    /** 登录/登出/本地恢复时同步 GA 用户属性(国内渠道为 no-op) */
    private fun syncAnalyticsUserProperties(vipLevel: Int, isWechat: Boolean) {
        analyticsManager.setUserProperty("vip_level", vipLevel.toString())
        analyticsManager.setUserProperty(
            "login_method",
            if (isWechat) "wechat" else "account"
        )
    }

    fun updatePoints(points: Int) {
        _loginState.update { it.copy(points = points) }
    }

    fun showLoginModal() {
        _loginState.update { it.copy(showLogin = true) }
    }

    fun hideLoginModal() {
        _loginState.update { it.copy(showLogin = false) }
    }

    fun showBindPhone() {
        _loginState.update { it.copy(showBind = true) }
    }

    fun hideBindPhone() {
        _loginState.update { it.copy(showBind = false) }
    }

    fun setLoggingIn(logging: Boolean) {
        _loginState.update { it.copy(isLoggingIn = logging) }
    }

    fun updateLoginStyle(style: com.shifenmiao.model.login.LoginStyle) {
        _loginState.update { it.copy(loginStyle = style) }
    }

    fun update(block: (LoginState) -> LoginState) {
        _loginState.update(block)
    }
}
