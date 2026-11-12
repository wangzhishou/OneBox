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

package com.wanbaohe.dsh.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient

/**
 * DSH 专用 OkHttpClient,刻意不复用全局 DefaultOkHttpClient:
 * 全局客户端挂有 DynamicBaseUrlInterceptor(会把请求 scheme/host/port 改写为
 * App 后端 baseUrl)以及 Auth/GlobalParams/Unauthorized/ErrorHandling 等
 * App 后端专用拦截器,会污染 DSH 流量(DSH 地址由用户输入,与 App 后端无关)。
 */
@Module
@InstallIn(SingletonComponent::class)
object DshModule {

    @Provides
    @Singleton
    @Named("DshOkHttpClient")
    fun provideDshOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
}
