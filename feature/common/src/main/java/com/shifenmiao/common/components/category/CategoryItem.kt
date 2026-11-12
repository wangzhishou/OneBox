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

package com.shifenmiao.common.components.category

import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.model.Source

/**
 * Wrapper for Category to implement ManageableItem
 * - 排序语义已删除；拖拽重排后续若需要可接 fractional indexing / 服务端字段。
 */
data class CategoryItem(
    val category: Category
) : ManageableItem {
    override val id: Int get() = category.id
    override val name: String get() = category.name
    override val order: Int get() = 0
    override val canEdit: Boolean? get() = category.canEdit
    override val source: Source get() = category.source
}

fun Category.toManageableItem(): CategoryItem = CategoryItem(this)
fun CategoryItem.toCategory(): Category = category
