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

package com.shifenmiao.demo.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifenmiao.common.markdown.OneBoxMarkdownContent

@Composable
fun KmpMarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    OneBoxMarkdownContent(
        message = markdown,
        modifier = modifier,
    )
}

internal const val markdownMediaDemoDocument = """# Markdown 新媒体与扩展语法

- [x] 任务列表与 GFM 表格
- [ ] 跨块文本选择与复制

> [!NOTE]
音频和视频由原生 Media3 播放，不经过 WebView。

这里包含 ==高亮文本==、H~2~O、X^2^ 与 <kbd>Ctrl</kbd> + <kbd>C</kbd>。

!AUDIO[SoundHelix 音频](https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3)

!VIDEO[MDN 示例视频](https://mdn.github.io/shared-assets/videos/flower.mp4){poster=https://mdn.github.io/shared-assets/images/examples/grapefruit-slice.jpg}

```text
!VIDEO[代码块中的语法不会创建播放器](https://example.com/demo.mp4)
```
"""
