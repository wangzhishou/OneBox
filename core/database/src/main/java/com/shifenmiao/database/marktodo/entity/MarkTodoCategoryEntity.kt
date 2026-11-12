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

package com.shifenmiao.database.marktodo.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marktodo_category")
data class MarkTodoCategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    /**
     * Display title.
     * Note: stored as resolved string to keep the table stable across app locales.
     */
    @ColumnInfo(name = "title") val title: String,
    /**
     * Stable icon key string that UI can map to an ImageVector.
     */
    @ColumnInfo(name = "icon_key") val iconKey: String,
    /**
     * 主题描述（可选）。
     */
    @ColumnInfo(name = "description") val description: String? = null,
    /**
     * 主题色 ARGB；null 表示按列表位置自动循环系统主题色。
     */
    @ColumnInfo(name = "color_argb") val colorArgb: Int? = null,
    /**
     * 是否置顶（置顶主题排在列表最前）。
     */
    @ColumnInfo(name = "is_pinned", defaultValue = "0") val isPinned: Boolean = false,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
