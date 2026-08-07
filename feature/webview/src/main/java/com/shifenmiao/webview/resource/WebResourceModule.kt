package com.shifenmiao.webview.resource

import android.content.Context
import androidx.webkit.WebViewAssetLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * WebView 资源引擎相关的 Hilt 绑定。
 *
 * 自动化绑定（无需 @Provides）：
 * - [com.shifenmiao.webview.resource.cache.WebResourceCache] — `@Inject constructor + @Singleton`
 * - [WebResourceEngine] — `@Inject constructor + @Singleton`
 *
 * 手写绑定：
 * - [WebViewAssetLoader] — androidx-webkit 提供，无 `@Inject` 构造器；
 *   三个标准路径（`/assets/` `/js/` `/res/`）与旧 [com.shifenmiao.webview.WebViewComponent] 保持一致。
 */
@Module
@InstallIn(SingletonComponent::class)
object WebResourceModule {

    @Provides
    @Singleton
    fun provideWebViewAssetLoader(
        @ApplicationContext context: Context,
    ): WebViewAssetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .addPathHandler("/js/", WebViewAssetLoader.AssetsPathHandler(context))
        .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(context))
        .build()
}
