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

package com.wanbaohe.codeeditor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * 只换样式不换字符：偏移映射恒等，光标/选区/输入法组合态全部保持原样。
 *
 * 高亮是异步算出来的，可能比正文落后一拍。落后时直接返回原文本（纯色），
 * 绝不把旧文本的样式贴到新文本上。
 */
class CodeEditorVisualTransformation(
    private val highlightedProvider: () -> AnnotatedString?,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = highlightedProvider()
        val styled = if (highlighted != null && highlighted.text == text.text) {
            highlighted
        } else {
            text
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}
