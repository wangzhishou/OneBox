package com.shifenmiao.app

import android.app.LocaleManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Process
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.arkivanov.decompose.DecomposeSettings
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.shifenmiao.app.functions.attachLogWriter
import com.shifenmiao.app.functions.injectBaseComponent
import com.shifenmiao.app.functions.registerSecurityProviders
import com.shifenmiao.app.functions.setupFlags
import com.shifenmiao.app.utils.isMain
import com.shifenmiao.base.BaseApplication
import com.shifenmiao.base.utils.CoreUtils
import com.shifenmiao.core.BuildConfig
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.event.StartupTraceMarkEvent
import com.shifenmiao.model.wechat.Wechat
import com.shifenmiao.network.NetworkBuilder
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.webview.common.WebViewPool
import com.shifenmiao.app.BuildConfig as AppBuildConfig
import com.t8rin.imagetoolbox.core.crash.presentation.components.applyGlobalExceptionHandler
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.t8rin.imagetoolbox.core.domain.saving.KeepAliveService
import com.t8rin.imagetoolbox.core.utils.LocaleSwitchWatcher
import com.t8rin.imagetoolbox.core.utils.initAppContext
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@OptIn(ExperimentalDecomposeApi::class)
@HiltAndroidApp
class AppApplication : BaseApplication() {
    @Inject
    lateinit var keepAliveService: KeepAliveService
    private val backgroundInitStarted = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()

        Wechat.applyChannelEnabled(enabled = AppBuildConfig.ENABLE_WECHAT)
        NetworkBuilder.setBaseUrl(AppBuildConfig.API_BASE_URL)
        tryInvokeGoogleChannelInitializer()

        StartupTrace.addFileSink(File(cacheDir, "startup_trace.log"))
        StartupTrace.addSink { entry ->
            AppEventBus.emitStartupTraceMark(
                StartupTraceMarkEvent(
                    stage = entry.stage,
                    totalMs = entry.totalMs,
                    deltaMs = entry.deltaMs,
                )
            )
        }
        StartupTrace.begin("AppApplication.onCreate")

        if (!isMain()) {
            StartupTrace.mark("AppApplication.non_main_process_exit")
            handleNonMainProcess(false)
            return
        }

