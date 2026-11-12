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

package com.shifenmiao.database.theme.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shifenmiao.model.theme.ThemeDefaults

/**
 * 用户自建主题持久化实体。
 *
 * 内置主题不入库，仅用户通过"新建主题"创建的主题写入此表。
 */
@Entity(tableName = "theme_preset")
data class ThemePresetEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "color_tuple") val colorTupleString: String,
    @ColumnInfo(name = "night_mode") val nightMode: Int = 2,
    @ColumnInfo(name = "is_dynamic_colors") val isDynamicColors: Boolean = false,
    @ColumnInfo(name = "is_glassmorphism_enabled") val isGlassmorphismEnabled: Boolean = true,
    @ColumnInfo(name = "is_liquid_glass_enabled") val isLiquidGlassEnabled: Boolean = false,
    @ColumnInfo(name = "is_mesh_gradient_bg_enabled") val isMeshGradientBackgroundEnabled: Boolean = true,
    @ColumnInfo(name = "gradient_bg_style") val gradientBackgroundStyle: Int = 3, // Sunset ordinal
    @ColumnInfo(name = "glass_base_alpha") val glassBaseAlpha: Float = 1.0f,
    @ColumnInfo(name = "custom_bg_image_uri") val customBackgroundImageUri: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    // 这里的 DDL 默认值刻意停在 "0.17"、不跟随 ThemeDefaults 一起改:
    // 它只写进建表语句, 而改建表语句会改 Room 的 schema / identityHash, 老库升级时
    // 会走 fallbackToDestructiveMigration 把整个库重建(丢数据)。Room 生成的 INSERT
    // 是全字段显式写入, 运行时永远读不到这个 DDL 默认值; 真正的默认值只有
    // ThemeDefaults.DEFAULT_GLASS_BORDER_ALPHA 一处。
    @ColumnInfo(name = "glass_border_alpha", defaultValue = "0.17")
    val glassBorderAlpha: Float = ThemeDefaults.DEFAULT_GLASS_BORDER_ALPHA,
)

