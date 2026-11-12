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
 * 远程资源拦截器：匹配 [WebResourceEngine.remoteRules]（来自 RemoteConfig / 远程下发），
 * 并通过 [com.shifenmiao.webview.resource.cache.WebResourceCache] 取真实 URL 的内容。
 *
 * 支持两种规则：
 * - [WebResourceRule.AssetRule] — 同 [BuiltinAssetInterceptor]，但用远程下发的路径
 *   （允许运营动态切换本地资源）。
 * - [WebResourceRule.RemoteUrlRule] — 命中后 fetch realUrl（带 HTTP 缓存），用真实资源响应。
 */
class RemoteRuleInterceptor : WebResourceInterceptor {

    override fun intercept(
        request: WebResourceRequest,
        engine: WebResourceEngine,
    ): WebResourceResponse? {
        val rule = engine.remoteRules.value.firstMatchingOrNull(request) ?: return null
        return when (rule) {
            is WebResourceRule.AssetRule -> engine.serveAssetFromLoader(rule.assetPath)
            is WebResourceRule.RemoteUrlRule -> engine.cache.fetchBlocking(rule.realUrl, rule.cacheTtlMillis)
        }
    }
}
