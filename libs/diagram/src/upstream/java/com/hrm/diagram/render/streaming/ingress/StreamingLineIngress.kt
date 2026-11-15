package com.hrm.diagram.render.streaming.ingress

/** Stateful chunk-to-line splitter for line-oriented streaming languages. */
internal class StreamingLineIngress {
    private var pending: String = ""

    fun feed(chunk: CharSequence, isFinal: Boolean): List<String> {
        val merged = pending + chunk.toString()
        val lines = ArrayList<String>()
        var start = 0
        for (i in merged.indices) {
            if (merged[i] == '\n') {
                lines += merged.substring(start, i).trimEnd('\r')
                start = i + 1
            }
        }
        pending = if (start < merged.length) merged.substring(start) else ""
        if (isFinal && pending.isNotEmpty()) {
            lines += pending.trimEnd('\r')
            pending = ""
        }
        return lines
    }

    fun clear() {
        pending = ""
    }
}

/** Stateful token-line drain that preserves the partial tail between append calls. */
internal class TokenLineDrain<T>(
    private val isNewline: (T) -> Boolean,
) {
    private val buffer: MutableList<T> = ArrayList()

    fun add(tokens: List<T>) {
        buffer += tokens
    }

    fun drain(eos: Boolean): List<List<T>> {
        val out = ArrayList<List<T>>()
        var start = 0
        for (i in buffer.indices) {
            if (isNewline(buffer[i])) {
                if (i > start) out += buffer.subList(start, i).toList()
                start = i + 1
            }
        }
        if (eos && start < buffer.size) {
            out += buffer.subList(start, buffer.size).toList()
            buffer.clear()
        } else {
            val tail = if (start < buffer.size) buffer.subList(start, buffer.size).toList() else emptyList()
            buffer.clear()
            buffer.addAll(tail)
        }
        return out
    }

    fun clear() {
        buffer.clear()
    }
}
