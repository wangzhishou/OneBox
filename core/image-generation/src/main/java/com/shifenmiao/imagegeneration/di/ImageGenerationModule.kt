package com.shifenmiao.imagegeneration.di

import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.imagegeneration.loader.ImageGenerationLoader
import com.shifenmiao.imagegeneration.loader.ImageGenerationLoaderImpl
import com.shifenmiao.imagegeneration.provider.ImageGenerationProvider
import com.shifenmiao.imagegeneration.provider.qwen.QwenImageApi
import com.shifenmiao.imagegeneration.provider.qwen.QwenImageProvider
import com.shifenmiao.imagegeneration.service.ImageGenerationManager
import com.shifenmiao.imagegeneration.service.ImageGenerationManagerImpl
import com.shifenmiao.network.NetworkBuilder
import com.shifenmiao.network.interceptor.AuthInterceptor
import com.shifenmiao.network.interceptor.ErrorHandlingInterceptor
import com.shifenmiao.network.interceptor.GlobalParamsInterceptor
import com.shifenmiao.network.interceptor.UnauthorizedInterceptor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageGenerationModule {
    @Binds
    @Singleton
    abstract fun bindImageGenerationManager(
        impl: ImageGenerationManagerImpl,
    ): ImageGenerationManager

    @Binds
    @Singleton
    abstract fun bindImageGenerationLoader(
        impl: ImageGenerationLoaderImpl,
    ): ImageGenerationLoader

    @Binds
    @IntoSet
    abstract fun bindQwenImageProvider(
        provider: QwenImageProvider,
    ): ImageGenerationProvider

    companion object {
        @Provides
        @Singleton
        @Named("DirectImageGenerationClient")
        fun provideDirectClient(): OkHttpClient = OkHttpClient.Builder()
            // 不安装 AuthInterceptor，防止把 App 登录凭证泄露给第三方 Provider。
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .callTimeout(0, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(ErrorHandlingInterceptor())
            .addInterceptor(NetworkBuilder.provideHttpLoggingInterceptor())
            .build()

        @Provides
        @Singleton
        @Named("ProxyImageGenerationClient")
        fun provideProxyClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(GlobalParamsInterceptor())
            .addInterceptor(UnauthorizedInterceptor())
            .addInterceptor(ErrorHandlingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .callTimeout(0, TimeUnit.MILLISECONDS)
            .addInterceptor(NetworkBuilder.provideHttpLoggingInterceptor())
            .build()

        @Provides
        @Singleton
        @Named("DirectImageGenerationApi")
        fun provideDirectApi(
            @Named("DirectImageGenerationClient") client: OkHttpClient,
        ): QwenImageApi = Retrofit.Builder()
            .baseUrl(UrlConstants.Q_WEN_AI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(QwenImageApi::class.java)

        @Provides
        @Singleton
        @Named("ProxyImageGenerationApi")
        fun provideProxyApi(
            @Named("ProxyImageGenerationClient") client: OkHttpClient,
        ): QwenImageApi = Retrofit.Builder()
            .baseUrl(NetworkBuilder.getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(QwenImageApi::class.java)
    }
}
