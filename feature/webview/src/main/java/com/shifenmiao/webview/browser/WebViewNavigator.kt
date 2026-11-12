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

package com.shifenmiao.webview.browser

import android.graphics.Bitmap
import android.print.PrintDocumentAdapter
import android.webkit.WebView

interface WebViewNavigator {
    /**
     * 把 WebView 接上。
     *
     * @return 挂上之前已经排队的内容（网址或收藏快照）是否已投给这个实例。
     * 为 true 时调用方不要再 loadUrl，否则会把快照盖掉。
     */
    fun attach(webView: WebView): Boolean
    fun detach()
    fun isAttached(): Boolean
    fun loadUrl(url: String)

    /**
     * 用 [baseUrl] 作为文档基准地址渲染一段 HTML 字符串（收藏快照离线查看）。
     * baseUrl 只影响相对路径/Cookie 归属，地址栏与历史仍是原始网址。
     */
    fun loadHtml(html: String, baseUrl: String?)

    fun goBack()
    fun goForward()
    fun reload()
    fun stopLoading()
    fun canGoBack(): Boolean
    fun canGoForward(): Boolean
    fun captureBitmap(): Bitmap?
    fun createPrintDocumentAdapter(jobName: String): PrintDocumentAdapter?
    fun evaluateJavascript(script: String, callback: (String?) -> Unit)
    fun applySettings(settings: BrowserSettings)
    fun clearCache()
}
