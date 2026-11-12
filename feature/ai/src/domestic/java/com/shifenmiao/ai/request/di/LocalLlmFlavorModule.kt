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

import com.shifenmiao.ai.request.EmptyLocalLlmModelRegistry
import com.shifenmiao.ai.request.LocalLlmModelRegistry
import com.shifenmiao.ai.request.LocalLlmRuntime
import com.shifenmiao.ai.request.UnsupportedLocalLlmRuntime
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 国内渠道的端侧绑定:降级运行时(prepare 直接失败)+ 空注册表。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LocalLlmFlavorModule {

    @Binds
    @Singleton
    abstract fun bindLocalLlmRuntime(impl: UnsupportedLocalLlmRuntime): LocalLlmRuntime

    @Binds
    @Singleton
    abstract fun bindLocalLlmModelRegistry(impl: EmptyLocalLlmModelRegistry): LocalLlmModelRegistry
}
