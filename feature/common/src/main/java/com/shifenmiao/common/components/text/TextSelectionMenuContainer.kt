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

package com.shifenmiao.common.components.text

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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.channels.Channel

/** SelectionManager.getContentRect 底部多出来的手柄留白,单位是 px(见调用处注释) */
private const val SELECTION_CONTENT_RECT_BOTTOM_SLOP_PX = 25f * 4f

/**
 * 选区浮动工具条上的一个动作。
 *
 * [onSelected] 收到的是当前选中的纯文本,容器统一负责取文本、关菜单、清选区,
 * 调用方只需要描述"拿到这段文字之后做什么"。
 */
@Immutable
data class TextSelectionMenuItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val onSelected: (String) -> Unit,
)

/**
 * 用自定义浮动工具条替换系统文本选择菜单的通用容器。
 *
 * 覆盖 Compose 的 [LocalTextContextMenuDropdownProvider] / [LocalTextContextMenuToolbarProvider],
 * 只记录菜单会话而不弹系统菜单,再用会话里的 contentBounds 定位 [TextSelectionToolbar]。
 * AI 聊天、博客详情、笔记详情共用这一套,避免三处各自实现选区菜单。
 */
@Composable
fun TextSelectionMenuContainer(
    items: List<TextSelectionMenuItem>,
    modifier: Modifier = Modifier,
    selectionState: SelectionState = rememberSelectionState(),
    preferAbove: Boolean = true,
    content: @Composable () -> Unit,
) {
    val menuProvider = remember { ToolbarTextContextMenuProvider() }
    var containerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    fun runSelectionAction(action: (String) -> Unit) {
        val text = selectionState.selectedTexts.joinToString("\n") { it.text }
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
        if (session != null && coordinates != null && items.isNotEmpty()) {
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
                preferAbove = preferAbove,
                actions = items.map { item ->
                    TextSelectionAction(
                        key = item.key,
                        label = item.label,
                        icon = item.icon,
                        onClick = { runSelectionAction(item.onSelected) },
                        // 聊天/博客/笔记这类只读内容页统一显示图标下方的文字标签
                        showLabel = true,
                    )
                },
            )
        }
    }
}

/** 只持有菜单会话、不弹系统菜单的 provider;close 时恢复挂起的 showTextContextMenu 调用 */
private class ToolbarTextContextMenuProvider : TextContextMenuProvider {
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
