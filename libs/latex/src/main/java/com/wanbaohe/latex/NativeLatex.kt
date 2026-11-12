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

package com.wanbaohe.latex

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.hrm.latex.renderer.Latex
import com.hrm.latex.renderer.LatexAutoWrap
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme

@Composable
fun NativeInlineLatex(
    latex: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    val fontSize = textStyle.fontSize.takeIf(TextUnit::isSpecified) ?: 16.sp
    val color = textStyle.color.takeOrElse { LocalContentColor.current }
    Latex(
        latex = normalizeNativeLatexSource(latex),
        modifier = modifier,
        config = nativeLatexConfig(fontSize, color),
    )
}

@Composable
fun NativeBlockLatex(
    latex: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
) {
    val fontSize = textStyle.fontSize.takeIf(TextUnit::isSpecified) ?: 20.sp
    val color = textStyle.color.takeOrElse { MaterialTheme.colorScheme.onSurface }
    LatexAutoWrap(
        latex = normalizeNativeLatexSource(latex),
        modifier = modifier,
        config = nativeLatexConfig(fontSize, color),
    )
}

@Composable
private fun nativeLatexConfig(fontSize: TextUnit, color: Color): LatexConfig {
    val theme = remember(color) {
        LatexTheme.light(color = color, backgroundColor = Color.Transparent)
    }
    return remember(fontSize, theme) {
        LatexConfig(
            fontSize = fontSize,
            theme = theme,
            accessibilityEnabled = true,
        )
    }
}
