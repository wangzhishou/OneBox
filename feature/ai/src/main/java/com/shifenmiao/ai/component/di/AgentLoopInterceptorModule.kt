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

package com.shifenmiao.ai.component.di

import com.shifenmiao.ai.component.AgentLoopInterceptor
import com.shifenmiao.ai.component.AgentLoopLoggingInterceptor
import com.shifenmiao.ai.component.AgentLoopUsageObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * AgentLoopInterceptor 多绑定模块 —— 通过 @IntoSet 注册循环拦截器。
 *
 * 新增拦截器时在此追加一个 @Provides @IntoSet 即可,
 * AgentLoopRunner / AgentLoopExecutor 注入的 Set<AgentLoopInterceptor> 自动收齐。
 */
@Module
@InstallIn(SingletonComponent::class)
object AgentLoopInterceptorModule {

    @Provides
    @IntoSet
    fun provideLoggingInterceptor(impl: AgentLoopLoggingInterceptor): AgentLoopInterceptor = impl

    @Provides
    @IntoSet
    fun provideUsageObserver(impl: AgentLoopUsageObserver): AgentLoopInterceptor = impl
}
