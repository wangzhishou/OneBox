package com.shifenmiao.app

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.graphics.drawable.Icon
import android.util.Rational
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.arkivanov.decompose.retainedComponent
import com.shifenmiao.base.pip.VideoPipSpec
import com.shifenmiao.base.pip.VideoPipState
import com.wanbaohe.media.R as MediaR
import com.wanbaohe.media.playback.MediaPlaybackService
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.model.event.PermissionRequest
import com.wanbaohe.app.AppContent
import com.wanbaohe.core.ui.review.InAppReviewPrompt
import com.wanbaohe.core.ui.review.ReviewPromptHost
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import com.wanbaohe.visual.automation.service.CurrentActivityProvider
import dagger.hilt.android.AndroidEntryPoint
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.t8rin.imagetoolbox.core.ui.utils.ComposeActivity
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AppActivity : ComposeActivity() {

    @Inject
    lateinit var rootComponentFactory: RootComponent.Factory

    @Inject
    lateinit var currentActivityProvider: CurrentActivityProvider

    // 视频播放页登记的画中画规格：API 26-30 没有 setAutoEnterEnabled，只能在这里手动进
    @Inject
    lateinit var videoPipState: VideoPipState

    private val component: RootComponent by lazy {
        retainedComponent(factory = rootComponentFactory::invoke)
    }


    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>


    private val supportsPip: Boolean by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

    /** 画中画参数的唯一写入方：视频规格或 AI 聊天状态变化时重算一次。 */
    private val pipScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun handleIntent(intent: Intent) = component.handleDeeplinks(intent, this)

    @Composable
    override fun Content() {
        val appComponent = component.appComponent

        DisposableEffect(appComponent) {
            StartupTrace.markOnce("app_activity_content_attached", "AppActivity.Content.attached")
            lifecycle.addObserver(appComponent)
            appComponent.setActivityResultLauncher(activityResultLauncher)
            appComponent.registerShakeListener()

            onDispose {
                appComponent.unregisterShakeListener()
                lifecycle.removeObserver(appComponent)
            }
        }

        AppContent(
            rootComponent = component,
            appComponent = appComponent,
            startupPhaseController = startupPhaseController,
        )
    }

    // google 渠道:文件保存成功 / AI 回答成功 / 小游戏通关等成功时刻上报后,
    // 按累计次数+冷却节奏弹应用内评分层(国内渠道为空实现)。
    // 持有 lambda 引用是为了 onDestroy 注销时做身份校验:极端时序下(新实例 onCreate 早于
    // 旧实例 onDestroy)无条件置空会误清新实例刚注册的 handler
    private val reviewPromptHandler: (ReviewPromptTrigger) -> Unit = { trigger ->
        InAppReviewPrompt.maybePrompt(this, trigger)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        StartupTrace.mark("AppActivity.onCreate.enter")
        super.onCreate(savedInstanceState)
        ContextUtils.registerCurrentActivity(this)
        currentActivityProvider.setCurrentActivity(this)

        // 预热 RootComponent：在 SplashScreen 覆盖期间完成所有重量级初始化
        //（AppComponent、childStack、默认 Screen 的 NavigationChild 等），
        // 避免首帧渲染时才触发构造，阻塞 Compose 的关键路径。
        val prewarmedComponent = component

        ReviewPromptHost.bind(reviewPromptHandler)

        initActivityResultLauncher()
        observePictureInPicture()
        StartupTrace.mark("AppActivity.onCreate.ready")
    }

    /**
     * 观察"该不该允许自动进画中画"。
     *
     * 视频页只登记 [VideoPipState]，AI 聊天是否在进行也在这里统一看，
     * 避免两个页面各自 setPictureInPictureParams 互相覆盖（栈里可能同时挂着
     * 多个媒体页，后写的那个会赢，表现为退后台不进小窗）。
     */
    private fun observePictureInPicture() {
        if (!supportsPip) return
        pipScope.launch {
            combine(videoPipState.spec, chatActiveFlow()) { spec, chatActive ->
                spec to chatActive
            }
                // chatActiveFlow 每 500ms 发一次；不变化时不要反复 setPictureInPictureParams
                .distinctUntilChanged()
                .collect { (spec, chatActive) ->
                    applyPipParams(spec, chatActive)
                }
        }
    }

    /** 聊天组件按需创建，没创建过就当没在聊；500ms 轮询比提前拉起重量级组件便宜。 */
    private fun chatActiveFlow(): Flow<Boolean> = flow {
        while (true) {
            emit(
                if (component.isGlobalAIChatComponentInitialized) {
                    component.globalAIChatComponent.chatUIState.value.chatActive
                } else {
                    false
                }
            )
            delay(CHAT_POLL_INTERVAL_MS)
        }
    }

    private fun applyPipParams(videoSpec: VideoPipSpec?, chatActive: Boolean) {
        val builder = PictureInPictureParams.Builder()
        when {
            videoSpec != null -> {
                val (width, height) = videoSpec.aspectRatio()
                builder.setAspectRatio(Rational(width, height))
            }

            chatActive -> builder.setAspectRatio(Rational(9, 16))
        }
        if (videoSpec != null) {
            // 小窗里也能直接暂停/继续，不用先点开 App
            builder.setActions(listOf(pipToggleAction(isPlaying = videoSpec.isPlaying)))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 暂停时保留动作但不自动进小窗
            builder.setAutoEnterEnabled(videoSpec?.isPlaying == true || chatActive)
        }
        runCatching { setPictureInPictureParams(builder.build()) }
    }

    /** 画中画窗口里的播放/暂停按钮：直接投给后台播放服务。 */
    private fun pipToggleAction(isPlaying: Boolean): RemoteAction {
        val toggleIntent = Intent(this, MediaPlaybackService::class.java)
            .setAction(MediaPlaybackService.ACTION_TOGGLE)
        val pendingIntent = PendingIntent.getService(
            this,
            PIP_TOGGLE_REQUEST_CODE,
            toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val label = getString(
            if (isPlaying) MediaR.string.media_pause else MediaR.string.media_play
        )
        val icon = if (isPlaying) {
            MediaR.drawable.ic_media_notification_pause
        } else {
            MediaR.drawable.ic_media_notification_play
        }
        return RemoteAction(
            Icon.createWithResource(this, icon),
            label,
            label,
            pendingIntent,
        )
    }

    private fun initActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { _ ->
            if (component.appComponent.getRequestPermissionCode() == PermissionRequest.APK_INSTALL.code) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (packageManager.canRequestPackageInstalls()) {
                        component.appComponent.requestPermissionSuccess()
                    } else {
                        component.appComponent.requestPermissionFail()
                        ActionUtils.showToast(
                            this.getString(R.string.grant_permission_manual)
                        )
                    }
                }
            }
        }
    }

    /**
     * API 26-30 的 PiP 兜底：只有用户主动按 Home / 最近任务离开时才触发，
     * 锁屏、弹框等引起的 onPause 不会走到这里，避免误进 PiP。
     *
     * 注：API 31+ 的 setAutoEnterEnabled 监听已迁移到 [AppContent] 的 Compose 副作用中，
     * 保证只在 AIChatComponent 被懒加载创建后才启动，避免 App 冷启动时立即初始化。
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S &&
            supportsPip
        ) {
            // 视频正在播放时优先按视频比例进画中画；视频没有播放再看 AI 聊天是否在进行中
            val videoSpec = videoPipState.spec.value?.takeIf { it.isPlaying }
            if (videoSpec != null) {
                val (width, height) = videoSpec.aspectRatio()
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(width, height))
                    .build()
                enterPictureInPictureMode(params)
                return
            }
            if (component.isGlobalAIChatComponentInitialized &&
                component.globalAIChatComponent.chatUIState.value.chatActive
            ) {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(9, 16))
                    .build()
                enterPictureInPictureMode(params)
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        // AI 聊天的小窗只展示机器人；视频小窗则由播放页自己只渲染画面，
        // 不能套用 App 级 PiP 模式（那会把整个页面换成机器人）。
        videoPipState.setInPip(isInPictureInPictureMode)
        val isVideoPip = videoPipState.spec.value != null
        if (isInPictureInPictureMode && !isVideoPip) {
            component.appComponent.enterPipMode()
        } else {
            component.appComponent.exitPipMode()
        }
    }

    override fun onResume() {
        super.onResume()
        ContextUtils.registerCurrentActivity(this)
        currentActivityProvider.setCurrentActivity(this)
        component.appComponent.registerShakeListener()
    }

    override fun onPause() {
        super.onPause()
        component.appComponent.unregisterShakeListener()
        currentActivityProvider.setCurrentActivity(null)
        // PiP 进入逻辑已迁移至 onUserLeaveHint (API 26-30) 和 setAutoEnterEnabled (API 31+)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        pipScope.cancel()
        ContextUtils.clearCurrentActivity(this)
        currentActivityProvider.setCurrentActivity(null)
        ReviewPromptHost.unbind(reviewPromptHandler)
    }

    public override fun onStart() {
        super.onStart()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == component.appComponent.getRequestPermissionCode()) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                component.appComponent.requestPermissionSuccess()
            } else if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
                component.appComponent.requestPermissionFail()
                ActionUtils.showToast(
                    this.getString(R.string.grant_permission_manual)
                )
            } else {
                component.appComponent.requestPermissionFail()
            }
        }
    }

    private companion object {
        const val CHAT_POLL_INTERVAL_MS = 500L
        const val PIP_TOGGLE_REQUEST_CODE = 0x50495041
    }
}
