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

package com.shifenmiao.imagegeneration.di

import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.imagegeneration.provider.ImageGenerationProvider
import com.shifenmiao.imagegeneration.provider.openai.OpenAiImageApi
import com.shifenmiao.imagegeneration.provider.openai.OpenAiImageProvider
import com.shifenmiao.network.NetworkBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.shifenmiao.model.ModelProvider
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * OpenAI 生图 provider 的注册模块,只编进海外渠道(src/overseas = google + foss)。
 * 纯 BYOK:只提供直连 API,复用不装 AuthInterceptor 的直连 client。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class OpenAiImageModule {
    @Binds
    @IntoSet
    abstract fun bindOpenAiImageProvider(
        provider: OpenAiImageProvider,
    ): ImageGenerationProvider

    companion object {
        @Provides
        @Singleton
        @Named("DirectOpenAiImageApi")
        fun provideDirectOpenAiApi(
            @Named("DirectImageGenerationClient") client: OkHttpClient,
        ): OpenAiImageApi = Retrofit.Builder()
            .baseUrl(UrlConstants.OPENAI_BASE_URL)
            .addConverterFactory(ModelProvider.AppJson.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .build()
            .create(OpenAiImageApi::class.java)
    }
}
