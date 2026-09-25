package com.wanbaohe.notification.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.network.model.comment.MyComment
import com.shifenmiao.network.model.notification.UserNotification
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.notification.service.NotificationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
    /** 「用户回复」section(通知 type=comment_reply) */
    val replies: List<UserNotification> = emptyList(),
    val repliesPage: Int = 0,
    val repliesPageCount: Int = 1,
    val isLoadingMoreReplies: Boolean = false,
)

/**
 * 消息中心 Component — 「我发表的评论」+「用户回复」双 section 分页 + 已读操作编排。
 *
 * 未读数由 [NotificationRepository.unreadCount] 全局共享,
 * 标记已读/全部已读直接同步给个人中心角标。
 * 「用户回复」只拉取 comment_reply 类型通知,卡片点击即标已读。
 */
class NotificationComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val repository: NotificationRepository,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState

    val unreadCount: StateFlow<Int> = repository.unreadCount

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
            if (!state.isLoadingMoreReplies && state.repliesPage < state.repliesPageCount) {
                loadRepliesPage(state.repliesPage + 1)
            }
        }
    }

    /** 点击回复卡片:已读的忽略,未读的乐观更新后调接口 */
    fun markRead(item: UserNotification) {
        if (item.read) return
        _uiState.value = _uiState.value.copy(
            replies = _uiState.value.replies.map {
                if (it.id == item.id) it.copy(read = true) else it
            }
        )
        componentScope.launch {
            repository.markRead(item.id)
        }
    }

    fun markAllRead() {
        if (_uiState.value.replies.none { !it.read }) return
        _uiState.value = _uiState.value.copy(
            replies = _uiState.value.replies.map { it.copy(read = true) }
        )
        componentScope.launch {
            repository.markAllRead()
        }
    }

    /** 首屏 / 刷新:两个 section 的第一页并发拉取,两个都失败且都为空才算错误态 */
    private suspend fun loadFirstPages(isRefresh: Boolean) {
        val state = _uiState.value
        val showLoading = !isRefresh && state.myComments.isEmpty() && state.replies.isEmpty()
        _uiState.value = state.copy(
            isLoading = showLoading,
            isRefreshing = isRefresh,
            isError = false,
        )
        coroutineScope {
            val myCommentsDeferred = async { repository.fetchMyComments(page = 1) }
            val repliesDeferred = async {
                repository.fetchNotifications(page = 1, type = UserNotification.TYPE_COMMENT_REPLY)
            }
            val myCommentsResult = myCommentsDeferred.await()
            val repliesResult = repliesDeferred.await()

            var newState = _uiState.value
            myCommentsResult
                .onSuccess { response ->
                    newState = newState.copy(
                        myComments = response.data.distinctBy { it.id },
                        myCommentsPage = response.meta.pagination.page,
                        myCommentsPageCount = response.meta.pagination.pageCount,
                    )
                }
            repliesResult
                .onSuccess { response ->
                    newState = newState.copy(
                        replies = response.data.distinctBy { it.id },
                        repliesPage = response.meta.pagination.page,
                        repliesPageCount = response.meta.pagination.pageCount,
                    )
                }
            val bothFailed = myCommentsResult.isFailure && repliesResult.isFailure
            _uiState.value = newState.copy(
                isLoading = false,
                isRefreshing = false,
                isError = bothFailed && newState.myComments.isEmpty() && newState.replies.isEmpty(),
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

    private suspend fun loadRepliesPage(page: Int) {
        _uiState.value = _uiState.value.copy(isLoadingMoreReplies = true)
        repository.fetchNotifications(page = page, type = UserNotification.TYPE_COMMENT_REPLY)
            .onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMoreReplies = false,
                    replies = (_uiState.value.replies + response.data).distinctBy { it.id },
                    repliesPage = response.meta.pagination.page,
                    repliesPageCount = response.meta.pagination.pageCount,
                )
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(isLoadingMoreReplies = false)
            }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): NotificationComponent
    }
}
