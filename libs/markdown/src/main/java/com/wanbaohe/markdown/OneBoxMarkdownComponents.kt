package com.wanbaohe.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect

@Immutable
data class OneBoxMarkdownCodeBlockInfo(
    val code: String,
    val language: String,
    val title: String?,
)

@Immutable
data class OneBoxMarkdownSelectionInfo(
    val text: String,
    val boundsInWindow: Rect,
)

class OneBoxMarkdownSelectionActions internal constructor(
    private val onCopy: () -> Unit,
    private val onSelectAll: () -> Unit,
    private val onDismiss: () -> Unit,
) {
    fun copySelection() = onCopy()
    fun selectAll() = onSelectAll()
    fun dismiss() = onDismiss()
}

@Immutable
data class OneBoxMarkdownComponents(
    val header: (@Composable () -> Unit)? = null,
    val footer: (@Composable () -> Unit)? = null,
    val codeBlockHeader: (@Composable (OneBoxMarkdownCodeBlockInfo) -> Unit)? = null,
    val selectionToolbar: (@Composable (
        OneBoxMarkdownSelectionInfo,
        OneBoxMarkdownSelectionActions,
    ) -> Unit)? = null,
) {
    companion object {
        val Default = OneBoxMarkdownComponents()
    }
}
