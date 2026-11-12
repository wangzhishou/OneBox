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

package com.shifenmiao.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.model.webview.WebViewType

@Composable
fun BaseWebViewScreen(
    webViewComponent: WebViewComponent
) {
    val webViewParams by webViewComponent.webViewParams.collectAsState()
    if (webViewParams != null) {
        when (webViewParams!!.type) {
            WebViewType.DEFAULT -> {
                ColumnWebViewScreen(
                    onGoBack = webViewComponent.onGoBack,
                    webViewComponent = webViewComponent
                )
            }

            WebViewType.EXTERNAL -> {
                webViewParams?.url?.let {
                    // 处理外部WebView，使用系统默认浏览器打开
                    val context = LocalContext.current
                    ActionUtils.openWebBrowser(context, it)
                }
            }

            WebViewType.PREVIEW -> {
                PreviewWebViewScreen(
                    onGoBack = webViewComponent.onGoBack,
                    webViewComponent = webViewComponent
                )
            }

            WebViewType.MARKDOWN_RENDER -> {
                PreviewWebViewScreen(
                    onGoBack = webViewComponent.onGoBack,
                    webViewComponent = webViewComponent
                )
            }


            WebViewType.COLUMN -> {
                ColumnWebViewScreen(
                    onGoBack = webViewComponent.onGoBack,
                    webViewComponent = webViewComponent
                )
            }
        }
    }
}
