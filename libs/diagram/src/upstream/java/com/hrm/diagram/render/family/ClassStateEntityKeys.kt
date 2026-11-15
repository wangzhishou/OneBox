package com.hrm.diagram.render.family

import com.hrm.diagram.core.ir.ClassIR
import com.hrm.diagram.core.ir.StateIR

internal object ClassStateEntityKeys {
    fun stateKeys(prefix: String, model: StateIR): List<String> {
        val keys = ArrayList<String>()
        model.states.forEach { state -> keys += "$prefix.state.node.${stableSegment(state.id.value)}" }
        model.transitions.forEachIndexed { index, transition ->
            keys += "$prefix.state.transition.$index.${stableSegment(transition.from.value)}-${stableSegment(transition.to.value)}"
        }
        model.notes.forEachIndexed { index, note ->
            keys += "$prefix.state.note.$index.${stableSegment(note.targetState?.value.orEmpty())}"
        }
        return keys
    }

    fun classKeys(prefix: String, model: ClassIR): List<String> {
        val keys = ArrayList<String>()
        model.namespaces.forEach { namespace -> keys += "$prefix.class.namespace.${stableSegment(namespace.id)}" }
        model.classes.forEach { klass ->
            keys += "$prefix.class.node.${stableSegment(klass.id.value)}"
            klass.members.forEachIndexed { index, member ->
                keys += "$prefix.class.member.${stableSegment(klass.id.value)}.$index.${stableSegment(member.name)}"
            }
        }
        model.relations.forEachIndexed { index, relation ->
            keys += "$prefix.class.relation.$index.${stableSegment(relation.from.value)}-${stableSegment(relation.to.value)}"
        }
        model.notes.forEachIndexed { index, note ->
            keys += "$prefix.class.note.$index.${stableSegment(note.targetClass?.value.orEmpty())}"
        }
        return keys
    }
}
