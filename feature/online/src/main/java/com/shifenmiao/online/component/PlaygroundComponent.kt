package com.shifenmiao.online.component

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.blog.BlogRepository
import com.shifenmiao.common.logic.CommonComponent
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.model.blog.BlogItem
import com.shifenmiao.network.api.ApiService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 「玩法」Tab 业务组件。
 *
 * 数据来自 `/api/blogs?blogType=2`，进入页面时自动刷新并写入 Room，列表从本地分页读取。
 */
class PlaygroundComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    settingsManager: SettingsManager,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    apiService: ApiService,
    fileController: FileController,
    private val blogRepository: BlogRepository,
) : CommonComponent(
    settingsManager = settingsManager,
    dispatchersHolder = dispatchersHolder,
    componentContext = componentContext,
    appDatabase = appDatabase,
    apiService = apiService,
    fileController = fileController,
) {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = MutableStateFlow<Throwable?>(null)
    val refreshError: StateFlow<Throwable?> = _refreshError.asStateFlow()

    private val _lastRefreshTime = MutableStateFlow(0L)

    private val blogPagingFlow: Flow<PagingData<BlogItem>> by lazy {
        blogRepository.pagingFlow(BLOG_TYPE_PLAYGROUND)
            .cachedIn(componentScope)
    }

    fun getBlogFlow(): Flow<PagingData<BlogItem>> = blogPagingFlow

    fun refresh() {
        if (_isRefreshing.value) return
        componentScope.launch(ioDispatcher) {
            _isRefreshing.value = true
            _refreshError.value = null
            try {
                val result = blogRepository.refresh(BLOG_TYPE_PLAYGROUND)
                result.onSuccess {
                    _lastRefreshTime.value = System.currentTimeMillis()
                }
                result.onFailure { error ->
                    _refreshError.value = error
                    AppToastHost.showToast(
                        error.message ?: "加载失败，请稍后重试"
                    )
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * 仅在首次进入或距离上次成功刷新超过 [REFRESH_INTERVAL_MS] 时才发起刷新，
     * 避免玩法 Tab 每次重新可见都重新请求（玩法内容更新不频繁）。
     */
    fun refreshIfNeeded() {
        val last = _lastRefreshTime.value
        val elapsed = System.currentTimeMillis() - last
        if (last == 0L || elapsed >= REFRESH_INTERVAL_MS) {
            refresh()
        }
    }

    fun consumeRefreshError() {
        _refreshError.value = null
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
        ): PlaygroundComponent
    }

    companion object {
        const val BLOG_TYPE_PLAYGROUND = 2

        /** 玩法 Tab 自动刷新最小间隔：5 分钟 */
        private const val REFRESH_INTERVAL_MS = 5 * 60 * 1000L
    }
}
