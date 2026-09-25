package com.shifenmiao.app

import android.app.LocaleManager
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Process
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.arkivanov.decompose.DecomposeSettings
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.shifenmiao.ai.request.di.LocalLlmEntryPoint
import com.shifenmiao.app.functions.attachLogWriter
import com.shifenmiao.app.functions.injectBaseComponent
import com.shifenmiao.app.functions.registerSecurityProviders
import com.shifenmiao.app.functions.setupFlags
import com.shifenmiao.app.utils.isMain
import com.shifenmiao.base.BaseApplication
import com.shifenmiao.base.utils.CoreUtils
import com.shifenmiao.core.BuildConfig
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.model.channel.FlavorType
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
import dagger.hilt.android.EntryPointAccessors
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

    /**
     * 内存压力保命路径: 端侧 LLM Engine 持有数 GB native 权重, 系统内存紧张时
     * 主动释放比等进程被 LMK 杀掉代价小得多。
     * 触发级别: TRIM_MEMORY_MODERATE(60, 应用退到后台且系统内存紧张) 及以上
     * (含 TRIM_MEMORY_COMPLETE(80)); 前台级别(RUNNING_*)不触发, 避免打断正在进行的推理。
     * 经 LocalLlmSessionManager 释放(其 loadedModelId 缓存随之失效);
     * 国内渠道底层绑定的是 UnsupportedLocalLlmRuntime, releaseAll 为 no-op, 同一入口无需区分渠道。
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            releaseLocalLlmEngine()
        }
    }

    private fun releaseLocalLlmEngine() {
        val sessionManager = EntryPointAccessors.fromApplication(
            this,
            LocalLlmEntryPoint::class.java,
        ).localLlmSessionManager()
        CoroutineScope(Dispatchers.Default).launch {
            runCatching { sessionManager.releaseAll() }
                .onFailure { android.util.Log.w("AppApplication", "LocalLlmSessionManager.releaseAll failed", it) }
        }
    }

    private fun initializeMainProcess(needShowPrivacyPolicyDialog: Boolean) {
        // 最先初始化 AppContext: LocaleUtils 在 API 33+ 需经它直查系统 LocaleManager
        initAppContext()
        // 记录进程启动时的语言, 之后运行中语言变化会触发冷重启(见 onConfigurationChanged)
        LocaleSwitchWatcher.onProcessStart()
        applyLocaleFallbackIfNeeded()
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
            tryUploadFcmToken()
            StartupTrace.mark("background_init.completed")
        }
    }

    /**
     * 语言兜底: 用户从未手动选择过应用语言时, 按渠道锁定兜底语言。
     * 默认资源 values/ 是英文, 中文在 values-zh-rCN:
     * - 海外(google/foss): 系统语言非中文时把应用语言切到英文, 避免日/韩/法等
     *   地区用户看到中文界面。
     * - 国内: 包内只有中文(values-zh-rCN) + 默认英文两份资源, 系统语言非中文时
     *   锁 zh-CN, 否则英文系统会解析到默认英文资源, 整个界面变英文。
     * 用户一旦选过语言(应用内选择页落 LANGUAGE_USER_CHOSEN 标记——含"跟随系统";
     * 或系统侧 per-app locales 非空), 本逻辑不再干预。
     *
     * API 33+ 必须直查/直写系统 LocaleManager: AppCompatDelegate 静态方法在
     * Application.onCreate 期间(尚无 Activity delegate)读恒为空、写为 no-op——
     * 之前因此每次冷启动都误判"从未选择", 虽没改成系统存储, 但会把
     * LocaleUtils 缓存错误覆写成 "en"(strings 与数据源语言不一致的根因)。
     */
    private fun applyLocaleFallbackIfNeeded() {
        // 用户显式选择过语言(含"跟随系统")就不再干预;
        // 否则"跟随系统"的用户每次冷启动都会被强制改写语言
        if (AppSharedStorage.loadLanguageUserChosen()) return
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
        val fallbackTag = if (FlavorType.fromName().isOverseas) "en" else "zh-CN"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSystemService(LocaleManager::class.java).applicationLocales =
                android.os.LocaleList.forLanguageTags(fallbackTag)
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(fallbackTag))
        }
        // 启动期主动改写语言, 告知 watcher 避免被误判为运行中切换而触发重启
        LocaleSwitchWatcher.onLocaleOverriddenAtStartup(fallbackTag)
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

    /**
     * google 渠道 FCM token 冷启动补报 (src/google/ 源集, 反射调用)。
     * 必须在 MMKV.initialize 之后调用(只挂在 startBackgroundInitIfNeeded 里);
     * 国内 flavor 没有该类, runCatching 静默跳过。
     */
    private fun tryUploadFcmToken() {
        runCatching {
            val cls = Class.forName("com.shifenmiao.app.push.FcmTokenUploader")
            val instance = cls.getDeclaredField("INSTANCE").get(null)
            val method = cls.getMethod("tryUpload", android.content.Context::class.java)
            method.invoke(instance, this)
        }.onFailure {
            android.util.Log.w("AppApplication", "FcmTokenUploader invoke failed", it)
        }
    }

}
