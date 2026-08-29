package com.t8rin.imagetoolbox.core.ui.utils

import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.domain.model.SystemBarsVisibility
import com.t8rin.imagetoolbox.core.settings.domain.model.DomainFontFamily
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import com.t8rin.imagetoolbox.core.settings.domain.model.SettingsState

/**
 * 从 MMKV 同步读取启动关键设置，构建 partial SettingsState。
 * 覆盖影响首帧渲染和 Activity 配置的字段，其余使用 Default 值。
 * 完整状态将在 onCreate 中通过 settingsManager.settingsState Flow 异步补全。
 *
 * ComposeActivity.attachBaseContext 与 RootComponent 的初始 settingsState 共用此函数,
 * 保证 Compose 首帧也按用户上次的主题渲染, 不出现 Default → 真实主题的闪变。
 */
fun loadStartupSettingsSnapshot(): SettingsState {
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
        isGlassmorphismEnabled = AppSharedStorage.loadStartupIsGlassmorphismEnabled(),
        isLiquidGlassEnabled = AppSharedStorage.loadStartupIsLiquidGlassEnabled(),
        isMeshGradientBackgroundEnabled = AppSharedStorage.loadStartupIsMeshGradientEnabled(),
        gradientBackgroundStyle = GradientBackgroundStyle.fromOrdinal(
            AppSharedStorage.loadStartupGradientStyle()
        ),
        glassBaseAlpha = AppSharedStorage.loadStartupGlassBaseAlpha(),
        customBackgroundImageUri = AppSharedStorage.loadStartupCustomBackgroundImageUri(),
        activeThemeId = AppSharedStorage.loadStartupActiveThemeId(),
    )
}
