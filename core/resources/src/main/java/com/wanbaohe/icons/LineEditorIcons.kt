package com.wanbaohe.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave

/** 笔记和提示词共用的细线矢量图标，统一使用 24 单位画布。 */
object LineEditorIcons {
    val Edit by lazy { line("Edit", "M4 20l1-5 11-11a2.8 2.8 0 0 1 4 4L9 19z M14 6l4 4") }
    val Preview by lazy { line("Preview", "M2 12s3.6-6.5 10-6.5S22 12 22 12s-3.6 6.5-10 6.5S2 12 2 12z M15 12a3 3 0 1 1-6 0 3 3 0 1 1 6 0z") }
    val Code by lazy { line("Code", "M8 6l-6 6 6 6 M16 6l6 6-6 6 M14 4l-4 16") }
    val Voice by lazy { line("Voice", "M9 6a3 3 0 0 1 6 0v5a3 3 0 0 1-6 0z M6 11a6 6 0 0 0 12 0 M12 17v4 M9 21h6") }
    val Undo by lazy { line("Undo", "M8 4L3 9l5 5 M3 9h11a6 6 0 0 1 0 12h-3", autoMirror = true) }
    val Redo by lazy { line("Redo", "M16 4l5 5-5 5 M21 9H10a6 6 0 0 0 0 12h3", autoMirror = true) }
    val Bold by lazy { line("Bold", "M6 4h7a4 4 0 0 1 0 8H6m0-8v16h8a4 4 0 0 0 0-8h-1", width = 1.8f) }
    val Italic by lazy { line("Italic", "M11 4h8 M5 20h8 M15 4L9 20") }
    val Underline by lazy { line("Underline", "M6 4v8a6 6 0 0 0 12 0V4 M5 21h14") }
    val Strike by lazy { line("Strike", "M17 6c-1-1.4-2.6-2-5-2-2.7 0-4.5 1.4-4.5 3.5 0 1 .5 1.8 1.5 2.4 M15 14c1 .5 1.5 1.2 1.5 2.4 0 2.2-2 3.6-4.7 3.6-2.4 0-4.1-.7-5.4-2.2 M4 12h16") }
    val Highlight by lazy { line("Highlight", "M8 12l8-9 5 4-8 9z M8 12l-2 5 2 2 5-3 M6 17l-2 3 4-1 M3 22h12") }
    val Paragraph by lazy { line("Paragraph", "M4 5h16 M4 10h16 M4 15h16 M4 20h10", autoMirror = true) }
    val Headings by lazy {
        val h = "M3 5v14 M12 5v14 M3 12h9 "
        listOf(
            "M16 12l2-2v9 M16 19h4",
            "M16 12a2.5 2.5 0 0 1 5 0c0 2-3 3.5-5 7h5",
            "M16 11c1.5-1.7 5-1.5 5 1 0 1.4-1.2 2.2-2.5 2.2 1.5 0 2.5.9 2.5 2.3 0 2.7-3.5 3-5 1.4",
            "M20 19v-9l-4 6h6",
        ).mapIndexed { index, digit -> line("Heading${index + 1}", h + digit) }
    }
    val Quote by lazy { line("Quote", "M4 8h6v5l-3 5H5l2-5H4z M14 8h6v5l-3 5h-2l2-5h-3z") }
    val ListBulleted by lazy { line("ListBulleted", "M4 6h.2 M4 12h.2 M4 18h.2 M9 6h11 M9 12h11 M9 18h11", autoMirror = true) }
    val InsertText by lazy { line("InsertText", "M4 5h13 M10.5 5v14 M15 15h6 M18 12v6") }
    val CodeBlock by lazy { line("CodeBlock", "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z M9 9l-3 3 3 3 M15 9l3 3-3 3 M13 8l-2 8") }
    val Diagram by lazy { line("Diagram", "M9 3h6v5H9z M3 16h6v5H3z M15 16h6v5h-6z M12 8v4 M6 16v-4h12v4") }
    val Table by lazy { line("Table", "M5 4h14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z M3 9h18 M3 14.5h18 M12 9v11") }
    val Tasks by lazy { line("Tasks", "M3 4h7v7H3z M5 7l1.5 1.5L9 6 M3 14h7v7H3z M14 7.5h7 M14 17.5h7", autoMirror = true) }
    val Link by lazy { line("Link", "M10 8l3-3a4 4 0 0 1 6 6l-3 3 M14 16l-3 3a4 4 0 0 1-6-6l3-3 M9 15l6-6") }
    val Image by lazy { line("Image", "M5 4h14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z M3 16l5-4 4 3 4-5 5 6 M9 8h.1") }
    val Audio by lazy { line("Audio", "M9 17V6l11-3v12 M9 17c0-1.5-1.5-2.4-3.2-2.1C4 15.2 3 16.4 3 18s1.5 2.4 3.2 2.1C8 19.8 9 18.6 9 17z M20 15c0-1.5-1.5-2.4-3.2-2.1C15 13.2 14 14.4 14 16s1.5 2.4 3.2 2.1c1.8-.3 2.8-1.5 2.8-3.1z") }
    val Video by lazy { line("Video", "M5 5h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z M10 9l5 3-5 3z") }
    val ChevronDown by lazy { line("ChevronDown", "M5 9l7 7 7-7") }
    val Check by lazy { line("Check", "M4 12l5 5L20 6") }
    val Save get() = Icons.Outlined.LineSave
    val More by lazy { line("More", "M12 5h.1 M12 12h.1 M12 19h.1", width = 2f) }

    private fun line(
        name: String,
        path: String,
        autoMirror: Boolean = false,
        width: Float = 1.4f,
    ): ImageVector = ImageVector.Builder(
        name = "LineEditor.$name",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = autoMirror,
    ).addPath(
        pathData = addPathNodes(path),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()
}
