package com.wanbaohe.notification.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.network.model.notification.UserNotification
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.notification.service.NotificationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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
    /** 分页追加中 */
    val isLoadingMore: Boolean = false,
    /** 首屏加载失败(展示错误态) */
    val isError: Boolean = false,
    val items: List<UserNotification> = emptyList(),
    val page: Int = 0,
    val pageCount: Int = 1,
)

/**
 * 消息中心 Component — 列表分页追加 + 已读操作编排。
 *
 * 未读数由 [NotificationRepository.unreadCount] 全局共享,
 * 标记已读/全部已读直接同步给个人中心角标。
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
            loadPage(page = 1, isRefresh = false)
            repository.refreshUnreadCount()
        }
    }

    /** 登录引导:登录成功后以登录态重新加载列表 */
    fun login() {
        ActionUtils.showLogin(source = "notification_center") {
            _uiState.value = _uiState.value.copy(isLoggedIn = true)
            componentScope.launch {
                loadPage(page = 1, isRefresh = false)
                repository.refreshUnreadCount()
            }
        }
    }

    fun refresh() {
        if (!_uiState.value.isLoggedIn || _uiState.value.isRefreshing) return
        componentScope.launch {
            loadPage(page = 1, isRefresh = true)
            repository.refreshUnreadCount()
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.isLoggedIn || state.isLoading || state.isRefreshing || state.isLoadingMore) return
        if (state.page >= state.pageCount) return
        componentScope.launch {
            loadPage(page = state.page + 1, isRefresh = false)
        }
    }

    /** 点击单条:已读的忽略,未读的乐观更新后调接口 */
    fun markRead(item: UserNotification) {
        if (item.read) return
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map {
                if (it.id == item.id) it.copy(read = true) else it
            }
        )
        componentScope.launch {
            repository.markRead(item.id)
        }
    }

    fun markAllRead() {
        if (_uiState.value.items.none { !it.read }) return
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map { it.copy(read = true) }
        )
        componentScope.launch {
            repository.markAllRead()
        }
    }

    private suspend fun loadPage(page: Int, isRefresh: Boolean) {
        val firstPage = page == 1
        _uiState.value = _uiState.value.copy(
            isLoading = firstPage && !isRefresh && _uiState.value.items.isEmpty(),
            isRefreshing = isRefresh,
            isLoadingMore = !firstPage,
            isError = false,
        )
        repository.fetchNotifications(page = page)
            .onSuccess { response ->
                val merged = if (firstPage) {
                    response.data
                } else {
                    _uiState.value.items + response.data
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    isError = false,
                    items = merged.distinctBy { it.id },
                    page = response.meta.pagination.page,
                    pageCount = response.meta.pagination.pageCount,
                )
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    // 已有列表数据时追加失败不打断浏览,仅首屏展示错误态
                    isError = _uiState.value.items.isEmpty(),
                )
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
