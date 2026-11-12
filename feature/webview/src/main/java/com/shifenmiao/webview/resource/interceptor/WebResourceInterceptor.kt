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

/**
 * 资源拦截器节点。链式调用，**第一个返回非 null 的拦截器胜出**。
 *
 * 设计原则：
 * - 拦截器**无状态**，可被多 WebView 并发复用。
 * - 拦截器**只读** [WebResourceEngine] 暴露的规则 / 缓存 / 资源，不持有可变状态。
 * - 拦截失败 / 资源不存在时返回 null，**绝不抛异常**（异常会被 WebView 当作 network error 处理，
 *   污染主框架错误状态）。
 */
fun interface WebResourceInterceptor {
    fun intercept(request: WebResourceRequest, engine: WebResourceEngine): WebResourceResponse?
}
