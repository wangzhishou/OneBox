package com.hrm.diagram.render.family

import com.hrm.diagram.core.ir.ActivityIR
import com.hrm.diagram.core.ir.ClassIR
import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.core.ir.GaugeIR
import com.hrm.diagram.core.ir.GitGraphIR
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.JourneyIR
import com.hrm.diagram.core.ir.KanbanIR
import com.hrm.diagram.core.ir.PieIR
import com.hrm.diagram.core.ir.QuadrantChartIR
import com.hrm.diagram.core.ir.SankeyIR
import com.hrm.diagram.core.ir.SequenceIR
import com.hrm.diagram.core.ir.StateIR
import com.hrm.diagram.core.ir.StructIR
import com.hrm.diagram.core.ir.TimeSeriesIR
import com.hrm.diagram.core.ir.TreeIR
import com.hrm.diagram.core.ir.WireframeIR
import com.hrm.diagram.core.ir.XYChartIR
import com.hrm.diagram.render.cache.DrawEntityKey

/**
 * Central registry for stable entity keys shared by Mermaid and PlantUML family renderers.
 */
internal object FamilyEntityKeyRegistry {
    fun semanticKeys(prefix: String, model: DiagramModel?): List<String> {
        val p = normalizedEntityPrefix(prefix)
        val keys = ArrayList<String>()
        when (model) {
            null -> keys += DrawEntityKey.decoration(p, "empty", "frame")
            is GraphIR -> keys += GraphEntityKeys.keys(p, model)
            is SequenceIR -> keys += SequenceEntityKeys.keys(p, model)
            is TimeSeriesIR -> keys += TimeSeriesEntityKeys.keys(p, model)
            is TreeIR -> keys += TreeEntityKeys.keys(p, model)
            is JourneyIR -> keys += MiscFamilyEntityKeys.journeyKeys(p, model)
            is PieIR -> keys += PieEntityKeys.keys(p, model)
            is GaugeIR -> keys += ChartEntityKeys.gaugeKeys(p, model)
            is KanbanIR -> keys += MiscFamilyEntityKeys.kanbanKeys(p, model)
            is XYChartIR -> keys += ChartEntityKeys.xyKeys(p, model)
            is QuadrantChartIR -> keys += ChartEntityKeys.quadrantKeys(p, model)
            is SankeyIR -> keys += MiscFamilyEntityKeys.sankeyKeys(p, model)
            is GitGraphIR -> keys += MiscFamilyEntityKeys.gitGraphKeys(p, model)
            is ActivityIR -> keys += StructuralEntityKeys.activityKeys(p, model)
            is WireframeIR -> keys += StructuralEntityKeys.wireframeKeys(p, model)
            is StructIR -> keys += StructuralEntityKeys.structKeys(p, model)
            is StateIR -> keys += ClassStateEntityKeys.stateKeys(p, model)
            is ClassIR -> keys += ClassStateEntityKeys.classKeys(p, model)
        }
        return keys.distinct()
    }

    fun familyKey(model: DiagramModel?): String =
        when (model) {
            null -> "empty"
            is GraphIR -> "graph"
            is SequenceIR -> "sequence"
            is TimeSeriesIR -> "time-series"
            is TreeIR -> "tree"
            is JourneyIR -> "journey"
            is PieIR -> "pie"
            is GaugeIR -> "gauge"
            is KanbanIR -> "kanban"
            is XYChartIR -> "xy-chart"
            is QuadrantChartIR -> "quadrant-chart"
            is SankeyIR -> "sankey"
            is GitGraphIR -> "git-graph"
            is ActivityIR -> "activity"
            is WireframeIR -> "wireframe"
            is StructIR -> "struct"
            is StateIR -> "state"
            is ClassIR -> "class"
        }
}
