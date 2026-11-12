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

package com.shifenmiao.ai.di

import com.shifenmiao.ai.repository.ConversationRepository
import com.shifenmiao.ai.repository.DefaultConversationRepository
import com.shifenmiao.ai.repository.DefaultMessageRepository
import com.shifenmiao.ai.repository.DefaultPromptRepository
import com.shifenmiao.ai.repository.MessageRepository
import com.shifenmiao.ai.repository.PromptRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPromptRepository(
        impl: DefaultPromptRepository,
    ): PromptRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        impl: DefaultConversationRepository,
    ): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: DefaultMessageRepository,
    ): MessageRepository
}

