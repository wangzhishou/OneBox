package com.shifenmiao.ai.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.contextmenu.data.TextContextMenuSession
import androidx.compose.foundation.text.contextmenu.provider.LocalTextContextMenuDropdownProvider
import androidx.compose.foundation.text.contextmenu.provider.LocalTextContextMenuToolbarProvider
import androidx.compose.foundation.text.contextmenu.provider.TextContextMenuDataProvider
import androidx.compose.foundation.text.contextmenu.provider.TextContextMenuProvider
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.SelectionState
import androidx.compose.foundation.text.selection.rememberSelectionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.common.components.text.TextSelectionAction
import com.shifenmiao.common.components.text.TextSelectionToolbar
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTextCard
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/** SelectionManager.getContentRect 底部多出来的手柄留白,单位是 px(见调用处注释) */
private const val SELECTION_CONTENT_RECT_BOTTOM_SLOP_PX = 25f * 4f

@Composable
fun ChatTextSelectionContainer(
    appComponent: AppComponent,
    onNavigate: (Screen) -> Unit,
    selectionState: SelectionState = rememberSelectionState(),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val menuProvider = remember { ChatTextContextMenuProvider() }
    val dataDraftHelper = LocalDataDraftHelper.current
    val scope = rememberCoroutineScope()
    var containerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    fun selectedText() = selectionState.selectedTexts.joinToString("\n") { it.text }

    fun runSelectionAction(action: (String) -> Unit) {
        val text = selectedText()
        if (text.isBlank()) return
        menuProvider.close()
        selectionState.clear()
        action(text)
    }

    DisposableEffect(menuProvider) {
        onDispose { menuProvider.close() }
    }
    LaunchedEffect(selectionState.selectedTexts.isEmpty()) {
        if (selectionState.selectedTexts.isEmpty()) menuProvider.close()
    }
    BackHandler(enabled = selectionState.selectedTexts.isNotEmpty()) {
        menuProvider.close()
        selectionState.clear()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { containerCoordinates = it }
    ) {
        CompositionLocalProvider(
            LocalTextContextMenuDropdownProvider provides menuProvider,
            LocalTextContextMenuToolbarProvider provides menuProvider,
        ) {
            SelectionContainer(
                state = selectionState,
                modifier = Modifier.fillMaxSize(),
                content = content,
            )
        }

        val session = menuProvider.session
        val coordinates = containerCoordinates
        if (session != null && coordinates != null) {
            // SelectionManager 的 content rect 底部多加了 HandleHeight.value * 4。
            // HandleHeight 是 25.dp, .value 没乘 density,所以是固定 100px,不是 100.dp。
            // 那截是给系统工具条让手柄的;这里锚回真实选区,菜单才贴得上高亮。
            val rawBounds = session.dataProvider.contentBounds(coordinates)
                .translate(coordinates.localToWindow(Offset.Zero))
            val anchorBounds = rawBounds.copy(
                bottom = (rawBounds.bottom - SELECTION_CONTENT_RECT_BOTTOM_SLOP_PX)
                    .coerceAtLeast(rawBounds.top),
            )
            TextSelectionToolbar(
                anchorBounds = anchorBounds,
                preferAbove = true,
                actions = listOf(
                    TextSelectionAction(
                        key = "copy",
                        label = stringResource(R.string.copy),
                        icon = Icons.Rounded.ContentCopy,
                        onClick = {
                            runSelectionAction(Clipboard::copy)
                        }
                    ),
                    TextSelectionAction(
                        key = "share",
                        label = stringResource(R.string.share_title),
                        icon = Icons.Outlined.LineShare,
                        onClick = {
                            runSelectionAction(appComponent::shareText)
                        }
                    ),
                    TextSelectionAction(
                        key = "image",
                        label = stringResource(R.string.button_export_image),
                        icon = Icons.Outlined.LineTextCard,
                        onClick = {
                            runSelectionAction { text ->
                                onNavigate(Screen.TextCard(initialText = text))
                            }
                        }
                    ),
                    TextSelectionAction(
                        key = "note",
                        label = stringResource(R.string.nav_notebook),
                        icon = Icons.Outlined.NoteAdd,
                        onClick = {
                            runSelectionAction { text ->
                                scope.launch {
                                    val draftId = dataDraftHelper.createDraft(
                                        draftType = ListItemType.NOTE.id,
                                        data = text,
                                    )
                                    onNavigate(Screen.CreateNote(draftId = draftId))
                                }
                            }
                        }
                    ),
                ),
            )
        }
    }
}

private class ChatTextContextMenuProvider : TextContextMenuProvider {
    private val mutatorMutex = MutatorMutex()

    var session by mutableStateOf<Session?>(null)
        private set

    override suspend fun showTextContextMenu(dataProvider: TextContextMenuDataProvider) {
        val newSession = Session(dataProvider)
        mutatorMutex.mutate {
            try {
                session = newSession
                newSession.closed.receive()
            } finally {
                if (session === newSession) session = null
            }
        }
    }

    fun close() {
        session?.close()
    }

    class Session(
        val dataProvider: TextContextMenuDataProvider,
    ) : TextContextMenuSession {
        val closed = Channel<Unit>(Channel.CONFLATED)

        override fun close() {
            closed.trySend(Unit)
        }
    }
}
