package com.t8rin.imagetoolbox.core.settings.domain

import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import kotlinx.coroutines.flow.Flow

/**
 * 主题仓库 —— 合并内置主题 + 用户自建主题，并提供 CRUD。
 */
interface ThemeRepository {

    /** 观察所有主题（内置 + 用户自建），按展示顺序排列 */
    fun observeAllThemes(): Flow<List<AppThemePreset>>

    /** 最近一次已加载的主题列表快照 —— 用于 UI 首帧 initial, 避免先渲染纯内置列表再跳动 */
    val themesSnapshot: List<AppThemePreset>

    /** 一次性获取所有主题 */
    suspend fun getAllThemes(): List<AppThemePreset>

    /** 保存用户自建主题（新建或更新） */
    suspend fun saveUserTheme(preset: AppThemePreset)

    /** 删除用户自建主题 */
    suspend fun deleteUserTheme(id: String)
}

