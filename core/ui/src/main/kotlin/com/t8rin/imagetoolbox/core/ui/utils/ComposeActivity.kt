/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.core.ui.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.di.entryPoint
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.model.SystemBarsVisibility
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.t8rin.imagetoolbox.core.domain.remote.AnalyticsManager
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.FileController.Companion.toMetadataProvider
import com.t8rin.imagetoolbox.core.domain.startup.StartupPhase
import com.t8rin.imagetoolbox.core.domain.startup.StartupPhaseController
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.t8rin.imagetoolbox.core.settings.di.SettingsStateEntryPoint
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.settings.domain.model.DomainFontFamily
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import com.t8rin.imagetoolbox.core.settings.domain.model.SettingsState
import com.t8rin.imagetoolbox.core.settings.domain.toSimpleSettingsInteractor
import com.t8rin.imagetoolbox.core.settings.presentation.model.asColorTuple
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.adjustFontSize
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalMetadataProvider
import com.t8rin.imagetoolbox.core.ui.utils.provider.setContentWithWindowSizeClass
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
abstract class ComposeActivity : AppCompatActivity() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var dispatchersHolder: DispatchersHolder

    @Inject
    lateinit var fileController: FileController

    @Inject
    lateinit var startupPhaseController: StartupPhaseController

    private lateinit var settingsManager: SettingsManager

    private val activityScope: CoroutineScope
        get() = lifecycleScope + dispatchersHolder.defaultDispatcher

    private val windowInsetsController: WindowInsetsControllerCompat?
        get() = window?.let {
            WindowCompat.getInsetsController(it, it.decorView)
        }

    private val _settingsState = mutableStateOf(SettingsState.Default)
    private val settingsState: SettingsState by _settingsState


    private var contentSet: Boolean = false

    private var splashScreenAboutToExit: Boolean = false

    @Composable
    abstract fun Content()

    open fun onFirstLaunch() = handleIntent(intent)

    open fun handleIntent(intent: Intent) = Unit

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun attachBaseContext(newBase: Context) {
        StartupTrace.mark("ComposeActivity.attachBaseContext.enter")
        newBase.entryPoint<SettingsStateEntryPoint> {
            this@ComposeActivity.settingsManager = this.settingsManager
            // 使用 MMKV 同步读取启动关键设置，避免 runBlocking 阻塞主线程
            _settingsState.update {
                loadStartupSettingsFromMMKV()
            }
            handleSystemBarsBehavior()
            handleSecureMode()
        }
        val baseConfig = newBase.resources.configuration
        val newOverride = Configuration(baseConfig).apply {
            setLocales(baseConfig.locales)
        }
        settingsState.fontScale?.let {
            newOverride.fontScale = it
            AppTheme.fontScale = it
        }
        applyOverrideConfiguration(newOverride)
        super.attachBaseContext(newBase)
        StartupTrace.mark("ComposeActivity.attachBaseContext.exit")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setupSplashScreen(splashScreen, savedInstanceState == null)

        settingsManager
            .settingsState
            .onEach { state ->
                _settingsState.update { state }
                handleSystemBarsBehavior()
                handleSecureMode()
                updateFirebaseParams()
                applyDynamicColors()
                adjustFontSize(state.fontScale)
            }
            .launchIn(activityScope)

        if (savedInstanceState == null) onFirstLaunch()

        // Registering the SplashScreen exit listener before setContent keeps the
        // fast parallel render path without racing the first pre-draw on slower OEM ROMs.
        renderContent()
    }

    @Volatile
    private var splashScreenRemoved = false

    private fun setupSplashScreen(
        splashScreen: SplashScreen,
        shouldCheckAnimation: Boolean
    ) {
        if (shouldCheckAnimation && AppContext.isAppColdStart) {
            AppContext.isAppColdStart = false

            // Fallback for OEM ROMs where setOnExitAnimationListener never fires.
            // It does not delay rendering; it only prevents a permanently stuck splash.
            lifecycleScope.launch {
                delay(SPLASH_FALLBACK_TIMEOUT_MS.milliseconds)
                if (!splashScreenAboutToExit) {
                    forceRemoveSplashViews()
                }
            }

            // Cross-fade transition from the splash screen to the main content.
            // Content is rendered immediately after this listener is registered.
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenAboutToExit = true
                performSplashScreenExitAnimation(splashScreenView)

                // Hard deadline for cases where the platform/compat view animator stalls.
                lifecycleScope.launch {
                    delay(SPLASH_MAX_EXIT_DURATION_MS.milliseconds)
                    if (!splashScreenRemoved) {
                        splashScreenView.remove()
                        markSplashRemoved()
                    }
                }
            }
        }

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Fix for three-button nav not properly going edge-to-edge.
            // See: https://issuetracker.google.com/issues/298296168
            window.isNavigationBarContrastEnforced = false
        }
    }

    /**
     * Forcefully detach any leftover compat splash views from the DecorView and
     * advance the startup phase. Called from the fallback timeout when
     * setOnExitAnimationListener never fires, and from the safety net when the
     * exit animation does not complete.
     *
     * The compat layout exposes only the icon id, so the containing splash
     * FrameLayout is removed through the icon parent instead of scanning
     * arbitrary class names in the whole view tree.
     */
    private fun forceRemoveSplashViews() {
        if (splashScreenRemoved) return
        window.decorView
            .findViewById<View>(androidx.core.splashscreen.R.id.splashscreen_icon_view)
            ?.parent
            ?.let { splashView ->
                val splashContainer = splashView as? View ?: return@let
                val splashViewParent = splashContainer.parent as? ViewGroup
                splashViewParent?.removeView(splashContainer)
            }
        markSplashRemoved()
    }

    private fun markSplashRemoved() {
        splashScreenRemoved = true
        startupPhaseController.advanceTo(StartupPhase.SHELL_VISIBLE)
    }

    /**
     * Animate the splash screen out (fade-out only) then remove it.
     *
     * Content has already been rendered before this method is called, so this
     * method only handles the visual exit animation and phase advancement.
     */
    private fun performSplashScreenExitAnimation(
        splashScreenView: SplashScreenViewProvider
    ) {
        val iconAnimationStartMs = splashScreenView.iconAnimationStartMillis
        val duration = splashScreenView.iconAnimationDurationMillis

        // Check if the splash screen animation is actually running.
        // When the process is killed and restarted, iconAnimationStartMillis may be 0,
        // indicating that the system skipped the splash screen animation.
        val hasValidAnimation = iconAnimationStartMs > 0 && duration > 0

        val fadeOut = ObjectAnimator.ofFloat(splashScreenView.view, View.ALPHA, 1f, 0f)
        fadeOut.interpolator = DecelerateInterpolator()
        fadeOut.duration = 300L
        fadeOut.doOnEnd {
            if (!splashScreenRemoved) {
                splashScreenView.remove()
                markSplashRemoved()
            }
        }

        if (hasValidAnimation) {
            // Normal case: splash screen animation is running, sync with it
            val now = System.currentTimeMillis()
            lifecycleScope.launch {
                val animationDelay = duration - (now - iconAnimationStartMs) - 300
                if (animationDelay > 0) {
                    delay(animationDelay.milliseconds)
                }
                fadeOut.start()
            }
        } else {
            // Edge case: splash screen animation was skipped (e.g., after process kill)
            fadeOut.start()
        }
    }

    protected open fun renderContent() {
        if (contentSet) {
            return
        }
        startupPhaseController.advanceTo(StartupPhase.SPLASH_RENDERING)
        setContentWithWindowSizeClass {
            CompositionLocalProvider(
                LocalSimpleSettingsInteractor provides settingsManager.toSimpleSettingsInteractor(),
                LocalMetadataProvider provides fileController.toMetadataProvider(),
                content = ::Content
            )
        }
        contentSet = true
        // Remove the window background to eliminate 1x overdraw now that
        // Compose is rendering its own opaque surface.
        //
        // This is safe to call while the splash screen is still showing:
        // the compat splash screen has its OWN View layer on the DecorView
        // (with its own background + icon), completely independent of the
        // Window background. On API 31+ the system compositor renders the
        // splash, which is also independent of the app's Window background.
        window.setBackgroundDrawable(null)
    }

    fun applyDynamicColors() {
        val colorTuple = settingsState.appColorTuple.asColorTuple()
        DynamicColors.applyToActivityIfAvailable(
            this@ComposeActivity,
            DynamicColorsOptions.Builder()
                .setContentBasedSource(colorTuple.primary.toArgb())
                .build()
        )
    }

    /**
     * 从 MMKV 同步读取启动关键设置，构建 partial SettingsState。
     * 仅覆盖影响首帧渲染和 Activity 配置的字段，其余使用 Default 值。
     * 完整状态将在 onCreate 中通过 settingsManager.settingsState Flow 异步补全。
     */
    private fun loadStartupSettingsFromMMKV(): SettingsState {
        val default = SettingsState.Default
        return default.copy(
            fontScale = AppSharedStorage.loadStartupFontScale(),
            nightMode = NightMode.fromOrdinal(AppSharedStorage.loadStartupNightMode())
                ?: default.nightMode,
            isDynamicColors = AppSharedStorage.loadStartupIsDynamicColors(),
            appColorTuple = AppSharedStorage.loadStartupAppColorTuple(),
            isAmoledMode = AppSharedStorage.loadStartupIsAmoledMode(),
            themeStyle = AppSharedStorage.loadStartupThemeStyle(),
            themeContrastLevel = AppSharedStorage.loadStartupThemeContrastLevel(),
            isInvertThemeColors = AppSharedStorage.loadStartupIsInvertTheme(),
            allowCollectCrashlytics = AppSharedStorage.loadStartupAllowCrashlytics(),
            systemBarsVisibility = SystemBarsVisibility.fromOrdinal(
                AppSharedStorage.loadStartupSystemBarsVisibility()
            ) ?: default.systemBarsVisibility,
            isSystemBarsVisibleBySwipe = AppSharedStorage.loadStartupIsSystemBarsVisibleBySwipe(),
            isSecureMode = AppSharedStorage.loadStartupIsSecureMode(),
            clearCacheOnLaunch = AppSharedStorage.loadStartupClearCacheOnLaunch(),
            borderWidth = AppSharedStorage.loadStartupBorderWidth(),
            font = DomainFontFamily.fromString(AppSharedStorage.loadStartupSelectedFont())
                ?: default.font,
        )
    }

    private fun updateFirebaseParams() = analyticsManager.apply {
        updateAllowCollectCrashlytics(settingsState.allowCollectCrashlytics)
        updateAnalyticsCollectionEnabled(settingsState.allowCollectAnalytics)
    }

    private var recreationJob: Job? by smartJob()

    override fun recreate() {
        recreationJob = activityScope.launch {
            delay(200L.milliseconds)
            runOnUiThread { super.recreate() }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) handleSystemBarsBehavior()
        handleSecureMode()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handleSecureMode()
    }

    private fun handleSystemBarsBehavior() = runOnUiThread {
        windowInsetsController?.apply {
            when (settingsState.systemBarsVisibility) {
                SystemBarsVisibility.Auto -> {
                    val orientation = resources.configuration.orientation

                    show(STATUS_BARS)

                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        hide(NAV_BARS)
                    } else {
                        show(NAV_BARS)
                    }
                }

                SystemBarsVisibility.HideAll -> {
                    hide(SYSTEM_BARS)
                }

                SystemBarsVisibility.ShowAll -> {
                    show(SYSTEM_BARS)
                }

                SystemBarsVisibility.HideNavigationBar -> {
                    show(STATUS_BARS)
                    hide(NAV_BARS)
                }

                SystemBarsVisibility.HideStatusBar -> {
                    show(NAV_BARS)
                    hide(STATUS_BARS)
                }
            }

            systemBarsBehavior = if (settingsState.isSystemBarsVisibleBySwipe) {
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }

    private fun handleSecureMode() = runOnUiThread {
        if (settingsState.isSecureMode) {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window?.clearFlags(
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }

    companion object {
        private val NAV_BARS = WindowInsetsCompat.Type.navigationBars()
        private val SYSTEM_BARS = WindowInsetsCompat.Type.systemBars()
        private val STATUS_BARS = WindowInsetsCompat.Type.statusBars()

        /**
         * Fallback timeout for removing splash views when the splash screen exit
         * listener never fires. Some devices (notably Huawei/Android 10) are slow
         * to fire the listener, so give it a short leash before force-removing.
         */
        private const val SPLASH_FALLBACK_TIMEOUT_MS = 800L

        /**
         * Hard deadline after the exit listener fires. If the fade-out animation
         * does not complete by then, the splash view is forcefully removed.
         */
        private const val SPLASH_MAX_EXIT_DURATION_MS = 1000L
    }
}
