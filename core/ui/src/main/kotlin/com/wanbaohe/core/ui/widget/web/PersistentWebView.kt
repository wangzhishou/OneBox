/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.core.ui.widget.web

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.web.WebContentView

/**
 * 挂载由 [session] 持有的 Web 会话。WEB_CONTENT 不可用时，[viewFactory] 创建的
 * [WebContentView] 按普通 WebView 使用，并在离开组合时走旧版销毁流程。
 */
@Composable
fun PersistentWebView(
    session: PersistentWebContentViewModel,
    modifier: Modifier = Modifier,
    viewFactory: (Context) -> WebContentView = ::WebContentView,
    configure: (WebView) -> Unit = {},
    onViewReady: (WebView) -> Unit = {},
    onViewReleased: (WebView) -> Unit = {},
    update: (WebView) -> Unit = {},
) {
    key(session) {
        AndroidView(
            factory = { context ->
                session.attach(context, viewFactory).also { view ->
                    configure(view)
                    onViewReady(view)
                }
            },
            modifier = modifier,
            update = update,
            onRelease = { view ->
                try {
                    onViewReleased(view)
                } finally {
                    (view.parent as? ViewGroup)?.removeView(view)
                    if (!session.detach(view)) {
                        destroyLegacyWebView(view)
                    }
                }
            },
        )
    }
}

private fun destroyLegacyWebView(webView: WebView) {
    webView.stopLoading()
    webView.onPause()
    webView.setLayerType(View.LAYER_TYPE_NONE, null)
    webView.loadUrl("about:blank")
    mainHandler.postDelayed({
        runCatching {
            webView.clearHistory()
            webView.clearCache(false)
            webView.clearFormData()
            webView.destroy()
        }
    }, LEGACY_DESTROY_DELAY_MS)
}

private const val LEGACY_DESTROY_DELAY_MS = 500L
private val mainHandler = Handler(Looper.getMainLooper())
