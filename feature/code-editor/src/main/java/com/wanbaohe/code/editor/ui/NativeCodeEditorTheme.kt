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

package com.wanbaohe.code.editor.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.hrm.codehigh.renderer.CodeColors
import com.hrm.codehigh.renderer.CodeDefaults
import com.wanbaohe.codeeditor.ui.CodeEditorTheme

/** 页面用的完整编辑区外观：排版 + 语法配色 + 光标/查找/当前行底色。 */
@Composable
internal fun rememberNativeCodeEditorTheme(): CodeEditorTheme {
    val colors = rememberCodeEditorColors()
    val textStyle = rememberCodeEditorTextStyle(colors)
    val colorScheme = MaterialTheme.colorScheme
    return remember(colors, textStyle, colorScheme) {
        CodeEditorTheme(
            textStyle = textStyle,
            colors = colors,
            cursorColor = colorScheme.onPrimaryContainer,
            activeLineHighlight = colorScheme.surfaceContainer,
            matchHighlight = colorScheme.primaryContainer,
            activeMatchHighlight = colorScheme.primary,
            activeMatchContent = colorScheme.onPrimary,
        )
    }
}

/** 语法配色：以 M3 的 container 族兜住各种 token，保证浅色/深色主题下都可读。 */
@Composable
internal fun rememberCodeEditorColors(): CodeColors {
    val colorScheme = MaterialTheme.colorScheme
    return remember(
        colorScheme.onSurface,
        colorScheme.onSurfaceVariant,
        colorScheme.onPrimaryContainer,
        colorScheme.onSecondaryContainer,
        colorScheme.onTertiaryContainer,
        colorScheme.primary,
        colorScheme.outlineVariant,
    ) {
        CodeDefaults.oneDarkProColors().copy(
            background = Color.Transparent,
            text = colorScheme.onSurface,
            identifier = colorScheme.onSurface,
            variable = colorScheme.onSurface,
            keyword = colorScheme.onPrimaryContainer,
            annotation = colorScheme.onPrimaryContainer,
            decorator = colorScheme.onPrimaryContainer,
            string = colorScheme.onTertiaryContainer,
            type = colorScheme.onTertiaryContainer,
            builtin = colorScheme.onTertiaryContainer,
            number = colorScheme.onSecondaryContainer,
            function = colorScheme.onSecondaryContainer,
            constant = colorScheme.onSecondaryContainer,
            comment = colorScheme.onSurfaceVariant,
            operator = colorScheme.onSurfaceVariant,
            punctuation = colorScheme.onSurfaceVariant,
            lineNumber = colorScheme.onSurfaceVariant,
            lineNumberDivider = colorScheme.outlineVariant,
        )
    }
}

/** 代码正文排版：等宽 + 关闭字体额外行距，行号才能和正文逐行对齐。 */
@Composable
internal fun rememberCodeEditorTextStyle(colors: CodeColors): TextStyle = remember(colors) {
    TextStyle(
        color = colors.text,
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        ),
    )
}
