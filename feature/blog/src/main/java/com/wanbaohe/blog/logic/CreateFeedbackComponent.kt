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

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.logic.CommonComponent
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.model.blog.FeedbackRequest
import com.shifenmiao.model.blog.Tag
import com.shifenmiao.network.api.ApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager

class CreateFeedbackComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val blogType: Int = 1,
    settingsManager: SettingsManager,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    private val apiService: ApiService,
    fileController: FileController
) : CommonComponent(
    settingsManager,
    dispatchersHolder,
    componentContext,
    appDatabase,
    apiService,
    fileController
) {

    private val _feedbackRequest = MutableStateFlow(
        FeedbackRequest(
            title = "",
            content = "",
            pictureIds = emptyList(),
            tagIds = emptyList(),
            blogType = blogType
        )
    )
    val feedbackRequest: StateFlow<FeedbackRequest> = _feedbackRequest.asStateFlow()

    private val _isDataChange = MutableStateFlow(false)
    val isDataChange: StateFlow<Boolean> = _isDataChange.asStateFlow()

    private val _isCheckError = MutableStateFlow(false)
    val isCheckError: StateFlow<Boolean> = _isCheckError.asStateFlow()

    private val _tags: MutableStateFlow<List<Tag>> = MutableStateFlow(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    // 初始选中只记 id, 名称在 fetchTags 成功后用接口下发的本地化文案补齐,
    // 避免硬编码中文与列表项重复展示
    private val _selectedTags: MutableStateFlow<List<Tag>> = MutableStateFlow(
        listOf(
            Tag(
                id = 1,
                name = "",
                desc = ""
            )
        )
    )
    val selectedTags: StateFlow<List<Tag>> = _selectedTags.asStateFlow()

    init {
        fetchTags()
        initTags()
    }

    private fun initTags() {
        componentScope.launch {
            _feedbackRequest.value = _feedbackRequest.value.copy(
                tagIds = _selectedTags.value.map { tag ->
                    tag.id
                }
            )
        }
    }

    private fun fetchTags() {
        componentScope.launch {
            try {
                val response = apiService.fetchBlogTags()
                if (response.isSuccessful) {
                    val fetched = response.body()?.data ?: emptyList()
                    _tags.value = fetched.distinctBy { tag -> tag.id }
                    // 选中项名称以接口下发的本地化文案为准, 同时保证选中区与列表按 id 一致
                    _selectedTags.value = _selectedTags.value.map { selected ->
                        fetched.firstOrNull { it.id == selected.id } ?: selected
                    }
                }
            } catch (_: Exception) {
                // Handle error
            }
        }
    }

    fun updateTitle(title: String) {
        componentScope.launch {
            if (title.isNotEmpty()) {
                _isDataChange.value = true
                _feedbackRequest.value = _feedbackRequest.value.copy(title = title)
            }
        }
    }

    fun updateContent(content: String) {
        componentScope.launch {
            if (content.isNotEmpty()) {
                _isDataChange.value = true
                _feedbackRequest.value = _feedbackRequest.value.copy(content = content)
            }
        }
    }

    private fun updateTags(tags: List<Int>) {
        componentScope.launch {
            _isDataChange.value = true
            // Use distinct() to filter out duplicate tag IDs
            _feedbackRequest.value = _feedbackRequest.value.copy(tagIds = tags.distinct())
        }
    }

    fun addImage(imageId: Int) {
        componentScope.launch {
            _isDataChange.value = true
            // Only add the image ID if it's not already in the list
            if (!_feedbackRequest.value.pictureIds.contains(imageId)) {
                _feedbackRequest.value = _feedbackRequest.value.copy(
                    pictureIds = _feedbackRequest.value.pictureIds + imageId
                )
            }
        }
    }

    private fun checkDataReady(): Boolean {
        return _feedbackRequest.value.title.isNotEmpty() &&
                _feedbackRequest.value.content.isNotEmpty()
    }

    private fun checkDataReadyDone(doFunction: () -> Unit) {
        if (checkDataReady()) {
            _isCheckError.value = false
            doFunction()
        } else {
            _isCheckError.value = true
        }
    }

    fun submitFeedback(
        onFailed: () -> Unit = {},
        onSuccess: () -> Unit = {}
    ) {
        checkDataReadyDone {
            componentScope.launch {
                try {
                    val response = apiService.createFeedback(_feedbackRequest.value)
                    if (response.isSuccessful) {
                        _isDataChange.value = false
                        onSuccess()
                    } else {
                        onFailed.invoke()
                    }
                } catch (_: Exception) {
                    onFailed.invoke()
                }
            }
        }
    }

    fun setSelected(selectTags: List<Tag>) {
        if (selectTags.isNotEmpty()) {
            componentScope.launch {
                _selectedTags.value = selectTags
                updateTags(
                    selectTags.map { tag ->
                        tag.id
                    }
                )
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            blogType: Int,
        ): CreateFeedbackComponent
    }
}