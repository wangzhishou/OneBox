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

package com.wanbaohe.core.ui.widget.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.R
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewRootForInspector
import androidx.compose.ui.semantics.popup
import androidx.compose.ui.semantics.semantics
import androidx.core.view.children
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.util.UUID

@Composable
fun FullscreenPopup(
    onDismiss: (() -> Unit)? = null,
    placeAboveAll: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val parentComposition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)
    val popupId = rememberSaveable { UUID.randomUUID() }
    val popupLayout = remember {
        PopupLayout(
            onDismiss = onDismiss,
            composeView = view,
            popupId = popupId,
            placeAboveAll = placeAboveAll
        ).apply {
            setContent(parentComposition) {
                Box(Modifier.semantics { this.popup() }) {
                    // 弹层挂在 decorView 上，是独立于宿主窗口的 view 树根，但组合仍继承宿主的
                    // CompositionLocals。宿主若有 SelectionContainer，弹层里的 Text 会注册成
                    // 宿主选区节点，布局坐标却属于另一个窗口；宿主下次排序选区就会抛
                    // "layouts are not part of the same hierarchy"（AI 助手长按 Markdown 后
                    // 打开全文弹层、再长按标题即可复现）。跨窗口本来无法选区，这里统一断开注册；
                    // 弹层内部若自带 SelectionContainer，仍会提供自己的 registrar，照常可选。
                    DisableSelection { currentContent() }
                }
            }
        }
    }

    DisposableEffect(popupLayout) {
        popupLayout.show()
        popupLayout.updateParameters(
            onDismiss = onDismiss,
            placeAboveAll = placeAboveAll
        )
        onDispose {
            popupLayout.disposeComposition()
            popupLayout.dismiss()
        }
    }

    SideEffect {
        popupLayout.updateParameters(
            onDismiss = onDismiss,
            placeAboveAll = placeAboveAll
        )
    }
}

@SuppressLint("ViewConstructor")
private class PopupLayout(
    private var onDismiss: (() -> Unit)?,
    composeView: View,
    popupId: UUID,
    private var placeAboveAll: Boolean
) : AbstractComposeView(composeView.context), ViewRootForInspector {

    private val ABOVE_ALL_Z = Float.MAX_VALUE

    private val decorView = findOwner<Activity>(composeView.context)?.window?.decorView as ViewGroup

    override val subCompositionView: AbstractComposeView get() = this

    init {
        id = android.R.id.content
        setViewTreeLifecycleOwner(composeView.findViewTreeLifecycleOwner())
        setViewTreeViewModelStoreOwner(composeView.findViewTreeViewModelStoreOwner())
        setViewTreeSavedStateRegistryOwner(composeView.findViewTreeSavedStateRegistryOwner())
        setTag(R.id.compose_view_saveable_id_tag, "Popup:$popupId")
        setTag(R.id.consume_window_insets_tag, false)
    }

    private var content: @Composable () -> Unit by mutableStateOf({})

    override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    fun show() {
        var placeAboveAllView: View? = null
        val topView = decorView.children.maxBy {
            if (it.z == ABOVE_ALL_Z) {
                placeAboveAllView = it
                -ABOVE_ALL_Z
            } else it.z
        }

        z = if (placeAboveAll) ABOVE_ALL_Z
        else topView.z + 1

        decorView.addView(
            this,
            0,
            MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )

        placeAboveAllView?.bringToFront()
        (decorView as View).invalidate()

        requestFocus()
    }

    fun setContent(parent: CompositionContext, content: @Composable () -> Unit) {
        setParentCompositionContext(parent)
        this.content = content
        shouldCreateCompositionOnAttachedToWindow = true
    }

    @Composable
    override fun Content() {
        content()
    }

    @Suppress("ReturnCount")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && onDismiss != null) {
            if (keyDispatcherState == null) {
                return super.dispatchKeyEvent(event)
            }
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                val state = keyDispatcherState
                state?.startTracking(event, this)
                return true
            } else if (event.action == KeyEvent.ACTION_UP) {
                val state = keyDispatcherState
                if (state != null && state.isTracking(event) && !event.isCanceled) {
                    onDismiss?.invoke()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    fun updateParameters(
        onDismiss: (() -> Unit)?,
        placeAboveAll: Boolean
    ) {
        this.onDismiss = onDismiss
        this.placeAboveAll = placeAboveAll
    }

    fun dismiss() {
        setViewTreeLifecycleOwner(null)
        decorView.removeView(this)
    }
}

private inline fun <reified T> findOwner(context: Context): T? {
    var innerContext = context
    while (innerContext is ContextWrapper) {
        if (innerContext is T) {
            return innerContext
        }
        innerContext = innerContext.baseContext
    }
    return null
}
