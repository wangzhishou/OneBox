/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.core.settings.domain

import com.t8rin.imagetoolbox.core.settings.domain.model.AppColorSystem
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
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

    suspend fun setGlassBorderAlpha(alpha: Float)

    suspend fun saveUserTheme(preset: AppThemePreset)

    suspend fun deleteUserTheme(id: String)

    suspend fun applyThemePreset(preset: AppThemePreset)

    /**
     * 实时预览草稿主题: 应用全部视觉字段但不改写 ACTIVE_THEME_ID,
     * 避免预览哨兵 id 被持久化(进程死亡后 activeThemeId 悬垂)。
     */
    suspend fun previewThemePreset(preset: AppThemePreset)

    /**
     * 显式设置全局日夜模式(浅色 / 深色 / 跟随系统)。
     *
     * 日夜模式是全局偏好: 主题预设自带的 nightMode 只在"点选预设"这种明确动作下生效,
     * 草稿预览([previewThemePreset])不会顺手改写它。
     */
    suspend fun setNightMode(nightMode: NightMode)

    /** 读取当前全局日夜模式 */
    suspend fun getNightMode(): NightMode

    /** 读取全局色彩系统(调色板风格 / 对比度 / 色彩规范 / Expressive 动效) */
    suspend fun getColorSystem(): AppColorSystem

    /** 覆盖全局色彩系统 —— 与主题预设无关, 四项一起写入 */
    suspend fun setColorSystem(system: AppColorSystem)
}
