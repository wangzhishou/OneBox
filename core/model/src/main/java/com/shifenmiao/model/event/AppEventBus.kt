package com.shifenmiao.model.event

import com.shifenmiao.model.ai.event.MainClickEvent
import com.shifenmiao.model.auth.RequestAuthorizationCodeEvent
import com.shifenmiao.model.login.LoginEvent
import com.shifenmiao.model.points.ConsumePointsEvent
import com.shifenmiao.model.points.RewardPointsEvent
import com.shifenmiao.model.user.event.BindPhoneEvent
import com.shifenmiao.model.wechat.event.WechatEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 启动链路单点埋点事件。供 analytics / 调试 overlay 订阅。
 */
data class StartupTraceMarkEvent(
    val stage: String,
    val totalMs: Long,
    val deltaMs: Long,
)

/**
 * 全局事件总线，替代 EventBus 库。
 *
 * 使用 MutableSharedFlow 实现，优势：
 * - 无反射扫描开销（register 0ms vs EventBus ~2-3ms）
 * - 类型安全（编译时检查）
 * - 自动取消（配合 CoroutineScope 生命周期管理）
 * - 移除 greenrobot EventBus 库依赖
 */
object AppEventBus {

    private val _wechatEvents = MutableSharedFlow<WechatEvent>(extraBufferCapacity = 8)
    val wechatEvents = _wechatEvents.asSharedFlow()
    fun emit(event: WechatEvent) = _wechatEvents.tryEmit(event)

    // 微信登录回调专用：replay=1 保证 LoginComponent (lazy) 创建后能收到
    // 从 WXEntryActivity 发出的、在 LoginComponent 初始化之前就已 emit 的事件
    private val _wechatLoginEvents = MutableSharedFlow<WechatEvent>(
        replay = 1,
        extraBufferCapacity = 4
    )
    val wechatLoginEvents = _wechatLoginEvents.asSharedFlow()
    fun emitWechatLogin(event: WechatEvent) = _wechatLoginEvents.tryEmit(event)

    private val _loginEvents = MutableSharedFlow<LoginEvent>(extraBufferCapacity = 4)
    val loginEvents = _loginEvents.asSharedFlow()
    fun emit(event: LoginEvent) = _loginEvents.tryEmit(event)

    private val _consumePointsEvents = MutableSharedFlow<ConsumePointsEvent>(extraBufferCapacity = 4)
    val consumePointsEvents = _consumePointsEvents.asSharedFlow()
    fun emit(event: ConsumePointsEvent) = _consumePointsEvents.tryEmit(event)

    private val _rewardPointsEvents = MutableSharedFlow<RewardPointsEvent>(extraBufferCapacity = 4)
    val rewardPointsEvents = _rewardPointsEvents.asSharedFlow()
    fun emit(event: RewardPointsEvent) = _rewardPointsEvents.tryEmit(event)
    private val _permissionEvents = MutableSharedFlow<RequestPermissionEvent>(extraBufferCapacity = 4)
    val permissionEvents = _permissionEvents.asSharedFlow()
    fun emit(event: RequestPermissionEvent) = _permissionEvents.tryEmit(event)

    private val _agreePrivacyPolicyEvents = MutableSharedFlow<AgreePrivacyPolicyEvent>(extraBufferCapacity = 2)
    val agreePrivacyPolicyEvents = _agreePrivacyPolicyEvents.asSharedFlow()
    fun emit(event: AgreePrivacyPolicyEvent) = _agreePrivacyPolicyEvents.tryEmit(event)

    private val _mainClickEvents = MutableSharedFlow<MainClickEvent>(extraBufferCapacity = 4)
    val mainClickEvents = _mainClickEvents.asSharedFlow()
    fun emit(event: MainClickEvent) = _mainClickEvents.tryEmit(event)

    private val _bindPhoneEvents = MutableSharedFlow<BindPhoneEvent>(extraBufferCapacity = 4)
    val bindPhoneEvents = _bindPhoneEvents.asSharedFlow()
    fun emit(event: BindPhoneEvent) = _bindPhoneEvents.tryEmit(event)

    private val _editorResultEvents = MutableSharedFlow<EditorResultEvent>(extraBufferCapacity = 4)
    val editorResultEvents = _editorResultEvents.asSharedFlow()
    fun emit(event: EditorResultEvent) = _editorResultEvents.tryEmit(event)

    private val _startupTraceMarks = MutableSharedFlow<StartupTraceMarkEvent>(extraBufferCapacity = 64)
    val startupTraceMarks = _startupTraceMarks.asSharedFlow()
    fun emitStartupTraceMark(event: StartupTraceMarkEvent) = _startupTraceMarks.tryEmit(event)

    private val _requestAuthorizationCodeEvents = MutableSharedFlow<RequestAuthorizationCodeEvent>(extraBufferCapacity = 4)
    val requestAuthorizationCodeEvents = _requestAuthorizationCodeEvents.asSharedFlow()
    fun emit(event: RequestAuthorizationCodeEvent) = _requestAuthorizationCodeEvents.tryEmit(event)
}
