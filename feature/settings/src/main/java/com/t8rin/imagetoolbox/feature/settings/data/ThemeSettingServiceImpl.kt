package com.t8rin.imagetoolbox.feature.settings.data

import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.settings.domain.ThemeRepository
import com.t8rin.imagetoolbox.core.settings.domain.ThemeSettingService
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.domain.model.SettingsState
import com.wanbaohe.settings.R
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeSettingServiceImpl @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val settingsManager: SettingsManager,
) : ThemeSettingService {

    override suspend fun getCurrentTheme(): AppThemePreset {
        val state = settingsManager.getSettingsState()
        return themeRepository.getAllThemes().firstOrNull { it.id == state.activeThemeId }
            ?: state.toCustomTheme()
    }

    /**
     * activeThemeId 匹配不到任何预设时(自定义/旧版预览残留), 从实际生效的
     * 设置状态重建当前主题 —— 保证"读到的当前主题"与"实际生效的配色"一致,
     * 在此基础上 copy 出的修改不会静默回滚上一次写入。
     */
    private fun SettingsState.toCustomTheme(): AppThemePreset = AppThemePreset(
        id = AppThemePreset.CUSTOM_ID,
        name = AppContext.getString(R.string.theme_preset_custom),
        colorTupleString = appColorTuple,
        nightMode = nightMode,
        isDynamicColors = isDynamicColors,
        isGlassmorphismEnabled = isGlassmorphismEnabled,
        isLiquidGlassEnabled = isLiquidGlassEnabled,
        isMeshGradientBackgroundEnabled = isMeshGradientBackgroundEnabled,
        gradientBackgroundStyle = gradientBackgroundStyle,
        glassBaseAlpha = glassBaseAlpha,
        customBackgroundImageUri = customBackgroundImageUri,
        isBuiltin = false,
    )

    override suspend fun listThemes(): List<AppThemePreset> {
        return themeRepository.getAllThemes()
    }

    override val themesSnapshot: List<AppThemePreset> get() = themeRepository.themesSnapshot

    override fun observeThemes(): Flow<List<AppThemePreset>> {
        return themeRepository.observeAllThemes()
    }

    override suspend fun switchTheme(themeId: String): AppThemePreset? {
        val theme = themeRepository.getAllThemes().firstOrNull { it.id == themeId }
            ?: return null
        settingsManager.applyThemePreset(theme)
        return theme
    }

    override suspend fun updateThemeColors(
        primary: Int?,
        secondary: Int?,
        tertiary: Int?,
        surface: Int?,
    ) {
        val current = getCurrentTheme()
        // 当前主题无有效色元组(动态取色)时, 以品牌色为基线, 避免空基线导致静默 no-op
        val parts = AppThemePreset.parseColorTuple(current.colorTupleString).takeIf { it.size == 4 }
            ?: AppThemePreset.parseColorTuple(AppThemePreset.LogoTheme.colorTupleString)
        val newPrimary = primary ?: parts.getOrNull(0) ?: return
        val newSecondary = secondary ?: parts.getOrNull(1) ?: return
        val newTertiary = tertiary ?: parts.getOrNull(2) ?: return
        val newSurface = surface ?: parts.getOrNull(3) ?: return
        val updated = current.copy(
            colorTupleString = listOf(newPrimary, newSecondary, newTertiary, newSurface)
                .joinToString("*"),
            isDynamicColors = false,
        )
        settingsManager.applyThemePreset(updated.asCustomTheme())
    }

    override suspend fun setGlassmorphism(enabled: Boolean) {
        val current = getCurrentTheme()
        settingsManager.applyThemePreset(current.copy(isGlassmorphismEnabled = enabled).asCustomTheme())
    }

    override suspend fun setLiquidGlass(enabled: Boolean) {
        val current = getCurrentTheme()
        settingsManager.applyThemePreset(current.copy(isLiquidGlassEnabled = enabled).asCustomTheme())
    }

    override suspend fun setMeshGradient(enabled: Boolean) {
        val current = getCurrentTheme()
        settingsManager.applyThemePreset(current.copy(isMeshGradientBackgroundEnabled = enabled).asCustomTheme())
    }

    override suspend fun setGradientStyle(style: GradientBackgroundStyle) {
        val current = getCurrentTheme()
        settingsManager.applyThemePreset(current.copy(gradientBackgroundStyle = style).asCustomTheme())
    }

    override suspend fun setGlassBaseAlpha(alpha: Float) {
        val current = getCurrentTheme()
        settingsManager.applyThemePreset(current.copy(glassBaseAlpha = alpha.coerceIn(0.1f, 1f)).asCustomTheme())
    }

    /** 修改使配置偏离了已存预设: 应用时改写为自定义哨兵 id, 避免选择器高亮与实际配色脱节 */
    private fun AppThemePreset.asCustomTheme(): AppThemePreset =
        if (id == AppThemePreset.CUSTOM_ID) this else copy(id = AppThemePreset.CUSTOM_ID)

    override suspend fun saveUserTheme(preset: AppThemePreset) {
        themeRepository.saveUserTheme(preset)
    }

    override suspend fun deleteUserTheme(id: String) {
        themeRepository.deleteUserTheme(id)
    }

    override suspend fun applyThemePreset(preset: AppThemePreset) {
        settingsManager.applyThemePreset(preset)
    }

    override suspend fun previewThemePreset(preset: AppThemePreset) {
        settingsManager.applyThemePreset(preset, updateActiveThemeId = false)
    }
}
