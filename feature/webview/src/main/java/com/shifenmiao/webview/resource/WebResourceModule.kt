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
