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

package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

/**
 * 待办标签：驱动首页标签筛选 chips，并为任务标签提供配色。
 *
 * 任务的 tags 存标签名，按 [name] 与标签关联。
 *
 * @property id Unique identifier for the tag.
 * @property name Display name (预置标签已按建库语言本地化).
 * @property colorArgb Optional chip accent color (ARGB); null = 使用系统主题色.
 * @property isPreset 是否为预置标签（预置标签不可删除）.
 */
@Immutable
data class TodoTag(
    val id: String,
    val name: String,
    val colorArgb: Int? = null,
    val isPreset: Boolean = false
)
