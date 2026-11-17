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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.code.editor.ui

import android.content.ClipboardManager
import android.content.Context

/** 代码编辑器只需要纯文本剪贴板，不涉及跨 App 富文本。 */
internal class PlainTextClipboard(context: Context) {
    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(ClipboardManager::class.java)

    fun copy(text: String): Boolean {
        if (text.isEmpty()) return false
        manager?.setPrimaryClip(android.content.ClipData.newPlainText("code", text))
        return true
    }

    fun paste(): String? {
        val item = manager?.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)
        return item?.coerceToText(appContext)?.toString()?.takeIf { it.isNotEmpty() }
    }
}
