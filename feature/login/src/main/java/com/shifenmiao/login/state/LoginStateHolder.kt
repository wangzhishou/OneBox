package com.shifenmiao.login.state

import com.shifenmiao.model.login.LoginMethod
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

    /** 已经处理过的微信授权 code（code 一次性，replay 时用于去重） */
    private val handledWechatCodes: MutableSet<String> =
        java.util.Collections.synchronizedSet(mutableSetOf())

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
        // 重新打开登录框时顺手复位 loading：任何流程（微信/Google/网络）中断都可能
        // 把 isLoggingIn 留在 true，导致下次进来整屏表单都是灰的、像卡死。
        _loginState.update {
            it.copy(
                showLogin = true,
                loggingInMethod = null,
                isAwaitingWechatAuth = false,
            )
        }
    }

    fun hideLoginModal() {
        _loginState.update {
            it.copy(
                showLogin = false,
                loggingInMethod = null,
                isAwaitingWechatAuth = false,
            )
        }
    }

    fun showBindPhone() {
        _loginState.update { it.copy(showBind = true) }
    }

    fun hideBindPhone() {
        _loginState.update { it.copy(showBind = false) }
    }

    /** 开始一次登录请求；同时只允许一种登录方式在跑，避免并发登录互相覆盖用户态 */
    fun startLogin(method: LoginMethod) {
        _loginState.update {
            it.copy(
                loggingInMethod = method,
                isAwaitingWechatAuth = method == LoginMethod.WECHAT,
            )
        }
    }

    /** 结束登录请求（成功/失败/取消/超时都要走到这里） */
    fun finishLogin() {
        _loginState.update {
            it.copy(
                loggingInMethod = null,
                isAwaitingWechatAuth = false,
            )
        }
    }

    /** 已经收到微信回传的授权结果，不再等微信客户端；后续若还要请求后端，loading 继续保留 */
    fun finishWechatAuthWait() {
        _loginState.update { it.copy(isAwaitingWechatAuth = false) }
    }

    /**
     * 微信授权 code 一次性：同一 code 只处理一次。
     * SharedFlow 带 replay，Activity/Component 重建后旧事件会被重放，
     * 如果不拦住就会拿过期 code 再请求一次，用户看到莫名其妙的「登录失败」。
     */
    fun markWechatCodeHandled(code: String): Boolean = handledWechatCodes.add(code)

    fun updateLoginStyle(style: com.shifenmiao.model.login.LoginStyle) {
        _loginState.update { it.copy(loginStyle = style) }
    }

    fun update(block: (LoginState) -> LoginState) {
        _loginState.update(block)
    }
}
