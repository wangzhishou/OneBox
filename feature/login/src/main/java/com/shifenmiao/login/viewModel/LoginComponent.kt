package com.shifenmiao.login.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.login.state.LoginStateHolder
import com.shifenmiao.model.login.LoginChannelConfig
import com.shifenmiao.model.login.ErrorState
import com.shifenmiao.model.login.LoginErrorState
import com.shifenmiao.model.login.LoginState
import com.shifenmiao.model.login.LoginStyle
import com.shifenmiao.model.login.LoginType
import com.shifenmiao.login.state.LoginUiEvent
import com.shifenmiao.login.state.RegistrationErrorState
import com.shifenmiao.login.state.RegistrationState
import com.shifenmiao.login.state.RegistrationUiEvent
import com.shifenmiao.login.state.codeEmptyErrorState
import com.shifenmiao.login.state.codeIsDigitErrorState
import com.shifenmiao.login.state.codeLengthErrorState
import com.shifenmiao.login.state.confirmPasswordEmptyErrorState
import com.shifenmiao.login.state.emailEmptyErrorState
import com.shifenmiao.login.state.emailOrMobileEmptyErrorState
import com.shifenmiao.login.state.emailOrUsernameAlreadyTakenErrorState
import com.shifenmiao.login.state.passwordEmptyErrorState
import com.shifenmiao.login.state.passwordMismatchErrorState
import com.shifenmiao.login.state.userNameEmptyErrorState
import com.shifenmiao.model.common.Status
import com.shifenmiao.model.login.LoginEvent
import com.shifenmiao.model.pay.alipay.AlipayResult
import com.shifenmiao.model.pay.wechat.WechatPayQueryRequest
import com.shifenmiao.model.pay.wechat.WechatPayResult
import com.shifenmiao.model.points.ConsumePointsEvent
import com.shifenmiao.model.points.RewardPointsEvent
import com.shifenmiao.model.user.Login
import com.shifenmiao.model.user.LoginRequest
import com.shifenmiao.model.user.RegisterRequest
import com.shifenmiao.model.user.SMSRequest
import com.shifenmiao.model.user.UserInviteRequest
import com.shifenmiao.model.user.VerifyCodeRequest
import com.shifenmiao.model.user.ForgotPasswordRequest
import com.shifenmiao.model.user.GoogleLoginRequest
import com.shifenmiao.model.user.ResetPasswordRequest
import com.shifenmiao.model.user.WechatLoginRequest
import com.shifenmiao.model.user.event.BindPhoneEvent
import com.shifenmiao.login.state.ForgotPasswordState
import com.shifenmiao.login.state.ForgotPasswordUiEvent
import com.shifenmiao.login.state.forgotPasswordCodeEmptyErrorState
import com.shifenmiao.login.state.forgotPasswordConfirmPasswordMismatchErrorState
import com.shifenmiao.login.state.forgotPasswordEmailEmptyErrorState
import com.shifenmiao.login.state.forgotPasswordNewPasswordEmptyErrorState
import com.shifenmiao.model.wechat.Wechat
import com.shifenmiao.model.wechat.event.WechatEvent
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.utils.NetworkUtils
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.logger.makeLog
import com.tencent.mm.opensdk.constants.ConstantsAPI
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.utils.appContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val MAX_USERNAME_RETRY = 3
private const val DEFAULT_USERNAME_PREFIX = "user"

/**
 * 用于在 Activity 重建时保持登录回调的 Holder
 */
private class LoginCallbackHolder : InstanceKeeper.Instance {
    private var _onSuccessHandle: (Response<Login>) -> Unit = {}
    private var _onFailHandle: (Response<Login>) -> Unit = {}
    private var callbackConsumed = java.util.concurrent.atomic.AtomicBoolean(false)
    var bindPhoneEvent: BindPhoneEvent = BindPhoneEvent()

    var onSuccessHandle: (Response<Login>) -> Unit
        get() = _onSuccessHandle
        set(value) {
            _onSuccessHandle = value
            callbackConsumed.set(false) // 设置新回调时重置消费状态
        }

    var onFailHandle: (Response<Login>) -> Unit
        get() = _onFailHandle
        set(value) {
            _onFailHandle = value
            callbackConsumed.set(false) // 设置新回调时重置消费状态
        }

    /**
     * 执行成功回调，确保只执行一次
     */
    fun invokeSuccessOnce(response: Response<Login>) {
        if (callbackConsumed.compareAndSet(false, true)) {
            _onSuccessHandle(response)
            clearLoginHandle()
        }
    }

    /**
     * 执行失败回调，确保只执行一次
     */
    fun invokeFailOnce(response: Response<Login>) {
        if (callbackConsumed.compareAndSet(false, true)) {
            _onFailHandle(response)
            clearLoginHandle()
        }
    }

    fun clearLoginHandle() {
        _onSuccessHandle = {}
        _onFailHandle = {}
    }

