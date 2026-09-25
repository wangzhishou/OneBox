package com.wanbaohe.notification.service

import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.model.notification.UserNotificationListResponse
import com.shifenmiao.storage.TokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 消息中心数据仓库:通知列表分页拉取 + 未读数全局状态。
 *
 * [unreadCount] 是个人中心角标与消息中心共用的单一数据源:
 * 个人中心进入/恢复时调 [refreshUnreadCount] 拉取,
 * 消息中心内标记已读/全部已读时同步更新,保证返回后角标立即正确。
 * 全部接口需登录 JWT,未登录时静默跳过。
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val apiService: ApiService,
) {

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    suspend fun fetchNotifications(
        page: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): Result<UserNotificationListResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.listUserNotifications(page = page, pageSize = pageSize)
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                error("fetch notifications failed: ${response.code()}")
            }
            body
        }
    }

    /** 单条标已读,成功后本地未读数减一 */
    suspend fun markRead(id: Int): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.markNotificationRead(id)
            if (response.isSuccessful) {
                decrementUnread()
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }

    /** 全部标已读,成功后未读数清零 */
    suspend fun markAllRead(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.markAllNotificationsRead()
            if (response.isSuccessful) {
                _unreadCount.value = 0
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }

    /** 拉取最新未读数;未登录时清零并跳过 */
    suspend fun refreshUnreadCount() {
        if (!TokenStorage.isLogin()) {
            _unreadCount.value = 0
            return
        }
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.unreadNotificationCount()
                if (response.isSuccessful) {
                    _unreadCount.value = response.body()?.data?.count ?: 0
                }
            }
        }
    }

    fun setUnreadCount(count: Int) {
        _unreadCount.value = count.coerceAtLeast(0)
    }

    private fun decrementUnread() {
        _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
