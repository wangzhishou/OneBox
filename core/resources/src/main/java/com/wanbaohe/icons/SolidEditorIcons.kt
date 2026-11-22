package com.wanbaohe.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** 笔记编辑图标，统一使用 24 单位画布和填充路径，由调用方决定尺寸与着色。 */
object SolidEditorIcons {
    val Edit by lazy { solid("Edit", "M4 15.8 14.8 5 19 9.2 8.2 20H4z M16 3.8l.9-.9a1.8 1.8 0 0 1 2.5 0l1.7 1.7a1.8 1.8 0 0 1 0 2.5l-.9.9z") }
    val Preview by lazy { solid("Preview", "M2 12C6 3 18 3 22 12 18 21 6 21 2 12z M12 7.8a4.2 4.2 0 1 0 0 8.4 4.2 4.2 0 0 0 0-8.4z M12 9.8a2.2 2.2 0 1 0 0 4.4 2.2 2.2 0 0 0 0-4.4z") }
    val Code by lazy { solid("Code", "M7.6 5 9.5 7 4.5 12l5 5-1.9 2L1 12z M16.4 5 23 12l-6.6 7-1.9-2 5-5-5-5z M13 4h2.5L11 20H8.5z") }
    val Voice by lazy { solid("Voice", "M12 2a4 4 0 0 1 4 4v5a4 4 0 0 1-8 0V6a4 4 0 0 1 4-4z M5 10h2v1a5 5 0 0 0 10 0v-1h2v1a7 7 0 0 1-6 6.9V20h3v2H8v-2h3v-2.1A7 7 0 0 1 5 11z") }
    val Undo by lazy { solid("Undo", "M2 9 9 2v5h5a7 7 0 0 1 0 14H9v-3h5a4 4 0 0 0 0-8H9v6z", autoMirror = true) }
    val Redo by lazy { solid("Redo", "M22 9 15 2v5h-5a7 7 0 0 0 0 14h5v-3h-5a4 4 0 0 1 0-8h5v6z", autoMirror = true) }
    val Bold by lazy { solid("Bold", "M5 3h7.7a4.6 4.6 0 0 1 3.5 7.6A5.4 5.4 0 0 1 13 21H5z M8.5 6v4h4a2 2 0 0 0 0-4z M8.5 13v5h4.7a2.5 2.5 0 0 0 0-5z") }
    val Italic by lazy { solid("Italic", "M10 3h10v3h-3.4l-5.2 12H15v3H5v-3h3.1l5.2-12H10z") }
    val Underline by lazy { solid("Underline", "M5 3h3.2v8a3.8 3.8 0 0 0 7.6 0V3H19v8a7 7 0 0 1-14 0z M4 20h16v2H4z") }
    val Strike by lazy { solid("Strike", "M5.5 8a6.5 6.5 0 0 1 12.8.2h-3.2a3.4 3.4 0 0 0-6.4 0c0 .5.3.8 1 .9H6z M3 10.5h18v2.7H3z M8.6 15a3.4 3.4 0 0 0 6.7 0h3.2a6.5 6.5 0 0 1-13 .1z") }
    val Highlight by lazy { solid("Highlight", "M8 12 16 3a1.5 1.5 0 0 1 2.1-.1L22 6.4a1.5 1.5 0 0 1 .1 2.1l-8 9z M6.8 13.4l5.1 4.6-2.6 2.6-4 .5.1-4z M2 22h12v2H2z") }
    val Paragraph by lazy { solid("Paragraph", "M4 5h16v3H4z M4 10.5h16v3H4z M4 16h10v3H4z", autoMirror = true) }
    val Headings by lazy {
        val h = "M2.5 4h3v6.5H11V4h3v16h-3v-6.5H5.5V20h-3z "
        listOf(
            "M16 12l2.4-2H21v10h-2.8v-6.5L16 15z",
            "M15.5 13a3.6 3.6 0 0 1 7.2 0c0 1.8-1.5 3.2-3.4 4.6h3.5V20h-7.4v-2.1c2-1.7 4.6-3.8 4.6-4.8a.9.9 0 0 0-1.8-.1z",
            "M15.7 13a3.5 3.5 0 0 1 7 0c0 .8-.4 1.5-1 2 .7.4 1.2 1.2 1.2 2.1a3.7 3.7 0 0 1-7.4 0h2.7a1 1 0 1 0 1-1h-1v-2h1a.9.9 0 1 0-.9-.9z",
            "M18.5 10h2.8v5.6H23V18h-1.7v2h-2.7v-2h-3.5v-2.2z M18.6 13.8l-1.1 1.8h1.1z",
        ).mapIndexed { index, digit -> solid("Heading${index + 1}", h + digit) }
    }
    val Quote by lazy { solid("Quote", "M3 8h7v6l-3 5H4l2-5H3z M14 8h7v6l-3 5h-3l2-5h-3z") }
    val ListBulleted by lazy { solid("ListBulleted", "M3 5h3v3H3z M3 10.5h3v3H3z M3 16h3v3H3z M8 5h13v3H8z M8 10.5h13v3H8z M8 16h13v3H8z", autoMirror = true) }
    val InsertText by lazy { solid("InsertText", "M3 4h17v3h-7v13h-3V7H3z M17 11h2v3h3v2h-3v3h-2v-3h-3v-2h3z") }
    val CodeBlock by lazy { solid("CodeBlock", "M4 3h16a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z M8.2 7.8 4 12l4.2 4.2 1.6-1.6L7.2 12l2.6-2.6z M15.8 7.8l-1.6 1.6 2.6 2.6-2.6 2.6 1.6 1.6L20 12z") }
    val Diagram by lazy { solid("Diagram", "M8 2h8v6h-3v3h7v5h2v6h-7v-6h2v-2H7v2h2v6H2v-6h2v-5h7V8H8z") }
    val Table by lazy { solid("Table", "M4 3h16a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z M5 9v4h5V9z M13 9v4h6V9z M5 15v3h5v-3z M13 15v3h6v-3z") }
    val Tasks by lazy { solid("Tasks", "M3 4h8v8H3z M4.2 7.7 6.5 10 10 6.4 8.9 5.3 6.5 7.7 5.3 6.6z M3 14h8v7H3z M5 16v3h4v-3z M13 6h8v3h-8z M13 16h8v3h-8z", autoMirror = true) }
    val Link by lazy { solid("Link", "M10.8 4.3 14 2.7a5.2 5.2 0 0 1 4.6 9.3l-3.2 1.6-1.3-2.6 3.2-1.6a2.3 2.3 0 0 0-2-4.1l-3.2 1.6z M8.6 10.4l1.3 2.6-3.2 1.6a2.3 2.3 0 0 0 2 4.1l3.2-1.6 1.3 2.6-3.2 1.6a5.2 5.2 0 0 1-4.6-9.3z M8.7 12.3l5.6-2.8 1 2.1-5.6 2.8z") }
    val Image by lazy { solid("Image", "M4 3h16a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z M7.5 6.5a2 2 0 1 0 0 4 2 2 0 0 0 0-4z M5 18h14l-4.8-6.5-3 3.8-2.1-2.4z") }
    val Audio by lazy { solid("Audio", "M10 4 21 2v14a4 4 0 1 1-3-3.9V7.5l-5 1V18a4 4 0 1 1-3-3.9z") }
    val Video by lazy { solid("Video", "M4 5h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z M10 8v8l6-4z") }
    val ChevronDown by lazy { solid("ChevronDown", "M5.2 8 12 13.8 18.8 8 21 10.5 12 18 3 10.5z") }
    val Check by lazy { solid("Check", "M3 12.6l2.2-2.3 4.2 4.1 9.3-9.3L21 7.4 9.4 19z") }
    val Save by lazy { solid("Save", "M4 2h13l5 5v13a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z M7 5v4h9V5z M6 13v6h12v-6z M8 15h8v2H8z") }
    val More by lazy { solid("More", "M12 3a2 2 0 1 0 0 4 2 2 0 0 0 0-4z M12 10a2 2 0 1 0 0 4 2 2 0 0 0 0-4z M12 17a2 2 0 1 0 0 4 2 2 0 0 0 0-4z") }

    private fun solid(name: String, path: String, autoMirror: Boolean = false): ImageVector =
        ImageVector.Builder(
            name = "SolidEditor.$name",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = autoMirror,
        ).addPath(
            pathData = addPathNodes(path),
            pathFillType = PathFillType.EvenOdd,
            fill = SolidColor(Color.Black),
        ).build()
}
