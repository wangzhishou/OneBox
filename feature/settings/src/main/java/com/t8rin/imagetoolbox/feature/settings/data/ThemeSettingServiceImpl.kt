package com.t8rin.imagetoolbox.feature.settings.data

import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.settings.domain.ThemeRepository
import com.t8rin.imagetoolbox.core.settings.domain.ThemeSettingService
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeSettingServiceImpl @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val settingsManager: SettingsManager,
) : ThemeSettingService {

    override suspend fun getCurrentTheme(): AppThemePreset {
        val activeId = settingsManager.getSettingsState().activeThemeId
        return themeRepository.getAllThemes().firstOrNull { it.id == activeId }
            ?: AppThemePreset.Default
    }

    override suspend fun listThemes(): List<AppThemePreset> {
        return themeRepository.getAllThemes()
    }

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
        val parts = AppThemePreset.parseColorTuple(current.colorTupleString).takeIf { it.size == 4 }
            ?: AppThemePreset.parseColorTuple(AppThemePreset.Default.colorTupleString)
        val newPrimary = primary ?: parts.getOrNull(0) ?: return
        val newSecondary = secondary ?: parts.getOrNull(1) ?: return
        val newTertiary = tertiary ?: parts.getOrNull(2) ?: return
        val newSurface = surface ?: parts.getOrNull(3) ?: return
        val updated = current.copy(
            colorTupleString = listOf(newPrimary, newSecondary, newTertiary, newSurface)
                .joinToString("*"),
            isDynamicColors = false,
        )
        settingsManager.applyThemePreset(updated)
    }

    override suspend fun setGlassmorphism(enabled: Boolean) {
        val current = getCurrentTheme()
        settingsManager.applyThemePreset(current.copy(isGlassmorphismEnabled = enabled))
    }

    override suspend fun setLiquidGlass(enabled: Boolean) {
        val current = getCurrentTheme()
        settingsManager.applyThemePreset(current.copy(isLiquidGlassEnabled = enabled))
    }

    override suspend fun setMeshGradient(enabled: Boolean) {
        val current = getCurrentTheme()
        settingsManager.applyThemePreset(current.copy(isMeshGradientBackgroundEnabled = enabled))
    }

    override suspend fun setGradientStyle(style: GradientBackgroundStyle) {
        val current = getCurrentTheme()
        settingsManager.applyThemePreset(current.copy(gradientBackgroundStyle = style))
    }

    override suspend fun setGlassBaseAlpha(alpha: Float) {
        val current = getCurrentTheme()
        settingsManager.applyThemePreset(current.copy(glassBaseAlpha = alpha.coerceIn(0.1f, 1f)))
    }

    override suspend fun saveUserTheme(preset: AppThemePreset) {
        themeRepository.saveUserTheme(preset)
    }

    override suspend fun deleteUserTheme(id: String) {
        themeRepository.deleteUserTheme(id)
    }

    override suspend fun applyThemePreset(preset: AppThemePreset) {
        settingsManager.applyThemePreset(preset)
    }
}
