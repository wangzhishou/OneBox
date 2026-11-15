package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.ir.SeriesKind
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.parser.plantuml.PlantUmlStructFormat
import com.hrm.diagram.render.streaming.dispatcher.SubPipelineRegistry

internal enum class PlantUmlDiagramKind {
    Sequence,
    Class,
    State,
    Component,
    Usecase,
    Activity,
    Object,
    Deployment,
    Erd,
    Mindmap,
    Wbs,
    Json,
    Yaml,
    Network,
    Gantt,
    Timing,
    Salt,
    Archimate,
    C4,
    Ditaa,
    Pie,
    BarChart,
    LineChart,
    ScatterChart,
}

internal data class PlantUmlStartDirective(
    val kind: PlantUmlDiagramKind,
    val closingDirective: String,
)

internal class PlantUmlSubPipelineRegistry(
    private val textMeasurer: TextMeasurer,
    private val theme: DiagramTheme,
) : SubPipelineRegistry<PlantUmlDiagramKind, PlantUmlSubPipeline> {
    private companion object {
        val ANCHORED_NOTE = Regex(
            "^note\\s+(left|right|top|bottom)\\s+of\\s+([A-Za-z0-9_.:-]+)\\s*:\\s*(.+)$",
            RegexOption.IGNORE_CASE,
        )
        val ANCHORED_NOTE_BLOCK = Regex(
            "^note\\s+(left|right|top|bottom)\\s+of\\s+([A-Za-z0-9_.:-]+)\\s*$",
            RegexOption.IGNORE_CASE,
        )
        val WBS_PREFIX_CUE = Regex("""^(?:\*+|[+\-]+)[<>]?_?(?:\s+|:).+""")
        val ARCHIMATE_REL_CUE = Regex("""^Rel(?:_[A-Za-z0-9_]+)?\(""")
        val GANTT_TASK_CUE = Regex("""^\[[^\]]+\]\s+(starts|lasts|ends|happens)\b""", RegexOption.IGNORE_CASE)
        val ER_RELATION_CUE = Regex("""^[A-Za-z0-9_.:-]+\s+[|}{o.\-]+\s+[A-Za-z0-9_.:-]+(?:\s*:\s*.+)?$""")
        val USECASE_ALIAS_CUE = Regex("^:[^:]+:\\s+as\\s+[A-Za-z0-9_.:-]+$")
        val USECASE_SLASH_ALIAS_CUE = Regex("^:[^:]+:/\\s+as\\s+[A-Za-z0-9_.:-]+$")
        val TIMING_STATE_CUE = Regex("""^[A-Za-z0-9_.:-]+\s+is\s+.+$""", RegexOption.IGNORE_CASE)
        val C4_ELEMENT_CUE = Regex("""^(Person|Person_Ext|System|System_Ext|SystemDb|SystemDb_Ext|SystemQueue|SystemQueue_Ext|Container|Container_Ext|ContainerDb|ContainerDb_Ext|ContainerQueue|ContainerQueue_Ext|Component|Component_Ext|ComponentDb|ComponentDb_Ext|ComponentQueue|ComponentQueue_Ext|Boundary|Enterprise_Boundary|System_Boundary|Container_Boundary)\(""")
    }

    override val defaultKind: PlantUmlDiagramKind = PlantUmlDiagramKind.Sequence

    override fun create(kind: PlantUmlDiagramKind): PlantUmlSubPipeline =
        when (kind) {
            PlantUmlDiagramKind.Sequence -> PlantUmlSequenceSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Class -> PlantUmlClassSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.State -> PlantUmlStateSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Component -> PlantUmlComponentSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Usecase -> PlantUmlUsecaseSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Activity -> PlantUmlActivitySubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Object -> PlantUmlObjectSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Deployment -> PlantUmlDeploymentSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Erd -> PlantUmlErdSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Mindmap -> PlantUmlMindmapSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Wbs -> PlantUmlWbsSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Json -> PlantUmlStructSubPipeline(PlantUmlStructFormat.JSON, textMeasurer, theme)
            PlantUmlDiagramKind.Yaml -> PlantUmlStructSubPipeline(PlantUmlStructFormat.YAML, textMeasurer, theme)
            PlantUmlDiagramKind.Network -> PlantUmlNetworkSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Gantt -> PlantUmlTimeSeriesSubPipeline(PlantUmlTimeSeriesSubPipeline.Kind.Gantt, textMeasurer, theme)
            PlantUmlDiagramKind.Timing -> PlantUmlTimeSeriesSubPipeline(PlantUmlTimeSeriesSubPipeline.Kind.Timing, textMeasurer, theme)
            PlantUmlDiagramKind.Salt -> PlantUmlSaltSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Archimate -> PlantUmlArchimateSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.C4 -> PlantUmlC4SubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Ditaa -> PlantUmlDitaaSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.Pie -> PlantUmlPieSubPipeline(textMeasurer, theme)
            PlantUmlDiagramKind.BarChart -> PlantUmlXYChartSubPipeline(SeriesKind.Bar, textMeasurer, theme)
            PlantUmlDiagramKind.LineChart -> PlantUmlXYChartSubPipeline(SeriesKind.Line, textMeasurer, theme)
            PlantUmlDiagramKind.ScatterChart -> PlantUmlXYChartSubPipeline(SeriesKind.Scatter, textMeasurer, theme)
        }

    fun acceptsBufferedSkinparam(kind: PlantUmlDiagramKind): Boolean =
        when (kind) {
            PlantUmlDiagramKind.Sequence,
            PlantUmlDiagramKind.Activity,
            PlantUmlDiagramKind.Usecase,
            PlantUmlDiagramKind.State,
            PlantUmlDiagramKind.Class,
            PlantUmlDiagramKind.Component,
            PlantUmlDiagramKind.Deployment,
            PlantUmlDiagramKind.Object,
            PlantUmlDiagramKind.Pie,
            PlantUmlDiagramKind.BarChart,
            PlantUmlDiagramKind.LineChart,
            PlantUmlDiagramKind.ScatterChart,
            -> true
            else -> false
        }

    fun acceptsActiveSkinparam(kind: PlantUmlDiagramKind, trimmed: String): Boolean =
        when {
            acceptsBufferedSkinparam(kind) -> true
            kind == PlantUmlDiagramKind.Ditaa -> trimmed.startsWith("skinparam handwritten", ignoreCase = true)
            else -> false
        }

    fun acceptsBufferedSkinparamLine(kind: PlantUmlDiagramKind, trimmed: String): Boolean =
        acceptsBufferedSkinparam(kind) ||
            (kind == PlantUmlDiagramKind.Ditaa && trimmed.startsWith("skinparam handwritten", ignoreCase = true))

    fun requiresRawPayload(kind: PlantUmlDiagramKind): Boolean =
        kind == PlantUmlDiagramKind.Json ||
            kind == PlantUmlDiagramKind.Yaml ||
            kind == PlantUmlDiagramKind.Ditaa

    fun matchStartDirective(trimmed: String): PlantUmlStartDirective? =
        when {
            trimmed.equals("@startmindmap", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.Mindmap, "@endmindmap")
            trimmed.equals("@startwbs", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.Wbs, "@endwbs")
            trimmed.equals("@startjson", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.Json, "@endjson")
            trimmed.equals("@startyaml", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.Yaml, "@endyaml")
            trimmed.equals("@startnwdiag", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.Network, "@endnwdiag")
            trimmed.equals("@startgantt", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.Gantt, "@endgantt")
            trimmed.equals("@startsalt", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.Salt, "@endsalt")
            trimmed.equals("@startditaa", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.Ditaa, "@endditaa")
            trimmed.equals("@startpie", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.Pie, "@endpie")
            trimmed.equals("@startbar", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.BarChart, "@endbar")
            trimmed.equals("@startline", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.LineChart, "@endline")
            trimmed.equals("@startscatter", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.ScatterChart, "@endscatter")
            trimmed.equals("@startchart", ignoreCase = true) -> PlantUmlStartDirective(PlantUmlDiagramKind.BarChart, "@endchart")
            else -> null
        }

    fun classifyImmediate(line: String): PlantUmlDiagramKind? {
        val lower = line.lowercase()
        if (lower == "nwdiag {" || lower == "nwdiag{") {
            return PlantUmlDiagramKind.Network
        }
        if (lower == "salt" || lower == "salt {" || lower == "salt{") {
            return PlantUmlDiagramKind.Salt
        }
        if (isWbsCue(line)) {
            return PlantUmlDiagramKind.Wbs
        }
        if (lower == "pie") {
            return PlantUmlDiagramKind.Pie
        }
        if (lower == "bar") {
            return PlantUmlDiagramKind.BarChart
        }
        if (lower == "line") {
            return PlantUmlDiagramKind.LineChart
        }
        if (lower == "scatter") {
            return PlantUmlDiagramKind.ScatterChart
        }
        if (lower == "chart") {
            return PlantUmlDiagramKind.BarChart
        }
        if (lower.startsWith("bar ") || lower.startsWith("h-axis ") || lower.startsWith("v-axis ")) {
            return PlantUmlDiagramKind.BarChart
        }
        if (lower.startsWith("line ")) {
            return PlantUmlDiagramKind.LineChart
        }
        if (lower.startsWith("scatter ")) {
            return PlantUmlDiagramKind.ScatterChart
        }
        if (isC4Cue(line)) {
            return PlantUmlDiagramKind.C4
        }
        if (lower.startsWith("archimate ") || ARCHIMATE_REL_CUE.containsMatchIn(line)) {
            return PlantUmlDiagramKind.Archimate
        }
        if (lower.startsWith("project starts") || GANTT_TASK_CUE.containsMatchIn(line)) {
            return PlantUmlDiagramKind.Gantt
        }
        if (isTimingCue(line)) {
            return PlantUmlDiagramKind.Timing
        }
        if (
            lower.startsWith("object ") ||
            (line.contains(':') && line.contains('=') && !line.startsWith(":") && !line.contains("->") && !line.contains("<-"))
        ) {
            return PlantUmlDiagramKind.Object
        }
        if (
            lower.startsWith("entity ") ||
            ER_RELATION_CUE.matches(line)
        ) {
            return PlantUmlDiagramKind.Erd
        }
        if (lower.startsWith("artifact ")) {
            return PlantUmlDiagramKind.Deployment
        }
        if (lower.startsWith("storage ")) {
            return PlantUmlDiagramKind.Deployment
        }
        if (
            lower == "start" ||
            lower == "stop" ||
            lower == "end" ||
            lower == "(*)" ||
            lower == "(*top)" ||
            lower == "}" ||
            lower.startsWith("partition ") ||
            line.trim().startsWith("===") ||
            line.startsWith("#") ||
            (line.startsWith(":") && line.endsWith(";")) ||
            lower.startsWith("if ") ||
            lower.startsWith("if(") ||
            lower.startsWith("else") ||
            lower == "endif" ||
            lower.startsWith("while ") ||
            lower.startsWith("while(") ||
            lower == "endwhile" ||
            lower == "end note" ||
            lower == "endnote" ||
            lower.startsWith("note:") ||
            (lower.startsWith("note ") && !ANCHORED_NOTE.matches(line) && !ANCHORED_NOTE_BLOCK.matches(line) && !line.contains(" of ")) ||
            isLegacyActivityArrowCue(line)
        ) {
            return PlantUmlDiagramKind.Activity
        }
        if (
            lower.startsWith("usecase ") ||
            lower.startsWith("rectangle ") ||
            (line.startsWith("(") && line.contains(")")) ||
            (line.startsWith(":") && (line.endsWith(":") || line.endsWith(":/") || USECASE_ALIAS_CUE.matches(line) || USECASE_SLASH_ALIAS_CUE.matches(line))) ||
            lower.startsWith("actor/") ||
            (
                line.contains("(") &&
                    line.contains(")") &&
                    (line.contains("--") || line.contains("..") || line.contains(".>") || line.contains("<."))
                )
        ) {
            return PlantUmlDiagramKind.Usecase
        }
        if (
            lower.startsWith("component ") ||
            lower.startsWith("()") ||
            (
                line.startsWith("[") &&
                    line.contains("]") &&
                    !line.contains("[*]") &&
                    !line.contains("[H]") &&
                    !line.contains("[H*]")
                ) ||
            lower.startsWith("database ") ||
            lower.startsWith("queue ") ||
            lower.startsWith("frame ") ||
            lower.startsWith("port ") ||
            lower.startsWith("portin ") ||
            lower.startsWith("portout ")
        ) {
            return PlantUmlDiagramKind.Component
        }
        if (
            lower.startsWith("state ") ||
            line.contains("[*]") ||
            line.contains("[H]") ||
            line.contains("[H*]") ||
            line == "--"
        ) {
            return PlantUmlDiagramKind.State
        }
        if (isWbsCue(line)) {
            return PlantUmlDiagramKind.Wbs
        }
        if (
            lower.startsWith("class ") ||
            lower.startsWith("abstract class ") ||
            lower.startsWith("interface ") ||
            lower.startsWith("enum ") ||
            line.contains("<|--") ||
            line.contains("<|..") ||
            line.contains("..|>") ||
            line.contains("*--") ||
            line.contains("o--")
        ) {
            return PlantUmlDiagramKind.Class
        }
        if (
            lower.startsWith("participant ") ||
            lower.startsWith("boundary ") ||
            lower.startsWith("control ") ||
            lower.startsWith("entity ") ||
            lower.startsWith("database ") ||
            lower.startsWith("collections ") ||
            lower.startsWith("queue ") ||
            lower.startsWith("activate ") ||
            lower.startsWith("deactivate ") ||
            lower.startsWith("autonumber") ||
            lower.startsWith("create ") ||
            lower.startsWith("destroy ") ||
            lower.startsWith("ref ") ||
            lower.startsWith("box") ||
            lower == "end box" ||
            lower == "endbox" ||
            lower.startsWith("return") ||
            lower.startsWith("loop ") ||
            lower.startsWith("alt ") ||
            lower.startsWith("opt ") ||
            lower.startsWith("par ") ||
            lower.startsWith("critical ") ||
            lower.startsWith("break ") ||
            lower == "end" ||
            line.contains("->>") ||
            line.contains("<<-") ||
            line.contains("-> ") ||
            line.contains("->:") ||
            line.contains(" <-")
        ) {
            return PlantUmlDiagramKind.Sequence
        }
        return null
    }

    fun shouldDeferImmediate(kind: PlantUmlDiagramKind, line: String, bufferedBodyLines: List<String>): Boolean {
        val lower = line.lowercase()
        val sawAmbiguousContainerCue = bufferedBodyLines.any {
            val candidate = it.lowercase()
            candidate.startsWith("node ") || candidate.startsWith("cloud ")
        }
        if (!sawAmbiguousContainerCue) return false
        return when (kind) {
            PlantUmlDiagramKind.Component ->
                lower.startsWith("database ") ||
                    line.startsWith("[") ||
                    lower.startsWith("queue ") ||
                    lower.startsWith("frame ")
            PlantUmlDiagramKind.Sequence -> true
            PlantUmlDiagramKind.Activity -> line == "}"
            else -> false
        }
    }

    fun detectBufferedKind(bufferedBodyLines: List<String>): PlantUmlDiagramKind? {
        var sawStateCue = false
        var sawClassCue = false
        var sawSequenceCue = false
        var sawComponentCue = false
        var sawExplicitComponentCue = false
        var sawUsecaseCue = false
        var sawPackageCue = false
        var sawActorCue = false
        var sawActivityCue = false
        var sawObjectCue = false
        var sawDeploymentCue = false
        var sawErdCue = false
        var sawNetworkCue = false
        var sawGanttCue = false
        var sawTimingCue = false
        var sawSaltCue = false
        var sawArchimateCue = false
        var sawC4Cue = false
        var sawWbsCue = false
        var sawPieCue = false
        var sawBarCue = false
        var sawLineCue = false
        var sawScatterCue = false
        var sawAmbiguousContainerCue = false
        var sawBracketArtifactCue = false
        for (line in bufferedBodyLines) {
            val lower = line.lowercase()
            if (lower == "nwdiag {" || lower == "nwdiag{") {
                sawNetworkCue = true
            }
            if (lower == "salt" || lower == "salt {" || lower == "salt{") {
                sawSaltCue = true
            }
            if (isWbsCue(line)) {
                sawWbsCue = true
            }
            if (lower == "pie") {
                sawPieCue = true
            }
            if (lower == "bar") {
                sawBarCue = true
            }
            if (lower == "line") {
                sawLineCue = true
            }
            if (lower == "scatter") {
                sawScatterCue = true
            }
            if (isC4Cue(line)) {
                sawC4Cue = true
            }
            if (lower.startsWith("archimate ") || ARCHIMATE_REL_CUE.containsMatchIn(line)) {
                sawArchimateCue = true
            }
            if (lower.startsWith("project starts") || GANTT_TASK_CUE.containsMatchIn(line)) {
                sawGanttCue = true
            }
            if (isTimingCue(line)) {
                sawTimingCue = true
            }
            if (
                lower.startsWith("object ") ||
                (line.contains(':') && line.contains('=') && !line.startsWith(":") && !line.contains("->") && !line.contains("<-"))
            ) {
                sawObjectCue = true
            }
            if (
                lower.startsWith("entity ") ||
                ER_RELATION_CUE.matches(line)
            ) {
                sawErdCue = true
            }
            if (line.startsWith("[") && line.contains("]")) {
                sawBracketArtifactCue = true
            }
            if (
                lower.startsWith("artifact ") ||
                lower.startsWith("database ") ||
                lower.startsWith("frame ") ||
                lower.startsWith("storage ")
            ) {
                sawDeploymentCue = true
            }
            if (
                lower.startsWith("node ") ||
                lower.startsWith("cloud ")
            ) {
                sawAmbiguousContainerCue = true
            }
            if (
                lower == "start" ||
                lower == "stop" ||
                lower == "end" ||
                lower == "(*)" ||
                lower == "(*top)" ||
                lower == "}" ||
                lower.startsWith("partition ") ||
                line.trim().startsWith("===") ||
                line.startsWith("#") ||
                (line.startsWith(":") && line.endsWith(";")) ||
                lower.startsWith("if ") ||
                lower.startsWith("if(") ||
                lower.startsWith("else") ||
                lower == "endif" ||
                lower.startsWith("while ") ||
                lower.startsWith("while(") ||
                lower == "endwhile" ||
                lower == "end note" ||
                lower == "endnote" ||
                lower.startsWith("note:") ||
                (lower.startsWith("note ") && !ANCHORED_NOTE.matches(line) && !ANCHORED_NOTE_BLOCK.matches(line) && !line.contains(" of ")) ||
                isLegacyActivityArrowCue(line)
            ) {
                sawActivityCue = true
            }
            if (lower.startsWith("actor ") || lower.startsWith("actor/")) {
                sawActorCue = true
            }
            if (lower.startsWith("package ")) {
                sawPackageCue = true
            }
            if (
                lower.startsWith("usecase ") ||
                lower.startsWith("rectangle ") ||
                (line.startsWith("(") && line.contains(")")) ||
                (line.startsWith(":") && (line.endsWith(":") || line.endsWith(":/") || USECASE_ALIAS_CUE.matches(line) || USECASE_SLASH_ALIAS_CUE.matches(line))) ||
                (
                    line.contains("(") &&
                        line.contains(")") &&
                        (line.contains("--") || line.contains("..") || line.contains(".>") || line.contains("<."))
                    )
            ) {
                sawUsecaseCue = true
            }
            if (
                lower.startsWith("component ") ||
                lower.startsWith("()") ||
                lower.startsWith("port ") ||
                lower.startsWith("portin ") ||
                lower.startsWith("portout ")
            ) {
                sawExplicitComponentCue = true
            }
            if (
                lower.startsWith("component ") ||
                lower.startsWith("()") ||
                lower.startsWith("database ") ||
                lower.startsWith("queue ") ||
                lower.startsWith("frame ") ||
                lower.startsWith("port ") ||
                lower.startsWith("portin ") ||
                lower.startsWith("portout ")
            ) {
                sawComponentCue = true
            }
            if (
                lower.startsWith("state ") ||
                trimmedEqualsDirection(lower) ||
                line.contains("[*]") ||
                line.contains("[H]") ||
                line.contains("[H*]") ||
                line == "--"
            ) {
                sawStateCue = true
            }
            if (
                lower.startsWith("class ") ||
                lower.startsWith("abstract class ") ||
                lower.startsWith("interface ") ||
                lower.startsWith("enum ") ||
                line.contains("<|--") ||
                line.contains("--|>") ||
                line.contains("<|..") ||
                line.contains("..|>") ||
                line.contains("*--") ||
                line.contains("o--") ||
                line.contains("..>") ||
                line.contains(" -- ") ||
                line.contains(" .. ")
            ) {
                sawClassCue = true
            }
            if (
                lower.startsWith("participant ") ||
                lower.startsWith("boundary ") ||
                lower.startsWith("control ") ||
                lower.startsWith("database ") ||
                lower.startsWith("collections ") ||
                lower.startsWith("queue ") ||
                lower.startsWith("activate ") ||
                lower.startsWith("deactivate ") ||
                lower.startsWith("autonumber") ||
                lower.startsWith("create ") ||
                lower.startsWith("destroy ") ||
                lower.startsWith("ref ") ||
                lower.startsWith("box") ||
                lower == "end box" ||
                lower == "endbox" ||
                lower.startsWith("return") ||
                lower.startsWith("loop ") ||
                lower.startsWith("alt ") ||
                lower.startsWith("opt ") ||
                lower.startsWith("par ") ||
                lower.startsWith("critical ") ||
                lower.startsWith("break ") ||
                lower == "end" ||
                line.contains("->") ||
                line.contains("<-")
            ) {
                sawSequenceCue = true
            }
        }
        return when {
            sawNetworkCue -> PlantUmlDiagramKind.Network
            sawSaltCue -> PlantUmlDiagramKind.Salt
            sawPieCue -> PlantUmlDiagramKind.Pie
            sawBarCue -> PlantUmlDiagramKind.BarChart
            sawLineCue -> PlantUmlDiagramKind.LineChart
            sawScatterCue -> PlantUmlDiagramKind.ScatterChart
            sawC4Cue -> PlantUmlDiagramKind.C4
            sawArchimateCue -> PlantUmlDiagramKind.Archimate
            sawGanttCue -> PlantUmlDiagramKind.Gantt
            sawTimingCue -> PlantUmlDiagramKind.Timing
            sawObjectCue -> PlantUmlDiagramKind.Object
            sawErdCue -> PlantUmlDiagramKind.Erd
            sawAmbiguousContainerCue && sawBracketArtifactCue && !sawExplicitComponentCue -> PlantUmlDiagramKind.Deployment
            sawAmbiguousContainerCue && sawActorCue && !sawUsecaseCue && !sawExplicitComponentCue -> PlantUmlDiagramKind.Deployment
            sawComponentCue -> PlantUmlDiagramKind.Component
            sawDeploymentCue -> PlantUmlDiagramKind.Deployment
            sawWbsCue -> PlantUmlDiagramKind.Wbs
            sawActivityCue -> PlantUmlDiagramKind.Activity
            sawStateCue -> PlantUmlDiagramKind.State
            sawClassCue -> PlantUmlDiagramKind.Class
            sawPackageCue && sawClassCue -> PlantUmlDiagramKind.Class
            sawPackageCue && sawUsecaseCue -> PlantUmlDiagramKind.Usecase
            sawActorCue && sawUsecaseCue -> PlantUmlDiagramKind.Usecase
            sawUsecaseCue -> PlantUmlDiagramKind.Usecase
            sawSequenceCue -> PlantUmlDiagramKind.Sequence
            sawActorCue -> PlantUmlDiagramKind.Usecase
            sawAmbiguousContainerCue -> PlantUmlDiagramKind.Component
            sawPackageCue -> PlantUmlDiagramKind.Component
            else -> null
        }
    }

    private fun trimmedEqualsDirection(lower: String): Boolean =
        lower == "left to right direction" ||
            lower == "right to left direction" ||
            lower == "top to bottom direction" ||
            lower == "bottom to top direction"

    private fun isWbsCue(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed == "--" || trimmed.contains("->") || trimmed.contains("<-")) return false
        if (trimmed.startsWith("@") || trimmed.startsWith("!") || trimmed.startsWith("'") || trimmed.startsWith("//")) return false
        return WBS_PREFIX_CUE.matches(trimmed)
    }

    private fun isLegacyActivityArrowCue(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.startsWith("[*]") || trimmed.startsWith("[H]") || trimmed.startsWith("[H*]")) return false
        return trimmed.startsWith("->") ||
            trimmed.startsWith("-->") ||
            trimmed.startsWith("-up->") ||
            trimmed.startsWith("-down->") ||
            trimmed.startsWith("-left->") ||
            trimmed.startsWith("-right->") ||
            trimmed.startsWith("(*)") ||
            trimmed.startsWith("(*top)") ||
            trimmed.startsWith("\"")
    }

    private fun isTimingCue(line: String): Boolean {
        val lower = line.lowercase()
        return lower.startsWith("clock ") ||
            lower.startsWith("binary ") ||
            lower.startsWith("concise ") ||
            lower.startsWith("robust ") ||
            line.startsWith("@") ||
            TIMING_STATE_CUE.matches(line)
    }

    private fun isC4Cue(line: String): Boolean {
        val trimmed = line.trim()
        val lower = trimmed.lowercase()
        if (lower.startsWith("!include") && lower.contains("c4")) return true
        if (trimmed in setOf("C4Context", "C4Container", "C4Component", "C4Dynamic", "C4Deployment")) return true
        return C4_ELEMENT_CUE.containsMatchIn(trimmed)
    }

}
