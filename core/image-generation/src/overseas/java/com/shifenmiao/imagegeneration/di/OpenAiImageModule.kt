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
import retrofit2.converter.gson.GsonConverterFactory
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
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(OpenAiImageApi::class.java)
    }
}
