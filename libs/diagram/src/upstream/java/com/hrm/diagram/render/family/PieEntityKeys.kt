package com.hrm.diagram.render.family

import com.hrm.diagram.core.ir.PieIR
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.render.cache.DrawEntityKey

internal object PieEntityKeys {
    fun keys(prefix: String, model: PieIR): List<String> {
        val keys = ArrayList<String>()
        keys += DrawEntityKey.decoration(prefix, "pie", "title")
        model.slices.forEachIndexed { index, slice ->
            val segment = stableSegment(labelText(slice.label).ifBlank { "slice-$index" })
            keys += "$prefix.pie.slice.$index.$segment"
            keys += "$prefix.pie.legend.$index.$segment"
            keys += "$prefix.pie.label.$index.$segment"
        }
        return keys
    }

    private fun labelText(label: RichLabel): String =
        when (label) {
            is RichLabel.Plain -> label.text
            is RichLabel.Markdown -> label.source
            is RichLabel.Html -> label.html
        }
}
