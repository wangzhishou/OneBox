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

package com.wanbaohe.setting.prompt.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SystemPromptDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val promptId: Int,
    @Assisted val onGoBack: () -> Unit,
    private val appDatabase: AppDatabase,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _prompt = MutableStateFlow<PromptEntity?>(null)
    val prompt: StateFlow<PromptEntity?> = _prompt.asStateFlow()

    init {
        componentScope.launch(ioDispatcher) {
            val entity = appDatabase.chatPromptDao().getPromptById(promptId)
            _prompt.value = entity
        }
    }

    fun save(
        content: String,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {},
    ) {
        val current = _prompt.value ?: run {
            onFailure("Prompt not loaded")
            return
        }
        componentScope.launch(ioDispatcher) {
            try {
                val updated = current.copy(
                    prompt = content,
                    updatedAt = System.currentTimeMillis()
                )
                appDatabase.chatPromptDao().upsertLocalPrompt(updated)
                _prompt.value = updated
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            promptId: Int,
            onGoBack: () -> Unit,
        ): SystemPromptDetailComponent
    }
}
