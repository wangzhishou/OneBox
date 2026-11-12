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

package com.wanbaohe.code.editor.webview

import android.webkit.JavascriptInterface
import androidx.annotation.Keep

/**
 * CodeEditor JavaScript Bridge
 *
 * 提供 Android ↔ CodeMirror 6 WebView 双向通信
 *
 * 注意：类和方法使用 @Keep 注解防止被 ProGuard/R8 混淆
 */
@Keep
class CodeEditorBridge(
    private val onContentChanged: (String) -> Unit,
    private val onEditorReady: () -> Unit,
    private val onCursorChange: ((Int, Int) -> Unit)? = null,
    private val onSelectionChange: ((Int, Int) -> Unit)? = null,
    private val onEditorScroll: ((Int, Int) -> Unit)? = null,
    private val onPopupStateChanged: ((Boolean) -> Unit)? = null
) {
    @Keep
    @JavascriptInterface
    fun onContentChanged(text: String) {
        onContentChanged.invoke(text)
    }

    @Keep
    @JavascriptInterface
    fun onEditorReady() {
        onEditorReady.invoke()
    }

    @Keep
    @JavascriptInterface
    fun onCursorChange(line: Int, column: Int) {
        onCursorChange?.invoke(line, column)
    }

    @Keep
    @JavascriptInterface
    fun onSelectionChange(from: Int, to: Int) {
        onSelectionChange?.invoke(from, to)
    }

    @Keep
    @JavascriptInterface
    fun onEditorScroll(scrollTop: Int, deltaY: Int) {
        onEditorScroll?.invoke(scrollTop, deltaY)
    }

    @Keep
    @JavascriptInterface
    fun onPopupStateChanged(isOpen: Boolean) {
        onPopupStateChanged?.invoke(isOpen)
    }

    companion object {
        const val BRIDGE_NAME = "Android"
    }
}
