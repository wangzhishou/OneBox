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

package com.halilibo.richtext.commonmark

import org.commonmark.node.CustomBlock

/**
 * 块级数学公式节点（`$$...$$`），由自定义块解析器创建，只承载公式源码。
 *
 * 之前借用 markwon 的公式扩展节点，会把旧渲染库连同字体一起拖进安装包；
 * 公式渲染改由 `:libs:latex` 的 `NativeBlockLatex` 承担之后，
 * 解析侧只需要一个不依赖任何渲染库的纯数据节点。
 */
internal class MarkdownMathBlock : CustomBlock() {
    var latex: String = ""
}
