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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.shifenmiao.webview.common.CommonWebView
import com.shifenmiao.webview.common.WebViewTopAppBar

@Composable
fun ColumnWebViewScreen(
    onGoBack: () -> Unit,
    webViewComponent: WebViewComponent
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val webViewParams by webViewComponent.webViewParams.collectAsState()
    val webViewState by webViewComponent.webViewState.collectAsState()

    Column(
        Modifier
            .fillMaxSize().navigationBarsPadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        WebViewTopAppBar(
            title = webViewParams?.title ?: "",
            onBackClick = onGoBack,
            scrollBehavior = scrollBehavior
        )

        CommonWebView(
            webViewParams = webViewParams,
            webViewState = webViewState,
            modifier = Modifier.fillMaxSize(),
            onWebViewCreated = { webView -> webViewComponent.setWebView(webView) },
            onTitleChanged = { newTitle -> webViewComponent.updateTitle(newTitle) },
            client = webViewComponent.getWebViewClient(),
            chromeClient = webViewComponent.getWebChromeClient(),
            onReload = { webViewComponent.reload() }
        )
    }

    BackHandler {
        onGoBack()
    }
}
