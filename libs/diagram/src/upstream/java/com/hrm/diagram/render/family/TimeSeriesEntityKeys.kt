package com.hrm.diagram.render.family

import com.hrm.diagram.core.ir.TimeSeriesIR
import com.hrm.diagram.render.cache.DrawEntityKey

internal object TimeSeriesEntityKeys {
    fun keys(prefix: String, model: TimeSeriesIR): List<String> {
        val keys = ArrayList<String>()
        keys += DrawEntityKey.decoration(prefix, "time-series", "axis")
        model.tracks.forEach { track -> keys += "$prefix.time-series.lane.${stableSegment(track.id.value)}" }
        model.items.forEach { item ->
            keys += "$prefix.time-series.item.${stableSegment(item.id.value)}"
            item.depends.forEach { depends ->
                keys += "$prefix.time-series.dependency.${stableSegment(depends.value)}-${stableSegment(item.id.value)}"
            }
        }
        return keys
    }
}
