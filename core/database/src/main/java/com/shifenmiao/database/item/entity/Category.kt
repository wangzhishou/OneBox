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

package com.shifenmiao.database.item.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifenmiao.model.Source

/**
 * 分类（全局共享，按使用反查 listType）。
 * - 唯一索引 (name, source)
 * - 删除分类用 RESTRICT（业务上分类有"被引用"语义，不能随手 CASCADE）
 * - 不再保留 listType 字段；"该 listType 用哪些分类" 在 query 阶段 JOIN item_category 推算
 */
@Entity(
    tableName = "category",
    indices = [
        Index(value = ["name", "source"], unique = true)
    ]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    /** Strapi v5 文档级 cuid；chip 过滤等服务端定位用，数字 id 重发后会漂移故不存。 */
    @ColumnInfo(name = "document_id") val documentId: String? = null,
    @ColumnInfo(name = "can_edit", defaultValue = "0") val canEdit: Boolean = false,
    @ColumnInfo(name = "source", defaultValue = "0") val source: Source = Source.REMOTE,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

fun Category.toModel(): com.shifenmiao.model.Category {
    return com.shifenmiao.model.Category(
        id = this.id,
        name = this.name,
        canEdit = this.canEdit,
        source = this.source,
        updatedAt = this.updatedAt,
        documentId = this.documentId,
    )
}
