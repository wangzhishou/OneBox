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

package com.wanbaohe.diagram

import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.ir.ArgbColor
import com.hrm.diagram.core.ir.Severity
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.render.Diagram

/**
 * 把已接入的 native 图表渲染器以「无 UI」方式跑一遍，直接产出 SVG 文本。
 *
 * 与 [NativeDiagram] 的区别：后者是 Compose 组件，只能挂在界面上；这里用
 * `Diagram.session()` 的流式会话一次性喂入完整源码，取出最终快照后导出 SVG，
 * 供 WebView 编辑器把图表内联显示在对应代码块的位置。渲染全过程不依赖
 * Compose / 主线程，可在 WebView 的 JavaBridge 线程同步调用。
 *
 * 解析失败、源码为空、诊断为 ERROR 时返回 null，由调用方回退展示原始代码。
 */
object HeadlessDiagramRenderer {

    /**
     * @param source 图表源码（不含围栏标记）
     * @param languageHint 围栏上的语言标识，例如 mermaid / plantuml / dot
     * @param isDark 当前是否暗色主题
     * @param colors 与当前编辑器主题一致的配色
     */
    fun renderSvg(
        source: String,
        languageHint: String?,
        isDark: Boolean,
        colors: HeadlessDiagramColors,
    ): String? {
        if (source.isBlank()) return null

        val language = resolveLanguage(source, languageHint) ?: return null
        val normalizedSource = if (language == SourceLanguage.MERMAID) {
            normalizeMermaidChainedEdges(source)
        } else {
            source
        }

        val session = Diagram.session(
            language = language,
            theme = buildTheme(isDark, colors),
        )

        return try {
            session.append(normalizedSource)
            val snapshot = session.finish()
            val hasErrors = snapshot.diagnostics.any { it.severity == Severity.ERROR }
            if (hasErrors || snapshot.drawCommands.isEmpty()) {
                null
            } else {
                snapshot.exportNativeSvg(colors.backgroundArgb)
            }
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Headless diagram render failed: $languageHint", e)
            null
        } finally {
            session.close()
        }
    }

    private fun resolveLanguage(source: String, hint: String?): SourceLanguage? =
        when (hint?.trim()?.lowercase()) {
            "mermaid", "mmd" -> SourceLanguage.MERMAID
            "plantuml", "puml", "uml" -> SourceLanguage.PLANTUML
            "dot", "gv", "graphviz" -> SourceLanguage.DOT
            else -> Diagram.detectSource(source, hint = hint).language
        }

    /** 与 [oneBoxDiagramTheme] 保持同一套节点 / 连线 / 文字配色，只是不依赖 ColorScheme。 */
    private fun buildTheme(isDark: Boolean, colors: HeadlessDiagramColors): DiagramTheme {
        val base = if (isDark) DiagramTheme.Dark else DiagramTheme.Default
        val accent = Color(colors.accentArgb)
        val nodeFill = Color(colors.nodeFillArgb)
        val labelBackground = Color(colors.labelBackgroundArgb)
        return base.copy(
            background = Color.Transparent,
            nodeDefaults = base.nodeDefaults.copy(
                fill = ArgbColor(nodeFill.argb),
                stroke = ArgbColor(accent.argb),
                textColor = ArgbColor(accent.argb),
            ),
            edgeDefaults = base.edgeDefaults.copy(color = ArgbColor(accent.argb)),
            arrowDefaults = base.arrowDefaults.copy(color = accent),
            graphColors = base.graphColors.copy(
                background = Color.Transparent,
                nodeFill = nodeFill,
                nodeStroke = accent,
                nodeText = accent,
                edge = accent,
                edgeLabelText = accent,
                edgeLabelBackground = labelBackground,
            ),
        )
    }

    private const val TAG = "HeadlessDiagramRenderer"
}

/** WebView 内联图表使用的主题色（ARGB）。 */
data class HeadlessDiagramColors(
    val backgroundArgb: Int,
    val nodeFillArgb: Int,
    val accentArgb: Int,
    val labelBackgroundArgb: Int,
)
