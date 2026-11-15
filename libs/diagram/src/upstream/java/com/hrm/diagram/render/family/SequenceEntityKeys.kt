package com.hrm.diagram.render.family

import com.hrm.diagram.core.ir.SequenceIR

internal object SequenceEntityKeys {
    fun keys(prefix: String, model: SequenceIR): List<String> {
        val keys = ArrayList<String>()
        model.participants.forEach { keys += "$prefix.sequence.participant.${stableSegment(it.id.value)}" }
        model.messages.forEachIndexed { index, message ->
            keys += "$prefix.sequence.message.$index.${stableSegment(message.from.value)}-${stableSegment(message.to.value)}"
        }
        model.fragments.forEachIndexed { index, fragment ->
            keys += "$prefix.sequence.fragment.$index.${stableSegment(fragment.kind.name)}"
        }
        return keys
    }
}
