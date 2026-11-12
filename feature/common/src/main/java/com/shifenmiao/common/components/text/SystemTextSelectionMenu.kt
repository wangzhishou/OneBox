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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ActionMode
import androidx.appcompat.view.WindowCallbackWrapper
import androidx.compose.foundation.text.contextmenu.provider.LocalTextContextMenuToolbarProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

/**
 * 屏蔽系统文本选择菜单(Copy/Cut/Paste/Select all 的浮动 ActionMode)。
 *
 * Compose 1.13 有两条路径:
 * - 新上下文菜单:LocalTextContextMenuToolbarProvider;
 * - 旧 TextToolbar:LocalTextToolbar。
 * 两条都覆盖成空实现;另外编辑期间临时包一层 Window.Callback,平台一旦启动
 * 文本 ActionMode 立即 finish()——IME/系统智能选择有绕过 CompositionLocal 的路径。
 * 这样选中文字后只显示自绘的 [TextSelectionToolbar],不会压住画布或重复操作。
 *
 * 注意保留系统选区手柄(蓝色拖柄)与放大镜:它们由 Compose 自己绘制,不走这里。
 */
@Composable
fun SuppressSystemTextSelectionMenu(content: @Composable () -> Unit) {
    val view = LocalView.current
    DisposableEffect(view) {
        // Compose 1.13 的某些路径会绕过 LocalTextToolbar 直接走平台 ActionMode
        // (尤其是 IME/系统智能选择),仅覆盖 CompositionLocal 还压不住。
        // 这里在编辑期间临时包一层 Window.Callback:平台一旦启动文本 ActionMode,
        // 立即 finish(),只保留自绘工具条。dispose 时恢复原 callback,不污染其它页面。
        val activity = view.context.findActivity()
        val window = activity?.window
        val original = window?.callback
        if (window == null || original == null) {
            onDispose { }
        } else {
            val wrapper = object : WindowCallbackWrapper(original) {
                override fun onActionModeStarted(mode: ActionMode?) {
                    mode?.finish()
                    // 不调用 super:避免平台把菜单真正挂到窗口上
                }
            }
            window.callback = wrapper
            onDispose {
                if (window.callback === wrapper) window.callback = original
            }
        }
    }
    CompositionLocalProvider(
        LocalTextToolbar provides NoOpTextToolbar,
        LocalTextContextMenuToolbarProvider provides null,
    ) {
        content()
    }
}

private object NoOpTextToolbar : TextToolbar {
    override val status: TextToolbarStatus = TextToolbarStatus.Hidden

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) = Unit

    override fun hide() = Unit
}

private fun Context.findActivity(): Activity? {
    var context: Context? = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
