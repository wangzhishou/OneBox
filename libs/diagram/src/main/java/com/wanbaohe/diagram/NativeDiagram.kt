/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.diagram

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.hrm.diagram.core.ir.Severity
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.render.Diagram
import com.hrm.diagram.render.compose.DiagramPresentationMode
import com.hrm.diagram.render.compose.DiagramSurface
import com.hrm.diagram.render.compose.rememberDiagramRenderState
import com.hrm.diagram.render.theme.material3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private const val EXPORT_SETTLE_DEBOUNCE_MS = 300L

@Composable
fun NativeDiagram(
    source: String,
    modifier: Modifier = Modifier,
    zoomEnabled: Boolean = false,
    languageHint: String? = null,
    onFrame: (NativeDiagramFrame) -> Unit = {},
) {
    val currentOnFrame by rememberUpdatedState(onFrame)
    val language = remember(source, languageHint) {
        Diagram.detectSource(source, hint = languageHint).language ?: SourceLanguage.MERMAID
    }
    val state = rememberDiagramRenderState(
        source = source,
        theme = DiagramTheme.material3(),
        languageHint = language,
    )

    val exportBackgroundArgb = MaterialTheme.colorScheme.surfaceContainer.toArgb()
    LaunchedEffect(state.snapshot, exportBackgroundArgb) {
        val snapshot = state.snapshot
        if (!snapshot.isFinal) return@LaunchedEffect
        val errors = snapshot.diagnostics.filter { it.severity == Severity.ERROR }
        val svg = if (errors.isEmpty() && snapshot.drawCommands.isNotEmpty()) {
            delay(EXPORT_SETTLE_DEBOUNCE_MS)
            withContext(Dispatchers.Default) { snapshot.exportNativeSvg(exportBackgroundArgb) }
        } else {
            null
        }
        currentOnFrame(
            NativeDiagramFrame(
                source = source,
                svg = svg,
                width = snapshot.laidOut?.bounds?.size?.width?.roundToInt() ?: 0,
                height = snapshot.laidOut?.bounds?.size?.height?.roundToInt() ?: 0,
                hasErrors = errors.isNotEmpty(),
                diagnosticMessages = errors.map { "${it.code}: ${it.message}" }.distinct(),
            )
        )
    }

    DiagramSurface(
        state = state,
        modifier = modifier,
        zoomEnabled = zoomEnabled,
        presentationMode = DiagramPresentationMode.Embedded,
    )
}
