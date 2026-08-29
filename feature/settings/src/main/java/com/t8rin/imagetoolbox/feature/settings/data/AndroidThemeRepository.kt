package com.t8rin.imagetoolbox.feature.settings.data

import com.shifenmiao.database.theme.dao.ThemePresetDao
import com.shifenmiao.database.theme.entity.ThemePresetEntity
import com.t8rin.imagetoolbox.core.domain.coroutines.AppScope
import com.t8rin.imagetoolbox.core.settings.domain.ThemeRepository
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidThemeRepository @Inject constructor(
    private val themePresetDao: ThemePresetDao,
    appScope: AppScope,
) : ThemeRepository {

    /**
     * 单例内共享的热流 —— 各 UI 入口(抽屉/弹窗/显示设置)直接拿到最近一次的列表,
     * 避免每次打开都先渲染纯内置列表一帧再跳动。
     */
    private val themesFlow: StateFlow<List<AppThemePreset>> = themePresetDao.observeAll()
        .map { userEntities ->
            AppThemePreset.builtinThemes + userEntities.map { it.toDomain() }
        }
        .stateIn(appScope, SharingStarted.Eagerly, AppThemePreset.builtinThemes)

    override fun observeAllThemes(): Flow<List<AppThemePreset>> = themesFlow

    override val themesSnapshot: List<AppThemePreset> get() = themesFlow.value

    override suspend fun getAllThemes(): List<AppThemePreset> {
        return AppThemePreset.builtinThemes + themePresetDao.getAll().map { it.toDomain() }
    }

    override suspend fun saveUserTheme(preset: AppThemePreset) {
        // upsert 时保留原创建时间, 避免编辑后 createdAt 被重置成当前时间
        val existingCreatedAt = themePresetDao.getById(preset.id)?.createdAt
        themePresetDao.upsert(
            preset.toEntity(createdAt = existingCreatedAt ?: System.currentTimeMillis())
        )
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

    private fun AppThemePreset.toEntity(createdAt: Long): ThemePresetEntity = ThemePresetEntity(
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
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis(),
    )
}
