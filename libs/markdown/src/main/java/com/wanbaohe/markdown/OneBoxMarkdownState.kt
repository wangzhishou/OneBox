package com.wanbaohe.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.hrm.markdown.renderer.MarkdownState
import com.hrm.markdown.renderer.rememberMarkdownState

@Stable
class OneBoxMarkdownState internal constructor(
    internal val engineState: MarkdownState,
) {
    suspend fun scrollToContentStart(): Boolean = engineState.scrollToBlock(0)
    suspend fun scrollToEnd(): Boolean = engineState.scrollToEnd()
    fun clearSelection() = engineState.clearSelection()
}

@Composable
fun rememberOneBoxMarkdownState(): OneBoxMarkdownState {
    val engineState = rememberMarkdownState()
    return remember(engineState) { OneBoxMarkdownState(engineState) }
}
