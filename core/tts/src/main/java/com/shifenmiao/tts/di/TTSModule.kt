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

package com.shifenmiao.tts.di

import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ModelProvider
import com.shifenmiao.network.api.MimoTTSApi
import com.shifenmiao.network.api.TTSSpeechApi
import com.shifenmiao.tts.cache.TTSCacheManagerImpl
import com.shifenmiao.tts.service.MimoTTSProvider
import com.shifenmiao.tts.service.OpenAITTSProvider
import com.shifenmiao.tts.service.TTSCacheManager
import com.shifenmiao.tts.service.TTSProvider
import com.shifenmiao.tts.service.TTSService
import com.shifenmiao.tts.service.TTSServiceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class TTSModule {

    @Binds
    @Singleton
    abstract fun bindTTSService(impl: TTSServiceImpl): TTSService

    @Binds
    @Singleton
    abstract fun bindTTSCacheManager(impl: TTSCacheManagerImpl): TTSCacheManager

    @Binds
    @IntoSet
    abstract fun bindMimoTTSProvider(provider: MimoTTSProvider): TTSProvider

    @Binds
    @IntoSet
    abstract fun bindOpenAITTSProvider(provider: OpenAITTSProvider): TTSProvider

    companion object {
        // TTSSpeechApi 已迁移 kotlinx.serialization(阶段①):OpenAICompatibleRetrofit 仍属
        // Gson 阵营(流式 service 在用),这里用同一 OkHttpClient 单独构造 kotlinx converter 实例;
        // 实际请求路径由 @Url 传入,baseUrl 仅作占位
        @Provides
        @Singleton
        fun provideTTSSpeechApi(
            @Named("OpenAICompatibleOkHttpClient") okHttpClient: okhttp3.OkHttpClient
        ): TTSSpeechApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlConstants.OPENAI_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(ModelProvider.AppJson.asConverterFactory("application/json".toMediaType()))
                .build()
            return retrofit.create(TTSSpeechApi::class.java)
        }

        @Provides
        @Singleton
        @Named("MimoDirectApi")
        fun provideMimoDirectApi(
            @Named("OpenAICompatibleOkHttpClient") okHttpClient: okhttp3.OkHttpClient
        ): MimoTTSApi {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.xiaomimimo.com/v1/")
                .client(okHttpClient)
                .addConverterFactory(ModelProvider.AppJson.asConverterFactory("application/json".toMediaType()))
                .build()
            return retrofit.create(MimoTTSApi::class.java)
        }

        @Provides
        @Singleton
        @Named("MimoProxyApi")
        fun provideMimoProxyApi(
            @Named("DefaultOkHttpClient") okHttpClient: okhttp3.OkHttpClient
        ): MimoTTSApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(UrlConstants.RELEASE_URL.ifBlank { "http://localhost/" })
                .client(okHttpClient)
                .addConverterFactory(ModelProvider.AppJson.asConverterFactory("application/json".toMediaType()))
                .build()
            return retrofit.create(MimoTTSApi::class.java)
        }
    }
}
