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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
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
    var isWebContentReady by remember { mutableStateOf(false) }

    LaunchedEffect(webViewComponent) {
        withFrameNanos { }
        withFrameNanos { }
        isWebContentReady = true
    }

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

        if (isWebContentReady) {
            CommonWebView(
                webViewParams = webViewParams,
                webViewState = webViewState,
                session = webViewComponent.webContentSession,
                modifier = Modifier.fillMaxSize(),
                onWebViewCreated = { webView -> webViewComponent.setWebView(webView) },
                onWebViewReleased = webViewComponent::releaseWebView,
                onTitleChanged = { newTitle -> webViewComponent.updateTitle(newTitle) },
                client = webViewComponent.getWebViewClient(),
                chromeClient = webViewComponent.getWebChromeClient(),
                onReload = { webViewComponent.reload() }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    BackHandler {
        onGoBack()
    }
}
