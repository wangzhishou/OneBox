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

import android.webkit.WebResourceRequest

/**
 * 资源匹配策略。
 *
 * 设计要点：
 * - **短路求值**：[matches] 内部按成本递增顺序（精确 → 前缀 → host）排列，
 *   让热路径（如 full-URL 等值）零开销命中。
 * - **大小写不敏感**：[Host] 匹配按 host lowercase 比对；[ExactUrl] / [UrlPrefix]
 *   按字符串 equals（调用方负责大小写，URL 在 shouldInterceptRequest 阶段已规范化）。
 */
sealed interface WebResourceMatch {

    fun matches(request: WebResourceRequest): Boolean

    /** 匹配 host（path 任意）。如 `cdn.tailwindcss.com` 命中该 host 下所有请求。 */
    data class Host(val host: String) : WebResourceMatch {
        override fun matches(request: WebResourceRequest): Boolean =
            request.url.host?.equals(host, ignoreCase = true) == true
    }

    /** 完整 URL 等值匹配（包含 query）。 */
    data class ExactUrl(val url: String) : WebResourceMatch {
        override fun matches(request: WebResourceRequest): Boolean =
            request.url.toString() == url
    }

    /** 完整 URL 前缀匹配（包含 query）。 */
    data class UrlPrefix(val prefix: String) : WebResourceMatch {
        override fun matches(request: WebResourceRequest): Boolean =
            request.url.toString().startsWith(prefix)
    }
}
