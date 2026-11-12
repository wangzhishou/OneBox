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

package com.shifenmiao.ai.request.di

import com.shifenmiao.ai.request.DefaultLlmContextBudgetService
import com.shifenmiao.ai.request.DefaultOnDeviceLlmService
import com.shifenmiao.ai.request.LlmContextBudgetService
import com.shifenmiao.ai.request.LlmProviderAdapter
import com.shifenmiao.ai.request.LocalOnDeviceAdapter
import com.shifenmiao.ai.request.OnDeviceLlmService
import com.shifenmiao.ai.request.adapter.AnthropicAdapter
import com.shifenmiao.ai.request.adapter.OpenAiChatAdapter
import com.shifenmiao.ai.request.adapter.OwnProxyAdapter
import com.shifenmiao.ai.request.adapter.ResponsesAdapter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LlmRequestBindingsModule {

    @Binds
    @IntoSet
    abstract fun bindOpenAiChatAdapter(impl: OpenAiChatAdapter): LlmProviderAdapter

    @Binds
    @IntoSet
    abstract fun bindResponsesAdapter(impl: ResponsesAdapter): LlmProviderAdapter

    @Binds
    @IntoSet
    abstract fun bindAnthropicAdapter(impl: AnthropicAdapter): LlmProviderAdapter

    @Binds
    @IntoSet
    abstract fun bindOwnProxyAdapter(impl: OwnProxyAdapter): LlmProviderAdapter

    @Binds
    @IntoSet
    abstract fun bindLocalOnDeviceAdapter(impl: LocalOnDeviceAdapter): LlmProviderAdapter

    @Binds
    @Singleton
    abstract fun bindOnDeviceLlmService(impl: DefaultOnDeviceLlmService): OnDeviceLlmService

    @Binds
    abstract fun bindLlmContextBudgetService(impl: DefaultLlmContextBudgetService): LlmContextBudgetService
}
