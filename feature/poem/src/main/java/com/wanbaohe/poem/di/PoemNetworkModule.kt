package com.wanbaohe.poem.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PoemNetworkModule {

    /** 诗泉 API 专用 OkHttpClient:无鉴权拦截器,普通 JSON 请求超时 */
    @Provides
    @Singleton
    @Named("PoemOkHttpClient")
    fun providePoemOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
}
