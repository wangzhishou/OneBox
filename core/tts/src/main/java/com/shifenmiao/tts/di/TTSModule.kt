package com.shifenmiao.tts.di

import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.network.api.MimoTTSApi
import com.shifenmiao.network.api.TTSSpeechApi
import com.shifenmiao.tts.cache.TTSCacheManagerImpl
import com.shifenmiao.tts.service.MimoTTSProvider
import com.shifenmiao.tts.service.OpenAITTSProvider
import com.shifenmiao.tts.service.TTSCacheManager
import com.shifenmiao.tts.service.TTSService
import com.shifenmiao.tts.service.TTSServiceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    companion object {
        @Provides
        @Singleton
        fun provideTTSSpeechApi(
            @Named("OpenAICompatibleRetrofit") retrofit: Retrofit
        ): TTSSpeechApi = retrofit.create(TTSSpeechApi::class.java)

        @Provides
        @Singleton
        @Named("MimoDirectApi")
        fun provideMimoDirectApi(
            @Named("OpenAICompatibleOkHttpClient") okHttpClient: okhttp3.OkHttpClient
        ): MimoTTSApi {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.xiaomimimo.com/v1/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
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
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(MimoTTSApi::class.java)
        }

        @Provides
        @Singleton
        fun provideOpenAITTSProvider(api: TTSSpeechApi): OpenAITTSProvider =
            OpenAITTSProvider(api)

        @Provides
        @Singleton
        fun provideMimoTTSProvider(
            @Named("MimoDirectApi") directApi: MimoTTSApi,
            @Named("MimoProxyApi") proxyApi: MimoTTSApi,
        ): MimoTTSProvider = MimoTTSProvider(directApi, proxyApi)
    }
}
