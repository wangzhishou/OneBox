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

package com.shifenmiao.ai.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.ai.content.AIChatHistory
import com.shifenmiao.ai.ui.AIChatBottom
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.model.state.LocalChatUIState
import com.shifenmiao.model.state.PageState
import com.wanbaohe.core.ui.utils.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun AIChatBody(
    appComponent: AppComponent,
    aiChatComponent: AIChatComponent,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    showNavigationBarsPadding: Boolean = false,
) {
    val chatUIState by aiChatComponent.chatUIState.collectAsState()
    val messageUiModels by aiChatComponent.messageUiModels.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTopButton by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 && lazyListState.firstVisibleItemScrollOffset > 0
        }
    }

    LaunchedEffect(messageUiModels.size, chatUIState.chatActive) {
        if (messageUiModels.isNotEmpty() && chatUIState.pageState != PageState.INITIALIZING) {
            lazyListState.scrollToItem(0)
        }
    }

    CompositionLocalProvider(
        LocalChatUIState provides chatUIState,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(
                    if (showNavigationBarsPadding) {
                        Modifier.navigationBarsPadding()
                    } else {
                        Modifier
                    }
                )
        ) {
            if (chatUIState.showHistory) {
                AIChatHistory(aiChatBaseComponent = aiChatComponent)
            } else {
                ChatContent(
                    chatUIState = chatUIState,
                    messageUiModels = messageUiModels,
                    lazyListState = lazyListState,
                    appComponent = appComponent,
                    aiChatComponent = aiChatComponent,
                    onNavigate = onNavigate,
                )
            }

            if (!chatUIState.showHistory) {
                AIChatBottom(
                    showTop = showScrollToTopButton,
                ) {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(0)
                    }
                }
            }
        }
    }
}

