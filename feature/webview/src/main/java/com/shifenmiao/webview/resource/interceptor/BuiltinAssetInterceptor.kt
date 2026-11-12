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
import com.shifenmiao.webview.resource.WebResourceEngine
import com.shifenmiao.webview.resource.WebResourceRule
import com.shifenmiao.webview.resource.firstMatchingOrNull

/**
 * 内置资源拦截器：优先匹配代码内硬编码的 [com.shifenmiao.webview.resource.BuiltinResources]。
 *
 * 实现要点：
 * - 不直接读 `AssetManager`，而是把请求改写到 `https://appassets.androidplatform.net/assets/<path>` 再让
 *   [androidx.webkit.WebViewAssetLoader] 解析，**完全复用**其缓存与并发安全保证。
 * - 具体改写逻辑在 [WebResourceEngine.serveAssetFromLoader]。
 */
class BuiltinAssetInterceptor : WebResourceInterceptor {

    override fun intercept(
        request: WebResourceRequest,
        engine: WebResourceEngine,
    ): WebResourceResponse? {
        val rule = engine.builtinRules.firstMatchingOrNull(request) as? WebResourceRule.AssetRule
            ?: return null
        return engine.serveAssetFromLoader(rule.assetPath)
    }
}
