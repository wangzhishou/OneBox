package com.hrm.diagram.render.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.text.rememberTextMeasurer
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.render.Diagram
import com.hrm.diagram.render.streaming.DiagramSession

/**
 * Composable factory for a [DiagramSession]. Internally:
 * 1. wires Compose's pixel-perfect `TextMeasurer` into the layout pipeline so labels
 *    are measured before sizing/placement (the "measure-first" approach used by
 *    mermaid/dagre/elk);
 * 2. ties the session's lifecycle to the composition (auto-`close()` when leaving).
 *
 * Callers should treat the returned session as opaque — they never see the measurer
 * adapter, just the session and its [DiagramSession.state] flow.
 *
 * The session is rebuilt whenever any element of [keys] changes (use the same identity
 * key you'd pass to `remember(...)` — typically the selected source/language).
 */
@Composable
internal fun rememberDiagramSession(
    language: SourceLanguage,
    key: Any? = language,
    theme: DiagramTheme = DiagramTheme.Default,
    layoutOptions: LayoutOptions = LayoutOptions(),
): DiagramSession {
    val composeMeasurer = rememberTextMeasurer()
    val adapter = remember(composeMeasurer) { ComposeTextMeasurerAdapter(composeMeasurer) }
    val session = remember(language, adapter, theme, layoutOptions, key) {
        Diagram.session(
            language = language,
            theme = theme,
            layoutOptions = layoutOptions,
            textMeasurer = adapter,
        )
    }
    DisposableEffect(session) { onDispose { session.close() } }
    return session
}

@Composable
internal fun rememberDiagramTextMeasurer(): TextMeasurer {
    val composeMeasurer = rememberTextMeasurer()
    return remember(composeMeasurer) { ComposeTextMeasurerAdapter(composeMeasurer) }
}
