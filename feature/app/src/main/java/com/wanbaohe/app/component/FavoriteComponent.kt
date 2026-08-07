package com.wanbaohe.app.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.handle.HandleEvent
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.resource.ResourceManager
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * FavoriteScreen 数据组件 — 统一从 ItemEntity 表获取各 Section 数据。
 *
 * 数据源说明：
 * - 精选: recommend = true
 * - 收藏: isFavorited = true
 * - 我的: canEdit = true
 * - 本地工具: isOnline = false
 * - 最近访问: item_click_stat.clickTime > 0, 按 clickTime DESC
 */
class FavoriteComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    private val appDatabase: AppDatabase,
    resourceManager: ResourceManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext), ResourceManager by resourceManager {

    private val itemDao get() = appDatabase.itemEntityDao()

    // ── 精选 ──
    private val _recommendedFlow = MutableStateFlow<List<ItemWithCategoriesAndStats>>(emptyList())
    val recommendedFlow: StateFlow<List<ItemWithCategoriesAndStats>> get() = _recommendedFlow

    // ── 收藏 ──
    private val _favoritedFlow = MutableStateFlow<List<ItemWithCategoriesAndStats>>(emptyList())
    val favoritedFlow: StateFlow<List<ItemWithCategoriesAndStats>> get() = _favoritedFlow

    // ── 我的 ──
    private val _editableFlow = MutableStateFlow<List<ItemWithCategoriesAndStats>>(emptyList())
    val editableFlow: StateFlow<List<ItemWithCategoriesAndStats>> get() = _editableFlow

    // ── 最近访问 ──
    private val _recentClickedFlow = MutableStateFlow<List<ItemWithCategoriesAndStats>>(emptyList())
    val recentClickedFlow: StateFlow<List<ItemWithCategoriesAndStats>> get() = _recentClickedFlow

    init {
        loadAll()
    }

    private fun loadAll() {
        componentScope.launch(Dispatchers.IO) {
            itemDao.getRecommendedItems().collect { _recommendedFlow.value = it.take(10) }
        }
        componentScope.launch(Dispatchers.IO) {
            itemDao.getFavoritedItems().collect { _favoritedFlow.value = it.take(10) }
        }
        componentScope.launch(Dispatchers.IO) {
            itemDao.getEditableItems().collect { _editableFlow.value = it.take(10) }
        }
        componentScope.launch(Dispatchers.IO) {
            itemDao.getRecentClickedItems().collect { _recentClickedFlow.value = it.take(10) }
        }
    }

    /** 取消收藏 */
    fun unfavoriteItem(itemId: Int) {
        componentScope.launch(Dispatchers.IO) {
            itemDao.setFavorited(itemId, false, System.currentTimeMillis())
        }
    }

    /** 删除用户创建的 item（我的 Section） */
    fun deleteItem(itemId: Int) {
        componentScope.launch(Dispatchers.IO) {
            runCatching {
                itemDao.deleteItemById(itemId)
            }
        }
    }

    /** 记录点击并导航 */
    fun handleItemClick(
        itemWithCategories: ItemWithCategoriesAndStats,
        onNavigate: (Screen) -> Unit
    ) {
        componentScope.launch(Dispatchers.IO) {
            runCatching {
                // 记录点击
                itemDao.recordClick(itemWithCategories.item.id, System.currentTimeMillis())
                // 资源预查（item → link → agent/prompt）
                val resource = com.shifenmiao.common.handle.ItemResourceResolver.resolve(
                    appDatabase = appDatabase,
                    itemId = itemWithCategories.item.id,
                    listType = itemWithCategories.item.listType,
                )
                // 导航
                HandleEvent.handleCardClick(
                    context = AppContext.getContext(),
                    onNavigate = onNavigate,
                    itemWithRelation = itemWithCategories.toItemWithCategories(),
                    resource = resource,
                )
            }.onFailure {
                ActionUtils.showToast(AppContext.getString(R.string.need_update_new_version))
            }
        }
    }


    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): FavoriteComponent
    }
}