package com.t8rin.imagetoolbox.core.settings.domain

import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import kotlinx.coroutines.flow.Flow

/**
 * 主题设置服务 —— UI 与 AI Agent Tool 共用的主题操作接口。
 *
 * 将主题相关的读写操作统一收敛，UI Component 和 AgentTool
 * 通过同一接口操作主题，避免逻辑散落和重复。
 */
interface ThemeSettingService {

    suspend fun getCurrentTheme(): AppThemePreset

    suspend fun listThemes(): List<AppThemePreset>

    /** 最近一次已加载的主题列表快照 —— 用于 UI 首帧 initial, 避免列表闪烁 */
    val themesSnapshot: List<AppThemePreset>

    fun observeThemes(): Flow<List<AppThemePreset>>

    suspend fun switchTheme(themeId: String): AppThemePreset?

    suspend fun updateThemeColors(
        primary: Int? = null,
        secondary: Int? = null,
        tertiary: Int? = null,
        surface: Int? = null,
    )

    suspend fun setGlassmorphism(enabled: Boolean)

    suspend fun setLiquidGlass(enabled: Boolean)

    suspend fun setMeshGradient(enabled: Boolean)

    suspend fun setGradientStyle(style: GradientBackgroundStyle)

    suspend fun setGlassBaseAlpha(alpha: Float)

    suspend fun saveUserTheme(preset: AppThemePreset)

    suspend fun deleteUserTheme(id: String)

    suspend fun applyThemePreset(preset: AppThemePreset)

    /**
     * 实时预览草稿主题: 应用全部视觉字段但不改写 ACTIVE_THEME_ID,
     * 避免预览哨兵 id 被持久化(进程死亡后 activeThemeId 悬垂)。
     */
    suspend fun previewThemePreset(preset: AppThemePreset)
}
