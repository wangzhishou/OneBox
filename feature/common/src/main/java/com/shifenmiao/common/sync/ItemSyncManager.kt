package com.shifenmiao.common.sync

import androidx.room.withTransaction
import com.shifenmiao.base.utils.CoreUtils
import com.shifenmiao.core.constants.Constants
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.utils.DataBaseUtils
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import com.shifenmiao.model.datasource.DataItemRemoteDataSource
import com.shifenmiao.model.datasource.SyncResult
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.logger.makeLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统一负责条目与分类的增量同步。
 *
 * - 按 [ListItemType] + categoryId 维度记录最后同步时间与同步状态。
 * - 分类与条目一起同步，保证 chips 与列表一致。
 * - 服务端下架（publishedAt 为空）的数据会被识别为删除并清理本地副本。
 * - 应用启动时的全量同步受 [SyncIntervalPolicy] 控制，避免频繁请求。
 * - 用户手动刷新通过 [ManualRefreshPolicy] 做会话内冷却。
 */
@Singleton
class ItemSyncManager @Inject constructor(
    private val appDatabase: AppDatabase,
    private val remoteDataSource: DataItemRemoteDataSource,
    dispatchersHolder: DispatchersHolder,
) {
    private val ioDispatcher = dispatchersHolder.ioDispatcher
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val syncStates = mutableMapOf<SyncKey, MutableStateFlow<SyncState>>()
    private val syncLocks = mutableMapOf<SyncKey, Mutex>()
    private val categoriesSyncLock = Mutex()
    private val appLaunchSyncCalled = AtomicBoolean(false)

    /**
     * 订阅指定 (listType, categoryId) 的同步状态。
     */
    fun syncStateFlow(
        listType: ListItemType,
        categoryId: Int? = null,
    ): MutableStateFlow<SyncState> = synchronized(syncStates) {
        syncStates.getOrPut(SyncKey(listType.id, categoryId)) { MutableStateFlow(SyncState.Idle) }
    }

    private fun getLock(listType: ListItemType, categoryId: Int?): Mutex = synchronized(syncLocks) {
        syncLocks.getOrPut(SyncKey(listType.id, categoryId)) { Mutex() }
    }

    private data class SyncKey(val listType: Int, val categoryId: Int?)

    /**
     * 在后台触发指定列表类型的增量同步（会同步一次分类）。
     * 手动刷新场景，强制走网络。
     */
    fun sync(listType: ListItemType, categoryId: Int? = null) {
        scope.launch(ioDispatcher) {
            syncInternal(listType, categoryId, syncCategories = true, forceRefresh = true)
        }
    }

    /**
     * 阻塞式同步指定列表类型。
     */
    suspend fun syncAndAwait(listType: ListItemType, categoryId: Int? = null): Result<Unit> {
        return withContext(ioDispatcher) {
            syncInternal(listType, categoryId, syncCategories = true, forceRefresh = true)
        }
    }

    /**
     * 应用启动时调用：在同步间隔到期时，同步分类一次，然后依次同步所有列表类型。
     * 重复调用会被忽略。
     */
    fun syncAllOnAppLaunch() {
        if (!appLaunchSyncCalled.compareAndSet(false, true)) return
        if (!SyncIntervalPolicy.shouldSync()) {
            makeLog { "syncAllOnAppLaunch: skipped, within sync interval" }
            return
        }
        scope.launch(ioDispatcher) {
            syncAll(forceRefresh = true).onSuccess {
                AppSharedStorage.saveFullSyncLastAt(System.currentTimeMillis())
            }.getOrElse { error ->
                makeLog { "syncAllOnAppLaunch failed: ${error.message}" }
            }
        }
    }

    /**
     * 依次同步所有列表类型。分类只会被同步一次。
     *
     * @param forceRefresh 是否强制走网络，绕过 HTTP 缓存。
     */
    suspend fun syncAll(forceRefresh: Boolean = false): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            syncCategories(forceRefresh)
            listOf(
                ListItemType.NORMAL,
                ListItemType.HTML,
                ListItemType.PROMPT,
                ListItemType.AGENT,
            ).forEach { listType ->
                syncInternal(listType, categoryId = null, syncCategories = false, forceRefresh = forceRefresh).getOrThrow()
            }
        }
    }

    private suspend fun syncInternal(
        listType: ListItemType,
        categoryId: Int?,
        syncCategories: Boolean,
        forceRefresh: Boolean,
    ): Result<Unit> {
        val stateFlow = syncStateFlow(listType, categoryId)
        val lock = getLock(listType, categoryId)
        return lock.withLock {
            runCatching {
                // 隐私政策未同意时阻塞，避免发出网络请求；同意后自动继续。
                CoreUtils.awaitPrivacyPolicyAccepted()
                // 已经有另一个协程在同步同一维度，直接返回，避免重复跑完整分页。
                if (stateFlow.value is SyncState.Loading) return@withLock Result.success(Unit)
                stateFlow.value = SyncState.Loading(page = 1, totalPage = null)
                if (syncCategories) {
                    syncCategories(forceRefresh)
                }
                syncItems(listType, categoryId, forceRefresh)
                stateFlow.value = SyncState.Success
            }.onFailure { error ->
                if (error is CancellationException) throw error
                stateFlow.value = SyncState.Error(error)
            }
        }
    }

    private suspend fun syncCategories(forceRefresh: Boolean) {
        // 隐私政策未同意时阻塞，避免发出网络请求；同意后自动继续。
        CoreUtils.awaitPrivacyPolicyAccepted()
        categoriesSyncLock.withLock {
            val lastSyncAt = AppSharedStorage.loadCategoriesLastSyncAt()
            val result = remoteDataSource.syncCategories(
                updatedAfter = SyncTimeUtils.formatTimestamp(lastSyncAt),
                forceRefresh = forceRefresh,
            )
            applyCategoryResult(result)
        }
    }

    private suspend fun applyCategoryResult(result: SyncResult<com.shifenmiao.model.CategoryList>) {
        val categories = result.data.data
        if (categories.isEmpty()) {
            result.serverTime?.let { AppSharedStorage.saveCategoriesLastSyncAt(SyncTimeUtils.parseIsoTime(it)) }
            return
        }
        appDatabase.withTransaction {
            categories.forEach { category ->
                // Category.listType 已删除；同步协议不再依赖 publishedAt 字段判定删除
                // 简单策略：直接 upsert。如果未来需要"未发布即删除"语义，
                // 在 SyncResult.meta 或专门的 deleted_ids 字段里下发。
                val name = category.name.takeIf { it.isNotBlank() } ?: return@forEach
                appDatabase.itemEntityDao().upsertCategory(
                    Category(
                        id = 0,
                        name = name,
                        canEdit = category.canEdit,
                        source = Source.REMOTE,
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            }
        }
        result.serverTime?.let { AppSharedStorage.saveCategoriesLastSyncAt(SyncTimeUtils.parseIsoTime(it)) }
    }

    private suspend fun syncItems(
        listType: ListItemType,
        categoryId: Int?,
        forceRefresh: Boolean,
    ) {
        val lastSyncAt = AppSharedStorage.loadItemsLastSyncAt(listType.id, categoryId)
        val stateFlow = syncStateFlow(listType, categoryId)
        var page = 1
        var totalPage: Int? = null
        var endOfPaginationReached = false
        var lastServerTime: String? = null
        while (!endOfPaginationReached) {
            val result = remoteDataSource.syncDataItems(
                listType = listType.id,
                categoryId = categoryId,
                pageNumber = page,
                updatedAfter = SyncTimeUtils.formatTimestamp(lastSyncAt),
                pageSize = Constants.PAGE_SIZE,
                forceRefresh = forceRefresh,
            )
            lastServerTime = result.serverTime
            val response = result.data
            totalPage = response.meta.pagination.pageCount
            stateFlow.value = SyncState.Loading(page, totalPage)
            applyItemResult(response.data)
            endOfPaginationReached = response.data.isEmpty() || page >= totalPage
            page++
        }
        lastServerTime?.let {
            AppSharedStorage.saveItemsLastSyncAt(listType.id, categoryId, SyncTimeUtils.parseIsoTime(it))
        }
    }

    private suspend fun applyItemResult(dataItems: List<com.shifenmiao.model.DataItem>) {
        if (dataItems.isEmpty()) return
        appDatabase.withTransaction {
            dataItems.forEach { dataItem ->
                // 只删除远程条目；本地用户创建的内容（source = LOCAL）不受影响。
                if (dataItem.publishedAt.isNullOrBlank()) {
                    appDatabase.itemEntityDao().deleteItemByRemoteId(dataItem.id, Source.REMOTE)
                } else {
                    // item + categories + agent/prompt/data 资源统一写入，link 由 DAO 内部按 listType 决定。
                    val itemWithRelation = DataBaseUtils.dataItemToItemWithRelation(dataItem)
                    appDatabase.itemEntityDao()
                        .insertItemWithCategoriesFromSync(itemWithRelation, appDatabase.itemDataDao())
                }
            }
        }
    }
}

/**
 * 同步状态机。
 */
sealed interface SyncState {
    data object Idle : SyncState
    data class Loading(val page: Int, val totalPage: Int?) : SyncState
    data object Success : SyncState
    data class Error(val cause: Throwable) : SyncState
}
