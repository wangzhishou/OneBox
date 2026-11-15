/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.base.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wanbaohe.markdown.OneBoxMarkdown

/**
 * 通用流式 Markdown 内容。解析、追加合并和未闭合语法处理由 KMP 引擎负责。
 */
@Composable
fun StreamingMarkdownContent(
    content: String,
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    blockSpacing: Dp = 8.dp,
) {
    OneBoxMarkdown(
        markdown = content,
        modifier = modifier.fillMaxWidth(),
        isStreaming = isStreaming,
        enableScroll = false,
        textStyle = textStyle,
        contentColor = contentColor,
        blockSpacing = blockSpacing,
    )
}
