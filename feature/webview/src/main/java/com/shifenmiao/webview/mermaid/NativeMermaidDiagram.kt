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

package com.shifenmiao.webview.mermaid

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.wanbaohe.diagram.NativeDiagram
import com.wanbaohe.diagram.NativeDiagramFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val NATIVE_ERROR_SETTLE_MS = 400L

@Composable
fun NativeMermaidDiagram(
    code: String,
    modifier: Modifier = Modifier,
    language: String = "mermaid",
) {
    val isDark = isSystemInDarkTheme()
    val supportsWebViewFallback = language == "mermaid" || language == "mmd"
    var nativeFailed by remember(code, language) { mutableStateOf(false) }
    var nativeHasErrors by remember(code, language) { mutableStateOf(false) }
    var diagnostics by remember(code, language) { mutableStateOf<List<String>>(emptyList()) }
    var exportFrame by remember(code, language) { mutableStateOf<NativeDiagramFrame?>(null) }

    LaunchedEffect(code, language, nativeHasErrors) {
        if (!nativeHasErrors || !supportsWebViewFallback) return@LaunchedEffect
        delay(NATIVE_ERROR_SETTLE_MS)
        if (nativeHasErrors) {
            Log.w(TAG, "Native Mermaid failed, using WebView: $diagnostics")
            nativeFailed = true
        }
    }

    LaunchedEffect(exportFrame, isDark, language) {
        val frame = exportFrame ?: return@LaunchedEffect
        val svg = frame.svg ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            MermaidBitmapCache.put(
                code = frame.source,
                isDark = isDark,
                svgString = svg,
                heightPx = frame.height,
                language = language,
            )
        }
    }

    if (nativeFailed) {
        MermaidDiagramView(code = code, modifier = modifier)
    } else {
        NativeDiagram(
            source = code,
            modifier = modifier.alpha(if (nativeHasErrors) 0f else 1f),
            languageHint = language,
            onFrame = { frame ->
                if (frame.source == code) {
                    nativeHasErrors = frame.hasErrors ||
                        (frame.source.isNotBlank() && frame.svg == null)
                    diagnostics = frame.diagnosticMessages
                }
                if (frame.svg != null) exportFrame = frame
            },
        )
    }
}

private const val TAG = "NativeMermaidDiagram"
