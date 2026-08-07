package com.wanbaohe.speedtest.data.di

import com.wanbaohe.speedtest.data.SpeedTestConfigRepository
import com.wanbaohe.speedtest.data.SpeedTestConfigRepositoryImpl
import com.wanbaohe.speedtest.data.SpeedTestRepository
import com.wanbaohe.speedtest.data.SpeedTestRepositoryImpl
import dagger.Binds
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
abstract class SpeedTestModule {

    /** 将 SpeedTestRepositoryImpl 绑定为 SpeedTestRepository */
    @Binds
    @Singleton
    abstract fun bindSpeedTestRepository(impl: SpeedTestRepositoryImpl): SpeedTestRepository

    @Binds
    @Singleton
    abstract fun bindSpeedTestConfigRepository(impl: SpeedTestConfigRepositoryImpl): SpeedTestConfigRepository

    companion object {
        /** 专用 OkHttpClient：无鉴权拦截，超时适配大文件下载 */
        @Provides
        @Named("SpeedTestOkHttpClient")
        fun provideSpeedTestOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