    fun clearBindPhoneEvent() {
        bindPhoneEvent = BindPhoneEvent()
    }
}

/**
 * ViewModel for Login Screen
 */
class LoginComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    private val apiService: ApiService,
    private val loginStateHolder: LoginStateHolder,
    defaultDispatchersHolder: DispatchersHolder
) : BaseComponent(defaultDispatchersHolder, componentContext) {

    val loginState: StateFlow<LoginState> = loginStateHolder.loginState

    var registrationState = mutableStateOf(RegistrationState())
        private set

    var forgotPasswordState = mutableStateOf(ForgotPasswordState())
        private set

    private var forgotPasswordTimerJob: kotlinx.coroutines.Job? = null

    // 使用 InstanceKeeper 保持回调，确保跳转第三方登录返回后回调不丢失
    private val callbackHolder: LoginCallbackHolder =
        instanceKeeper.getOrCreate("LoginCallbackHolder") { LoginCallbackHolder() }

    private var onSuccessHandle: (Response<Login>) -> Unit
        get() = callbackHolder.onSuccessHandle
        set(value) { callbackHolder.onSuccessHandle = value }

    private var onFailHandle: (Response<Login>) -> Unit
        get() = callbackHolder.onFailHandle
        set(value) { callbackHolder.onFailHandle = value }

    private var bindPhoneEvent: BindPhoneEvent
        get() = callbackHolder.bindPhoneEvent
        set(value) { callbackHolder.bindPhoneEvent = value }

    init {
        componentScope.launch {
            AppEventBus.wechatLoginEvents.collect { onWechatLoginEvent(it) }
        }
        componentScope.launch {
            AppEventBus.loginEvents.collect { onLoginEvent(it) }
        }
        componentScope.launch {
            AppEventBus.consumePointsEvents.collect { onConsumePointsEvent(it) }
        }
        componentScope.launch {
            AppEventBus.rewardPointsEvents.collect { onRewardPointsEvent(it) }
        }
        componentScope.launch {
            AppEventBus.bindPhoneEvents.collect { onBindPhoneEvent(it) }
        }
    }

    fun onDestroy() {
        clearLoginHandle()
        clearBindPhoneEvent()
    }

    /**
     * ErrCode	ERR_OK = 0(用户同意) ERR_AUTH_DENIED = -4（用户拒绝授权） ERR_USER_CANCEL = -2（用户取消）
     * code	用户换取 access_token 的 code，仅在 ErrCode 为 0 时有效
     * state 第三方程序发送时用来标识其请求的唯一性的标志，由第三方程序调用 sendReq 时传入，由微信终端回传，state 字符串长度不能超过 1K
     * lang	微信客户端当前语言
     * country	微信用户当前国家信息
     *
     */
    fun onWechatLoginEvent(event: WechatEvent) {
        CoroutineScope(defaultDispatcher).launch {
            val resp = event.message
            if (resp.type == ConstantsAPI.COMMAND_SENDAUTH) {
                if (resp.errCode == com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode.ERR_OK) {
                    wechatLogin(resp.code)
                } else if (resp.errCode == com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode.ERR_AUTH_DENIED || resp.errCode == com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode.ERR_USER_CANCEL) {
                    endLogin()
                }
            } else {
                makeLog {
                    "Wechat login ignored, resp type: ${resp.type}"
                }
                endLogin()
            }
        }
    }

    fun onLoginEvent(event: LoginEvent) {
        CoroutineScope(defaultDispatcher).launch {
            showLoginAndCallback(
                onSuccess = {
                    event.onSuccess(it)
                    clearLoginHandle()
                },
                onFail = {
                    event.onFailure(it)
                    clearLoginHandle()
                }
            )
        }
    }

    fun onBindPhoneEvent(event: BindPhoneEvent) {
        CoroutineScope(defaultDispatcher).launch {
            bindPhoneEvent.source = event.source
            bindPhoneEvent.onSuccess = {
                event.onSuccess()
                clearBindPhoneEvent()
            }
            bindPhoneEvent.onError = {
                event.onError(it)
                clearBindPhoneEvent()
            }
            showBindPhone()
        }
    }


    fun onConsumePointsEvent(consumePointsEvent: ConsumePointsEvent) {
        CoroutineScope(defaultDispatcher).launch {
            val consumePoints = consumePointsEvent.consumePoints
            val response = NetworkUtils.safeApiCall {
                apiService.consumePoints(consumePoints)
            }
            response?.let {
                responseHandle(
                    response,
                    consumePointsEvent.onSuccess,
                    consumePointsEvent.onFailure,
                    true
                )
            }
        }
    }

    fun onRewardPointsEvent(event: RewardPointsEvent) {
        CoroutineScope(defaultDispatcher).launch {
            val rewardPoints = event.rewardPoints
            val response = NetworkUtils.safeApiCall {
                apiService.rewardPoints(rewardPoints)
            }
            response?.let {
                responseHandle(
                    response,
                    event.onSuccess,
                    event.onFailure,
                    true
                )
            }
        }
    }

    private fun clearBindPhoneEvent() {
        bindPhoneEvent = BindPhoneEvent()
    }

    private fun clearLoginHandle() {
        callbackHolder.clearLoginHandle()
    }

    private fun showLoginModal() {
        loginStateHolder.showLoginModal()
    }

    fun hideLoginModal() {
        loginStateHolder.hideLoginModal()
    }

    fun showRegistration() {
        loginStateHolder.updateLoginStyle(LoginStyle.REGISTRATION)
    }

    fun hideRegistration() {
        loginStateHolder.updateLoginStyle(LoginStyle.LOGIN)
    }

    fun showForgotPassword() {
        loginStateHolder.updateLoginStyle(LoginStyle.FORGOT)
    }

    fun hideForgotPassword() {
        forgotPasswordTimerJob?.cancel()
        forgotPasswordTimerJob = null
        forgotPasswordState.value = ForgotPasswordState()
        loginStateHolder.updateLoginStyle(LoginStyle.LOGIN)
    }

    private fun responseHandle(
        response: Response<Login>,
        onSuccess: (str: Response<Login>) -> Unit = { _ -> },
        onFail: (str: String) -> Unit = { _ -> },
        onlyUpdate: Boolean = false
    ) {
        if (response.isSuccessful) {
            val loginResponse = response.body()
            if (loginResponse == null) {
                onFail(AppContext.getContext().getString(R.string.login_response_empty))
                callbackHolder.invokeFailOnce(response)
                endLogin()
                return
            }
            if (loginResponse.jwt.isNotEmpty()) {
                updateUserState(loginResponse)
                callbackHolder.invokeSuccessOnce(response)
                onSuccess(response)
                if (!onlyUpdate) {
                    endLogin()
                    detectBindPhone()
                }
            } else {
                NetworkUtils.handleErrorResponse(response, onFail)
                callbackHolder.invokeFailOnce(response)
                endLogin()
            }
        } else {
            NetworkUtils.handleErrorResponse(response, onFail)
            callbackHolder.invokeFailOnce(response)
            endLogin()
        }
    }

    private fun updateUserState(login: Login) {
        loginStateHolder.updateLoginState(login)
    }

    private fun startLogin() {
        CoroutineScope(defaultDispatcher).launch {
            loginStateHolder.setLoggingIn(true)
        }
    }

    private fun endLogin() {
        CoroutineScope(defaultDispatcher).launch {
            loginStateHolder.setLoggingIn(false)
        }
    }

    private fun wechatLogin(
        code: String,
        onSuccess: (str: Response<Login>) -> Unit = { _ -> },
        onFail: (str: String) -> Unit = { _ -> }
    ) {
        CoroutineScope(defaultDispatcher).launch {
            val response = NetworkUtils.safeApiCall(
                onError = { errorMsg ->
                    onFail(errorMsg)
                    endLogin()
                }
            ) {
                apiService.wechatLogin(
                    loginRequest = WechatLoginRequest(
                        Wechat.appId,
                        code
                    )
                )
            }
            if (response == null) {
                endLogin()
                return@launch
            }
            responseHandle(response, onSuccess, onFail)
        }
    }

    private fun login(
        identifier: String,
        password: String,
        onSuccess: (str: Response<Login>) -> Unit = { _ -> },
        onFail: (str: String) -> Unit = { _ -> }
    ) {
        CoroutineScope(defaultDispatcher).launch {
            startLogin()
            val response = NetworkUtils.safeApiCall(
                onError = { errorMsg ->
                    onFail(errorMsg)
                    endLogin()
                }
            ) {
                // 统一走 go-proxy 的 /api/login（支持邮箱或用户名），
                // 不再请求 Strapi 原生的 /api/auth/local
                apiService.loginUnified(
                    loginRequest = LoginRequest(
                        identifier,
                        password
                    )
                )
            }
            if (response == null) {
                endLogin()
                return@launch
            }
            responseHandle(response, onSuccess, onFail)
        }
    }

    private fun loginByCode(
        code: String,
        onSuccess: (str: Response<Login>) -> Unit = { _ -> },
        onFail: (str: String) -> Unit = { _ -> }
    ) {
        CoroutineScope(defaultDispatcher).launch {
            startLogin()
            val response = NetworkUtils.safeApiCall {
                apiService.wechatPublicAccountLogin(code.toInt())
            }
            if (response == null) {
                endLogin()
                return@launch
            } else {
                responseHandle(response, onSuccess, onFail)
            }
        }
    }

    private fun register(
        username: String,
        email: String,
        password: String,
        onSuccess: (str: Response<Login>) -> Unit = { _ -> },
        onFail: (str: String) -> Unit = { _ -> }
    ) {
        CoroutineScope(defaultDispatcher).launch {
            startLogin()
            val response = NetworkUtils.safeApiCall(
                onError = { errorMsg ->
                    onFail(errorMsg)
                    endLogin()
                }
            ) {
                apiService.register(
                    loginRequest = RegisterRequest(
                        username,
                        email,
                        password
                    )
                )
            }
            if (response == null) {
                endLogin()
                return@launch
            }
            responseHandle(response, onSuccess, onFail)
        }
    }

    /**
     * 邮箱注册不再要求用户单独填写用户名：
     * 用邮箱前缀生成候选用户名，若与已有用户重名则追加随机数字后缀自动重试。
     * 请求体格式与旧版一致（仍携带 username），服务端无需改动，旧版 App 不受影响。
     */
    private fun registerWithEmail(
        email: String,
        password: String,
        onSuccess: (str: Response<Login>) -> Unit = { _ -> },
        onFail: (str: String) -> Unit = { _ -> },
        attempt: Int = 0
    ) {
        val baseUsername = email.substringBefore('@')
            .replace(Regex("[^A-Za-z0-9_]"), "")
            .ifBlank { DEFAULT_USERNAME_PREFIX }
        val username = if (attempt == 0) {
            baseUsername
        } else {
            "$baseUsername${(1000..9999).random()}"
        }
        register(
            username = username,
            email = email,
            password = password,
            onSuccess = onSuccess,
            onFail = { error ->
                if (attempt < MAX_USERNAME_RETRY &&
                    error.contains("Username already taken", ignoreCase = true)
                ) {
                    registerWithEmail(email, password, onSuccess, onFail, attempt + 1)
                } else {
                    onFail(error)
                }
            }
        )
    }

    private fun forgotPassword(email: String) {
        CoroutineScope(defaultDispatcher).launch {
            val response = NetworkUtils.safeApiCall {
                apiService.forgotPassword(ForgotPasswordRequest(email))
            }
            if (response != null && response.isSuccessful) {
                ActionUtils.showToast(R.string.forgot_password_code_sent)
                forgotPasswordState.value = forgotPasswordState.value.copy(isCodeSent = true)
            } else if (response != null) {
                NetworkUtils.handleErrorResponse(response) {
                    ActionUtils.showError(it)
                }
            } else {
                ActionUtils.showToast(R.string.login_error_toast)
            }
        }
    }

    private fun resetPassword(email: String, code: String, newPassword: String) {
        CoroutineScope(defaultDispatcher).launch {
            val response = NetworkUtils.safeApiCall {
                apiService.resetPassword(
                    ResetPasswordRequest(email, code, newPassword)
                )
            }
            if (response != null && response.isSuccessful) {
                forgotPasswordState.value = forgotPasswordState.value.copy(isResetSuccessful = true)
            } else if (response != null) {
                NetworkUtils.handleErrorResponse(response) {
                    ActionUtils.showError(it)
                }
            } else {
                ActionUtils.showToast(R.string.login_error_toast)
            }
        }
    }

    fun onForgotPasswordUiEvent(event: ForgotPasswordUiEvent) {
        when (event) {
            is ForgotPasswordUiEvent.EmailChanged -> {
                forgotPasswordState.value = forgotPasswordState.value.copy(
                    email = event.inputValue,
                    errorState = forgotPasswordState.value.errorState.copy(
                        emailErrorState = if (event.inputValue.trim().isEmpty()) {
                            forgotPasswordEmailEmptyErrorState
                        } else {
                            ErrorState()
                        }
                    )
                )
            }

            is ForgotPasswordUiEvent.CodeChanged -> {
                forgotPasswordState.value = forgotPasswordState.value.copy(
                    code = event.inputValue,
                    errorState = forgotPasswordState.value.errorState.copy(
                        codeErrorState = if (event.inputValue.trim().isEmpty()) {
                            forgotPasswordCodeEmptyErrorState
                        } else {
                            ErrorState()
                        }
                    )
                )
            }

            is ForgotPasswordUiEvent.NewPasswordChanged -> {
                forgotPasswordState.value = forgotPasswordState.value.copy(
                    newPassword = event.inputValue,
                    errorState = forgotPasswordState.value.errorState.copy(
                        newPasswordErrorState = if (event.inputValue.trim().isEmpty()) {
                            forgotPasswordNewPasswordEmptyErrorState
                        } else {
                            ErrorState()
                        }
                    )
                )
            }

            is ForgotPasswordUiEvent.ConfirmPasswordChanged -> {
                forgotPasswordState.value = forgotPasswordState.value.copy(
                    confirmPassword = event.inputValue,
                    errorState = forgotPasswordState.value.errorState.copy(
                        confirmPasswordErrorState = when {
                            event.inputValue.trim().isEmpty() -> {
                                forgotPasswordNewPasswordEmptyErrorState
                            }
                            forgotPasswordState.value.newPassword.trim() != event.inputValue.trim() -> {
                                forgotPasswordConfirmPasswordMismatchErrorState
                            }
                            else -> ErrorState()
                        }
                    )
                )
            }

            is ForgotPasswordUiEvent.SendCode -> {
                val email = forgotPasswordState.value.email.trim()
                if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    forgotPasswordState.value = forgotPasswordState.value.copy(
                        errorState = forgotPasswordState.value.errorState.copy(
                            emailErrorState = forgotPasswordEmailEmptyErrorState
                        )
                    )
                    return
                }
                forgotPassword(email)
                forgotPasswordTimerJob?.cancel()
                forgotPasswordState.value = forgotPasswordState.value.copy(countdown = 60)
                forgotPasswordTimerJob = CoroutineScope(defaultDispatcher).launch {
                    while (forgotPasswordState.value.countdown > 0) {
                        kotlinx.coroutines.delay(1000L)
                        forgotPasswordState.value = forgotPasswordState.value.copy(
                            countdown = forgotPasswordState.value.countdown - 1
                        )
                    }
                }
            }

            is ForgotPasswordUiEvent.Submit -> {
                val state = forgotPasswordState.value
                val email = state.email.trim()
                val code = state.code.trim()
                val newPassword = state.newPassword.trim()
                val confirmPassword = state.confirmPassword.trim()

                val emailError = if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    forgotPasswordEmailEmptyErrorState
                } else ErrorState()

                val codeError = if (code.isEmpty()) forgotPasswordCodeEmptyErrorState else ErrorState()
                val passwordError = if (newPassword.isEmpty()) forgotPasswordNewPasswordEmptyErrorState else ErrorState()
                val confirmError = when {
                    confirmPassword.isEmpty() -> forgotPasswordNewPasswordEmptyErrorState
                    newPassword != confirmPassword -> forgotPasswordConfirmPasswordMismatchErrorState
                    else -> ErrorState()
                }

                if (emailError.hasError || codeError.hasError || passwordError.hasError || confirmError.hasError) {
                    forgotPasswordState.value = state.copy(
                        errorState = state.errorState.copy(
                            emailErrorState = emailError,
                            codeErrorState = codeError,
                            newPasswordErrorState = passwordError,
                            confirmPasswordErrorState = confirmError
                        )
                    )
                    return
                }

                resetPassword(email, code, newPassword)
            }
        }
    }

    fun applyInvitationCode(
        invitationCode: String,
        onSuccess: () -> Unit,
        onFail: (String) -> Unit
    ) {
        CoroutineScope(defaultDispatcher).launch {
            val lastPoints = loginState.value.points
            val response = NetworkUtils.safeApiCall(
                onError = { errorMsg ->
                    onFail(errorMsg)
                }
            ) {
                apiService.applyInvitationCode(
                    userInviteRequest = UserInviteRequest(
                        invitationCode
                    )
                )
            }
            if (response == null) {
                return@launch
            }
            responseHandle(
                response,
                onSuccess = {
                    onSuccess()
                    val addPoints = loginState.value.points - lastPoints
                    if (addPoints > 0) {
                        ActionUtils.showToast(
                            AppContext.getContext().getString(
                                R.string.profile_invitation_code_success,
                                addPoints.toString()
                            )
                        )
                    }
                },
                onFail = {
                    onFail(it)
                },
                onlyUpdate = true
            )
        }
    }

    fun alipayReturnVerify(
        alipayResult: AlipayResult,
        onSuccess: () -> Unit,
        onFail: (String) -> Unit
    ) {
        CoroutineScope(defaultDispatcher).launch {
            val response = NetworkUtils.safeApiCall(
                onError = { errorMsg ->
                    onFail(errorMsg)
                }
            ) {
                apiService.alipayReturnVerify(
                    alipayResult = alipayResult,
                )
            }
            if (response == null) {
                onFail(appContext.getString(R.string.error_message))
                return@launch
            }
            responseHandle(
                response,
                onSuccess = {
                    onSuccess()
                },
                onFail = {
                    onFail(it)
                },
                onlyUpdate = true
            )
        }
    }

    fun wechatReturnVerify(
        payResult: WechatPayResult,
        onSuccess: () -> Unit,
        onFail: (String) -> Unit
    ) {
        CoroutineScope(defaultDispatcher).launch {
            val response = NetworkUtils.safeApiCall(
                onError = { errorMsg ->
                    onFail(errorMsg)
                }
            ) {
                apiService.wechatReturnVerify(
                    payRequest = WechatPayQueryRequest(
                        transactionId = payResult.transaction,
                        openId = payResult.openId,
                        outTradeNo = payResult.outTradeNo
                    )
                )
            }
            if (response == null) {
                return@launch
            }
            responseHandle(
                response,
                onSuccess = {
                    onSuccess()
                },
                onFail = {
                    onFail(it)
                },
                onlyUpdate = true
            )
        }
    }


    /**
     * Function called on any login event [LoginUiEvent]
     */
    fun onUiEvent(loginUiEvent: LoginUiEvent) {
        CoroutineScope(defaultDispatcher).launch {
            when (loginUiEvent) {
                // Email/Mobile changed
                is LoginUiEvent.EmailOrMobileChanged -> {
                    loginStateHolder.update { state ->
                        state.copy(
                            emailOrMobile = loginUiEvent.inputValue,
                            errorState = state.errorState.copy(
                                emailOrMobileErrorState = if (loginUiEvent.inputValue.trim()
                                        .isNotEmpty()
                                ) {
                                    ErrorState()
                                } else {
                                    emailOrMobileEmptyErrorState
                                }
                            )
                        )
                    }
                }

                // Password changed
                is LoginUiEvent.PasswordChanged -> {
                    loginStateHolder.update { state ->
                        state.copy(
                            password = loginUiEvent.inputValue,
                            errorState = state.errorState.copy(
                                passwordErrorState = if (loginUiEvent.inputValue.trim().isNotEmpty()) {
                                    ErrorState()
                                } else {
                                    passwordEmptyErrorState
                                }
                            )
                        )
                    }
                }

                is LoginUiEvent.Submit -> {
                    val inputsValidated = validateInputs()
                    if (inputsValidated) {
                        login(
                            identifier = loginState.value.emailOrMobile,
                            password = loginState.value.password,
                            onFail = {
                                ActionUtils.showToast(R.string.login_error_toast)
                            },
                            onSuccess = {
                                Log.d("LoginUiEvent.onSuccess", it.toString())
                            }
                        )
                    }
                }

                is LoginUiEvent.VerificationCodeChanged -> {
                    loginStateHolder.update { state ->
                        state.copy(
                            verificationCode = loginUiEvent.inputValue,
                            errorState = state.errorState.copy(
                                verificationCodeErrorState = validateCode(loginUiEvent.inputValue)
                            )
                        )
                    }
                }

                is LoginUiEvent.SubmitCode -> {
                    val inputsValidated = validateCodeInputs()
                    if (inputsValidated) {
                        loginByCode(
                            loginState.value.verificationCode,
                            onFail = {
                                ActionUtils.showToast(it)
                            },
                        )
                    }

                }
            }
        }
    }

    private fun validateCode(inputValue: String): ErrorState {
        return when {
            inputValue.trim().isEmpty() -> {
                codeEmptyErrorState
            }

            inputValue.trim().length > 6 -> {
                codeLengthErrorState
            }

            !inputValue.all { it.isDigit() } -> {
                codeIsDigitErrorState
            }

            else -> {
                ErrorState()
            }
        }
    }

    /**
     * Function to validate inputs
     * Ideally it should be on domain layer (use case)
     * @return true -> inputs are valid
     * @return false -> inputs are invalid
     */
    private fun validateInputs(): Boolean {
        val emailOrMobileString = loginState.value.emailOrMobile.trim()
        val passwordString = loginState.value.password
        return when {

            // Email/Mobile empty
            emailOrMobileString.isEmpty() -> {
                loginStateHolder.update {
                    it.copy(errorState = LoginErrorState(
                        emailOrMobileErrorState = emailOrMobileEmptyErrorState
                    ))
                }
                false
            }

            //Password Empty
            passwordString.isEmpty() -> {
                loginStateHolder.update {
                    it.copy(errorState = LoginErrorState(
                        passwordErrorState = passwordEmptyErrorState
                    ))
                }
                false
            }

            // No errors
            else -> {
                loginStateHolder.update { it.copy(errorState = LoginErrorState()) }
                true
            }
        }
    }

    private fun validateCodeInputs(): Boolean {
        val verificationCodeString = loginState.value.verificationCode.trim()
        return when {
            // Code empty
            verificationCodeString.isEmpty() -> {
                loginStateHolder.update {
                    it.copy(errorState = LoginErrorState(
                        verificationCodeErrorState = codeEmptyErrorState
                    ))
                }
                false
            }

            verificationCodeString.length != 6 -> {
                loginStateHolder.update {
                    it.copy(errorState = LoginErrorState(
                        verificationCodeErrorState = codeLengthErrorState
                    ))
                }
                false
            }

            verificationCodeString.any { !it.isDigit() } -> {
                loginStateHolder.update {
                    it.copy(errorState = LoginErrorState(
                        verificationCodeErrorState = codeIsDigitErrorState
                    ))
                }
                false
            }

            // No errors
            else -> {
                loginStateHolder.update {
                    it.copy(errorState = LoginErrorState(
                        verificationCodeErrorState = ErrorState()
                    ))
                }
                true
            }
        }
    }

    fun loginExit() {
        loginStateHolder.clearLoginState()
        onSuccessHandle = {}
        onFailHandle = {}
    }

    private fun showLoginAndCallback(
        onSuccess: (response: Response<Login>) -> Unit = { _ -> },
        onFail: (response: Response<Login>) -> Unit = { _ -> },
    ) {
        onSuccessHandle = {
            onSuccess(it)
        }
        onFailHandle = {
            onFail(it)
        }
        showLoginModal()
    }


    /**
     * Function called on any login event [RegistrationUiEvent]
     */
    fun onRegistrationUiEvent(registrationUiEvent: RegistrationUiEvent) {
        when (registrationUiEvent) {

            // Email id changed event
            is RegistrationUiEvent.EmailChanged -> {
                registrationState.value = registrationState.value.copy(
                    emailId = registrationUiEvent.inputValue,
                    errorState = registrationState.value.errorState.copy(
                        emailIdErrorState = if (registrationUiEvent.inputValue.trim().isEmpty()) {
                            // Email id empty state
                            emailEmptyErrorState
                        } else {
                            // Valid state
                            ErrorState()
                        }

                    )
                )
            }

            // User Name changed event
            is RegistrationUiEvent.UsernameChanged -> {
                registrationState.value = registrationState.value.copy(
                    username = registrationUiEvent.inputValue,
                    errorState = registrationState.value.errorState.copy(
                        usernameErrorState = if (registrationUiEvent.inputValue.trim()
                                .isEmpty()
                        ) {
                            // Mobile Number Empty state
                            userNameEmptyErrorState
                        } else {
                            // Valid state
                            ErrorState()
                        }

                    )
                )
            }

            // Password changed event
            is RegistrationUiEvent.PasswordChanged -> {
                registrationState.value = registrationState.value.copy(
                    password = registrationUiEvent.inputValue,
                    errorState = registrationState.value.errorState.copy(
                        passwordErrorState = if (registrationUiEvent.inputValue.trim().isEmpty()) {
                            // Password Empty state
                            passwordEmptyErrorState
                        } else {
                            // Valid state
                            ErrorState()
                        }

                    )
                )
            }

            // Confirm Password changed event
            is RegistrationUiEvent.ConfirmPasswordChanged -> {
                registrationState.value = registrationState.value.copy(
                    confirmPassword = registrationUiEvent.inputValue,
                    errorState = registrationState.value.errorState.copy(
                        confirmPasswordErrorState = when {

                            // Empty state of confirm password
                            registrationUiEvent.inputValue.trim().isEmpty() -> {
                                confirmPasswordEmptyErrorState
                            }

                            // Password is different than the confirm password
                            registrationState.value.password.trim() != registrationUiEvent.inputValue -> {
                                passwordMismatchErrorState
                            }

                            // Valid state
                            else -> ErrorState()
                        }
                    )
                )
            }


            // Submit Registration event
            is RegistrationUiEvent.Submit -> {
                val inputsValidated = validateRegistrationInputs()
                if (inputsValidated) {
                    registerWithEmail(
                        email = registrationState.value.emailId.trim(),
                        password = registrationState.value.confirmPassword,
                        onSuccess = {
                            registrationState.value =
                                registrationState.value.copy(isRegistrationSuccessful = true)
                        },
                        onFail = {
                            registrationState.value = registrationState.value.copy(
                                errorState = RegistrationErrorState(
                                    emailIdErrorState = emailOrUsernameAlreadyTakenErrorState
                                )
                            )

                        }
                    )
                }
            }
        }
    }

    /**
     * Function to validate inputs
     * Ideally it should be on domain layer (use case)
     * @return true -> inputs are valid
     * @return false -> inputs are invalid
     */
    private fun validateRegistrationInputs(): Boolean {
        val emailString = registrationState.value.emailId.trim()
        val passwordString = registrationState.value.password.trim()
        val confirmPasswordString = registrationState.value.confirmPassword.trim()

        return when {

            // Email empty
            emailString.isEmpty() -> {
                registrationState.value = registrationState.value.copy(
                    errorState = RegistrationErrorState(
                        emailIdErrorState = emailEmptyErrorState
                    )
                )
                false
            }

            //Password Empty
            passwordString.isEmpty() -> {
                registrationState.value = registrationState.value.copy(
                    errorState = RegistrationErrorState(
                        passwordErrorState = passwordEmptyErrorState
                    )
                )
                false
            }

            //Confirm Password Empty
            confirmPasswordString.isEmpty() -> {
                registrationState.value = registrationState.value.copy(
                    errorState = RegistrationErrorState(
                        confirmPasswordErrorState = confirmPasswordEmptyErrorState
                    )
                )
                false
            }

            // Password and Confirm Password are different
            passwordString != confirmPasswordString -> {
                registrationState.value = registrationState.value.copy(
                    errorState = RegistrationErrorState(
                        confirmPasswordErrorState = passwordMismatchErrorState
                    )
                )
                false
            }

            // No errors
            else -> {
                // Set default error state
                registrationState.value =
                    registrationState.value.copy(errorState = RegistrationErrorState())
                true
            }
        }
    }

    fun reset() {
        CoroutineScope(defaultDispatcher).launch {
            if (!loginState.value.isLogin) {
                loginStateHolder.update { LoginState() }
            }
        }
    }

    fun loginByWechat() {
        CoroutineScope(defaultDispatcher).launch {
            loginStateHolder.setLoggingIn(true)
            Wechat.auth(
                URLEncoder.encode(
                    "login_screen",
                    StandardCharsets.UTF_8.toString()
                )
            )
        }
    }

    fun loginByGoogle(
        idToken: String,
        onSuccess: (Response<Login>) -> Unit = { _ -> },
        onFail: (String) -> Unit = { _ -> }
    ) {
        CoroutineScope(defaultDispatcher).launch {
            startLogin()
            val response = NetworkUtils.safeApiCall(
                onError = { errorMsg ->
                    onFail(errorMsg)
                    endLogin()
                }
            ) {
                apiService.googleLogin(
                    loginRequest = GoogleLoginRequest(idToken)
                )
            }
            if (response == null) {
                endLogin()
                return@launch
            }
            responseHandle(response, onSuccess, onFail)
        }
    }

    fun loginOut() {
        CoroutineScope(ioDispatcher).launch {
            val response = NetworkUtils.safeApiCall {
                apiService.loginOut()
            }
            if (response != null) {
                if (response.isSuccessful) {
                    loginExit()
                } else {
                    NetworkUtils.handleErrorResponse(response) {
                        ActionUtils.showError(it)
                    }
                }
            }
        }
    }

    fun showBindPhone() {
        CoroutineScope(defaultDispatcher).launch {
            loginStateHolder.showBindPhone()
        }
    }

    fun hideBind() {
        CoroutineScope(defaultDispatcher).launch {
            loginStateHolder.hideBindPhone()
        }
    }

    fun sendCode(
        value: String,
        sendType: Int = 0,
        onError: (String) -> Unit = {},
        onSuccess: () -> Unit,
    ) {
        CoroutineScope(defaultDispatcher).launch {
            val response: Response<Status>?
            if (sendType == 0) {
                response = NetworkUtils.safeApiCall(
                    onError = onError
                ) {
                    apiService.sendCode(
                        smsRequest = SMSRequest(
                            phone = value
                        )
                    )
                }
            } else {
                response = NetworkUtils.safeApiCall(
                    onError = onError
                ) {
                    apiService.sendCodeNoCheck(
                        smsRequest = SMSRequest(
                            phone = value
                        )
                    )
                }
            }
            if (response != null) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    NetworkUtils.handleErrorResponse(
                        response,
                        onFriendlyErrorTip = {
                            onError(it)
                        }) {
                        makeLog {
                            "sendCode" + it
                        }
                    }
                }
            }
        }
    }

    fun bindPhone(
        phone: String,
        code: String,
        onSuccess: (str: Response<Login>) -> Unit = { _ -> },
        onFail: (str: String) -> Unit = { _ -> }
    ) {
        CoroutineScope(defaultDispatcher).launch {
            val response = NetworkUtils.safeApiCall {
                apiService.bindPhone(
                    verifyCodeRequest = VerifyCodeRequest(
                        phone = phone,
                        code = code
                    )
                )
            }
            if (response == null) {
                return@launch
            } else {
                responseHandle(
                    response = response,
                    onSuccess = {
                        onSuccess(it)
                        bindPhoneEvent.onSuccess()
                    },
                    onFail = {
                        onFail(it)
                        bindPhoneEvent.onError(it)
                    }
                )
            }
        }
    }

    private fun detectBindPhone() {
        if (ActionUtils.isNeedBindPhone()) {
            if (loginState.value.isLogin) {
                if (loginState.value.isWechat) {
                    if (RemoteConfigStorage.getRemoteConfig().needWechatBindPhone == true) {
                        if (loginState.value.phone.trim().isEmpty()) {
                            showBindPhone()
                        }
                    }
                } else if (loginState.value.phone.trim().isEmpty()) {
                    showBindPhone()
                }
            }
        }
    }

    fun getLoginList(): List<LoginType> {
        return LoginChannelConfig.getConfigByFlavor().loginTypes
    }

    fun getLoginType(): LoginType {
        return LoginChannelConfig.getConfigByFlavor().defaultLoginType
    }

    fun loginByPhone(
        phone: String,
        code: String,
        onSuccess: (str: Response<Login>) -> Unit = { _ -> },
        onFail: (str: String) -> Unit = { _ -> }
    ) {
        CoroutineScope(defaultDispatcher).launch {
            startLogin()
            val response = NetworkUtils.safeApiCall {
                apiService.loginByPhone(
                    verifyCodeRequest = VerifyCodeRequest(
                        phone = phone,
                        code = code
                    )
                )
            }
            if (response == null) {
                endLogin()
                return@launch
            } else {
                responseHandle(response, onSuccess, onFail)
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): LoginComponent
    }
}
