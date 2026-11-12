/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.shifenmiao.model.item

import androidx.compose.runtime.Immutable
import com.shifenmiao.model.Category


@Immutable
data class ItemDataUiState(
    val itemId: Int = 0,
    val title: String = "",
    val description: String = "",
    val data: String = "",
    val iconName: String = "",
    val errorMessage: String? = null,
    val listType: Int = 0,
    val allCategories: List<Category> = emptyList(),
    val selectedCategories: Set<Category> = emptySet(),
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    /** 笔记已加锁且当前未通过授权码校验:正文不加载,页面应展示锁定占位 */
    val isLocked: Boolean = false
)
