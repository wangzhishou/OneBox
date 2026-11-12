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

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.repository.CategoryRepository
import com.shifenmiao.model.Source
import com.shifenmiao.model.reorderable.ReorderableType
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.state.update
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class ReorderableComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val type: ReorderableType,
    dispatchersHolder: DispatchersHolder,
    private val categoryRepository: CategoryRepository,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _items: MutableState<List<ManageableItem>> = mutableStateOf(emptyList())
    val items: List<ManageableItem> by _items

    init {
        when (type) {
            ReorderableType.CATEGORY -> {
                categoryRepository.getAllCategories()
                    .onEach { list ->
                        _items.update { list.map { it.toManageableItem() } }
                    }
                    .launchIn(componentScope)
            }
        }
    }

    fun addItem(name: String) {
        componentScope.launch {
            when (type) {
                ReorderableType.CATEGORY -> {
                    categoryRepository.insertOrUpdateCategory(
                        Category(
                            id = 0,
                            name = name,
                            canEdit = true,
                            source = Source.LOCAL,
                        )
                    )
                }
            }
        }
    }

    fun deleteItem(item: ManageableItem) {
        componentScope.launch {
            when (type) {
                ReorderableType.CATEGORY -> {
                    categoryRepository.deleteCategory(item.id)
                }
            }
        }
    }

    fun renameItem(item: ManageableItem, newName: String) {
        componentScope.launch {
            when (type) {
                ReorderableType.CATEGORY -> {
                    categoryRepository.updateCategoryName(item.id, newName)
                }
            }
        }
    }

    /**
     * 旧版 order 字段已删除；保留方法签名避免 UI 端编译失败。
     * 真实排序由 [updated_at] 决定（最新编辑的排前面），业务上拖拽重排意义有限。
     */
    @Suppress("UNUSED_PARAMETER")
    fun reorderItems(newOrder: List<ManageableItem>) {
        // no-op
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            type: ReorderableType
        ): ReorderableComponent
    }

}
