package com.hrm.diagram.render.graph

import com.hrm.diagram.core.ir.RichLabel

internal fun graphLabelText(label: RichLabel?): String =
    when (label) {
        is RichLabel.Plain -> label.text
        is RichLabel.Markdown -> label.source
        is RichLabel.Html -> label.html
        null -> ""
    }
