package com.wanbaohe.notification.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.components.comments.CommentsComponent
import com.shifenmiao.common.components.comments.commentUidForListType
import com.shifenmiao.common.components.comments.commentUidForSourceTitle
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.network.model.comment.MyComment
import com.shifenmiao.network.model.notification.UserNotification
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.notification.service.NotificationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

data class NotificationUiState(
    /** 未登录时展示登录引导态 */
    val isLoggedIn: Boolean = true,
    /** 首次加载中 */
    val isLoading: Boolean = false,
    /** 下拉刷新中 */
    val isRefreshing: Boolean = false,
    /** 首屏加载失败(展示错误态) */
    val isError: Boolean = false,
    /** 「我发表的评论」section */
    val myComments: List<MyComment> = emptyList(),
    val myCommentsPage: Int = 0,
    val myCommentsPageCount: Int = 1,
    val isLoadingMoreMyComments: Boolean = false,
    /** 「通知」section: 评论回复 + 反馈回复 + 系统通知, 全部通知类型 */
    val notifications: List<UserNotification> = emptyList(),
    val notificationsPage: Int = 0,
    val notificationsPageCount: Int = 1,
    val isLoadingMoreNotifications: Boolean = false,
)

/**
 * 消息中心 Component — 「我发表的评论」+「通知」双 section 分页 + 已读操作编排。
 *
 * 未读数由 [NotificationRepository.unreadCount] 全局共享, 标记已读/全部已读直接同步给
 * 个人中心角标。
 *
 * 「通知」section **不按 type 过滤**: 服务端的未读数统计的是该用户全部未读行
 * (`/user-notifications/unread-count` 不分类型), 之前这里只拉 comment_reply, 于是
 * 反馈回复 / 系统通知的未读在列表里根本没有入口可以点掉, 红点只能靠「全部已读」清。
 * 列表口径与未读数口径必须一致, 所以这里拉全部类型, 由 UI 按 type 渲染不同卡片。
 */
class NotificationComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val repository: NotificationRepository,
    private val appDatabase: AppDatabase,
    private val commentsComponentFactory: CommentsComponent.Factory,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState

    val unreadCount: StateFlow<Int> = repository.unreadCount

    // ── 评论浮动层 (Decompose childSlot) ──────────────────────────────────
    //
    // 复用列表页那套评论浮动层: 点消息卡片 → 打开该内容(博客 / 条目)的评论区,
    // 并带上目标评论 id 让弹层自动翻页定位 + 高亮.

    private val commentsNavigation = SlotNavigation<CommentsConfig>()

    val commentsSlot: Value<ChildSlot<CommentsConfig, CommentsChild>> = childSlot(
        source = commentsNavigation,
        serializer = CommentsConfig.serializer(),
        key = "NotificationCommentsSlot",
        initialConfiguration = { null },
        handleBackButton = false,
        childFactory = { config, context ->
            CommentsChild(
                component = commentsComponentFactory(
                    componentContext = context,
                    documentId = config.documentId,
                    itemTitle = config.itemTitle,
                    uid = config.uid,
                    focusCommentId = config.focusCommentId,
                    onClose = commentsNavigation::dismiss,
                    onCommentCountChanged = {},
                )
            )
        }
    )

    init {
        componentScope.launch {
            if (!TokenStorage.isLogin()) {
                _uiState.value = _uiState.value.copy(isLoggedIn = false)
                return@launch
            }
            loadFirstPages(isRefresh = false)
            repository.refreshUnreadCount()
        }
    }

    /** 登录引导:登录成功后以登录态重新加载列表 */
    fun login() {
        ActionUtils.showLogin(source = "notification_center") {
            _uiState.value = _uiState.value.copy(isLoggedIn = true)
            componentScope.launch {
                loadFirstPages(isRefresh = false)
                repository.refreshUnreadCount()
            }
        }
    }

    fun refresh() {
        if (!_uiState.value.isLoggedIn || _uiState.value.isRefreshing) return
        componentScope.launch {
            loadFirstPages(isRefresh = true)
            repository.refreshUnreadCount()
        }
    }

    /** 滚动到底部附近时,对还有下一页的 section 各自追加一页 */
    fun loadMore() {
        val state = _uiState.value
        if (!state.isLoggedIn || state.isLoading || state.isRefreshing) return
        componentScope.launch {
            if (!state.isLoadingMoreMyComments && state.myCommentsPage < state.myCommentsPageCount) {
                loadMyCommentsPage(state.myCommentsPage + 1)
            }
            if (!state.isLoadingMoreNotifications &&
                state.notificationsPage < state.notificationsPageCount
            ) {
                loadNotificationsPage(state.notificationsPage + 1)
            }
        }
    }

    /**
     * 点击通知卡片: 评论回复顺带打开来源评论区, 再乐观标已读并落库.
     *
     * 另外两类 (feedback_reply / system) 没有可跳转的页面, 点一下只负责把未读清掉 ——
     * 这正是之前红点掉不掉的症结: 它们从未出现在列表里, 也就永远点不到。
     */
    fun markRead(item: UserNotification) {
        if (item.type == UserNotification.TYPE_COMMENT_REPLY) {
            openComments(
                documentId = item.sourceDocumentId.orEmpty(),
                sourceTitle = item.sourceTitle,
                focusCommentId = item.relatedCommentId,
            )
        }
        if (item.read) return
        _uiState.value = _uiState.value.copy(
            notifications = _uiState.value.notifications.map {
                if (it.id == item.id) it.copy(read = true) else it
            }
        )
        componentScope.launch {
            repository.markRead(item.id)
        }
    }

    /** 点击「我发表的评论」卡片: 打开来源评论区并定位到这条评论. */
    fun openMyComment(comment: MyComment) {
        openComments(
            documentId = comment.sourceDocumentId.orEmpty(),
            sourceTitle = comment.sourceTitle,
            focusCommentId = comment.id,
        )
    }

    fun dismissComments() {
        commentsNavigation.dismiss()
    }

    /**
     * 打开来源内容的评论区.
     *
     * uid 决定了查询的是哪张表的评论 (related = "<uid>:<documentId>"), 上报接口只给
     * documentId + 来源标题, 所以先按本地同步下来的条目类型判断, 查不到再用
     * "有标题=博客" 兜底 (go-proxy 只给 blog 回填 sourceTitle).
     */
    private fun openComments(documentId: String, sourceTitle: String, focusCommentId: Int) {
        if (documentId.isBlank()) return
        componentScope.launch {
            val localItem = withContext(ioDispatcher) {
                runCatching {
                    appDatabase.itemEntityDao().getItemByDocumentId(documentId)
                }.getOrNull()
            }
            val uid = localItem?.listType?.let(::commentUidForListType)
                ?: commentUidForSourceTitle(sourceTitle)
            commentsNavigation.activate(
                CommentsConfig(
                    documentId = documentId,
                    // 条目类来源没有 sourceTitle (只有 blog 才有), 用本地同步下来的标题补上.
                    itemTitle = sourceTitle.ifBlank { localItem?.title.orEmpty() },
                    uid = uid,
                    focusCommentId = focusCommentId,
                    activationId = System.nanoTime(),
                )
            )
        }
    }

    fun markAllRead() {
        if (_uiState.value.notifications.none { !it.read }) return
        _uiState.value = _uiState.value.copy(
            notifications = _uiState.value.notifications.map { it.copy(read = true) }
        )
        componentScope.launch {
            repository.markAllRead()
        }
    }

    /** 首屏 / 刷新:两个 section 的第一页并发拉取,两个都失败且都为空才算错误态 */
    private suspend fun loadFirstPages(isRefresh: Boolean) {
        val state = _uiState.value
        val showLoading = !isRefresh && state.myComments.isEmpty() && state.notifications.isEmpty()
        _uiState.value = state.copy(
            isLoading = showLoading,
            isRefreshing = isRefresh,
            isError = false,
        )
        coroutineScope {
            val myCommentsDeferred = async { repository.fetchMyComments(page = 1) }
            // 不带 type: 与服务端未读数统计口径保持一致 (见类注释)
            val notificationsDeferred = async { repository.fetchNotifications(page = 1) }
            val myCommentsResult = myCommentsDeferred.await()
            val notificationsResult = notificationsDeferred.await()

            var newState = _uiState.value
            myCommentsResult
                .onSuccess { response ->
                    newState = newState.copy(
                        myComments = response.data.distinctBy { it.id },
                        myCommentsPage = response.meta.pagination.page,
                        myCommentsPageCount = response.meta.pagination.pageCount,
                    )
                }
            notificationsResult
                .onSuccess { response ->
                    newState = newState.copy(
                        notifications = response.data.distinctBy { it.id },
                        notificationsPage = response.meta.pagination.page,
                        notificationsPageCount = response.meta.pagination.pageCount,
                    )
                }
            val bothFailed = myCommentsResult.isFailure && notificationsResult.isFailure
            _uiState.value = newState.copy(
                isLoading = false,
                isRefreshing = false,
                isError = bothFailed &&
                    newState.myComments.isEmpty() &&
                    newState.notifications.isEmpty(),
            )
        }
    }

    private suspend fun loadMyCommentsPage(page: Int) {
        _uiState.value = _uiState.value.copy(isLoadingMoreMyComments = true)
        repository.fetchMyComments(page = page)
            .onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMoreMyComments = false,
                    myComments = (_uiState.value.myComments + response.data).distinctBy { it.id },
                    myCommentsPage = response.meta.pagination.page,
                    myCommentsPageCount = response.meta.pagination.pageCount,
                )
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(isLoadingMoreMyComments = false)
            }
    }

    private suspend fun loadNotificationsPage(page: Int) {
        _uiState.value = _uiState.value.copy(isLoadingMoreNotifications = true)
        repository.fetchNotifications(page = page)
            .onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMoreNotifications = false,
                    notifications = (_uiState.value.notifications + response.data).distinctBy { it.id },
                    notificationsPage = response.meta.pagination.page,
                    notificationsPageCount = response.meta.pagination.pageCount,
                )
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(isLoadingMoreNotifications = false)
            }
    }

    @Serializable
    data class CommentsConfig(
        val documentId: String,
        val itemTitle: String,
        val uid: String,
        val focusCommentId: Int = 0,
        val activationId: Long = 0,
    )

    data class CommentsChild(val component: CommentsComponent)

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): NotificationComponent
    }
}
