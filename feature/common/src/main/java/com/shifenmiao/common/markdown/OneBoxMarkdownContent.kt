package com.shifenmiao.common.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.components.CenterErrorBox
import com.shifenmiao.common.components.text.TextSelectionMenuItem
import com.shifenmiao.common.editor.EditorMediaStore
import com.shifenmiao.core.R
import com.wanbaohe.base.ui.loading.EmptyBox
import com.wanbaohe.core.ui.loading.DotDanceLoading
import com.wanbaohe.markdown.OneBoxMarkdown
import com.wanbaohe.markdown.OneBoxMarkdownCodeActions

@Composable
fun OneBoxMarkdownContent(
    message: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isStreaming: Boolean = false,
    enableScroll: Boolean = true,
    externalScrollState: ScrollState? = null,
    enableSelection: Boolean = true,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onGoBack: () -> Unit = {},
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    emptyText: String = stringResource(R.string.load_empty_toast),
    state: OneBoxMarkdownContentState = rememberOneBoxMarkdownContentState(),
    onLinkClick: ((String) -> Unit)? = null,
    selectionItems: List<TextSelectionMenuItem> = emptyList(),
    codeActions: OneBoxMarkdownCodeActions? = null,
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
) {
    val context = LocalContext.current
    val resolvedMessage = remember(context, message) {
        EditorMediaStore.resolveForDisplay(context, message)
    }
    when {
        isLoading -> Box(modifier.fillMaxSize(), Alignment.Center) { DotDanceLoading() }
        errorMessage != null -> CenterErrorBox(
            errorMessage = errorMessage,
            onRetry = onRetry,
            onGoBack = onGoBack,
        )
        message.isEmpty() -> Box(modifier.fillMaxSize(), Alignment.Center) {
            EmptyBox(
                modifier = Modifier.fillMaxWidth(),
                text = emptyText,
            )
        }
        else -> OneBoxMarkdown(
            markdown = resolvedMessage,
            modifier = modifier.padding(paddingValues),
            state = state.modernState,
            isStreaming = isStreaming,
            enableScroll = enableScroll,
            externalScrollState = externalScrollState,
            enableSelection = enableSelection,
            onLinkClick = onLinkClick,
            components = oneBoxMarkdownComponents(
                header = headerContent,
                footer = footerContent,
                selectionItems = selectionItems,
                codeActions = codeActions,
            ),
        )
    }
}
