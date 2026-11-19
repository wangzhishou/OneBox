/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.core.ui.widget.editor

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.ink.authoring.compose.InProgressStrokes
import androidx.ink.strokes.Stroke

@Composable
fun EditorDrawOverlay(
    session: EditorDrawSessionState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var pendingStrokes by remember(session) { mutableStateOf<List<Stroke>>(emptyList()) }
    SideEffect {
        if (pendingStrokes.isNotEmpty()) {
            pendingStrokes = if (session.inkStrokes.isEmpty()) {
                emptyList()
            } else {
                pendingStrokes.filter { pending ->
                    session.inkStrokes.none { it === pending }
                }
            }
        }
    }
    val strokesToDraw = remember(session.inkStrokes, pendingStrokes) {
        session.inkStrokes + pendingStrokes.filter { pending ->
            session.inkStrokes.none { it === pending }
        }
    }
    Box(
        modifier = modifier.size(
            width = with(density) { canvasWidthPx.toDp() },
            height = with(density) { canvasHeightPx.toDp() },
        )
    ) {
        EditorInkStrokeCanvas(
            strokes = strokesToDraw,
            modifier = Modifier.fillMaxSize(),
        )
        when {
            session.isEraser -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(session) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                session.startErase()
                                session.erase(offset.x, offset.y)
                            },
                            onDragEnd = session::endErase,
                            onDragCancel = session::endErase,
                            onDrag = { change, _ ->
                                session.erase(change.position.x, change.position.y)
                                change.consume()
                            },
                        )
                    }
            )

            session.isPanMode -> Unit

            else -> InProgressStrokes(
                defaultBrush = session.currentInkBrush(),
                nextBrush = { session.currentInkBrush() },
                onStrokesFinished = { finished ->
                    pendingStrokes = pendingStrokes + finished
                    session.addStrokes(finished, canvasWidthPx, canvasHeightPx)
                },
            )
        }
    }
}
