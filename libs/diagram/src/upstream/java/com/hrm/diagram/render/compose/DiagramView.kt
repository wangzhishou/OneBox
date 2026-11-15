package com.hrm.diagram.render.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hrm.diagram.core.DiagramApi
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.render.Diagram
import com.hrm.diagram.render.streaming.DiagramSession
import com.hrm.diagram.render.streaming.DiagramSnapshot
import kotlinx.coroutines.delay

/**
 * Public Compose facade for rendering diagram source text.
 *
 * The view owns language detection, session lifecycle, snapshot production, and append-only
 * incremental updates internally.
 *
 * Minimal usage:
 * ```kotlin
 * DiagramView(source = mermaidText, modifier = Modifier.fillMaxWidth())
 * ```
 *
 * @param languageHint Optional explicit source language. Pass this when the caller already
 * knows the fenced language and wants to avoid detection churn while the source is still
 * streaming or incomplete.
 * @param sessionKey Optional stable identity for the internal streaming session. Provide a
 * caller-owned block identity when the same `DiagramView` instance should keep reusing its
 * incremental session across source appends, and change the key when a fresh session is required.
 */
@Composable
@DiagramApi
fun DiagramView(
    source: String,
    theme: DiagramTheme = DiagramTheme.Default,
    modifier: Modifier = Modifier,
    zoomEnabled: Boolean = false,
    presentationMode: DiagramPresentationMode = DiagramPresentationMode.Auto,
    languageHint: SourceLanguage? = null,
    sessionKey: Any? = null,
) {
    val state = rememberDiagramRenderState(
        source = source,
        theme = theme,
        languageHint = languageHint,
        sessionKey = sessionKey,
    )
    DiagramSurface(
        state = state,
        modifier = modifier,
        zoomEnabled = zoomEnabled,
        presentationMode = presentationMode,
    )
}

@Composable
@DiagramApi
fun rememberDiagramRenderState(
    source: String,
    theme: DiagramTheme = DiagramTheme.Default,
    languageHint: SourceLanguage? = null,
    sessionKey: Any? = null,
): DiagramRenderState {
    val languageDetectionSource = if (languageHint == null) source else ""
    val language = remember(languageHint, languageDetectionSource) {
        languageHint ?: detectSourceLanguage(source)
    }
    val textMeasurer = rememberDiagramTextMeasurer()
    val holder = remember { DiagramViewSessionHolder() }
    val state = remember { DiagramRenderState(DiagramSnapshot.empty(language)) }

    DisposableEffect(holder) {
        onDispose { holder.close() }
    }

    LaunchedEffect(language) {
        if (state.snapshot.sourceLanguage != language && source.isEmpty()) {
            state.snapshot = DiagramSnapshot.empty(language)
        }
    }

    LaunchedEffect(source, language, textMeasurer, theme, sessionKey) {
        val session = holder.sessionFor(sessionKey, language, source, textMeasurer, theme)
        val appendedFrom = holder.source.length
        if (source.length > appendedFrom) {
            session.append(source.substring(appendedFrom))
            state.snapshot = session.state.value
        } else if (source.length < appendedFrom) {
            state.snapshot = session.state.value
        }
        holder.source = source
        delay(FINISH_DEBOUNCE_MS)
        state.snapshot = session.finish()
    }

    return state
}

@DiagramApi
@Stable
class DiagramRenderState internal constructor(
    initialSnapshot: DiagramSnapshot,
) {
    var snapshot: DiagramSnapshot by mutableStateOf(initialSnapshot)
        internal set
}

private class DiagramViewSessionHolder {
    var source: String = ""
    private var sessionKey: Any? = null
    private var language: SourceLanguage? = null
    private var textMeasurer: TextMeasurer? = null
    private var theme: DiagramTheme? = null
    private var session: DiagramSession? = null

    fun sessionFor(
        nextSessionKey: Any?,
        nextLanguage: SourceLanguage,
        nextSource: String,
        nextTextMeasurer: TextMeasurer,
        nextTheme: DiagramTheme,
    ): DiagramSession {
        val current = session
        val canReuse = current != null &&
            sessionKey == nextSessionKey &&
            language == nextLanguage &&
            textMeasurer === nextTextMeasurer &&
            theme == nextTheme &&
            nextSource.startsWith(source)
        if (canReuse) return current

        close()
        source = ""
        sessionKey = nextSessionKey
        language = nextLanguage
        textMeasurer = nextTextMeasurer
        theme = nextTheme
        return Diagram.session(language = nextLanguage, theme = nextTheme, textMeasurer = nextTextMeasurer)
            .also { session = it }
    }

    fun close() {
        session?.close()
        session = null
        sessionKey = null
        theme = null
    }
}

private const val FINISH_DEBOUNCE_MS = 120L

internal fun detectSourceLanguage(source: CharSequence): SourceLanguage =
    Diagram.detectSource(source).language ?: SourceLanguage.MERMAID
