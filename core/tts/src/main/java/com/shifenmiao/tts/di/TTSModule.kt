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
