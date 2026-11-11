package com.wanbaohe.markuplayers.presentation.draw

import androidx.compose.runtime.Composable
import com.wanbaohe.core.ui.widget.editor.EditorDrawOverlay

@Composable
internal fun DrawOverlay(
    session: DrawSessionState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
) = EditorDrawOverlay(session, canvasWidthPx, canvasHeightPx)
