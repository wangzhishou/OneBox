package com.shifenmiao.storage

import android.os.Parcelable
import com.shifenmiao.core.constants.Constants
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

object AppSharedStorage {

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.APP_SHARED)

    /**
     * 按语言隔离的 MMKV（"app_<locale>"）：存放与分库数据绑定/按语言下发的内容——
     * 条目/分类同步水位线、系统预置 prompt 版本、工具目录快照版本、远程配置检查时间。
     * 这些 key 不进 memoryCache，避免跨语言串味（MMKV mmap 直读足够快）。
     */
    private val localeMmkv: MMKV get() = localizedMmkv(MMKVName.APP_SHARED)

    private val memoryCache = mutableMapOf<String, Any>()
    private const val IS_SHOW_POINTS_TIPS = "is_show_points_tips"
    private const val BASE_POINTS = "base_points_float"
    private const val PRIVACY_POLICY_VERSION = "privacy_policy_version"
    private const val IS_ENABLE_SENSOR = "is_enable_sensor"
    private const val ROBOT_CLICK_COUNT = "robot_click_count"
    private const val LAST_UPDATED = "last_updated"
    private const val LAST_PAGE_NUMBER = "last_page_number"
    private const val ITEMS_LAST_SYNC_AT = "items_last_sync_at"
    private const val CATEGORIES_LAST_SYNC_AT = "categories_last_sync_at"
    private const val FULL_SYNC_LAST_AT = "full_sync_last_at"
    private const val LAST_KNOWN_VERSION_CODE = "last_known_version_code"
    private const val PAGE_ENTER_SYNC_AT = "page_enter_sync_at"
    private const val MINI_PROGRAM_REMEMBER_CHOICE = "mini_program_remember_choice"
    private const val IS_EXPANDED_REASONING_CHAT = "is_expanded_reasoning_chat"
    private const val IS_EXPANDED_PROMPT = "is_expanded_chat_prompt"
    private const val IS_EXPANDED_TOOL_CALL = "is_expanded_tool_call"
    private const val IS_DISABLE_ROBOT = "disable_robot"
    private const val STARTUP_TRACE_OVERLAY_ENABLED = "startup_trace_overlay_enabled"
    private const val START_ENTRY_INDEX = "start_entry_index"  // 新增启动入口索引键
    private const val START_ENTRY_SCREEN_ID = "start_entry_screen_id"
    private const val REMOTE_CONFIG_LAST_CHECK_TIME = "remote_config_last_check_time"
    private const val TOOL_CATALOG_SNAPSHOT_VERSION = "tool_catalog_snapshot_version"
    private const val SYSTEM_PRESET_VERSION = "system_preset_version"
    private const val HABIT_PRESETS_SEEDED = "habit_presets_seeded"
    private const val LANGUAGE_SWITCH_NOTICE_DISMISSED = "language_switch_notice_dismissed"

    // ─── 启动关键设置 key（DataStore → MMKV 镜像缓存） ────────────────────────
    private const val S_FONT_SCALE = "s_font_scale"
    private const val S_NIGHT_MODE = "s_night_mode"
    private const val S_DYNAMIC_COLORS = "s_dynamic_colors"
    private const val S_APP_COLOR_TUPLE = "s_app_color_tuple"
    private const val S_AMOLED_MODE = "s_amoled_mode"
    private const val S_THEME_STYLE = "s_theme_style"
    private const val S_THEME_CONTRAST_LEVEL = "s_theme_contrast_level"
    private const val S_INVERT_THEME = "s_invert_theme"
    private const val S_ALLOW_CRASHLYTICS = "s_allow_crashlytics"
    private const val S_SYSTEM_BARS_VISIBILITY = "s_system_bars_visibility"
    private const val S_IS_SYSTEM_BARS_VISIBLE_BY_SWIPE = "s_is_system_bars_visible_by_swipe"
    private const val S_SECURE_MODE = "s_secure_mode"
    private const val S_CLEAR_CACHE_ON_LAUNCH = "s_clear_cache_on_launch"
    private const val S_BORDER_WIDTH = "s_border_width"
    private const val S_SELECTED_FONT = "s_selected_font"
    /** 标记 MMKV 启动缓存是否已被初始化过（首次从 DataStore 同步后置 true） */
    private const val S_CACHE_INITIALIZED = "s_cache_initialized"

    private val _isEnableSensor = MutableStateFlow(loadIsEnableSensor())
    val isEnableSensor: StateFlow<Boolean> get() = _isEnableSensor

    private val _isShowPointsTips = MutableStateFlow(loadIsShowPointsTips())
    val isShowPointsTips: StateFlow<Boolean> get() = _isShowPointsTips

    private val _isMiniProgramRememberChoice = MutableStateFlow(loadMiniProgramRememberChoice())
    val isMiniProgramRememberChoice: StateFlow<Boolean> get() = _isMiniProgramRememberChoice

    private val _isExpandedReasoningChat = MutableStateFlow(loadIsExpandedReasoningChat())
    val isExpandedReasoningChat: StateFlow<Boolean> get() = _isExpandedReasoningChat

    private val _isExpandedPrompt = MutableStateFlow(loadIsExpandedPrompt())
    val isExpandedPrompt: StateFlow<Boolean> get() = _isExpandedPrompt

    private val _isExpandedToolCall = MutableStateFlow(loadIsExpandedToolCall())
    val isExpandedToolCall: StateFlow<Boolean> get() = _isExpandedToolCall

    private val _isDisableRobot = MutableStateFlow(loadIsDisableRobot())
    val isDisableRobot: StateFlow<Boolean> get() = _isDisableRobot

    private val _isStartupTraceOverlayEnabled = MutableStateFlow(loadStartupTraceOverlayEnabled())
    val isStartupTraceOverlayEnabled: StateFlow<Boolean> get() = _isStartupTraceOverlayEnabled

    private val _startEntryIndex = MutableStateFlow(loadStartEntryIndex())
    val startEntryIndex: StateFlow<Int> get() = _startEntryIndex

    fun saveIsDisableRobot(isDisableRobot: Boolean) {
        save(IS_DISABLE_ROBOT, isDisableRobot)
        _isDisableRobot.value = isDisableRobot
    }

    fun loadIsDisableRobot(): Boolean {
        return load(IS_DISABLE_ROBOT, false) ?: false
    }

    fun saveStartupTraceOverlayEnabled(enabled: Boolean) {
        save(STARTUP_TRACE_OVERLAY_ENABLED, enabled)
        _isStartupTraceOverlayEnabled.value = enabled
    }

    fun loadStartupTraceOverlayEnabled(): Boolean {
        return load(STARTUP_TRACE_OVERLAY_ENABLED, false) ?: false
    }

    fun saveIsExpandedPrompt(isExpandedReasoningChat: Boolean) {
        save(IS_EXPANDED_PROMPT, isExpandedReasoningChat)
        _isExpandedPrompt.value = isExpandedReasoningChat
    }

    private fun loadIsExpandedPrompt(): Boolean {
        return load(IS_EXPANDED_PROMPT, true) ?: true
    }

    fun saveIsExpandedReasoningChat(isExpandedReasoningChat: Boolean) {
        save(IS_EXPANDED_REASONING_CHAT, isExpandedReasoningChat)
        _isExpandedReasoningChat.value = isExpandedReasoningChat
    }

    private fun loadIsExpandedReasoningChat(): Boolean {
        return load(IS_EXPANDED_REASONING_CHAT, true) ?: true
    }

    fun saveIsExpandedToolCall(isExpandedToolCall: Boolean) {
        save(IS_EXPANDED_TOOL_CALL, isExpandedToolCall)
        _isExpandedToolCall.value = isExpandedToolCall
    }

    private fun loadIsExpandedToolCall(): Boolean {
        return load(IS_EXPANDED_TOOL_CALL, true) ?: true
    }


    fun saveMiniProgramRememberChoice(rememberChoice: Boolean) {
        save(MINI_PROGRAM_REMEMBER_CHOICE, rememberChoice)
        _isMiniProgramRememberChoice.value = rememberChoice
    }

    fun loadMiniProgramRememberChoice(): Boolean {
        return load(MINI_PROGRAM_REMEMBER_CHOICE, false) ?: false
    }

    fun saveLastUpdated(categoryId: Int, lastTime: Long) {
        val key = "${LAST_UPDATED}_$categoryId"
        save(key, lastTime)
    }

    fun loadLastUpdated(categoryId: Int): Long {
        val key = "${LAST_UPDATED}_$categoryId"
        return load(key, 0L) ?: 0L
    }

    fun clearLastUpdated(categoryId: Int) {
        val key = "${LAST_UPDATED}_$categoryId"
        mmkv.removeValueForKey(key)
        memoryCache.remove(key)
    }

    fun saveLastPageNumber(categoryId: Int, pageNumber: Int) {
        val key = "${LAST_PAGE_NUMBER}_$categoryId"
        save(key, pageNumber, TimeUnit.SECONDS.convert(7, TimeUnit.DAYS).toInt())
    }

    fun loadLastPageNumber(categoryId: Int): Int {
        val key = "${LAST_PAGE_NUMBER}_$categoryId"
        return load(key, 0) ?: 0
    }

    fun clearLastPageNumber(categoryId: Int) {
        val key = "${LAST_PAGE_NUMBER}_$categoryId"
        mmkv.removeValueForKey(key)
        memoryCache.remove(key)
    }

    fun saveItemsLastSyncAt(listType: Int, categoryId: Int?, timestamp: Long) {
        val key = "${ITEMS_LAST_SYNC_AT}_${listType}_${categoryId ?: 0}"
        val current = localeMmkv.decodeLong(key, 0L)
        if (timestamp > current) {
            localeMmkv.encode(key, timestamp)
        }
    }

    fun loadItemsLastSyncAt(listType: Int, categoryId: Int?): Long {
        val key = "${ITEMS_LAST_SYNC_AT}_${listType}_${categoryId ?: 0}"
        return localeMmkv.decodeLong(key, 0L)
    }

    fun saveCategoriesLastSyncAt(timestamp: Long) {
        val current = localeMmkv.decodeLong(CATEGORIES_LAST_SYNC_AT, 0L)
        if (timestamp > current) {
            localeMmkv.encode(CATEGORIES_LAST_SYNC_AT, timestamp)
        }
    }

    fun loadCategoriesLastSyncAt(): Long {
        return localeMmkv.decodeLong(CATEGORIES_LAST_SYNC_AT, 0L)
    }

    fun saveFullSyncLastAt(timestamp: Long) {
        localeMmkv.encode(FULL_SYNC_LAST_AT, timestamp)
    }

    fun loadFullSyncLastAt(): Long {
        return localeMmkv.decodeLong(FULL_SYNC_LAST_AT, 0L)
    }

    fun saveLastKnownVersionCode(versionCode: Int) {
        save(LAST_KNOWN_VERSION_CODE, versionCode)
    }

    fun loadLastKnownVersionCode(): Int {
        return load(LAST_KNOWN_VERSION_CODE, 0) ?: 0
    }

    /**
     * 进入列表页增量同步的时间戳，按 listType 维度持久化（杀进程不重置）。
     * 按语言隔离：水位线度量的是按语言分库的 item 表。
     */
    fun savePageEnterSyncAt(listType: Int, timestamp: Long) {
        localeMmkv.encode("${PAGE_ENTER_SYNC_AT}_$listType", timestamp)
    }

    fun loadPageEnterSyncAt(listType: Int): Long {
        return localeMmkv.decodeLong("${PAGE_ENTER_SYNC_AT}_$listType", 0L)
    }

    /**
     * 清空条目/分类的同步水位线与全量同步时间戳。
     * 用于 App 版本升级后强制全量重拉：服务端按 version_code 过滤条目，
     * 新版本才可见的条目其 updatedAt 可能早于本地水位线，增量同步永远拉不到。
     * 只清当前语言的水位线（水位线已按语言隔离）。
     */
    fun clearSyncTimestamps() {
        val shouldClear: (String) -> Boolean = { key ->
            key.startsWith(ITEMS_LAST_SYNC_AT) ||
                key == CATEGORIES_LAST_SYNC_AT ||
                key == FULL_SYNC_LAST_AT
        }
        val syncKeys = localeMmkv.allKeys()?.filter(shouldClear)?.toTypedArray()
        if (!syncKeys.isNullOrEmpty()) {
            localeMmkv.removeValuesForKeys(syncKeys)
        }
    }

    fun saveIsEnableSensor(isEnableSensor: Boolean) {
        save(IS_ENABLE_SENSOR, isEnableSensor)
        _isEnableSensor.value = isEnableSensor
    }

    fun loadIsEnableSensor(): Boolean {
        return load(IS_ENABLE_SENSOR, false) ?: true
    }

    fun savePrivacyPolicyVersion(version: Int) {
        save(PRIVACY_POLICY_VERSION, version)
    }

    fun loadPrivacyPolicyVersion(): Int {
        return load(PRIVACY_POLICY_VERSION, 0) ?: 0
    }

    fun saveIsShowPointsTips(isShowPointsTips: Boolean) {
        save(IS_SHOW_POINTS_TIPS, isShowPointsTips)
        _isShowPointsTips.value = isShowPointsTips
    }

    fun loadIsShowPointsTips(): Boolean {
        return load(IS_SHOW_POINTS_TIPS, false) ?: false
    }

    fun saveBasePoints(basePoints: Float) {
        save(BASE_POINTS, basePoints)
    }

    fun loadBasePoints(): Float {
        return load(BASE_POINTS, Constants.BASE_POINTS_NUM) ?: Constants.BASE_POINTS_NUM
    }

    fun saveRobotClick() {
        val count = load(ROBOT_CLICK_COUNT, 0) ?: 0
        save(ROBOT_CLICK_COUNT, count + 1)
    }

    fun loadHasRobotClicked(): Boolean {
        val count = load(ROBOT_CLICK_COUNT, 0) ?: 0
        return count > 0
    }

    fun loadIsRobotDoubleClick(): Boolean {
        val count = load(ROBOT_CLICK_COUNT, 0) ?: 0
        return count >= 2
    }

    fun saveStartEntryIndex(index: Int) {
        save(START_ENTRY_INDEX, index)
        _startEntryIndex.value = index
    }

    fun saveStartEntry(index: Int, screenId: Int) {
        saveStartEntryIndex(index)
        save(START_ENTRY_SCREEN_ID, screenId)
    }

    fun loadStartEntryIndex(): Int {
        return load(START_ENTRY_INDEX, 0) ?: 0
    }

    fun loadStartEntryScreenId(): Int? {
        return load(START_ENTRY_SCREEN_ID, Int.MIN_VALUE)
            ?.takeIf { it != Int.MIN_VALUE }
    }

    // ─── Remote Config 检查限流（按语言隔离：远程配置本身按语言下发） ─────────────

    fun saveRemoteConfigLastCheckTime(timestamp: Long = System.currentTimeMillis()) {
        localeMmkv.encode(REMOTE_CONFIG_LAST_CHECK_TIME, timestamp)
    }

    fun loadRemoteConfigLastCheckTime(): Long {
        return localeMmkv.decodeLong(REMOTE_CONFIG_LAST_CHECK_TIME, 0L)
    }

    fun shouldCheckRemoteConfig(intervalMillis: Long = 10 * 60 * 1000): Boolean {
        return System.currentTimeMillis() - loadRemoteConfigLastCheckTime() >= intervalMillis
    }

    /**
     * 工具目录快照版本号 —— 由 [com.shifenmiao.ai.agent.tool.ToolCatalogRepository.ensureSnapshot]
     * 读取, 与 [com.shifenmiao.ai.agent.tool.AgentToolRegistry.getCatalogVersion] 比对;
     * 不一致时重写 tool_catalog 表 (BUILT_IN 记录).
     *
     * 与旧的 [AI_TOOL_CATALOG_VERSION] 区别: 旧字段是给 [ToolCatalogRepository.ensureCatalogSynced]
     * (已删除的运行时缓存) 用的; 现在仅服务于导出/导入场景下的快照判断.
     */
    fun saveToolCatalogSnapshotVersion(version: Int) {
        localeMmkv.encode(TOOL_CATALOG_SNAPSHOT_VERSION, version)
    }

    fun loadToolCatalogSnapshotVersion(): Int {
        return localeMmkv.decodeInt(TOOL_CATALOG_SNAPSHOT_VERSION, 0)
    }

    fun <T> save(key: String, value: T) {
        memoryCache[key] = value as Any
        when (value) {
            is String -> mmkv.encode(key, value)
            is Int -> mmkv.encode(key, value)
            is Boolean -> mmkv.encode(key, value)
            is Float -> mmkv.encode(key, value)
            is Long -> mmkv.encode(key, value)
            is Double -> mmkv.encode(key, value.toString()) // MMKV does not directly support Double
            is ByteArray -> mmkv.encode(key, value)
            is Parcelable -> mmkv.encode(key, value)
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    fun <T> save(key: String, value: T, expireDurationInSecond: Int) {
        memoryCache[key] = value as Any
        when (value) {
            is String -> mmkv.encode(key, value, expireDurationInSecond)
            is Int -> mmkv.encode(key, value, expireDurationInSecond)
            is Boolean -> mmkv.encode(key, value, expireDurationInSecond)
            is Float -> mmkv.encode(key, value, expireDurationInSecond)
            is Long -> mmkv.encode(key, value, expireDurationInSecond)
            // MMKV does not directly support Double
            is Double -> mmkv.encode(
                key,
                value.toString(),
                expireDurationInSecond
            )

            is ByteArray -> mmkv.encode(key, value, expireDurationInSecond)
            is Parcelable -> mmkv.encode(key, value, expireDurationInSecond)
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    private inline fun <reified T> load(key: String, defaultValue: T? = null): T? {
        memoryCache[key]?.let {
            return it as? T
        }
        return when (T::class) {
            String::class -> mmkv.decodeString(key, defaultValue as? String) as? T
            Int::class -> mmkv.decodeInt(key, defaultValue as? Int ?: -1) as? T
            Boolean::class -> mmkv.decodeBool(key, defaultValue as? Boolean ?: false) as? T
            Float::class -> mmkv.decodeFloat(key, defaultValue as? Float ?: -1f) as? T
            Long::class -> mmkv.decodeLong(key, defaultValue as? Long ?: -1L) as? T
            Double::class -> mmkv.decodeString(key)
                ?.toDouble() as? T // MMKV does not directly support Double
            ByteArray::class -> mmkv.decodeBytes(key) as? T
            Parcelable::class -> {
                @Suppress("UNCHECKED_CAST")
                mmkv.decodeParcelable(key, T::class.java as Class<Parcelable>) as? T
            }
            else -> null
        }.also {
            if (it != null) memoryCache[key] = it
        }
    }

    fun clear(key: String) {
        mmkv.remove(key)
        memoryCache.remove(key)
    }

    // ─── 启动关键设置：同步 & 加载 ─────────────────────────────────────────

    /**
     * 启动缓存是否已经被初始化过（至少从 DataStore 同步过一次）。
     * 首次安装 / 升级到新版时为 false。
     */
    fun isStartupCacheInitialized(): Boolean =
        load(S_CACHE_INITIALIZED, false) ?: false

    /**
     * 将 DataStore 中的启动关键字段一次性同步到 MMKV。
     * 在 [AndroidSettingsManager.settingsState] 首次 emit 及后续变更时调用。
     *
     * @param fontScale          字体缩放
     * @param nightMode          夜间模式 ordinal
     * @param isDynamicColors    动态取色
     * @param appColorTuple      主色字符串
     * @param isAmoledMode       AMOLED 模式
     * @param themeStyle         主题风格
     * @param themeContrastLevel 主题对比度
     * @param isInvertTheme      反色模式
     * @param allowCrashlytics   允许 Crashlytics
     * @param systemBarsVisibility 系统栏可见性 ordinal
     * @param isSystemBarsVisibleBySwipe 系统栏手势
     * @param isSecureMode       安全模式
     * @param clearCacheOnLaunch 启动清缓存
     * @param borderWidth        边框宽度
     * @param selectedFont       字体字符串
     */
    fun syncStartupSettings(
        fontScale: Float?,
        nightMode: Int,
        isDynamicColors: Boolean,
        appColorTuple: String,
        isAmoledMode: Boolean,
        themeStyle: Int,
        themeContrastLevel: Double,
        isInvertTheme: Boolean,
        allowCrashlytics: Boolean,
        systemBarsVisibility: Int,
        isSystemBarsVisibleBySwipe: Boolean,
        isSecureMode: Boolean,
        clearCacheOnLaunch: Boolean,
        borderWidth: Float,
        selectedFont: String,
    ) {
        save(S_FONT_SCALE, fontScale ?: -1f)
        save(S_NIGHT_MODE, nightMode)
        save(S_DYNAMIC_COLORS, isDynamicColors)
        save(S_APP_COLOR_TUPLE, appColorTuple)
        save(S_AMOLED_MODE, isAmoledMode)
        save(S_THEME_STYLE, themeStyle)
        save(S_THEME_CONTRAST_LEVEL, themeContrastLevel)
        save(S_INVERT_THEME, isInvertTheme)
        save(S_ALLOW_CRASHLYTICS, allowCrashlytics)
        save(S_SYSTEM_BARS_VISIBILITY, systemBarsVisibility)
        save(S_IS_SYSTEM_BARS_VISIBLE_BY_SWIPE, isSystemBarsVisibleBySwipe)
        save(S_SECURE_MODE, isSecureMode)
        save(S_CLEAR_CACHE_ON_LAUNCH, clearCacheOnLaunch)
        save(S_BORDER_WIDTH, borderWidth)
        save(S_SELECTED_FONT, selectedFont)
        save(S_CACHE_INITIALIZED, true)
    }

    // ─── 单个启动设置的同步读取方法 ─────────────────────────────────────────

    fun loadStartupFontScale(): Float? {
        val v = load(S_FONT_SCALE, -1f) ?: -1f
        return if (v <= 0f) null else v
    }

    fun loadStartupNightMode(): Int =
        load(S_NIGHT_MODE, 2) ?: 2  // default: System(2)

    fun loadStartupIsDynamicColors(): Boolean =
        load(S_DYNAMIC_COLORS, true) ?: true

    fun loadStartupAppColorTuple(): String =
        load(S_APP_COLOR_TUPLE, "") ?: ""

    fun loadStartupIsAmoledMode(): Boolean =
        load(S_AMOLED_MODE, false) ?: false

    fun loadStartupThemeStyle(): Int =
        load(S_THEME_STYLE, 0) ?: 0

    fun loadStartupThemeContrastLevel(): Double =
        load(S_THEME_CONTRAST_LEVEL, 0.0) ?: 0.0

    fun loadStartupIsInvertTheme(): Boolean =
        load(S_INVERT_THEME, false) ?: false

    fun loadStartupAllowCrashlytics(): Boolean =
        load(S_ALLOW_CRASHLYTICS, true) ?: true

    fun loadStartupSystemBarsVisibility(): Int =
        load(S_SYSTEM_BARS_VISIBILITY, 0) ?: 0  // default: Auto(0)

    fun loadStartupIsSystemBarsVisibleBySwipe(): Boolean =
        load(S_IS_SYSTEM_BARS_VISIBLE_BY_SWIPE, true) ?: true

    fun loadStartupIsSecureMode(): Boolean =
        load(S_SECURE_MODE, false) ?: false

    fun loadStartupClearCacheOnLaunch(): Boolean =
        load(S_CLEAR_CACHE_ON_LAUNCH, false) ?: false

    fun loadStartupBorderWidth(): Float =
        load(S_BORDER_WIDTH, -1f) ?: -1f

    fun loadStartupSelectedFont(): String =
        load(S_SELECTED_FONT, "0") ?: "0"  // default: System(0)

    // ─── 系统预置提示词版本（按语言隔离：预置 prompt 写入各语言自己的 Room 库） ──────────

    fun loadSystemPresetVersion(): String =
        localeMmkv.decodeString(SYSTEM_PRESET_VERSION, "") ?: ""

    fun saveSystemPresetVersion(version: String) {
        localeMmkv.encode(SYSTEM_PRESET_VERSION, version)
    }

    // ─── 习惯打卡预置播种 flag ───────────────────────────────────────────────

    /** 预置习惯是否已播种过(只播一次,用户删光也不再播) */
    fun loadHabitPresetsSeeded(): Boolean =
        load(HABIT_PRESETS_SEEDED, false) ?: false

    fun saveHabitPresetsSeeded(seeded: Boolean) {
        save(HABIT_PRESETS_SEEDED, seeded)
    }

    // ─── 语言切换重启提醒 ──────────────────────────────────────────────────

    /** 语言选择页"切换语言后将重启"提醒是否已被用户勾选不再提醒（全局偏好，不随语言隔离） */
    fun loadLanguageSwitchNoticeDismissed(): Boolean =
        load(LANGUAGE_SWITCH_NOTICE_DISMISSED, false) ?: false

    fun saveLanguageSwitchNoticeDismissed(dismissed: Boolean) {
        save(LANGUAGE_SWITCH_NOTICE_DISMISSED, dismissed)
    }
}
