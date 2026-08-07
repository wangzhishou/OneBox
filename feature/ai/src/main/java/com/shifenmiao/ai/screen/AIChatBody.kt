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
import kotlinx.coroutines.launch

@Composable
fun AIChatBody(
    appComponent: AppComponent,
    aiChatComponent: AIChatComponent,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    showNavigationBarsPadding: Boolean = false,
    isTextSelectionMode: Boolean = false,
    onEnterTextSelectionMode: () -> Unit = {},
    onExitTextSelectionMode: () -> Unit = {},
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
                    isTextSelectionMode = isTextSelectionMode,
                    onEnterTextSelectionMode = onEnterTextSelectionMode,
                    onExitTextSelectionMode = onExitTextSelectionMode,
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

