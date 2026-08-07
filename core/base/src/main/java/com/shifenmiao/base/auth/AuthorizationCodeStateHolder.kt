package com.shifenmiao.base.auth

import com.shifenmiao.base.authcode.AuthCodeService
import com.shifenmiao.model.auth.AuthCodeError
import com.shifenmiao.model.auth.AuthCodeMode
import com.shifenmiao.model.auth.AuthCodeSetupStep
import com.shifenmiao.model.auth.AuthCodeVerifier
import com.shifenmiao.model.auth.AuthorizationCodeState
import com.shifenmiao.model.auth.RequestAuthorizationCodeEvent
import com.shifenmiao.model.event.AppEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * 全局授权码状态持有者。
 *
 * - 持有内存中的授权码 ([authCode]),仅本进程内有效
 * - 持久化由 [AuthCodeService] 负责:Setup 成功后写库,下次冷启动时
 *   [AuthCodeVerifier.hasCode] 仍能正确返回 true,锁屏进入 Unlock 模式
 * - 订阅 [AppEventBus.requestAuthorizationCodeEvents] 接收解锁 / 设置请求
 * - 通过 [Provider] 懒获取 [AuthCodeVerifier],Verifier 未注册时降级为
 *   "非空即通过"(防御性,无 verifier 也能完成首次设置)
 * - 订阅协程采用懒启动:[ensureSubscribed] 只在首次
 *   [com.shifenmiao.base.utils.ActionUtils.showAuthCode] 时启动,
 *   不增加启动期开销
 * - Setup 模式的两步状态机 (Enter → Confirm) 由本持有者维护,
 *   UI 通过 [AuthorizationCodeState.setupStep] 渲染对应文案
 */
@Singleton
class AuthorizationCodeStateHolder @Inject constructor(
    private val verifierProvider: Provider<AuthCodeVerifier>,
    private val authCodeService: AuthCodeService,
) {

    private val _authCode = MutableStateFlow<String?>(null)
    val authCode: StateFlow<String?> = _authCode.asStateFlow()

    private val _state = MutableStateFlow(AuthorizationCodeState())
    val state: StateFlow<AuthorizationCodeState> = _state.asStateFlow()

    val isAuthorized: Boolean
        get() = !_authCode.value.isNullOrEmpty()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var subscriptionJob: Job? = null

    @Volatile
    private var pending: RequestAuthorizationCodeEvent? = null

    @Volatile
    private var pendingFirstCode: String? = null

    /**
     * 首次 [submit] / [cancel] / 事件订阅前调用,启动事件订阅。
     * 重复调用安全。
     */
    @Synchronized
    fun ensureSubscribed() {
        if (subscriptionJob?.isActive == true) return
        subscriptionJob = scope.launch {
            AppEventBus.requestAuthorizationCodeEvents.collect { event ->
                handleRequest(event)
            }
        }
    }

    /**
     * 用户在锁屏上输入完毕 (6 位) 时调用。
     *
     * - Unlock 模式:调 verifier 校验,成功 → 写码 → onSuccess,失败 → errorMessage
     * - Setup 模式:维护 Enter/Confirm,两次一致 → 持久化 + 写码 → onSuccess,不一致 → mismatch
     */
    fun submit(code: String) {
        ensureSubscribed()
        val event = pending ?: return
        val current = _state.value
        scope.launch {
            if (current.mode == AuthCodeMode.Setup) {
                handleSetupSubmit(code, event)
            } else {
                handleUnlockSubmit(code, event)
            }
        }
    }

    /**
     * 用户在锁屏上点击取消。
     */
    fun cancel() {
        ensureSubscribed()
        val event = pending
        resetPending()
        event?.onFailure?.invoke(null)
    }

    private suspend fun handleRequest(event: RequestAuthorizationCodeEvent) {
        if (isAuthorized) {
            event.onSuccess.invoke()
            return
        }
        pending = event
        pendingFirstCode = null
        val verifier = runCatching { verifierProvider.get() }.getOrNull()
        val hasCode = verifier?.let { runCatching { it.hasCode() }.getOrDefault(false) } ?: false
        _state.value = AuthorizationCodeState(
            showAuthCode = true,
            mode = if (hasCode) AuthCodeMode.Unlock else AuthCodeMode.Setup,
            setupStep = AuthCodeSetupStep.Enter,
        )
    }

    private suspend fun handleUnlockSubmit(code: String, event: RequestAuthorizationCodeEvent) {
        val verifier = runCatching { verifierProvider.get() }.getOrNull()
        val ok = if (verifier != null) {
            runCatching { verifier.verify(code) }.getOrDefault(false)
        } else {
            code.isNotBlank()
        }
        if (ok) {
            _authCode.value = code
            resetPending()
            event.onSuccess.invoke()
        } else {
            _state.value = _state.value.copy(error = AuthCodeError.WrongCode)
            event.onFailure.invoke("wrong_code")
        }
    }

    /**
     * Setup 模式两步状态机:
     * 1. Enter 步骤第一次输入 → 切到 Confirm,等待第二次输入
     * 2. Confirm 步骤第二次输入与第一次一致 → 持久化到数据库 + 写内存 → onSuccess
     * 3. Confirm 步骤第二次输入不一致 → errorMessage,回到 Enter
     */
    private suspend fun handleSetupSubmit(code: String, event: RequestAuthorizationCodeEvent) {
        val first = pendingFirstCode
        if (first == null) {
            pendingFirstCode = code
            _state.value = _state.value.copy(setupStep = AuthCodeSetupStep.Confirm, error = null)
        } else if (first == code) {
            runCatching { authCodeService.setCode(code) }
            _authCode.value = code
            resetPending()
            event.onSuccess.invoke()
        } else {
            pendingFirstCode = null
            _state.value = _state.value.copy(
                setupStep = AuthCodeSetupStep.Enter,
                error = AuthCodeError.Mismatch,
            )
            event.onFailure.invoke("mismatch")
        }
    }

    private fun resetPending() {
        pending = null
        pendingFirstCode = null
        _state.value = AuthorizationCodeState()
    }
}

