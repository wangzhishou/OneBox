/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.signature

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.container

@Composable
internal fun SignatureDrawDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Bitmap) -> Unit,
) {
    val finished = remember { mutableStateListOf<List<Offset>>() }
    var active by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.draw_signature)) },
        confirmButton = {
            EnhancedButton(
                enabled = finished.isNotEmpty(),
                onClick = {
                    renderSignature(canvasSize, finished.toList())?.let(onConfirm)
                    finished.clear()
                    active = emptyList()
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            EnhancedButton(
                onClick = onDismiss,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        text = {
            Column {
                ComposeCanvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                val stroke = mutableListOf(down.position)
                                active = stroke.toList()
                                drag(down.id) { change ->
                                    stroke.add(change.position)
                                    active = stroke.toList()
                                    change.consume()
                                }
                                finished.add(stroke.toList())
                                active = emptyList()
                            }
                        },
                ) {
                    (finished + listOf(active)).forEach { stroke ->
                        if (stroke.size < 2) return@forEach
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(stroke.first().x, stroke.first().y)
                            stroke.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = Stroke(
                                width = 6f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EnhancedButton(
                        onClick = {
                            finished.removeLastOrNull()
                            active = emptyList()
                        },
                        enabled = finished.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Text(stringResource(R.string.undo))
                    }
                    EnhancedButton(
                        onClick = {
                            finished.clear()
                            active = emptyList()
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                }
            }
        },
    )
}

private fun renderSignature(
    size: IntSize,
    strokes: List<List<Offset>>,
): Bitmap? {
    if (size.width <= 0 || size.height <= 0 || strokes.isEmpty()) return null
    val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }
    strokes.forEach { stroke ->
        if (stroke.size < 2) return@forEach
        val path = Path().apply {
            moveTo(stroke.first().x, stroke.first().y)
            stroke.drop(1).forEach { lineTo(it.x, it.y) }
        }
        canvas.drawPath(path, paint)
    }
    return bitmap
}

@Composable
internal fun SavedSignatureRow(
    signatures: List<String>,
    onSelect: (String) -> Unit,
    onDeleteRequest: (String) -> Unit,
) {
    if (signatures.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .container(shape = ShapeDefaults.large),
    ) {
        Text(
            text = stringResource(R.string.saved_signatures),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
        )
        Text(
            text = stringResource(R.string.saved_signatures_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 2.dp, end = 16.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(signatures) { signature ->
                Picture(
                    model = signature,
                    modifier = Modifier
                        .size(56.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = ShapeDefaults.small,
                        )
                        .clip(ShapeDefaults.small)
                        .combinedClickable(
                            onClick = { onSelect(signature) },
                            onLongClick = { onDeleteRequest(signature) },
                        ),
                    showTransparencyChecker = true,
                )
            }
        }
    }
}
