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

package com.shifenmiao.webview.resource.interceptor

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import com.shifenmiao.webview.resource.WebResourceEngine

/**
 * 兜底拦截器：把 `https://appassets.androidplatform.net/...` 的请求交由 [WebViewAssetLoader] 处理。
 *
 * [WebViewAssetLoader] 内置 AssetsPathHandler / ResourcesPathHandler，
 * 处理 `/assets/`、`/res/`、`/resources/` 等标准路径。
 * 命中失败返回 null，引擎继续放行 WebView 默认网络流程。
 */
class WebViewAssetLoaderInterceptor : WebResourceInterceptor {

    override fun intercept(
        request: WebResourceRequest,
        engine: WebResourceEngine,
    ): WebResourceResponse? {
        if (request.url.host != WebViewAssetLoader.DEFAULT_DOMAIN) return null
        return engine.assetLoader.shouldInterceptRequest(request.url)
    }
}
