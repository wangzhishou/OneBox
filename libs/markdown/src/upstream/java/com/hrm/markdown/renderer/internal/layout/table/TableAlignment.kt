package com.hrm.markdown.renderer.internal.layout.table

import androidx.compose.ui.text.style.TextAlign
import com.hrm.markdown.parser.ast.Table

/**
 * GFM's explicit left/right column markers are physical directions.
 * Only an unspecified alignment follows the surrounding logical direction.
 */
internal fun tableTextAlign(alignment: Table.Alignment): TextAlign = when (alignment) {
    Table.Alignment.LEFT -> TextAlign.Left
    Table.Alignment.CENTER -> TextAlign.Center
    Table.Alignment.RIGHT -> TextAlign.Right
    Table.Alignment.NONE -> TextAlign.Start
}
