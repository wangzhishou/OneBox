package com.hrm.diagram.render.family

import com.hrm.diagram.core.ir.GaugeIR
import com.hrm.diagram.core.ir.QuadrantChartIR
import com.hrm.diagram.core.ir.XYChartIR

internal object ChartEntityKeys {
    fun gaugeKeys(prefix: String, model: GaugeIR): List<String> =
        listOf(
            "$prefix.gauge.arc",
            "$prefix.gauge.value",
            "$prefix.gauge.label",
        )

    fun xyKeys(prefix: String, model: XYChartIR): List<String> {
        val keys = ArrayList<String>()
        keys += "$prefix.xy.axis.x"
        keys += "$prefix.xy.axis.y"
        keys += "$prefix.xy.grid"
        model.series.forEachIndexed { seriesIndex, series ->
            val seriesKey = stableSegment(series.name.ifBlank { "series-$seriesIndex" })
            keys += "$prefix.xy.series.$seriesIndex.$seriesKey"
            series.xs.indices.forEach { pointIndex -> keys += "$prefix.xy.point.$seriesIndex.$pointIndex.$seriesKey" }
        }
        keys += "$prefix.xy.legend"
        return keys
    }

    fun quadrantKeys(prefix: String, model: QuadrantChartIR): List<String> {
        val keys = ArrayList<String>()
        (1..4).forEach { keys += "$prefix.quadrant.area.$it" }
        model.points.forEach { point -> keys += "$prefix.quadrant.point.${stableSegment(point.id.value)}" }
        keys += "$prefix.quadrant.axis"
        return keys
    }
}
