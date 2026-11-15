package com.hrm.diagram.render.streaming

import com.hrm.diagram.core.DiagramApi
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.ir.Diagnostic
import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.core.ir.Edge
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.IrPatch
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.render.cache.DrawCommandIndex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The streaming entry point. See `docs/streaming.md` §2 for the full contract.
 *
 * Lifecycle:
 * ```
 *   val s = DiagramSession.create(SourceLanguage.MERMAID)
 *   chunks.collect { s.append(it) }
 *   s.finish()
 *   s.close()
 * ```
 *
 * Threading: [append] / [finish] MUST be called from a single producer (typically the LLM
 * stream collector); [state] is safe to observe from any thread / Compose composition.
 *
 * **NOT thread-safe for concurrent producers** — wrap with a Mutex if you have multiple writers.
 */
@DiagramApi
class DiagramSession internal constructor(
    val language: SourceLanguage,
    val theme: DiagramTheme,
    val layoutOptions: LayoutOptions,
    private val pipeline: SessionPipeline,
) {
    private val sourceBuffer = StringBuilder()
    private val _state = MutableStateFlow(DiagramSnapshot.empty(language))
    private var seq: Long = 0L
    private var closed: Boolean = false

    /** Append-only accumulator of every chunk the caller has fed in. */
    val source: CharSequence get() = sourceBuffer

    /** Latest snapshot, hot-updated after each [append] / [finish]. */
    val state: StateFlow<DiagramSnapshot> get() = _state.asStateFlow()

    /**
     * Push the next chunk of source. Returns the patch produced by this advance.
     * Cost target: < 16ms for chunks up to ~100 chars (see `docs/streaming.md` §4).
     */
    fun append(chunk: CharSequence): SessionPatch {
        check(!closed) { "DiagramSession is closed" }
        val absoluteOffset = sourceBuffer.length
        sourceBuffer.append(chunk)
        seq++
        val advance = pipeline.advance(
            previousSnapshot = _state.value,
            chunk = chunk,
            absoluteOffset = absoluteOffset,
            seq = seq,
            isFinal = false,
        )
        _state.value = advance.snapshot
        return advance.patch
    }

    /**
     * Signal end-of-stream. Triggers the pipeline's "finalize" pass which may close pending
     * blocks, run an optional layout finalize, and produce any remaining draw commands.
     * Cost target: < 50ms.
     */
    fun finish(): DiagramSnapshot {
        check(!closed) { "DiagramSession is closed" }
        seq++
        val advance = pipeline.advance(
            previousSnapshot = _state.value,
            chunk = "",
            absoluteOffset = sourceBuffer.length,
            seq = seq,
            isFinal = true,
        )
        _state.value = advance.snapshot
        return advance.snapshot
    }

    /** Release internal caches. Subsequent calls to [append] / [finish] throw. */
    fun close() {
        closed = true
        pipeline.dispose()
    }

    companion object {
        /**
     * Convenience factory for callers that explicitly provide a streaming pipeline.
     * Public facade construction should normally go through `Diagram.session(...)`, which selects
     * the registered Mermaid / PlantUML / DOT pipeline for the requested language.
         */
        internal fun create(
            language: SourceLanguage,
            theme: DiagramTheme = DiagramTheme.Default,
            layoutOptions: LayoutOptions = LayoutOptions(),
            pipeline: SessionPipeline,
        ): DiagramSession = DiagramSession(language, theme, layoutOptions, pipeline)
    }
}

/**
 * Snapshot of everything renderable at a given [seq]. Compose UIs `collectAsState()` this
 * through internal view plumbing; app-facing Compose code should prefer `DiagramView(source = ...)`.
 *
 * Invariants:
 * - `seq` is strictly increasing within one session.
 * - `drawCommands` is always a complete list (not just deltas) — UIs may rely on it for full repaint.
 * - `ir` is null only before the first non-empty chunk has produced any node.
 */
@DiagramApi
data class DiagramSnapshot(
    val ir: DiagramModel?,
    val laidOut: LaidOutDiagram?,
    val drawCommands: List<DrawCommand>,
    val diagnostics: List<Diagnostic>,
    val seq: Long,
    val isFinal: Boolean,
    val sourceLanguage: SourceLanguage,
) {
    internal val drawCommandIndex: DrawCommandIndex by lazy(LazyThreadSafetyMode.NONE) {
        DrawCommandIndex.from(drawCommands, laidOut?.bounds)
    }

    companion object {
        internal fun empty(language: SourceLanguage): DiagramSnapshot = DiagramSnapshot(
            ir = null,
            laidOut = null,
            drawCommands = emptyList(),
            diagnostics = emptyList(),
            seq = 0L,
            isFinal = false,
            sourceLanguage = language,
        )
    }
}

/**
 * Diff between two consecutive [DiagramSnapshot]s. Useful for animation hooks and benchmarks
 * (e.g. assert that no chunk produces > N new draw commands).
 */
@DiagramApi
data class SessionPatch(
    val seq: Long,
    val addedNodes: List<NodeId>,
    val addedEdges: List<Edge>,
    val addedDrawCommands: List<DrawCommand>,
    val newDiagnostics: List<Diagnostic>,
    val isFinal: Boolean,
) {
    val isEmpty: Boolean
        get() = addedNodes.isEmpty() && addedEdges.isEmpty() &&
            addedDrawCommands.isEmpty() && newDiagnostics.isEmpty()

    companion object {
        internal fun empty(seq: Long, isFinal: Boolean = false): SessionPatch =
            SessionPatch(seq, emptyList(), emptyList(), emptyList(), emptyList(), isFinal)
    }
}

/**
 * Output bundle returned by a single [SessionPipeline.advance] call.
 * The session owns wiring it into [DiagramSession.state]; the pipeline owns producing it.
 */
internal data class PipelineAdvance(
    val snapshot: DiagramSnapshot,
    val patch: SessionPatch,
    /** Raw IR patches from the parser layer this round (exposed for tests / observers). */
    val irBatch: IrPatchBatch = IrPatchBatch(snapshot.seq, emptyList()),
)

/**
 * SPI that glues lexer → parser → layout → render. Production sessions are selected by the
 * facade; tests can still inject a small no-op or capture pipeline through [DiagramSession.create].
 *
 * Implementations MUST honour the streaming contract from `docs/streaming.md` §3:
 * - Append-only IR patches (no removals across `advance` calls).
 * - Pinned layout coordinates when [LayoutOptions.incremental].
 * - Bounded per-call work; soft 16ms watchdog discipline.
 */
internal interface SessionPipeline {
    fun advance(
        previousSnapshot: DiagramSnapshot,
        chunk: CharSequence,
        absoluteOffset: Int,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance

    fun dispose() {}
}

/**
 * No-op pipeline: bumps `seq` and emits an empty patch. Useful for:
 * - Unit-testing the [DiagramSession] state machine in isolation.
 */
internal class NoopSessionPipeline : SessionPipeline {
    override fun advance(
        previousSnapshot: DiagramSnapshot,
        chunk: CharSequence,
        absoluteOffset: Int,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance {
        val snap = previousSnapshot.copy(seq = seq, isFinal = isFinal)
        return PipelineAdvance(
            snapshot = snap,
            patch = SessionPatch.empty(seq, isFinal),
        )
    }
}

@Deprecated(
    message = "Use NoopSessionPipeline for tests; production code should use Diagram.session(...).",
    replaceWith = ReplaceWith("NoopSessionPipeline()"),
)
internal typealias StubSessionPipeline = NoopSessionPipeline

/** Suppress unused import. */
@Suppress("unused") private val unusedIrPatch: IrPatch? = null
