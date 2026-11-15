package com.shifenmiao.common.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.wanbaohe.markdown.OneBoxMarkdownState
import com.wanbaohe.markdown.rememberOneBoxMarkdownState

@Stable
class OneBoxMarkdownContentState internal constructor(
    internal val modernState: OneBoxMarkdownState,
) {
    suspend fun scrollToContentStart() {
        modernState.scrollToContentStart()
    }
}

@Composable
fun rememberOneBoxMarkdownContentState(): OneBoxMarkdownContentState {
    val modernState = rememberOneBoxMarkdownState()
    return remember(modernState) { OneBoxMarkdownContentState(modernState) }
}
