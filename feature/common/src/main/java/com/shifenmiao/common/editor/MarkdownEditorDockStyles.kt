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

package com.shifenmiao.common.editor

import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.wanbaohe.icons.LineEditorIcons
import com.wanbaohe.richtext.editor.style.RichTextInlineStyle
import androidx.compose.material.icons.Icons as MaterialIcons
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons

@Composable
internal fun inlineGlyphStyle(id: String): TextStyle {
    val base = MaterialTheme.typography.labelMedium
    return when (id) {
        RichTextInlineStyle.BoldId -> base.copy(fontWeight = FontWeight.Bold)
        RichTextInlineStyle.ItalicId -> base.copy(fontStyle = FontStyle.Italic)
        RichTextInlineStyle.UnderlineId -> base.copy(textDecoration = TextDecoration.Underline)
        RichTextInlineStyle.StrikethroughId -> base.copy(textDecoration = TextDecoration.LineThrough)
        else -> base
    }
}

internal fun inlineStyleIcon(id: String): ImageVector? = when (id) {
    RichTextInlineStyle.BoldId -> LineEditorIcons.Bold
    RichTextInlineStyle.ItalicId -> LineEditorIcons.Italic
    RichTextInlineStyle.UnderlineId -> LineEditorIcons.Underline
    RichTextInlineStyle.StrikethroughId -> LineEditorIcons.Strike
    RichTextInlineStyle.InlineCodeId -> LineEditorIcons.Code
    "highlight" -> LineEditorIcons.Highlight
    else -> null
}

internal fun NativeMarkdownEditorMode.labelRes(): Int = when (this) {
    NativeMarkdownEditorMode.Edit -> R.string.native_editor_mode_edit
    NativeMarkdownEditorMode.Preview -> R.string.native_editor_mode_preview
    NativeMarkdownEditorMode.Source -> R.string.native_editor_mode_source
}

internal fun NativeMarkdownEditorMode.icon(customIcons: Boolean = false): ImageVector = when (this) {
    NativeMarkdownEditorMode.Edit -> if (customIcons) LineEditorIcons.Edit else OneBoxIcons.Outlined.Edit
    NativeMarkdownEditorMode.Preview -> if (customIcons) LineEditorIcons.Preview else MaterialIcons.Outlined.Visibility
    NativeMarkdownEditorMode.Source -> if (customIcons) LineEditorIcons.Code else MaterialIcons.Outlined.Code
}
