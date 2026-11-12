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

package com.wanbaohe.core.ui.widget.editor

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * 编辑器工具项(底部 Tab 栏 / 浮动竖栏)的统一标签:
 * labelSmall 单行,按可用宽度自动降字号——「基础工具」「文本样式」以及各语言长译名
 * 都能整词显示,不再被 56dp 的固定宽度硬裁成残词(如 Basic Tools 只剩 Basic)。
 *
 * 做法与全局底部导航栏 `BottomNavigationBar` 的 Tab 文案一致:字号从 9sp 起按 0.5sp
 * 步进回退到样式字号,极端窄屏仍放不下时才退回省略号。
 */
@Composable
fun EditorToolLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.labelSmall
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(
            minFontSize = 9.sp,
            maxFontSize = style.fontSize,
            stepSize = 0.5.sp
        ),
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
