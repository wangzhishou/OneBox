package com.hrm.diagram.render.family

import com.hrm.diagram.core.ir.GitGraphIR
import com.hrm.diagram.core.ir.JourneyIR
import com.hrm.diagram.core.ir.KanbanIR
import com.hrm.diagram.core.ir.SankeyIR

internal object MiscFamilyEntityKeys {
    fun journeyKeys(prefix: String, model: JourneyIR): List<String> =
        buildList {
            model.stages.forEachIndexed { stageIndex, stage ->
                add("$prefix.journey.stage.$stageIndex")
                stage.steps.forEachIndexed { stepIndex, step ->
                    add("$prefix.journey.step.$stageIndex.$stepIndex")
                    step.actors.forEachIndexed { actorIndex, _ -> add("$prefix.journey.actor.$stageIndex.$stepIndex.$actorIndex") }
                }
            }
        }

    fun kanbanKeys(prefix: String, model: KanbanIR): List<String> =
        buildList {
            model.columns.forEach { column ->
                add("$prefix.kanban.column.${stableSegment(column.id.value)}")
                column.cards.forEach { card -> add("$prefix.kanban.card.${stableSegment(card.id.value)}") }
            }
        }

    fun sankeyKeys(prefix: String, model: SankeyIR): List<String> =
        buildList {
            model.nodes.forEach { node -> add("$prefix.sankey.node.${stableSegment(node.id.value)}") }
            model.flows.forEachIndexed { index, flow ->
                add("$prefix.sankey.flow.$index.${stableSegment(flow.from.value)}-${stableSegment(flow.to.value)}")
            }
        }

    fun gitGraphKeys(prefix: String, model: GitGraphIR): List<String> =
        buildList {
            model.branches.forEach { branch -> add("$prefix.git.branch.${stableSegment(branch)}") }
            model.commits.forEach { commit ->
                add("$prefix.git.commit.${stableSegment(commit.id.value)}")
                commit.parents.forEach { parent -> add("$prefix.git.edge.${stableSegment(parent.value)}-${stableSegment(commit.id.value)}") }
            }
        }
}
