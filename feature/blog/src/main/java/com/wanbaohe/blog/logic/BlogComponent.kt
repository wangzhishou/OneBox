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

package com.wanbaohe.blog.logic

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.logic.CommonComponent
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.model.ScreenParams
import com.shifenmiao.model.blog.BlogDetailState
import com.shifenmiao.model.blog.BlogItem
import com.shifenmiao.network.api.ApiService
import com.wanbaohe.blog.repository.FeedbackRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager

class BlogComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val screenParams: ScreenParams?,
    settingsManager: SettingsManager,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    apiService: ApiService,
    fileController: FileController
) : CommonComponent(
    settingsManager,
    dispatchersHolder,
    componentContext,
    appDatabase,
    apiService,
    fileController
) {

    private val blogRepository: FeedbackRepository = FeedbackRepository(
        apiService
    )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _blogDetailState = MutableStateFlow<BlogDetailState>(BlogDetailState.Loading)
    val blogDetailState: StateFlow<BlogDetailState> = _blogDetailState

    // Cached paging data flow
    private val blogPagingFlow: Flow<PagingData<BlogItem>> by lazy {
        blogRepository.getBlogPager().flow.cachedIn(componentScope)
    }

    // Get the blog paging data flow
    fun getBlogFlow(): Flow<PagingData<BlogItem>> = blogPagingFlow

    init {
        // Initialize the blog detail state if an ID is provided
        loadData()
    }

    fun refreshBlogs() {
        componentScope.launch {
            _isRefreshing.value = true
            try {
                // Force refresh by invalidating the paging source
                blogRepository.invalidatePagingSource()
                // Short delay to ensure UI shows refresh indicator
                delay(500)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadData() {
        screenParams?.id?.let {
            if (it > 0) {
                fetchBlogDetail(screenParams.id)
            }
        }
    }

    fun onRetry() {
        loadData()
    }

    fun fetchBlogDetail(blogId: Int) {
        componentScope.launch(ioDispatcher) {
            try {
                _blogDetailState.value = BlogDetailState.Loading
                val blogDetail = blogRepository.getBlogDetail(blogId, screenParams?.blogType)
                _blogDetailState.value = if (blogDetail != null) {
                    BlogDetailState.Success(blogDetail)
                } else {
                    BlogDetailState.Empty
                }
            } catch (e: Exception) {
                _blogDetailState.value = BlogDetailState.Error(e.message)
            }
        }
    }

    fun refreshBlogDetail() {
        componentScope.launch(ioDispatcher) {
            val blogId =
                (_blogDetailState.value as? BlogDetailState.Success)?.blog?.id ?: return@launch
            _blogDetailState.value = BlogDetailState.PageLoading
            fetchBlogDetail(blogId)
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            screenParams: ScreenParams?
        ): BlogComponent
    }
}