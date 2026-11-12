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

package com.wanbaohe.app.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.wanbaohe.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.core.ui.widget.glass.liquidGlassThick
import kotlin.math.roundToInt
import com.t8rin.imagetoolbox.core.resources.icons.Close

@Composable
fun StartupTraceOverlay(
    modifier: Modifier = Modifier,
) {
    val enabled by AppSharedStorage.isStartupTraceOverlayEnabled.collectAsState()

    if (!enabled) return

    val entries by StartupTrace.entries.collectAsState()

    if (entries.isEmpty()) return

    val latestEntry = entries.last()
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var panelSize by remember { mutableStateOf(IntSize.Zero) }
    var panelOffsetX by remember { mutableFloatStateOf(Float.NaN) }
    var panelOffsetY by remember { mutableFloatStateOf(Float.NaN) }

    val maxOffsetX = (containerSize.width - panelSize.width).coerceAtLeast(0).toFloat()
    val maxOffsetY = (containerSize.height - panelSize.height).coerceAtLeast(0).toFloat()
    val resolvedOffsetX = if (panelOffsetX.isFinite()) panelOffsetX.coerceIn(0f, maxOffsetX) else maxOffsetX
    val resolvedOffsetY = if (panelOffsetY.isFinite()) panelOffsetY.coerceIn(0f, maxOffsetY) else 0f

    LaunchedEffect(containerSize, panelSize) {
        if (containerSize == IntSize.Zero || panelSize == IntSize.Zero) return@LaunchedEffect

        if (!panelOffsetX.isFinite() || !panelOffsetY.isFinite()) {
            panelOffsetX = maxOffsetX
            panelOffsetY = 0f
        } else {
            panelOffsetX = panelOffsetX.coerceIn(0f, maxOffsetX)
            panelOffsetY = panelOffsetY.coerceIn(0f, maxOffsetY)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .onSizeChanged { containerSize = it }
    ) {
        Column(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = resolvedOffsetX.roundToInt(),
                        y = resolvedOffsetY.roundToInt()
                    )
                }
                .widthIn(max = 320.dp)
                .onSizeChanged { panelSize = it }
                .pointerInput(maxOffsetX, maxOffsetY) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val currentOffsetX = panelOffsetX.takeIf { it.isFinite() } ?: maxOffsetX
                        val currentOffsetY = panelOffsetY.takeIf { it.isFinite() } ?: 0f
                        panelOffsetX = (currentOffsetX + dragAmount.x).coerceIn(0f, maxOffsetX)
                        panelOffsetY = (currentOffsetY + dragAmount.y).coerceIn(0f, maxOffsetY)
                    }
                }
                .liquidGlassThick(borderWidth = 0.8.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.startup_trace_overlay_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                GlassTonalIconButton(
                    onClick = {
                        AppSharedStorage.saveStartupTraceOverlayEnabled(false)
                        StartupTrace.setEnabled(false)
                    },
                    modifier = Modifier.size(28.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Text(
                text = stringResource(
                    R.string.startup_trace_overlay_total,
                    latestEntry.totalMs
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            entries.forEach { entry ->
                Text(
                    text = entry.formattedLine,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Text(
                text = stringResource(R.string.startup_trace_overlay_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

