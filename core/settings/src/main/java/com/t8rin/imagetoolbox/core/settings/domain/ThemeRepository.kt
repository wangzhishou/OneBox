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

