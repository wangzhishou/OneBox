package com.t8rin.imagetoolbox.feature.settings.data

import com.shifenmiao.database.theme.dao.ThemePresetDao
import com.shifenmiao.database.theme.entity.ThemePresetEntity
import com.t8rin.imagetoolbox.core.settings.domain.ThemeRepository
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidThemeRepository @Inject constructor(
    private val themePresetDao: ThemePresetDao,
) : ThemeRepository {

    override fun observeAllThemes(): Flow<List<AppThemePreset>> {
        return themePresetDao.observeAll().map { userEntities ->
            AppThemePreset.builtinThemes + userEntities.map { it.toDomain() }
        }
    }

    override suspend fun getAllThemes(): List<AppThemePreset> {
        return AppThemePreset.builtinThemes + themePresetDao.getAll().map { it.toDomain() }
    }

    override suspend fun saveUserTheme(preset: AppThemePreset) {
        themePresetDao.upsert(preset.toEntity())
    }

    override suspend fun deleteUserTheme(id: String) {
        themePresetDao.deleteById(id)
    }

    private fun ThemePresetEntity.toDomain(): AppThemePreset = AppThemePreset(
        id = id,
        name = name,
        colorTupleString = colorTupleString,
        nightMode = NightMode.fromOrdinal(nightMode) ?: NightMode.System,
        isDynamicColors = isDynamicColors,
        isGlassmorphismEnabled = isGlassmorphismEnabled,
        isLiquidGlassEnabled = isLiquidGlassEnabled,
        isMeshGradientBackgroundEnabled = isMeshGradientBackgroundEnabled,
        gradientBackgroundStyle = GradientBackgroundStyle.fromOrdinal(gradientBackgroundStyle),
        glassBaseAlpha = glassBaseAlpha,
        customBackgroundImageUri = customBackgroundImageUri,
        isBuiltin = false,
    )

    private fun AppThemePreset.toEntity(): ThemePresetEntity = ThemePresetEntity(
        id = id,
        name = name,
        colorTupleString = colorTupleString,
        nightMode = nightMode.ordinal,
        isDynamicColors = isDynamicColors,
        isGlassmorphismEnabled = isGlassmorphismEnabled,
        isLiquidGlassEnabled = isLiquidGlassEnabled,
        isMeshGradientBackgroundEnabled = isMeshGradientBackgroundEnabled,
        gradientBackgroundStyle = gradientBackgroundStyle.ordinal2,
        glassBaseAlpha = glassBaseAlpha,
        customBackgroundImageUri = customBackgroundImageUri,
    )
}

