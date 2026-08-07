package com.shifenmiao.app

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.arkivanov.decompose.retainedComponent
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.model.event.PermissionRequest
import com.wanbaohe.app.AppContent
import com.wanbaohe.core.ui.review.InAppReviewPrompt
import com.wanbaohe.visual.automation.service.CurrentActivityProvider
import dagger.hilt.android.AndroidEntryPoint
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.t8rin.imagetoolbox.core.ui.utils.ComposeActivity
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import javax.inject.Inject


@AndroidEntryPoint
class AppActivity : ComposeActivity() {

    @Inject
    lateinit var rootComponentFactory: RootComponent.Factory

    @Inject
    lateinit var currentActivityProvider: CurrentActivityProvider

    private val component: RootComponent by lazy {
        retainedComponent(factory = rootComponentFactory::invoke)
    }


    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>


    private val supportsPip: Boolean by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        StartupTrace.mark("AppActivity.onCreate.enter")
        super.onCreate(savedInstanceState)
        ContextUtils.registerCurrentActivity(this)
        currentActivityProvider.setCurrentActivity(this)

        // 预热 RootComponent：在 SplashScreen 覆盖期间完成所有重量级初始化
        //（AppComponent、childStack、默认 Screen 的 NavigationChild 等），
        // 避免首帧渲染时才触发构造，阻塞 Compose 的关键路径。
        val prewarmedComponent = component

        // google 渠道:全局 APP_OPEN_COUNT 达标后自动弹一次应用内评分层(国内渠道为空实现);
        // savedInstanceState == null 避免旋转等配置变更重建时重复调度弹出逻辑(计数本身在 SettingsManager 全局维护)
        if (savedInstanceState == null) {
            InAppReviewPrompt.maybeAutoPrompt(this)
        }

        initActivityResultLauncher()
        StartupTrace.mark("AppActivity.onCreate.ready")
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
            supportsPip &&
            component.isGlobalAIChatComponentInitialized &&
            component.globalAIChatComponent.chatUIState.value.chatActive
        ) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(9, 16))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (isInPictureInPictureMode) component.appComponent.enterPipMode()
        else component.appComponent.exitPipMode()
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
        ContextUtils.clearCurrentActivity(this)
        currentActivityProvider.setCurrentActivity(null)
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
}