        MMKV.initialize(this)
        StartupTrace.setEnabled(AppSharedStorage.loadStartupTraceOverlayEnabled())
        StartupTrace.mark("MMKV.initialize")
        val needShowPrivacyPolicyDialog = CoreUtils.isShowPrivacyPolicyDialog()
        initializeMainProcess(needShowPrivacyPolicyDialog)
        StartupTrace.mark("AppApplication.initializeMainProcess.done")
    }

    private fun handleNonMainProcess(needShowPrivacyPolicyDialog: Boolean) {
        if (needShowPrivacyPolicyDialog) {
            Process.killProcess(Process.myPid())
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 语言在进程存活期间被切换(应用内选择或系统 per-app 语言设置):
        // Room/MMKV 按语言分库, Hilt 单例只在进程创建时注入, 需要冷重启整体切换到新语言数据源
        LocaleSwitchWatcher.onConfigurationChanged(this)
    }

    private fun initializeMainProcess(needShowPrivacyPolicyDialog: Boolean) {
        // 最先初始化 AppContext: LocaleUtils 在 API 33+ 需经它直查系统 LocaleManager
        initAppContext()
        // 记录进程启动时的语言, 之后运行中语言变化会触发冷重启(见 onConfigurationChanged)
        LocaleSwitchWatcher.onProcessStart()
        applyEnglishFallbackLocaleIfNeeded()
        DecomposeSettings.update { it.copy(duplicateConfigurationsEnabled = true) }

        setupFlags()
        applyGlobalExceptionHandler()
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        if (needShowPrivacyPolicyDialog) {
            registerAgreePrivacyPolicyEventListenerIfNeeded()
        } else {
            startBackgroundInitIfNeeded()
        }
    }

    private fun registerAgreePrivacyPolicyEventListenerIfNeeded() {
        CoroutineScope(Dispatchers.Main).launch {
            AppEventBus.agreePrivacyPolicyEvents.collect { event ->
                if (event.isAgreed) {
                    startBackgroundInitIfNeeded()
                }
            }
        }
    }

    private     fun startBackgroundInitIfNeeded() {
        if (!backgroundInitStarted.compareAndSet(false, true)) return
        StartupTrace.mark("background_init.scheduled")
        WebViewPool.init(this)
        CoroutineScope(Dispatchers.IO).launch {
            StartupTrace.mark("background_init.started")
            // Stage 2-D：Room 预热必须排在最前，否则 Wechat.register / Security 等
            // 重操作会拖慢 IO 协程，main thread 抢在前面打开 Room，AppComponent ctor
            // 付 320ms。把 Room 预热拆出来在 background init 协程第一行就跑，
            // 让 IO 路径有 ~250ms 的领先优势。
            AppDatabase.getInstanceOrCreate(this@AppApplication)
            StartupTrace.mark("background_init.room_prewarmed")
            registerSecurityProviders()
            attachLogWriter()
            if (AppBuildConfig.ENABLE_WECHAT) {
                Wechat.register(this@AppApplication, Wechat.appId)
            }
            WebViewPool.initWhenIdle()
            injectBaseComponent()
            tryInvokeGoogleChannelInitializer()
            StartupTrace.mark("background_init.completed")
        }
    }

    /**
     * 海外语言兜底: 默认资源 values/ 是中文, 系统语言非中文且用户从未手动选择过
     * 应用语言时, 首次启动直接把应用语言切到英文, 避免日/韩/法等地区用户看到中文界面。
     * 用户一旦选过语言(per-app locales 非空), 本逻辑不再干预。
     *
     * API 33+ 必须直查/直写系统 LocaleManager: AppCompatDelegate 静态方法在
     * Application.onCreate 期间(尚无 Activity delegate)读恒为空、写为 no-op——
     * 之前因此每次冷启动都误判"从未选择", 虽没改成系统存储, 但会把
     * LocaleUtils 缓存错误覆写成 "en"(strings 与数据源语言不一致的根因)。
     */
    private fun applyEnglishFallbackLocaleIfNeeded() {
        val perAppTag: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSystemService(LocaleManager::class.java)
                ?.applicationLocales
                ?.takeIf { !it.isEmpty }
                ?.toLanguageTags()
        } else {
            AppCompatDelegate.getApplicationLocales()
                .takeIf { !it.isEmpty }
                ?.toLanguageTags()
        }
        if (!perAppTag.isNullOrBlank()) return
        val systemLanguage = Resources.getSystem().configuration.locales[0]?.language
        if (systemLanguage == Locale.CHINESE.language) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSystemService(LocaleManager::class.java).applicationLocales =
                android.os.LocaleList.forLanguageTags("en")
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        }
        // 启动期主动改写语言, 告知 watcher 避免被误判为运行中切换而触发重启
        LocaleSwitchWatcher.onLocaleOverriddenAtStartup("en")
    }

    /**
     * 调用 google flavor 专属的 GoogleChannelInitializer (src/google/ 源集).
     * 用反射避免在 main 源集里硬引用 google-only 类, 国内 flavor 编译时找不到类也不报错.
     */
    private fun tryInvokeGoogleChannelInitializer() {
        runCatching {
            val cls = Class.forName("com.shifenmiao.app.channel.GoogleChannelInitializer")
            // GoogleChannelInitializer 是 Kotlin object, tryInit 是单例的实例方法,
            // 必须取 INSTANCE 作为接收者, 传 null 会抛 IllegalArgumentException
            val instance = cls.getDeclaredField("INSTANCE").get(null)
            val method = cls.getMethod("tryInit", android.content.Context::class.java)
            method.invoke(instance, this)
        }.onFailure {
            android.util.Log.w("AppApplication", "GoogleChannelInitializer invoke failed", it)
        }
    }

}
