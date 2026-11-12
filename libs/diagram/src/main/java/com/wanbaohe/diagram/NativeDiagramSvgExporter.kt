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
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.TextAnchorX
import com.hrm.diagram.core.draw.TextAnchorY
import com.hrm.diagram.core.export.ExportBackground
import com.hrm.diagram.core.export.SvgExportOptions
import com.hrm.diagram.render.export.toSvg
import com.hrm.diagram.render.streaming.DiagramSnapshot

internal fun DiagramSnapshot.exportNativeSvg(backgroundArgb: Int): String {
    val textCommands = drawCommands.textCommandsInPaintOrder()
    var textIndex = 0
    val options = SvgExportOptions(
        background = ExportBackground.Solid(Color(backgroundArgb))
    )
    return toSvg(options).replace(TEXT_ELEMENT_REGEX) { match ->
        val command = textCommands.getOrNull(textIndex++)
            ?: return@replace match.value
        match.value
            .replace(TEXT_LENGTH_REGEX, "")
            .replace(LENGTH_ADJUST_REGEX, "")
            .replaceFirst(
                "<text ",
                "<text text-anchor=\"${command.anchorX.toSvg()}\"${command.anchorY.toSvg()} ",
            )
    }
}

private fun List<DrawCommand>.textCommandsInPaintOrder(): List<DrawCommand.DrawText> =
    withIndex()
        .sortedWith(compareBy { it.value.z })
        .flatMap { it.value.textCommandsInPaintOrder() }

private fun DrawCommand.textCommandsInPaintOrder(): List<DrawCommand.DrawText> = when (this) {
    is DrawCommand.DrawText -> listOf(this)
    is DrawCommand.Group -> children.textCommandsInPaintOrder()
    is DrawCommand.Clip -> children.textCommandsInPaintOrder()
    else -> emptyList()
}

private fun TextAnchorX.toSvg(): String = when (this) {
    TextAnchorX.Start -> "start"
    TextAnchorX.Center -> "middle"
    TextAnchorX.End -> "end"
}

private fun TextAnchorY.toSvg(): String = when (this) {
    TextAnchorY.Top -> " dominant-baseline=\"text-before-edge\""
    TextAnchorY.Middle -> " dominant-baseline=\"central\""
    TextAnchorY.Baseline -> ""
    TextAnchorY.Bottom -> " dominant-baseline=\"text-after-edge\""
}

private val TEXT_ELEMENT_REGEX = Regex("<text\\b[^>]*>.*?</text>", RegexOption.DOT_MATCHES_ALL)
private val TEXT_LENGTH_REGEX = Regex("\\s+textLength=\"[^\"]*\"")
private val LENGTH_ADJUST_REGEX = Regex("\\s+lengthAdjust=\"[^\"]*\"")
