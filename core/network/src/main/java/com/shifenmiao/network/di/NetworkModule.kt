package com.shifenmiao.network.di

import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ModelProvider
import com.shifenmiao.network.BuildConfig
import com.shifenmiao.network.NetworkBuilder
import com.shifenmiao.network.NetworkBuilder.cache
import com.shifenmiao.network.api.AnthropicCompatibleService
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.api.BaiduImageProcessApiService
import com.shifenmiao.network.api.DocConvertApiService
import com.shifenmiao.network.api.OpenAICompatibleService
import com.shifenmiao.network.api.OpenAIWithApiKeyService
import com.shifenmiao.network.api.OwnProxyAIService
import com.shifenmiao.network.api.QwenImageService
import com.shifenmiao.network.downloader.HtmlDownloader
import com.shifenmiao.network.downloader.OkHttpHtmlDownloader
import com.shifenmiao.network.interceptor.AuthInterceptor
import com.shifenmiao.network.interceptor.CacheInterceptor
import com.shifenmiao.network.interceptor.DynamicBaseUrlInterceptor
import com.shifenmiao.network.interceptor.ErrorHandlingInterceptor
import com.shifenmiao.network.interceptor.GlobalParamsInterceptor
import com.shifenmiao.network.interceptor.UnauthorizedInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Singleton
    @Provides
    fun provideHtmlDownloader(): HtmlDownloader {
        return OkHttpHtmlDownloader()
    }


    @Singleton
    @Provides
    @Named("DynamicBaseUrlInterceptor")
    fun provideDynamicBaseUrlInterceptor(): DynamicBaseUrlInterceptor {
        return DynamicBaseUrlInterceptor {
            NetworkBuilder.getBaseUrl()
        }
    }

    @Singleton
    @Provides
    @Named("DefaultOkHttpClient")
    fun provideOkHttpClient(
        @Named("DynamicBaseUrlInterceptor") dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor
    ) = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(dynamicBaseUrlInterceptor)
        .addInterceptor(AuthInterceptor()) // 请求头带上鉴权信息
        .addInterceptor(GlobalParamsInterceptor()) // 如果需要添加拦截器
        .addInterceptor(CacheInterceptor())
        .addInterceptor(UnauthorizedInterceptor())
        .addInterceptor(ErrorHandlingInterceptor())
        .connectTimeout(NetworkBuilder.getTimeOut(), TimeUnit.MINUTES)
        .readTimeout(NetworkBuilder.getTimeOut(), TimeUnit.MINUTES)
        .writeTimeout(NetworkBuilder.getTimeOut(), TimeUnit.MINUTES)
        .addInterceptor(NetworkBuilder.provideHttpLoggingInterceptor())
        .build()

    @Singleton
    @Provides
    @Named("OkHttpClientForBaiduOcr")
    fun provideOkHttpClientForBaiduOcr() = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor()) // 请求头带上鉴权信息
        .addInterceptor(GlobalParamsInterceptor()) // 如果需要添加拦截器
        .addInterceptor(CacheInterceptor())
        .addInterceptor(UnauthorizedInterceptor())
        .addInterceptor(ErrorHandlingInterceptor())
        .connectTimeout(NetworkBuilder.getTimeOut(), TimeUnit.MINUTES)
        .readTimeout(NetworkBuilder.getTimeOut(), TimeUnit.MINUTES)
        .writeTimeout(NetworkBuilder.getTimeOut(), TimeUnit.MINUTES)
        .addInterceptor(ErrorHandlingInterceptor())
        .addInterceptor(NetworkBuilder.provideHttpLoggingInterceptor())
        .build()

    @Singleton
    @Provides
    @Named("OpenAICompatibleOkHttpClient")
    // 直连 OpenAI-compatible 请求通过 OpenAICompatibleService 的 @Header("Authorization")
    // 按引擎配置单独传递鉴权信息；这里不要挂 AuthInterceptor，避免在用户未配置
    // provider token 时把 App 登录 token / 远端 accessToken 发到第三方直连地址。
    //
    // 流式 SSE 友好的超时配置：
    //  - connectTimeout 30s：建连阶段不应过长；
    //  - readTimeout 60s：作为"两次 chunk 之间最大空闲"。OkHttp 对 SSE 的 readTimeout
    //    指的是 socket 单次 read 的最大空闲时间，而不是整次响应的总时长；服务端只要保持
    //    定期发送 chunk（哪怕是 keep-alive 注释行）就不会超时；
    //  - writeTimeout 30s：上传体不大，30s 足够；
    //  - callTimeout 0：不限制整次请求的总时长（长回答可能持续数分钟）；
    //  - pingInterval 20s：HTTP/2 下主动 PING，便于尽早发现半开连接；
    //  - retryOnConnectionFailure true：建连/IO 抖动时由 OkHttp 自动重试一次。
    fun provideOpenAICompatibleOkHttpClient(): OkHttpClient {
        val appVersion = BuildConfig.VersionName
            .takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }
            ?: BuildConfig.VersionCode

        return OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "OneBox/$appVersion (Android ${android.os.Build.VERSION.SDK_INT})")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(ErrorHandlingInterceptor())
        .addInterceptor(NetworkBuilder.provideHttpLoggingInterceptor())
        .build()
    }

    @Singleton
    @Provides
    @Named("QwenImageOkHttpClient")
    fun provideQwenImageOkHttpClient(): OkHttpClient {
        val appVersion = BuildConfig.VersionName
            .takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }
            ?: BuildConfig.VersionCode

        // 不安装 AuthInterceptor：百炼 Token 由请求显式传入，避免泄露 App 登录凭证。
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .callTimeout(0, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "OneBox/$appVersion (Android ${android.os.Build.VERSION.SDK_INT})")
                        .build()
                )
            }
            .addInterceptor(ErrorHandlingInterceptor())
            .addInterceptor(NetworkBuilder.provideHttpLoggingInterceptor())
            .build()
    }

    @Singleton
    @Provides
    @Named("DefaultRetrofit")
    fun provideDefaultRetrofit(@Named("DefaultOkHttpClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkBuilder.getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create(ModelProvider.provideGson()))
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    @Named("OpenAICompatibleRetrofit")
    fun provideOpenAICompatibleRetrofit(
        @Named("OpenAICompatibleOkHttpClient") okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(UrlConstants.OPENAI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(ModelProvider.provideGson()))
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    @Named("QwenImageRetrofit")
    fun provideQwenImageRetrofit(
        @Named("QwenImageOkHttpClient") okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(UrlConstants.Q_WEN_AI_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(ModelProvider.provideGson()))
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    @Named("ProxyQwenImageOkHttpClient")
    fun provideProxyQwenImageOkHttpClient(
        @Named("DynamicBaseUrlInterceptor") dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor
    ): OkHttpClient {
        // 代理路由需要 AuthInterceptor 携带 App 登录凭证；生图可能耗时数分钟，
        // 因此使用与直连路由对齐的长超时配置。
        return OkHttpClient.Builder()
            .addInterceptor(dynamicBaseUrlInterceptor)
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
    }

    @Singleton
    @Provides
    @Named("ProxyQwenImageRetrofit")
    fun provideProxyQwenImageRetrofit(
        @Named("ProxyQwenImageOkHttpClient") okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(NetworkBuilder.getBaseUrl())
        .addConverterFactory(GsonConverterFactory.create(ModelProvider.provideGson()))
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    @Named("BaiduOcrRetrofit")
    fun provideBaiduOcrRetrofit(@Named("OkHttpClientForBaiduOcr") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(UrlConstants.BAIDU_OCR_BASE_URL.ifBlank { "http://localhost/" })
            .addConverterFactory(GsonConverterFactory.create(ModelProvider.provideGson()))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@Named("DefaultRetrofit") retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideDocConvertApiService(@Named("BaiduOcrRetrofit") retrofit: Retrofit): DocConvertApiService =
        retrofit.create(DocConvertApiService::class.java)

    @Provides
    @Singleton
    fun provideBaiduImageProcessApiService(@Named("BaiduOcrRetrofit") retrofit: Retrofit): BaiduImageProcessApiService =
        retrofit.create(BaiduImageProcessApiService::class.java)


    @Provides
    @Singleton
    fun provideOpenAICompatibleService(
        @Named("OpenAICompatibleRetrofit") retrofit: Retrofit
    ): OpenAICompatibleService = retrofit.create(OpenAICompatibleService::class.java)

    @Provides
    @Singleton
    fun provideOwnProxyAIService(@Named("DefaultRetrofit") retrofit: Retrofit): OwnProxyAIService =
        retrofit.create(OwnProxyAIService::class.java)

    @Provides
    @Singleton
    fun provideOpenAIWithApiKeyService(
        @Named("OpenAICompatibleRetrofit") retrofit: Retrofit
    ): OpenAIWithApiKeyService = retrofit.create(OpenAIWithApiKeyService::class.java)

    @Provides
    @Singleton
    fun provideAnthropicCompatibleService(
        @Named("OpenAICompatibleRetrofit") retrofit: Retrofit
    ): AnthropicCompatibleService = retrofit.create(AnthropicCompatibleService::class.java)

    @Provides
    @Singleton
    @Named("DirectQwenImageService")
    fun provideDirectQwenImageService(
        @Named("QwenImageRetrofit") retrofit: Retrofit
    ): QwenImageService = retrofit.create(QwenImageService::class.java)

    @Provides
    @Singleton
    @Named("ProxyQwenImageService")
    fun provideProxyQwenImageService(
        @Named("ProxyQwenImageRetrofit") retrofit: Retrofit
    ): QwenImageService = retrofit.create(QwenImageService::class.java)

}
