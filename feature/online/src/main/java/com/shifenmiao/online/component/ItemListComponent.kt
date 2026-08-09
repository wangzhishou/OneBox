package com.shifenmiao.online.component

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.room.withTransaction
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.shifenmiao.common.components.comments.CommentsComponent
import com.shifenmiao.common.logic.CommonComponent
import kotlinx.serialization.Serializable
import com.shifenmiao.common.sync.ItemSyncManager
import com.shifenmiao.common.sync.SyncState
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.core.constants.Constants
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.online.screen.ChipFilter
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class ItemListComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    settingsManager: SettingsManager,
    fileController: FileController,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    private val activityLogRecorder: ActivityLogRecorder,
    private val apiService: ApiService,
    private val itemSyncManager: ItemSyncManager,
    private val commentsComponentFactory: CommentsComponent.Factory,
) : CommonComponent(
    settingsManager,
    dispatchersHolder,
    componentContext,
    appDatabase,
    apiService,
    fileController
) {

    /**
     * 暴露 [ApiService] 给 UI 层, 便于在 PagingDataItemScreen 内直接调用
     * 评论等不属于主 Component 业务范围的接口, 避免每个新接口都走一遍 Hilt 注入.
     */
    val publicApiService: ApiService get() = apiService

    // ── 评论底部弹窗 (Decompose childSlot) ────────────────────────────────

    private val commentsNavigation = SlotNavigation<CommentsConfig>()

    val commentsSlot: Value<ChildSlot<CommentsConfig, CommentsChild>> = childSlot(
        source = commentsNavigation,
        serializer = CommentsConfig.serializer(),
        initialConfiguration = { null },
        handleBackButton = false,
        childFactory = { config, context ->
            CommentsChild(
                component = commentsComponentFactory(
                    componentContext = context,
                    documentId = config.documentId,
                    itemTitle = config.itemTitle,
                    uid = config.uid,
                    onClose = commentsNavigation::dismiss,
                    onCommentCountChanged = { delta ->
                        updateLocalCommentCount(config.localItemId, delta)
                    },
                )
            )
        }
    )

    fun showComments(documentId: String, itemTitle: String, uid: String, localItemId: Int) {
        // 每次点击都生成新的 activationId, 保证 Decompose 会重新创建 CommentsChild,
        // CommentsHost 重新进入 composition, 从而避免关闭动画期间再次点击无法 reopen 的问题.
        commentsNavigation.activate(
            CommentsConfig(
                documentId = documentId,
                itemTitle = itemTitle,
                uid = uid,
                localItemId = localItemId,
                activationId = System.nanoTime(),
            )
        )
    }

    fun dismissComments() {
        commentsNavigation.dismiss()
    }

    private fun updateLocalCommentCount(localItemId: Int, delta: Int) {
        if (localItemId <= 0) return
        componentScope.launch(ioDispatcher) {
            val dao = appDatabase.itemEntityDao()
            val item = dao.getItemEntityById(localItemId) ?: return@launch
            val newCount = (item.commentCount ?: 0) + delta
            dao.updateItem(item.copy(commentCount = newCount.coerceAtLeast(0)))
        }
    }

    @Serializable
    data class CommentsConfig(
        val documentId: String,
        val itemTitle: String,
        val uid: String,
        val localItemId: Int,
        val activationId: Long = 0,
    )

    data class CommentsChild(val component: CommentsComponent)

    private val _scrollToTopEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToTopEvent: SharedFlow<Unit> = _scrollToTopEvent.asSharedFlow()

    private val _myArticleItems = MutableStateFlow<List<ItemWithCategoriesAndStats>>(emptyList())
    val myArticleItems: StateFlow<List<ItemWithCategoriesAndStats>> = _myArticleItems.asStateFlow()

    private val pageFlowCache = mutableMapOf<PageKey, Flow<PagingData<ItemWithCategoriesAndStats>>>()
    private val chipsFlowCache = mutableMapOf<Int, Flow<List<ChipFilter>>>()

    private val myCreatedItemsObserved = AtomicBoolean(false)

    init {
        com.t8rin.imagetoolbox.core.domain.performance.StartupTrace.mark("ItemListComponent.ctor.end")
        itemSyncManager.syncAllOnAppLaunch()
    }

    fun ensureMyCreatedItemsObserved() {
        if (myCreatedItemsObserved.compareAndSet(false, true)) {
            componentScope.launch(ioDispatcher) {
                observeMyCreatedItems()
            }
        }
    }

    fun getItemsFlow(
        listType: ListItemType,
        chipCategoryId: Int?,
    ): Flow<PagingData<ItemWithCategoriesAndStats>> {
        val key = PageKey(listType.id, chipCategoryId)
        return pageFlowCache.getOrPut(key) {
            buildItemsFlow(listType, chipCategoryId)
        }
    }

    private fun buildItemsFlow(
        listType: ListItemType,
        chipCategoryId: Int?,
    ): Flow<PagingData<ItemWithCategoriesAndStats>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSize = 40,
            ),
            pagingSourceFactory = {
                appDatabase.itemEntityDao()
                    .pagingSourceByListTypeAndOptionalCategory(listType.id, chipCategoryId)
            },
        ).flow
            .map { paging ->
                paging.filter { row ->
                    BaseUtils.isShowByIdString(row.item.miniProgramId) &&
                        !BaseUtils.isHiddenId(row.item.id)
                }
            }
            .cachedIn(componentScope)
    }

    fun refreshStateFlow(
        listType: ListItemType,
        chipCategoryId: Int?,
    ): StateFlow<SyncState> = itemSyncManager.syncStateFlow(listType, chipCategoryId)

    fun refreshData(listType: ListItemType, chipCategoryId: Int?) {
        itemSyncManager.sync(listType, chipCategoryId)
    }

    /**
     * 进入列表页时调用，做一次带持久化冷却（按 listType）的增量同步。
     */
    fun syncOnPageEnter(listType: ListItemType) {
        itemSyncManager.syncOnPageEnter(listType)
    }

    /**
     * chip 分类列表：先取 listType 下被 item 引用过的分类；为空时回退到全部分类。
     */
    fun observeChips(listType: ListItemType): Flow<List<ChipFilter>> =
        chipsFlowCache.getOrPut(listType.id) {
            appDatabase.itemEntityDao()
                .observeCategoriesUsedByListType(listType.id)
                .map { rows -> rows.map { ChipFilter(categoryId = it.id, name = it.name) } }
        }

    fun setFavorite(item: ItemEntity) {
        componentScope.launch(ioDispatcher) {
            appDatabase.itemEntityDao().toggleFavorited(item.id, System.currentTimeMillis())
        }
    }

    fun togglePin(itemId: Int, @Suppress("UNUSED_PARAMETER") isPinned: Boolean) {
        // isPinned 参数保留用于 UI 即时反馈；实际值以 DAO 内取反为准。
        componentScope.launch(ioDispatcher) {
            appDatabase.itemEntityDao().togglePinned(itemId, System.currentTimeMillis())
        }
    }

    fun toggleRequiresAuth(itemId: Int) {
        componentScope.launch(ioDispatcher) {
            appDatabase.itemEntityDao().toggleRequiresAuth(itemId, System.currentTimeMillis())
        }
    }

    fun bringToTop(itemId: Int) {
        componentScope.launch(ioDispatcher) {
            appDatabase.itemEntityDao().bumpPinnedAt(itemId, System.currentTimeMillis())
        }
    }

    fun deleteItem(itemId: Int) {
        componentScope.launch(ioDispatcher) {
            appDatabase.withTransaction {
                val item = appDatabase.itemEntityDao().getItemEntityById(itemId)
                    ?: return@withTransaction
                activityLogRecorder.deleteRelatedLogsForItem(item)
                appDatabase.itemEntityDao().deleteItemById(itemId)
            }
        }
    }

    fun recordItemClick(itemId: Int) {
        componentScope.launch(ioDispatcher) {
            appDatabase.itemEntityDao().recordClick(itemId, System.currentTimeMillis())
        }
    }

    suspend fun getItemData(itemId: Int): String? =
        appDatabase.itemDataDao().getByItemId(itemId)?.data

    private suspend fun observeMyCreatedItems() {
        withContext(ioDispatcher) {
            appDatabase.itemEntityDao().getEditableItems().collect { items ->
                _myArticleItems.value = items.filter {
                    it.item.source == Source.LOCAL &&
                        when (it.item.listType) {
                            ListItemType.HTML.id,
                            ListItemType.AGENT.id,
                            ListItemType.PROMPT.id -> false
                            else -> true
                        }
                }
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): ItemListComponent
    }

    private data class PageKey(val listType: Int, val chipCategoryId: Int?)
}
