package com.shifenmiao.common.logic

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.base.channel.ChannelConfig
import com.shifenmiao.base.hilt.DeviceInfoModule
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.CoreUtils
import com.shifenmiao.base.utils.ImageUtils
import com.shifenmiao.base.utils.RateLimiter
import com.shifenmiao.common.BuildConfig
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.common.manager.AIEngineSyncManager
import com.shifenmiao.common.utils.StringUtils
import com.shifenmiao.core.common.EnvironmentModule
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.model.DeviceInfo
import com.shifenmiao.model.Response
import com.shifenmiao.model.ai.event.MainClickEvent
import com.shifenmiao.model.ai.event.MainShowType
import com.shifenmiao.model.event.PermissionRequest
import com.shifenmiao.model.event.RequestPermissionEvent
import com.shifenmiao.model.remote.CanShowPermission
import com.shifenmiao.model.state.RobotState
import com.shifenmiao.model.state.UIState
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.model.wechat.event.WechatEvent
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.utils.NetworkUtils
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.logger.makeLog
import com.tencent.mm.opensdk.constants.ConstantsAPI
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

class AppComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted("navigate") val onNavigate: (Screen) -> Unit,
    @Assisted("navigateReplacingCurrent") val onNavigateReplacingCurrent: (Screen) -> Unit,
    private val deviceInfoModule: DeviceInfoModule,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val apiService: ApiService,
    settingsManager: SettingsManager,
    fileController: FileController,
    private val environmentModule: EnvironmentModule,
    appDatabase: AppDatabase,
    @ApplicationContext private val applicationContext: Context,
    defaultDispatchersHolder: DispatchersHolder,
    private val aiEngineManagerLazy: Lazy<AIEngineManager>,
    private val aiEngineCatalogManagerLazy: Lazy<AIEngineCatalogManager>,
    private val aiEngineSyncManagerLazy: Lazy<AIEngineSyncManager>,
    val ttsService: com.shifenmiao.tts.service.TTSService,
    private val channelConfig: ChannelConfig,
) : CommonComponent(
    settingsManager,
    defaultDispatchersHolder,
    componentContext,
    appDatabase,
    apiService,
    fileController
),
    SensorEventListener, DefaultLifecycleObserver {

    val aiEngineManager: AIEngineManager
        get() = aiEngineManagerLazy.get()

    val aiEngineCatalogManager: AIEngineCatalogManager
        get() = aiEngineCatalogManagerLazy.get()

    val aiEngineSyncManager: AIEngineSyncManager
        get() = aiEngineSyncManagerLazy.get()

    fun onDestroy() {
        deviceInfoModule.cleanup()
    }

    // 生命周期方法的实现
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        componentScope.launch(ioDispatcher) {
            initRemoteConfig()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        onDestroy()
    }

    var requestPermissionEvent: RequestPermissionEvent? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    private var _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState

    private var _robotState = MutableStateFlow(RobotState())
    val robotState: StateFlow<RobotState> = _robotState

    private var deviceInformation by mutableStateOf<DeviceInfo?>(null)

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0
    private val postStartupInitStarted = AtomicBoolean(false)

    init {
        com.t8rin.imagetoolbox.core.domain.performance.StartupTrace.mark("AppComponent.ctor.end")
        componentScope.launch(ioDispatcher) {
            initUIState()
        }
        componentScope.launch {
            AppEventBus.wechatEvents.collect { onLoginEvent(it) }
        }
        componentScope.launch {
            AppEventBus.permissionEvents.collect { onRequestStoragePermissionEvent(it) }
        }
        componentScope.launch {
            AppEventBus.mainClickEvents.collect { onRobotClick(it) }
        }
    }

    private fun initUIState() {
        val robotIsDoubleClick = AppSharedStorage.loadIsRobotDoubleClick()
        val isDisableRobot = AppSharedStorage.loadIsDisableRobot()
        _uiState.value = _uiState.value.copy(
            topAppBarStartCollapsed = robotIsDoubleClick,
        )
        _robotState.value = _robotState.value.copy(
            isDoubleClicked = robotIsDoubleClick,
            disabled = isDisableRobot
        )
    }

    private suspend fun initRemoteConfig() {
        withContext(ioDispatcher) {
            if (CoreUtils.isShowPrivacyPolicyDialog()) {
                return@withContext
            }
            // 持久化限流：10 分钟内不重复请求（避免冷启动频繁触发）
            if (!AppSharedStorage.shouldCheckRemoteConfig(java.util.concurrent.TimeUnit.MINUTES.toMillis(5))) {
                return@withContext
            }

            val response = NetworkUtils.safeApiCall {
                apiService.updateRemoteConfig()
            }
            if (response != null && response.isSuccessful) {
                val netRemoteConfigList = response.body()
                if (netRemoteConfigList != null && netRemoteConfigList.data.isNotEmpty()) {
                    val netRemoteConfig = netRemoteConfigList.data[0].config
                    // 以本地（或默认）为底，网络字段作为 patch 合并：
                    // 服务端没下发 / 与本地一致的字段保留本地值，避免被回填成 null。
                    val currentConfig = RemoteConfigStorage.getRemoteConfig()
                    val mergedConfig = currentConfig.mergeWithNetwork(netRemoteConfig)
                    if (mergedConfig != currentConfig) {
                        RemoteConfigStorage.saveRemoteConfigToLocalStorage(mergedConfig)
                    }
                }
            }

            // 无论网络成功/失败，都用本地（或刚合并的）remote config 应用 AI 引擎默认值
            try {
                val currentConfig = RemoteConfigStorage.getRemoteConfig()
                aiEngineManagerLazy.get().applyRemoteConfigDefaults(currentConfig)
            } catch (e: Exception) {
                makeLog { "AppComponent: applyRemoteConfigDefaults failed: $e" }
            }

            // 记录本次检查时间
            AppSharedStorage.saveRemoteConfigLastCheckTime()
        }
    }

    private suspend fun initAgreeFirstLaunch() {
        withContext(ioDispatcher) {
            if (CoreUtils.isShowPrivacyPolicyDialog()) {
                return@withContext
            }
            initDeviceInfo()
            initSensor()
            initRemoteConfig()
            initUrl()
        }
    }

    fun startPostStartupInitIfNeeded() {
        if (CoreUtils.isShowPrivacyPolicyDialog()) return
        if (!postStartupInitStarted.compareAndSet(false, true)) return

        componentScope.launch(defaultDispatcher) {
            initAgreeFirstLaunch()
        }
    }

    fun agreeFirstLaunch() {
        if (!postStartupInitStarted.compareAndSet(false, true)) return

        componentScope.launch(defaultDispatcher) {
            initAgreeFirstLaunch()
        }
    }

    private suspend fun initUrl() {
        withContext(ioDispatcher) {
            if (RemoteConfigStorage.getRemoteConfig().requestUrl?.isNotEmpty() == true) {
                RemoteConfigStorage.getRemoteConfig().requestUrl?.let { environmentModule.setUrl(it) }
            } else {
                environmentModule.setUrl(UrlConstants.RELEASE_URL)
            }
        }
    }

    fun initSensor() {
        componentScope.launch(ioDispatcher) {
            if (AppSharedStorage.loadIsEnableSensor()) {
                ensureSensorInitialized()
            }
        }
    }

    fun registerShakeListener() {
        componentScope.launch(ioDispatcher) {
            if (AppSharedStorage.loadIsEnableSensor()) {
                ensureSensorInitialized()
                accelerometer?.let {
                    sensorManager?.registerListener(
                        this@AppComponent,
                        it,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            }
        }
    }

    fun unregisterShakeListener() {
        componentScope.launch(ioDispatcher) {
            sensorManager?.unregisterListener(this@AppComponent)
        }
    }

    private fun ensureSensorInitialized() {
        if (sensorManager != null && accelerometer != null) return

        sensorManager =
            applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
            if (acceleration > 5) { // Lowered threshold for shake detection
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastShakeTime > 500) { // Reduced interval to 0.5 seconds
                    lastShakeTime = currentTime
                    showAIChat()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    private suspend fun initDeviceInfo() {
        withContext(ioDispatcher) {
            deviceInfoModule.getDeviceInfo()
                .flowOn(Dispatchers.IO) // 在这里切换到 IO 线程
                .collect { response ->
                    when (response) {
                        is Response.Error -> Log.d("MainViewModel", "error:${response.error} ")
                        is Response.Success -> {
                            deviceInformation = response.data
                        }
                    }
                }
        }
    }

    fun onLoginEvent(event: WechatEvent) {
        val resp = event.message
        if (resp.type == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM) {
            _uiState.value = uiState.value.copy(
                launchMiniProResp = true
            )
        }
    }

    fun onRobotClick(event: MainClickEvent) {
        when (event.type) {
            MainShowType.ROBOT -> showAIChat()
            MainShowType.QUICK_DRAWER -> showDrawer()
            MainShowType.AI_SETTING -> showAIChatSettings()
            MainShowType.BUY_COFFEE -> {
                // 支付全关的渠道忽略购买入口; google 渠道(Play Billing)支付入口先登录, 国内渠道直接弹出
                if (channelConfig.enablePayment) {
                    if (channelConfig.enablePlayBilling) {
                        ActionUtils.showLogin(source = "RobotBuyCoffee") {
                            showBuyCoffeeDialogModalSheet()
                        }
                    } else {
                        showBuyCoffeeDialogModalSheet()
                    }
                }
            }
        }
    }

    fun onRequestStoragePermissionEvent(event: RequestPermissionEvent) {
        val shouldShowRationaleDialog = CanShowPermission.getConfigByFlavor().canShow &&
            event.permissionRequest.description.isNotEmpty()
        if (shouldShowRationaleDialog) {
            requestPermissionEvent = RequestPermissionEvent(
                permissions = event.permissions,
                permissionRequest = event.permissionRequest,
                onSuccess = event.onSuccess,
                onFailed = event.onFailed,
                onRequest = {
                    if (event.permissionRequest == PermissionRequest.APK_INSTALL) {
                        handleApkInstallPermission(event, activityResultLauncher)
                    } else {
                        event.onRequest.invoke()
                    }
                }
            )
            showRequestPermissionDialog()
        } else {
            requestPermissionEvent = event
            if (event.permissionRequest == PermissionRequest.APK_INSTALL) {
                handleApkInstallPermission(event, activityResultLauncher)
            } else {
                event.onRequest.invoke()
            }
        }
    }

    private fun handleApkInstallPermission(
        event: RequestPermissionEvent,
        activityResultLauncher: ActivityResultLauncher<Intent>?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent().apply {
                action = Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
                data = Uri.fromParts("package", applicationContext.packageName, null)
            }
            activityResultLauncher?.launch(intent)
        } else {
            event.onSuccess.invoke()
        }
    }

    private fun showRequestPermissionDialog() {
        _uiState.value = uiState.value.copy(
            showPermissionDialog = true
        )
    }

    fun shareText(value: String, onComplete: () -> Unit = {}) {
        shareProvider.shareText(value) {
            onComplete.invoke()
        }
    }

    fun requestPermissionSuccess() {
        requestPermissionEvent?.let {
            requestPermissionEvent!!.onSuccess.invoke()
            requestPermissionEvent = null
        }
    }

    fun requestPermissionFail() {
        requestPermissionEvent?.let {
            requestPermissionEvent!!.onFailed.invoke()
        }
    }

    fun getRequestPermissionCode(): Int {
        requestPermissionEvent?.let {
            return requestPermissionEvent!!.permissionRequest.code
        }
        return 0
    }

    fun resetLaunchMiniProResp() {
        _uiState.value = uiState.value.copy(
            launchMiniProResp = false
        )
    }

    fun showAIChat() {
        componentScope.launch {
            _uiState.value = uiState.value.copy(
                initAIChatAndShow = true
            )
        }
    }

    fun hideAIChat() {
        _uiState.value = uiState.value.copy(
            initAIChatAndShow = false
        )
    }

    fun enterPipMode() {
        _uiState.value = uiState.value.copy(isInPipMode = true)
    }

    fun exitPipMode() {
        _uiState.value = uiState.value.copy(isInPipMode = false)
    }

    fun showAIModelsModalSheet() {
        _uiState.value = uiState.value.copy(
            showAIModelsModalSheet = true
        )
    }

    fun hideAIModelsModalSheet() {
        _uiState.value = uiState.value.copy(
            showAIModelsModalSheet = false
        )
    }

    fun showAIChatSettings() {
        _uiState.value = uiState.value.copy(
            initAIChatAndShow = false,
            showAIModelsModalSheet = false,
        )
        onNavigate(Screen.AISettings())
    }

    fun getDeviceInfo(): DeviceInfo? {
        return deviceInformation
    }

    /**
     * 跳转到商店详情页自动更新
     */
    fun jumpToAppStoreDetailUpdate() {
        val packageName = applicationContext.packageName
        val url = "market://details?id=$packageName&th_name=self_update&th_update_delay=1"
        val intent = Intent().apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        applicationContext.startActivity(intent)
    }

    fun setActivityResultLauncher(activityResultLauncher: ActivityResultLauncher<Intent>) {
        activityResultLauncher.let {
            this.activityResultLauncher = activityResultLauncher
        }
    }

    fun setDebug() {
        environmentModule.setUrl(UrlConstants.DEBUG_URL)
    }

    fun setRelease() {
        environmentModule.setUrl(UrlConstants.RELEASE_URL)
    }

    fun isDebugEnvironment(): Boolean {
        return environmentModule.isDebug()
    }

    fun hidePromptPermissionDialog() {
        _uiState.value = uiState.value.copy(
            showPermissionDialog = false
        )
    }

    fun hideBuyCoffeeDialogModalSheet() {
        _uiState.value = uiState.value.copy(
            showBuyCoffeeDialog = false
        )
    }

    private fun showBuyCoffeeDialogModalSheet() {
        _uiState.value = uiState.value.copy(
            showBuyCoffeeDialog = true
        )
    }

    fun toggleDrawer() {
        _uiState.value = uiState.value.copy(
            showDrawer = !uiState.value.showDrawer
        )
    }

    fun showDrawer() {
        componentScope.launch {
            _uiState.value = uiState.value.copy(
                showDrawer = true
            )
        }
    }

    fun hideDrawer() {
        componentScope.launch {
            _uiState.value = uiState.value.copy(
                showDrawer = false
            )
        }
    }

    fun setRobotOffsetPercent(offsetXPercent: Float, offsetYPercent: Float) {
        componentScope.launch {
            _robotState.value = _robotState.value.copy(
                offsetXPercent = offsetXPercent,
                offsetYPercent = offsetYPercent
            )
        }
    }

    fun setIsInitialized(b: Boolean) {
        componentScope.launch {
            _robotState.value = _robotState.value.copy(
                isInitialized = b
            )
        }
    }

    fun setRobotScale(collapsedFraction: Float) {
        componentScope.launch {
            _robotState.value = _robotState.value.copy(
                scale = collapsedFraction
            )
        }
    }

    fun toggleRobotVisibility(b: Boolean) {
        componentScope.launch {
            if (b) {
                setRobotIsMenuExpanded(false)
                setRobotScale(0.6f)
            }
            if (!_robotState.value.hasBeenDragged) {
                _robotState.value = _robotState.value.copy(
                    visible = b
                )
            }
        }
    }

    /**
     * 强制显示机器人，忽略 [RobotState.hasBeenDragged] 保护。
     *
     * 专门用于 AI 流式输出期间用户退出聊天界面的场景：
     * 此时不管 AppBar 当前是否折叠、用户是否曾拖动过机器人，
     * 都应当让机器人可见，以便用户看到 AI 回复进度。
     */
    fun forceShowRobotForStreaming() {
        componentScope.launch {
            if (!_robotState.value.disabled) {
                _robotState.value = _robotState.value.copy(
                    visible = true
                )
            }
        }
    }

    fun setRobotIsDragging(b: Boolean) {
        componentScope.launch {
            _robotState.value = _robotState.value.copy(
                isDragging = b
            )
        }
    }

    fun setRobotIsMenuExpanded(b: Boolean) {
        componentScope.launch {
            if (b) {
                setRobotScale(1f)
            } else {
                setRobotScale(0.6f)
            }
        }
    }

    fun setRobotHasBeenDragged(b: Boolean) {
        componentScope.launch {
            _robotState.value = _robotState.value.copy(
                hasBeenDragged = b
            )
        }
    }

    fun setRobotIsDoubleClick(b: Boolean) {
        _robotState.value = _robotState.value.copy(
            isDoubleClicked = b
        )
    }

    fun setDisableRobot(b: Boolean) {
        AppSharedStorage.saveIsDisableRobot(b)
        _robotState.value = _robotState.value.copy(
            disabled = b
        )
    }

    fun hideBlogModalBottomSheet() {
        componentScope.launch {
            _uiState.value = uiState.value.copy(
                blogId = 0
            )
        }
    }

    fun showBlogModalBottomSheet(blogId: Int) {
        componentScope.launch {
            _uiState.value = uiState.value.copy(
                blogId = blogId
            )
        }
    }

    fun hideWebView() {
        componentScope.launch {
            _uiState.value = uiState.value.copy(
                openWebView = null
            )
        }
    }

    fun showWebView(webViewParams: WebViewParams) {
        componentScope.launch {
            _uiState.value = uiState.value.copy(
                openWebView = webViewParams
            )
        }
    }

    fun runCode(language: String, code: String) {
        when (language.lowercase(Locale.getDefault())) {
            "html" -> {
                showWebView(
                    WebViewParams(
                        baseUrl = UrlConstants.WEB_VIEW_BASE_URL,
                        title = "HTML",
                        isHtml = true,
                        htmlData = code,
                        enableSlowWholeDocumentDraw = true,
                    )
                )
            }

            "javascript" -> {
                showWebView(
                    WebViewParams(
                        baseUrl = UrlConstants.WEB_VIEW_BASE_URL,
                        title = "JavaScript",
                        isHtml = true,
                        htmlData = StringUtils.generateHtmlByJavascript(code),
                        enableSlowWholeDocumentDraw = true,
                    )
                )
            }

            else -> {
                ActionUtils.showToast("不支持的语言")
            }
        }
    }

    fun setRobotHasComputedInitialPosition(b: Boolean) {
        componentScope.launch {
            _robotState.value = _robotState.value.copy(
                hasComputedInitialPosition = b
            )
        }
    }

    /**
     * 保存代码文件
     * @param uri 文件的URI
     * @param onResult 保存结果的回调函数
     * @param codeString 代码内容的字符串
     */
    fun saveCodeFile(
        uri: Uri,
        onResult: (SaveResult) -> Unit,
        codeString: String = ""
    ) {
        codeString.takeIf { it.isNotEmpty() }?.let { data ->
            componentScope.launch {
                fileController.writeBytes(
                    uri = uri.toString(),
                    block = {
                        it.writeBytes(data.encodeToByteArray())
                    }
                ).also(onResult).onSuccess { success ->
                    registerSave(success)
                }
            }
        }
    }

    fun saveMermaidBitmapFile(
        uri: Uri,
        onResult: (SaveResult) -> Unit,
        bitmap: Bitmap,
    ) {
        componentScope.launch {
            fileController.writeBytes(
                uri = uri.toString(),
                block = {
                    it.writeBytes(ImageUtils.bitmapToByteArray(bitmap))
                }
            ).also(onResult).onSuccess { success ->
                registerSave(success)
            }
        }
    }

    fun saveMermaidFile(
        uri: Uri,
        onResult: (SaveResult) -> Unit,
        fileUri: Uri,
    ) {
        componentScope.launch {
            fileController.transferBytes(
                fromUri = fileUri.toString(),
                toUri = uri.toString()
            ).also(onResult).onSuccess { success ->
                registerSave(success)
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            @Assisted("navigate") onNavigate: (Screen) -> Unit,
            @Assisted("navigateReplacingCurrent") onNavigateReplacingCurrent: (Screen) -> Unit,
        ): AppComponent
    }
}
