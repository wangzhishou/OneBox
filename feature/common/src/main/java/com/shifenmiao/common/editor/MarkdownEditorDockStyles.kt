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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Highlighter
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatBold
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatItalic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatStrikethrough
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatUnderlined
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
    RichTextInlineStyle.BoldId -> OneBoxIcons.Outlined.LineFormatBold
    RichTextInlineStyle.ItalicId -> OneBoxIcons.Outlined.LineFormatItalic
    RichTextInlineStyle.UnderlineId -> OneBoxIcons.Outlined.LineFormatUnderlined
    RichTextInlineStyle.StrikethroughId -> OneBoxIcons.Outlined.LineFormatStrikethrough
    RichTextInlineStyle.InlineCodeId -> MaterialIcons.Outlined.Code
    "highlight" -> OneBoxIcons.Outlined.Highlighter
    else -> null
}

// H1–H4 的数字也是矢量路径，不随系统字号放大而被工具栏裁切。
internal val markdownHeadingIcons: List<ImageVector> by lazy {
    listOf(
        "M16 12 L18 10 V19",
        "M15 11 Q17 9 19 11 Q21 13 18 15 L15 19 H20",
        "M15 11 Q19 8 20 12 Q20 14 17 14 Q20 14 20 17 Q19 21 15 18",
        "M19 19 V10 L15 16 H21",
    ).mapIndexed { index, numeral ->
        ImageVector.Builder(
            name = "MarkdownHeading${index + 1}",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = addPathNodes("M3 5 V19 M12 5 V19 M3 12 H12 $numeral"),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ).build()
    }
}

internal fun NativeMarkdownEditorMode.labelRes(): Int = when (this) {
    NativeMarkdownEditorMode.Edit -> R.string.native_editor_mode_edit
    NativeMarkdownEditorMode.Preview -> R.string.native_editor_mode_preview
    NativeMarkdownEditorMode.Source -> R.string.native_editor_mode_source
}

internal fun NativeMarkdownEditorMode.icon(): ImageVector = when (this) {
    NativeMarkdownEditorMode.Edit -> OneBoxIcons.Outlined.Edit
    NativeMarkdownEditorMode.Preview -> MaterialIcons.Outlined.Visibility
    NativeMarkdownEditorMode.Source -> MaterialIcons.Outlined.Code
}
