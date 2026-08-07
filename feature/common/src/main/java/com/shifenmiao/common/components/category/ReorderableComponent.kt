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
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.state.update
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
